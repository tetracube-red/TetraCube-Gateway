package red.tetracube.house.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;

public class HouseCreateDTO {

    @JsonProperty
    @NotNull
    @NotEmpty
    public String name;
}
