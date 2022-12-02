package red.tetracube.accounts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

public class CreateAccountDTO {

    @JsonProperty
    @NotNull
    @NotEmpty
    public String username;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String authenticationToken;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String houseName;
}
