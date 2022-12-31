package red.tetracube.tetracube.services;

import io.smallrye.mutiny.Uni;
import red.tetracube.core.enumerations.FailureReason;
import red.tetracube.core.models.BusinessValidationResult;
import red.tetracube.data.entities.TetraCube;
import red.tetracube.data.repositories.TetraCubeRepository;
import red.tetracube.tetracube.payloads.TetraCubeCreateRequest;
import red.tetracube.tetracube.payloads.TetraCubeCreateResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TetraCubeCreateService {

    private final TetraCubeRepository tetraCubeRepository;

    @Inject
    public TetraCubeCreateService(TetraCubeRepository tetraCubeRepository) {
        this.tetraCubeRepository = tetraCubeRepository;
    }

    public Uni<BusinessValidationResult> validateTetraCubeCreation(TetraCubeCreateRequest request) {
        return this.tetraCubeRepository.existsByName(request.name)
                .map(tetracubeExists -> {
                    if (tetracubeExists) {
                        return BusinessValidationResult.failed(FailureReason.CONFLICTS, "HOUSE_ALREADY_EXISTS");
                    }

                    return BusinessValidationResult.success();
                });
    }

    public Uni<TetraCubeCreateResponse> createTetraCube(TetraCubeCreateRequest tetraCubeCreate) {
        var tetraCubeEntity = new TetraCube(tetraCubeCreate.name);
        var tetraCubeSaveUni = this.tetraCubeRepository.save(tetraCubeEntity);
        return tetraCubeSaveUni.map(this::mapEntityToResponse);
    }

    private TetraCubeCreateResponse mapEntityToResponse(TetraCube tetraCubeEntity) {
        var tetraCubeResponse = new TetraCubeCreateResponse();
        tetraCubeResponse.mapFromEntity(tetraCubeEntity);
        return tetraCubeResponse;
    }

}
