package red.tetracube.guest.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.enterprise.context.RequestScoped;

@RequestScoped
public class UserLoginService {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserLoginService.class);

  /*   private final GuestRepository userRepository;
    //private final AuthenticationTokenRepository authenticationTokenRepository;
    private final TetraCubeRepository houseRepository;
    private final BearerTokenConfigurationProperties bearerTokenConfigurationProperties;
    private final PermissionRepository authorizationRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserLoginService(GuestRepository userRepository,
                            AuthenticationTokenRepository authenticationTokenRepository,
                            TetraCubeRepository houseRepository,
                            BearerTokenConfigurationProperties bearerTokenConfigurationProperties,
                            PermissionRepository authorizationRepository) {
        this.userRepository = userRepository;
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.houseRepository = houseRepository;
        this.bearerTokenConfigurationProperties = bearerTokenConfigurationProperties;
        this.authorizationRepository = authorizationRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Uni<Result<Void>> validateUserAndAuthenticationTokenRelation(String username, String authenticationCode) {
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
                        return Result.failed(FailureReason.CONFLICTS, "USER_ALREADY_EXISTS");
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

    private Uni<Guest> getOrCreateUser(String username, String authenticationCode) {
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
                                    var newUser = new Guest(
                                            username,
                                            house,
                                            authenticationToken,
                                            authorizations
                                    );
                                    return this.userRepository.save(newUser);
                                })
                );
    }

    private String emitBearerToken(UUID userId, String username, Instant issuedAt, Instant expiresAt, TetraCube house) {
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
    } */
}
