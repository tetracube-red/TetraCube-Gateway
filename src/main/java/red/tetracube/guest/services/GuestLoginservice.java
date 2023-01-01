package red.tetracube.guest.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import red.tetracube.configuration.properties.BearerTokenConfigurationProperties;
import red.tetracube.core.enumerations.FailureReason;
import red.tetracube.core.models.BusinessValidationResult;
import red.tetracube.data.entities.Permission;
import red.tetracube.data.entities.TetraCube;
import red.tetracube.data.repositories.GuestRepository;
import red.tetracube.guest.payloads.GuestLoginRequest;
import red.tetracube.guest.payloads.GuestLoginResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class GuestLoginservice {

    private final static Logger LOGGER = LoggerFactory.getLogger(GuestLoginservice.class);

    private final GuestRepository guestRepository;
    private final BearerTokenConfigurationProperties bearerTokenConfigurationProperties;
    private final BCryptPasswordEncoder passwordEncoder;

    public GuestLoginservice(GuestRepository guestRepository,
            BearerTokenConfigurationProperties bearerTokenConfigurationProperties) {
        this.guestRepository = guestRepository;
        this.bearerTokenConfigurationProperties = bearerTokenConfigurationProperties;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Uni<BusinessValidationResult> validateGuestLoginRequest(GuestLoginRequest guestLoginRequest) {
        var guestUni = this.guestRepository.getByNickname(guestLoginRequest.nickname);
        return guestUni
                .map(guest -> {
                    if (guest == null) {
                        LOGGER.warn("Guest with nickname {} does not exists", guestLoginRequest.nickname);
                        return BusinessValidationResult.failed(FailureReason.UNAUTHORIZED, "USERNAME_NOT_FOUND");
                    }
                    if (!this.passwordEncoder.matches(guestLoginRequest.password, guest.getPassword())) {
                        LOGGER.warn("Wrong credentials for guest {}", guestLoginRequest.nickname);
                        return BusinessValidationResult.failed(FailureReason.UNAUTHORIZED, "WRONG_CREDENTIALS");
                    }
                    return BusinessValidationResult.success();
                });
    }

    public Uni<GuestLoginResponse> tryToLoginGuest(GuestLoginRequest guestLoginRequest) {
        var guestUni = this.guestRepository.getByNickname(guestLoginRequest.nickname)
                .onItem()
                .invoke(ignore -> LOGGER.info("Getting guest by nickname {}", guestLoginRequest.nickname));

        return guestUni.map(guest -> {
            var bearerToken = this.emitBearerToken(
                    guest.getId(),
                    guest.getNickname(),
                    Instant.now(),
                    Instant.now().plus(1, ChronoUnit.DAYS),
                    guest.getTetracube(),
                    guest.getPermissionList());

            var guestLoginResponse = new GuestLoginResponse();
            guestLoginResponse.token = bearerToken;
            return guestLoginResponse;
        });
    }

    private String emitBearerToken(UUID userId, String nickname, Instant issuedAt, Instant expiresAt,
            TetraCube tetracube, List<Permission> permissions) {
        var groups = permissions.stream()
                .map(p -> p.getName().toString())
                .collect(Collectors.toSet());
        return Jwt.issuer(bearerTokenConfigurationProperties.getIssuer())
                .upn(nickname)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .groups(groups)
                .audience(bearerTokenConfigurationProperties.getAudiences())
                .claim("userId", userId)
                .claim("houseId", tetracube.getId())
                .claim("houseName", tetracube.getName())
                .sign();
    }

}
