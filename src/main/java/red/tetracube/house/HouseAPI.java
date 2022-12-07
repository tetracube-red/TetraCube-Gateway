package red.tetracube.house;

import io.smallrye.mutiny.Uni;
import red.tetracube.house.payloads.HouseCreateResponse;
import red.tetracube.house.payloads.HouseCreateRequest;
import red.tetracube.house.services.HouseCreateService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/houses")
public class HouseAPI {

    private final HouseCreateService houseCreateService;

    @Inject
    public HouseAPI(HouseCreateService houseCreateService) {
        this.houseCreateService = houseCreateService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public Uni<HouseCreateResponse> createHouse(@Valid HouseCreateRequest houseCreate) {
        return this.houseCreateService.createHouse(houseCreate)
                .map(houseCreationResult -> {
                    if (!houseCreationResult.getSuccess()) {
                        houseCreationResult.mapAsResponse();
                    }
                    return houseCreationResult.getResultContent();
                });
    }
}
