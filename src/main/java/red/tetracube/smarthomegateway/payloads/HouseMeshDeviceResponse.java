package red.tetracube.smarthomegateway.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;
import red.tetracube.DeviceType;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

public class HouseMeshDeviceResponse {

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
    public String colorCode;

    @JsonProperty
    @NotNull
    public Boolean isOnline = true;

    @JsonProperty
    @NotNull
    public DeviceType deviceType;

    @JsonProperty
    @NotNull
    public HouseEnvironmentResponse environment;
}
