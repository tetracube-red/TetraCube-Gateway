package red.tetracube.guest.services;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.smallrye.mutiny.Uni;
import red.tetracube.core.enumerations.FailureReason;
import red.tetracube.core.models.BusinessValidationResult;
import red.tetracube.data.entities.Guest;
import red.tetracube.data.repositories.GuestRepository;
import red.tetracube.data.repositories.PermissionRepository;
import red.tetracube.data.repositories.TetraCubeRepository;
import red.tetracube.guest.payloads.GuestSubscriptionRequest;
import red.tetracube.guest.payloads.GuestSubscriptionResponse;

@ApplicationScoped
public class GuestSubscriptionService {

    private final Logger LOGGER = LoggerFactory.getLogger(GuestSubscriptionService.class);

    private final GuestRepository guestRepository;
    private final TetraCubeRepository tetraCubeRepository;
    private final PermissionRepository permissionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public GuestSubscriptionService(GuestRepository guestRepository,
            PermissionRepository permissionRepository,
            TetraCubeRepository tetraCubeRepository) {
        this.guestRepository = guestRepository;
        this.tetraCubeRepository = tetraCubeRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Uni<BusinessValidationResult> validateSubscriptionRequest(GuestSubscriptionRequest request) {
        LOGGER.info("Checking if there is a guest with same nickname");
        var guestExistsByNameUni = this.guestRepository.existsByName(request.nickname);
        var tetraCubeExistsByNameUni = this.tetraCubeRepository.existsByName(request.tetraCubeName);
        return Uni.combine().all().unis(guestExistsByNameUni, tetraCubeExistsByNameUni)
                .asTuple()
                .map(queriesResults -> {
                    var guestExistsByName = queriesResults.getItem1();
                    var tetraCubeExistsByName = queriesResults.getItem2();
                    if (guestExistsByName) {
                        LOGGER.warn("There is another guest with the same nick");
                        return BusinessValidationResult.failed(FailureReason.CONFLICTS, "GUEST_EXISTS");
                    }
                    if (!tetraCubeExistsByName) {
                        LOGGER.warn("There is another tetracube with the same name");
                        return BusinessValidationResult.failed(FailureReason.NOT_FOUND, "TETRACUBE_NOT_EXISTS");
                    }
                    return BusinessValidationResult.success();
                });
    }

    public Uni<GuestSubscriptionResponse> doGuestSubcription(GuestSubscriptionRequest request) {
        LOGGER.info("Creating random password");
        var authCode = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return this.tetraCubeRepository.getByName(request.tetraCubeName)
                .map(tetracube -> {
                    LOGGER.info("Encoding password");
                    var encodedAuthCode = this.passwordEncoder.encode(authCode);
                    LOGGER.info("Creating the guest entity and store it");
                    var guest = new Guest(request.nickname, encodedAuthCode, tetracube);
                    return guest;
                })
                .flatMap(guest -> {
                    LOGGER.info("Storing the guest");
                    return this.guestRepository.save(guest);
                })
                .onItem()
                .call(guest -> this.attachPermissionsToGuest(guest))
                .map(guestEntity -> {
                    LOGGER.info("Creating and returning response");
                    var response = new GuestSubscriptionResponse();
                    response.accessCode = authCode;
                    response.guestId = guestEntity.getId();
                    response.nickname = guestEntity.getNickname();
                    return response;
                });
    }

    private Uni<Void> attachPermissionsToGuest(Guest guest) {
        return this.permissionRepository.getAll()
            .flatMap(permissions -> {
                guest.getPermissionList().addAll(permissions);
                return this.guestRepository.save(guest);
            })
            .flatMap(ignored -> Uni.createFrom().voidItem());
    }

}
