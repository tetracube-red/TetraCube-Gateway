package red.tetracube.users.services;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.tetracube.configuration.properties.BearerTokenConfigurationProperties;
import red.tetracube.core.enumerations.FailureReason;
import red.tetracube.core.models.Result;
import red.tetracube.data.entities.AuthenticationToken;
import red.tetracube.data.entities.House;
import red.tetracube.data.entities.User;
import red.tetracube.data.repositories.AuthenticationTokenRepository;
import red.tetracube.data.repositories.HouseRepository;
import red.tetracube.data.repositories.UserRepository;
import red.tetracube.users.payloads.UserLoginRequest;
import red.tetracube.users.payloads.UserLoginResponse;

import javax.enterprise.context.RequestScoped;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class UserLoginService {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserLoginService.class);

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
        var authenticationTokenUni = authenticationTokenRepository.getByToken(userLoginRequest.authenticationCode)
                .invoke(ignore -> LOGGER.info("Getting authentication token"));
        var userFromAuthenticationTokenUni = userRepository.getUserFromAuthenticationToken(userLoginRequest.authenticationCode)
                .invoke(ignore -> LOGGER.info("Searching for user linked to the token"));
        return Uni.combine().all().unis(authenticationTokenUni, userFromAuthenticationTokenUni)
                .asTuple()
                .flatMap(authenticationTokenAndRelatedUser -> {
                    var authenticationToken = authenticationTokenAndRelatedUser.getItem1();
                    LOGGER.info("Got authentication from db - optional -");
                    var user = authenticationTokenAndRelatedUser.getItem2();
                    LOGGER.info("Got user related to authentication token from db - optional -");
                    var tokenEvaluation = this.validateAuthenticationToken(authenticationToken, user, userLoginRequest.username);
                    if (tokenEvaluation.isPresent()) {
                        return Uni.createFrom().item(tokenEvaluation.get());
                    }

                    return this.houseRepository.getById(authenticationToken.get().getHouse().getId())
                            .flatMap(house -> {
                                LOGGER.info("Took house linked to the authentication token");
                                return this.userRepository.existsByName(userLoginRequest.username)
                                        .flatMap(existsByName -> {
                                            var userEvaluation = this.validateAccount(user, existsByName);
                                            if (userEvaluation.isPresent()) {
                                                return Uni.createFrom().item(userEvaluation.get());
                                            }

                                            LOGGER.info("Creating promise of user creation - if needed -");
                                            final var isNew = user.isEmpty();
                                            var newUser = this.createUserEntity(userLoginRequest.username, authenticationToken.get(), house.get());
                                            var dbUserUni = isNew
                                                    ? userRepository.save(newUser)
                                                    : Uni.createFrom().item(user.get());

                                            LOGGER.info("Creating promise of authentication token update");
                                            authenticationToken.get().setAsInUse();
                                            var updateAuthenticationToken = authenticationTokenRepository.save(authenticationToken.get());

                                            LOGGER.info("Combining promises");
                                            return Uni.combine().all().unis(dbUserUni, updateAuthenticationToken)
                                                    .asTuple()
                                                    .map(userAndRelatedToken -> {
                                                        var userEntity = userAndRelatedToken.getItem1();
                                                        var authenticationTokenEntity = userAndRelatedToken.getItem2();
                                                        var bearerToken = emitBearerToken(
                                                                userEntity.getName(),
                                                                Instant.now(),
                                                                authenticationTokenEntity.getValidUntil().toInstant(),
                                                                house.get()
                                                        );

                                                        var userLoginResponse = new UserLoginResponse();
                                                        userLoginResponse.id = userEntity.getId();
                                                        userLoginResponse.name = userEntity.getName();
                                                        userLoginResponse.token = bearerToken;
                                                        return userLoginResponse;
                                                    })
                                                    .map(Result::success);
                                        });
                            });
                });
    }

    private Optional<Result<UserLoginResponse>> validateAuthenticationToken(Optional<AuthenticationToken> authenticationToken,
                                                                            Optional<User> user,
                                                                            String loginUsername) {
        if (authenticationToken.isEmpty()) {
            LOGGER.warn("Token does not exists returning not found exception");
            return Optional.of(Result.failed(FailureReason.NOT_FOUND, "TOKEN_NOT_FOUND"));
        }
        if (authenticationToken.get().getValidUntil().before(Timestamp.from(Instant.now()))) {
            LOGGER.warn("Token expired returning bad request exception");
            return Optional.of(Result.failed(FailureReason.BAD_REQUEST, "TOKEN_EXPIRED"));
        }
        if (user.isPresent() && authenticationToken.get().getInUse()) {
            LOGGER.warn("Token already in use");
            return Optional.of(Result.failed(FailureReason.BAD_REQUEST, "TOKEN_ALREADY_IN_USE"));
        }
        if (user.isPresent() && authenticationToken.get().getInUse() && !user.get().getName().equals(loginUsername)) {
            LOGGER.warn("The user related to the token is present, but is not the same to the name supplied by application");
            return Optional.of(Result.failed(FailureReason.UNAUTHORIZED, "INVALID_CREDENTIALS"));
        }
        return Optional.empty();
    }

    private Optional<Result<UserLoginResponse>> validateAccount(Optional<User> authenticationTokenLinkedUser, boolean userExistsByName) {
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
