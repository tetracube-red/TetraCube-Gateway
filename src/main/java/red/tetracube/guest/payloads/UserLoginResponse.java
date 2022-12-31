package red.tetracube.guest.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

public class UserLoginResponse {

    @JsonProperty
    @NotNull
    public UUID id;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String name;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String token;

    @JsonProperty
    public UUID houseId;

    @JsonProperty
    public String houseName;
}