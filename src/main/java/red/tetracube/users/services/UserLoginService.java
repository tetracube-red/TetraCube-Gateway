package red.tetracube.users.services;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import red.tetracube.data.entities.User;
import red.tetracube.configuration.properties.BearerTokenConfigurationProperties;
import red.tetracube.data.entities.AuthenticationToken;
import red.tetracube.data.entities.House;
import red.tetracube.data.repositories.UserRepository;
import red.tetracube.data.repositories.AuthenticationTokenRepository;
import red.tetracube.data.repositories.HouseRepository;
import red.tetracube.exceptions.ConflictsRequestException;
import red.tetracube.users.dto.UserLoginRequest;
import red.tetracube.users.dto.UserLoginResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;

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

    public Uni<UserLoginResponse> tryToLoginUser(UserLoginRequest userLoginRequest) {
        var authenticationTokenUni = authenticationTokenRepository.getByToken(userLoginRequest.authenticationCode);
        var userFromAuthenticationTokenUni = userRepository.getUserFromAuthenticationToken(userLoginRequest.authenticationCode);
        var userExistsUni = userRepository.existsByName(userLoginRequest.username);
        var houseUni = authenticationTokenUni.flatMap(authenticationToken -> houseRepository.getById(authenticationToken.getHouse().getId()));
        return Uni.combine().all().unis(authenticationTokenUni, userFromAuthenticationTokenUni, houseUni, userExistsUni)
                .collectFailures()
                .asTuple()
                .invoke(unis -> {
                    validateAuthenticationToken(unis.getItem1(), unis.getItem2(), userLoginRequest.username);
                    validateAccountExists(unis.getItem2(), unis.getItem4());
                })
                .<Tuple2<User, Boolean>>map(unis -> {
                    var optionalUserFromAuthenticationToken = unis.getItem2();
                    var user = optionalUserFromAuthenticationToken == null
                            ? createUserEntity(userLoginRequest.username, unis.getItem1(), unis.getItem3())
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

    private void validateAuthenticationToken(AuthenticationToken authenticationToken, User user, String loginUsername) {
        if (authenticationToken == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("TOKEN_NOT_FOUND").build());
        }
        if (authenticationToken.getValidUntil().before(Timestamp.from(Instant.now()))) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("TOKEN_EXPIRED").build());
        }
        if (user == null && authenticationToken.getInUse()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("TOKEN_ALREADY_IN_USE").build());
        }
        if (user != null && authenticationToken.getInUse()) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("TOKEN_ALREADY_IN_USE").build());
        }
        if (user != null && authenticationToken.getInUse() && user.getName().equals(loginUsername)) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("INVALID_CREDENTIALS").build());
        }
    }

    private void validateAccountExists(User authenticationTokenLinkedUser, boolean userExistsByName) {
        if (authenticationTokenLinkedUser == null && userExistsByName) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("ACCOUNT_ALREADY_EXISTS").build());
        }
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
