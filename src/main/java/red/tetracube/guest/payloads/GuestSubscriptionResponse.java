package red.tetracube.guest.payloads;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GuestSubscriptionResponse {

    @JsonProperty
    public UUID guestId;

    @JsonProperty
    public String nickname;

    @JsonProperty
    public String accessCode;

}
