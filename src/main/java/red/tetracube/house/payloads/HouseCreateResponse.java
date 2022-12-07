package red.tetracube.house.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;
import red.tetracube.data.entities.House;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

public class HouseCreateResponse {

    @JsonProperty
    @NotNull
    public UUID id;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String name;

    public void mapFromEntity(House house) {
        this.id = house.getId();
        this.name = house.getName();
    }
}
