package red.tetracube.tetracube;

import io.smallrye.mutiny.Uni;
import red.tetracube.tetracube.payloads.TetraCubeCreateRequest;
import red.tetracube.tetracube.payloads.TetraCubeCreateResponse;
import red.tetracube.tetracube.services.TetraCubeCreateService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/tetracubes")
public class TetracubeAPI {

    private final TetraCubeCreateService tetraCubeCreateService;

    @Inject
    public TetracubeAPI(TetraCubeCreateService tetraCubeCreateService) {
        this.tetraCubeCreateService = tetraCubeCreateService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/create")
    public Uni<TetraCubeCreateResponse> createTetraCube(@Valid TetraCubeCreateRequest houseCreate) {
        return this.tetraCubeCreateService.validateTetraCubeCreation(houseCreate)
            .invoke(businessValidationResult -> {
                if (!businessValidationResult.getSuccess()) {
                    businessValidationResult.mapAsResponse();
                }
            })
            .flatMap(ignored ->  this.tetraCubeCreateService.createTetraCube(houseCreate));
    }
    
}
