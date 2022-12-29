package red.tetracube.users.services;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.tetracube.configuration.properties.BearerTokenConfigurationProperties;
import red.tetracube.core.enumerations.FailureReason;
import red.tetracube.core.models.Result;
import red.tetracube.data.entities.House;
import red.tetracube.data.entities.User;
import red.tetracube.data.repositories.AuthenticationTokenRepository;
import red.tetracube.data.repositories.AuthorizationRepository;
import red.tetracube.data.repositories.HouseRepository;
import red.tetracube.data.repositories.UserRepository;
import red.tetracube.users.payloads.UserLoginRequest;
import red.tetracube.users.payloads.UserLoginResponse;

import javax.enterprise.context.RequestScoped;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class UserLoginService {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserLoginService.class);

    private final UserRepository userRepository;
    private final AuthenticationTokenRepository authenticationTokenRepository;
    private final HouseRepository houseRepository;
    private final BearerTokenConfigurationProperties bearerTokenConfigurationProperties;
    private final AuthorizationRepository authorizationRepository;

    public UserLoginService(UserRepository userRepository,
                            AuthenticationTokenRepository authenticationTokenRepository,
                            HouseRepository houseRepository,
                            BearerTokenConfigurationProperties bearerTokenConfigurationProperties,
                            AuthorizationRepository authorizationRepository) {
        this.userRepository = userRepository;
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.houseRepository = houseRepository;
        this.bearerTokenConfigurationProperties = bearerTokenConfigurationProperties;
        this.authorizationRepository = authorizationRepository;
    }

    public Uni<Result<Void>> validateAccountAndAuthenticationTokenRelation(String username, String authenticationCode) {
        var userFromTokenUni = this.userRepository.getUserFromAuthenticationCode(authenticationCode);
        var userExistsByNameUni = this.userRepository.existsByName(username);
        var authenticationTokenUni = this.authenticationTokenRepository.getByToken(authenticationCode);
        return Uni.combine().all().unis(userFromTokenUni, userExistsByNameUni, authenticationTokenUni)
                .asTuple()
                .map(queriesResultSet -> {
                    var authenticationTokenLinkedUser = queriesResultSet.getItem1();
                    var userExistsByName = queriesResultSet.getItem2();
                    var authenticationToken = queriesResultSet.getItem3();
                    if (authenticationToken == null) {
                        LOGGER.warn("Token does not exists returning not found exception");
                        return Result.failed(FailureReason.NOT_FOUND, "TOKEN_NOT_FOUND");
                    }
                    if (authenticationToken.getValidUntil().before(Timestamp.from(Instant.now()))) {
                        LOGGER.warn("Token expired returning bad request exception");
                        return Result.failed(FailureReason.BAD_REQUEST, "TOKEN_EXPIRED");
                    }
                    if (authenticationTokenLinkedUser == null && userExistsByName) {
                        return Result.failed(FailureReason.CONFLICTS, "ACCOUNT_ALREADY_EXISTS");
                    }
                    if (authenticationTokenLinkedUser != null && !authenticationTokenLinkedUser.getName().equals(username)) {
                        LOGGER.warn("The user related to the token is present, but is not the same to the name supplied by application");
                        return Result.failed(FailureReason.UNAUTHORIZED, "INVALID_CREDENTIALS");
                    }
                    return Result.success(null);
                });
    }

    public Uni<Result<UserLoginResponse>> tryToLoginUser(UserLoginRequest userLoginRequest) {
        var authenticationTokenUni = authenticationTokenRepository.getByToken(userLoginRequest.authenticationCode)
                .onItem()
                .invoke(ignore -> LOGGER.info("Getting authentication token"));
        var userFromAuthenticationTokenUni = this.getOrCreateUser(userLoginRequest.username, userLoginRequest.authenticationCode)
                .onItem()
                .invoke(ignore -> LOGGER.info("Searching for user linked to the token"));
        var houseUni = houseRepository.getByRelatedAuthenticationCode(userLoginRequest.authenticationCode)
                .onItem()
                .invoke(ignore -> LOGGER.info("Getting house linked to the authentication token"));

        return Uni.combine().all().unis(authenticationTokenUni, userFromAuthenticationTokenUni, houseUni)
                .asTuple()
                .map(queriesResults -> {
                    var authenticationToken = queriesResults.getItem1();
                    var house = queriesResults.getItem3();
                    var user = queriesResults.getItem2();

                    var bearerToken = emitBearerToken(
                            user.getId(),
                            user.getName(),
                            Instant.now(),
                            authenticationToken.getValidUntil().toInstant(),
                            house
                    );

                    var userLoginResponse = new UserLoginResponse();
                    userLoginResponse.id = user.getId();
                    userLoginResponse.name = user.getName();
                    userLoginResponse.token = bearerToken;
                    userLoginResponse.houseId = house.getId();
                    userLoginResponse.houseName = house.getName();
                    return Result.success(userLoginResponse);
                });
    }

    private Uni<User> getOrCreateUser(String username, String authenticationCode) {
        var userFromAuthenticationCodeUni = this.userRepository.getUserFromAuthenticationCode(authenticationCode);
        var authenticationTokenUni = this.authenticationTokenRepository.getByToken(authenticationCode);
        var houseUni = this.houseRepository.getByRelatedAuthenticationCode(authenticationCode);
        var authorizationsUni = this.authorizationRepository.getAll();
        return userFromAuthenticationCodeUni
                .onItem()
                .ifNull()
                .switchTo(() ->
                        Uni.combine().all().unis(authenticationTokenUni, houseUni, authorizationsUni)
                                .asTuple()
                                .flatMap(queriesResultTuple -> {
                                    var house = queriesResultTuple.getItem2();
                                    var authenticationToken = queriesResultTuple.getItem1();
                                    var authorizations = queriesResultTuple.getItem3();
                                    var newUser = new User(
                                            username,
                                            house,
                                            authenticationToken,
                                            authorizations
                                    );
                                    return this.userRepository.save(newUser);
                                })
                );
    }

    private String emitBearerToken(UUID userId, String username, Instant issuedAt, Instant expiresAt, House house) {
        return Jwt.issuer(bearerTokenConfigurationProperties.getIssuer())
                .upn(username)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .groups(new HashSet<>(List.of("User")))
                .audience(bearerTokenConfigurationProperties.getAudiences())
                .claim("userId", userId)
                .claim("houseId", house.getId())
                .claim("houseName", house.getName())
                .sign();
    }
}
