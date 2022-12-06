package red.tetracube.authenticationtoken.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;

public class CreateAuthenticationTokenRequest {

    @JsonProperty
    @NotNull
    @NotEmpty
    public String houseName;
}
