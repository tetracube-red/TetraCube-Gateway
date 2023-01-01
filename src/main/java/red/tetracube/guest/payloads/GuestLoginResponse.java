package red.tetracube.guest.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;

public class GuestLoginResponse {

    @JsonProperty
    @NotNull
    @NotEmpty
    public String token;
}
