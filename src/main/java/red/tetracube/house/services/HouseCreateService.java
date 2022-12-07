package red.tetracube.house.services;

import io.smallrye.mutiny.Uni;
import red.tetracube.core.enumerations.FailureReason;
import red.tetracube.core.models.Result;
import red.tetracube.data.entities.House;
import red.tetracube.data.repositories.HouseRepository;
import red.tetracube.house.payloads.HouseCreateRequest;
import red.tetracube.house.payloads.HouseCreateResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HouseCreateService {

    private final HouseRepository houseRepository;

    @Inject
    public HouseCreateService(HouseRepository houseRepository) {
        this.houseRepository = houseRepository;
    }

    public Uni<Result<HouseCreateResponse>> createHouse(HouseCreateRequest houseCreate) {
        return this.houseRepository.existsByName(houseCreate.name)
                .flatMap(houseExists -> {
                    if (houseExists) {
                        return Uni.createFrom()
                                .item(Result.failed(FailureReason.CONFLICTS, "HOUSE_ALREADY_EXISTS"));
                    }
                    return this.createHouse(houseCreate.name)
                            .map(this::mapEntityToResponse);
                });
    }

    private Result<HouseCreateResponse> mapEntityToResponse(House createdHouseDB) {
        var houseDTO = new HouseCreateResponse();
        houseDTO.mapFromEntity(createdHouseDB);
        return Result.success(houseDTO);
    }

    private Uni<House> createHouse(String houseName) {
        var newHouse = new House(houseName);
        return this.houseRepository.save(newHouse);
    }
}
