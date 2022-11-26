package red.tetracube.house;

import io.smallrye.mutiny.Uni;
import red.tetracube.house.dto.HouseDTO;
import red.tetracube.house.dto.HouseCreateDTO;
import red.tetracube.house.services.HouseCreateService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/houses")
public class HouseResources {

    private final HouseCreateService houseCreateService;

    @Inject
    public HouseResources(HouseCreateService houseCreateService) {
        this.houseCreateService = houseCreateService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public Uni<HouseDTO> createHouse(@Valid HouseCreateDTO houseCreate) {
        return this.houseCreateService.createHouse(houseCreate);
    }
}
