package red.tetracube.users;

import io.smallrye.mutiny.Uni;
import red.tetracube.users.payloads.UserLoginRequest;
import red.tetracube.users.payloads.UserLoginResponse;
import red.tetracube.users.services.UserLoginService;

import javax.enterprise.context.RequestScoped;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/users")
@RequestScoped
public class UserAPI {

    private final UserLoginService userLoginService;

    public UserAPI(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<UserLoginResponse> doUserLogin(@Valid UserLoginRequest userLoginRequest) {
        return this.userLoginService.tryToLoginUser(userLoginRequest)
                .map(loginUserResult -> {
                    if (!loginUserResult.getSuccess()) {
                        loginUserResult.mapAsResponse();
                    }
                    return loginUserResult.getResultContent();
                });
    }
}
