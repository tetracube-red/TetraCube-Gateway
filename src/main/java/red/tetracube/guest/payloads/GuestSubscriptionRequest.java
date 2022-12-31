package red.tetracube.guest.payloads;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.smallrye.common.constraint.NotNull;

public class GuestSubscriptionRequest {

    @NotNull
    @NotEmpty
    @JsonProperty
    public String tetraCubeName;
    
    @NotNull
    @NotEmpty
    @JsonProperty
    public String nickname;
    
}
