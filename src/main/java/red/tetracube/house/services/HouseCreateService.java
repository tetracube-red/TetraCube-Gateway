package red.tetracube.house.services;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.tetracube.data.entities.House;
import red.tetracube.data.repositories.HouseRepository;
import red.tetracube.house.dto.HouseCreateDTO;
import red.tetracube.house.dto.HouseDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class HouseCreateService {

    private final Logger LOGGER = LoggerFactory.getLogger(HouseCreateService.class);

    private final HouseRepository houseRepository;

    @Inject
    public HouseCreateService(HouseRepository houseRepository) {
        this.houseRepository = houseRepository;
    }

    public Uni<HouseDTO> createHouse(HouseCreateDTO houseCreate) {
        var houseDB = new House(houseCreate.name);
        return this.houseExists(houseCreate.name)
                .flatMap(ignored -> this.houseRepository.save(houseDB))
                .map(createdHouseDB -> {
                    var houseDTO = new HouseDTO();
                    houseDTO.mapFromEntity(createdHouseDB);
                    return houseDTO;
                })
                .onFailure()
                .invoke(exception -> LOGGER.warn("The house already exists"));
    }

    private Uni<Boolean> houseExists(String name) {
        return this.houseRepository.existsByName(name)
                .map(Unchecked.function(this::uniqueOrThrowException));
    }

    private Boolean uniqueOrThrowException(Boolean exists) {
        if (exists) {
            throw new ClientErrorException("HOUSE_ALREADY_EXISTS", Response.Status.CONFLICT);
        }
        return false;
    }
}
