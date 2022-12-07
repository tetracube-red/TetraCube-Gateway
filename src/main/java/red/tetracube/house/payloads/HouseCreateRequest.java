package red.tetracube.house.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;

public class HouseCreateRequest {

    @JsonProperty
    @NotNull
    @NotEmpty
    public String name;
}
