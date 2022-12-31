package red.tetracube.tetracube.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;
import red.tetracube.data.entities.TetraCube;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

public class TetraCubeCreateResponse {

    @JsonProperty
    @NotNull
    public UUID id;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String name;

    public void mapFromEntity(TetraCube house) {
        this.id = house.getId();
        this.name = house.getName();
    }
    
}
