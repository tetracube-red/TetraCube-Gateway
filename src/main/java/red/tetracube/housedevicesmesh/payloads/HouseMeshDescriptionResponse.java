package red.tetracube.housedevicesmesh.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;
import red.tetracube.DescribeDeviceMeshResponse;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HouseMeshDescriptionResponse {

    @JsonProperty
    @NotNull
    public UUID houseId;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String name;

    @JsonProperty
    @NotNull
    public List<HouseDevice> houseDeviceList = new ArrayList<>();

    public void fromGrpcServiceResponse(DescribeDeviceMeshResponse describeDeviceMeshResponse) {
        this.houseId = UUID.fromString(describeDeviceMeshResponse.getId());
        this.name = describeDeviceMeshResponse.getName();
        this.houseDeviceList = describeDeviceMeshResponse.getDevicesList().stream()
                .map(grpcDevice -> {
                    var environment = new HouseDeviceEnvironment();
                    environment.id = UUID.fromString(grpcDevice.getEnvironment().getId());
                    environment.name = grpcDevice.getEnvironment().getName();
                    var device = new HouseDevice();
                    device.id = UUID.fromString(grpcDevice.getId());
                    device.deviceType = grpcDevice.getDeviceType();
                    device.environment = environment;
                    device.isOnline = grpcDevice.getIsOnline();
                    device.name = grpcDevice.getName();
                    device.colorCode = grpcDevice.getColorCode();
                    return device;
                })
                .toList();
    }
}
