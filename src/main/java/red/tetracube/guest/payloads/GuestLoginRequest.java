package red.tetracube.guest.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;

public class GuestLoginRequest {

    @JsonProperty
    @NotNull
    @NotEmpty
    public String nickname;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String password;
}
