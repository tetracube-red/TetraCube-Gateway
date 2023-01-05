package red.tetracube.smarthomegateway;

import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.eclipse.microprofile.jwt.JsonWebToken;

import red.tetracube.DescribeDevicesMeshRequest;
import red.tetracube.TetracubeDevicesMesh;
import red.tetracube.smarthomegateway.payloads.GetHouseMeshDescriptionResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@RolesAllowed({"TetraCube Smart Home"})
@Path("/smart-home")
public class TetraCubeSmartHomeAPI {

    @GrpcClient
    TetracubeDevicesMesh devices;

    private final JsonWebToken jwt;

    public TetraCubeSmartHomeAPI(JsonWebToken jwt) {
        this.jwt = jwt;
    }

    @GET
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<GetHouseMeshDescriptionResponse> getHouseMeshDescription() {
        var optionalTetraCubeId = this.jwt.<String>claim("houseId");
        if (optionalTetraCubeId.isEmpty()) {
            throw new NotFoundException("NO_HOUSE_FOUND");
        }
        var describeDevicesMeshRequest = DescribeDevicesMeshRequest.newBuilder()
                .setTetracubeId(optionalTetraCubeId.get())
                .build();
        return this.devices.describeDevicesMesh(describeDevicesMeshRequest)
               /*  .invoke(Unchecked.consumer(response -> {
                            if (response == null) {
                                throw new NotFoundException("NO_HOUSE_FOUND");
                            }
                        })
                ) */
                .map(grpcResponse -> {
                    var houseMeshDescriptionResponse = new GetHouseMeshDescriptionResponse();
                    houseMeshDescriptionResponse.fromGrpcServiceResponse(grpcResponse);
                    return houseMeshDescriptionResponse;
                });
    }
}
