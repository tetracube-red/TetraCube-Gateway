package red.tetracube.housedevicesmesh;

import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.eclipse.microprofile.jwt.JsonWebToken;
import red.tetracube.*;
import red.tetracube.housedevicesmesh.payloads.HouseMeshDescriptionResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

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
    @Path("/{houseId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<HouseMeshDescriptionResponse> getHouseMeshDescription(@PathParam("houseId") UUID houseId) {
        return this.house.describeDevicesMesh(
                        DescribeDeviceMeshRequest.newBuilder()
                                .setHouseId(houseId.toString())
                                .build()
                )
                .invoke(Unchecked.consumer(response -> {
                            if (response == null) {
                                throw new NotFoundException("NO_HOUSE_FOUND");
                            }
                        })
                )
                .map(grpcResponse -> {
                    var houseMeshDescriptionResponse = new HouseMeshDescriptionResponse();
                    houseMeshDescriptionResponse.fromGrpcServiceResponse(grpcResponse);
                    return houseMeshDescriptionResponse;
                });
    }
}
