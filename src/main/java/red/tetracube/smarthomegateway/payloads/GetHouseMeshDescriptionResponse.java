package red.tetracube.smarthomegateway.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;
import red.tetracube.DescribeDevicesMeshResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetHouseMeshDescriptionResponse {

    @JsonProperty
    @NotNull
    public List<HouseMeshDeviceResponse> houseMeshDeviceResponseList = new ArrayList<>();

    public void fromGrpcServiceResponse(DescribeDevicesMeshResponse describeDevicesMeshResponse) {
        this.houseMeshDeviceResponseList = describeDevicesMeshResponse.getDevicesList().stream()
                .map(grpcDevice -> {
                    var environment = new HouseEnvironmentResponse();
                    environment.id = UUID.fromString(grpcDevice.getEnvironment().getId());
                    environment.name = grpcDevice.getEnvironment().getName();
                    var device = new HouseMeshDeviceResponse();
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
