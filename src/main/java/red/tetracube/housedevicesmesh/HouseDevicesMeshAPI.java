package red.tetracube.housedevicesmesh;

import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.eclipse.microprofile.jwt.JsonWebToken;
import red.tetracube.DescribeDeviceMeshRequest;
import red.tetracube.HouseDevicesMesh;
import red.tetracube.housedevicesmesh.payloads.GetHouseMeshDescriptionResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@RolesAllowed({"User"})
@Path("/house/mesh")
public class HouseDevicesMeshAPI {

    @GrpcClient
    HouseDevicesMesh house;

    private final JsonWebToken jwt;

    public HouseDevicesMeshAPI(JsonWebToken jwt) {
        this.jwt = jwt;
    }

    @GET
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<GetHouseMeshDescriptionResponse> getHouseMeshDescription() {
        var optionalHouseId = this.jwt.<String>claim("houseId");
        if (optionalHouseId.isEmpty()) {
            throw new NotFoundException("NO_HOUSE_FOUND");
        }
        var describeDeviceMeshRequest = DescribeDeviceMeshRequest.newBuilder()
                .setHouseId(optionalHouseId.get())
                .build();
        return this.house.describeDevicesMesh(describeDeviceMeshRequest)
                .invoke(Unchecked.consumer(response -> {
                            if (response == null) {
                                throw new NotFoundException("NO_HOUSE_FOUND");
                            }
                        })
                )
                .map(grpcResponse -> {
                    var houseMeshDescriptionResponse = new GetHouseMeshDescriptionResponse();
                    houseMeshDescriptionResponse.fromGrpcServiceResponse(grpcResponse);
                    return houseMeshDescriptionResponse;
                });
    }
}
