package red.tetracube.housedevicesmesh.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

public class HouseEnvironmentResponse {

    @JsonProperty
    @NotNull
    public UUID id;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String name;
}
