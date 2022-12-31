package red.tetracube.tetracube.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;

public class TetraCubeCreateRequest {

    @JsonProperty
    @NotNull
    @NotEmpty
    public String name;
    
}
