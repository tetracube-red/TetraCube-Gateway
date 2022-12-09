package red.tetracube.housedevicesmesh.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;
import red.tetracube.DeviceType;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

public class HouseDevice {

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
    DeviceType deviceType;

    @JsonProperty
    @NotNull
    public HouseDeviceEnvironment environment;
}
