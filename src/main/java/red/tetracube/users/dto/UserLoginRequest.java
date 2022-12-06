package red.tetracube.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;

public class UserLoginRequest {

    @JsonProperty
    @NotNull
    @NotEmpty
    public String username;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String authenticationCode;
}
