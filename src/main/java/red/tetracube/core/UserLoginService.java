package red.tetracube.core;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import red.tetracube.configuration.properties.BearerTokenConfigurationProperties;
import red.tetracube.core.enumerations.FailureReason;
import red.tetracube.core.models.Result;
import red.tetracube.data.entities.AuthenticationToken;
import red.tetracube.data.entities.House;
import red.tetracube.data.entities.User;
import red.tetracube.data.repositories.AuthenticationTokenRepository;
import red.tetracube.data.repositories.HouseRepository;
import red.tetracube.data.repositories.UserRepository;
import red.tetracube.users.dto.UserLoginRequest;
import red.tetracube.users.dto.UserLoginResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ValidationException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserLoginService {

    private final UserRepository userRepository;
    private final AuthenticationTokenRepository authenticationTokenRepository;
    private final HouseRepository houseRepository;
    private final BearerTokenConfigurationProperties bearerTokenConfigurationProperties;

    public UserLoginService(UserRepository userRepository,
                            AuthenticationTokenRepository authenticationTokenRepository,
                            HouseRepository houseRepository,
                            BearerTokenConfigurationProperties bearerTokenConfigurationProperties) {
        this.userRepository = userRepository;
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.houseRepository = houseRepository;
        this.bearerTokenConfigurationProperties = bearerTokenConfigurationProperties;
    }

    public Uni<Result<UserLoginResponse>> tryToLoginUser(UserLoginRequest userLoginRequest) {
        var authenticationTokenUni = authenticationTokenRepository.getByToken(userLoginRequest.authenticationCode);
        var userFromAuthenticationTokenUni = userRepository.getUserFromAuthenticationToken(userLoginRequest.authenticationCode);
        //    var userExistsUni =
        /*

                    var validateUsername = userRepository.existsByName(userLoginRequest.username)
                            .map(userExistsByNameResponse -> this.validateAccountExists(optionalUserFromToken, userExistsByNameResponse));
         */
        // var houseUni = authenticationTokenUni.flatMap(authenticationToken -> houseRepository.getById(authenticationToken.getHouse().getId()));
        return Uni.combine().all().unis(authenticationTokenUni, userFromAuthenticationTokenUni)
                .collectFailures()
                .asTuple()
                .map(responses -> {
                    var optionalAuthenticationToken = responses.getItem1();
                    var optionalUserFromToken = responses.getItem2();
                    return this.validateAuthenticationToken(optionalAuthenticationToken, optionalUserFromToken, userLoginRequest.username);
                })
                .invoke(Unchecked.consumer(authTokenValidationResponse -> {
                    if (authTokenValidationResponse.isPresent()) {
                        throw new ValidationException();
                    }
                }))
                .flatMap(authTokenValidationResponse -> {
                   return userRepository.existsByName(userLoginRequest.username)
                            .map(userExistsByNameResponse -> this.validateAccountExists(optionalUserFromToken, userExistsByNameResponse));
                })
                .<Tuple2<User, Boolean>>map(unis -> {
                    var optionalUserFromAuthenticationToken = unis.getItem2();
                    var user = optionalUserFromAuthenticationToken == null
                            ? createUserEntity(userLoginRequest.username, unis.getItem1(), unis.getItem3().get())
                            : optionalUserFromAuthenticationToken;
                    var isNew = optionalUserFromAuthenticationToken == null;
                    return Tuple2.of(user, isNew);
                })
                .<Tuple2<User, Boolean>>flatMap(user -> {
                    if (user.getItem2()) {
                        return userRepository.save(user.getItem1()).map(u -> Tuple2.of(u, true));
                    }
                    return Uni.createFrom().item(user.getItem1()).map(u -> Tuple2.of(u, false));
                })
                .<User>call(user -> {
                    if (user.getItem2()) {
                        var linkedAuthenticationToken = user.getItem1().getAuthenticationToken();
                        linkedAuthenticationToken.setAsInUse();
                        return authenticationTokenRepository.save(linkedAuthenticationToken);
                    }
                    return Uni.createFrom().item(user.getItem1());
                })
                .map(userEntity -> {
                    var bearerToken = emitBearerToken(
                            userEntity.getItem1().getName(),
                            Instant.now(),
                            userEntity.getItem1().getAuthenticationToken().getValidUntil().toInstant(),
                            userEntity.getItem1().getHouse()
                    );

                    var userLoginResponse = new UserLoginResponse();
                    userLoginResponse.id = userEntity.getItem1().getId();
                    userLoginResponse.name = userEntity.getItem1().getName();
                    userLoginResponse.token = bearerToken;
                    return userLoginResponse;
                });
    }

    private Optional<Result<UserLoginResponse>> validateAuthenticationToken(Optional<AuthenticationToken> authenticationToken, Optional<User> user, String loginUsername) {
        if (authenticationToken.isEmpty()) {
            return Optional.of(Result.failed(FailureReason.NOT_FOUND, "TOKEN_NOT_FOUND"));
        }
        if (authenticationToken.get().getValidUntil().before(Timestamp.from(Instant.now()))) {
            return Optional.of(Result.failed(FailureReason.BAD_REQUEST, "TOKEN_EXPIRED"));
        }
        if (user.isEmpty() && authenticationToken.get().getInUse()) {
            return Optional.of(Result.failed(FailureReason.BAD_REQUEST, "TOKEN_ALREADY_IN_USE"));
        }
        if (user.isPresent() && authenticationToken.get().getInUse() && user.get().getName().equals(loginUsername)) {
            return Optional.of(Result.failed(FailureReason.UNAUTHORIZED, "INVALID_CREDENTIALS"));
        }
        return Optional.empty();
    }

    private Optional<Result<UserLoginResponse>> validateAccountExists(Optional<User> authenticationTokenLinkedUser, boolean userExistsByName) {
        if (authenticationTokenLinkedUser.isEmpty() && userExistsByName) {
            return Optional.of(Result.failed(FailureReason.CONFLICTS, "ACCOUNT_ALREADY_EXISTS"));
        }
        return Optional.empty();
    }

    private User createUserEntity(String username, AuthenticationToken authenticationToken, House house) {
        return new User(
                username,
                house,
                authenticationToken
        );
    }

    private String emitBearerToken(String username, Instant issuedAt, Instant expiresAt, House house) {
        return Jwt.issuer(bearerTokenConfigurationProperties.getIssuer())
                .upn(username)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .groups(new HashSet<>(List.of("User")))
                .audience(bearerTokenConfigurationProperties.getAudiences())
                .sign();
    }
}
