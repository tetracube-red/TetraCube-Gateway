package red.tetracube.users;

import io.smallrye.mutiny.Uni;
import red.tetracube.core.models.Result;
import red.tetracube.users.payloads.UserLoginRequest;
import red.tetracube.users.payloads.UserLoginResponse;
import red.tetracube.users.services.UserLoginService;

import javax.enterprise.context.RequestScoped;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
        var validateAccountUni = this.userLoginService.validateAccountAndAuthenticationTokenRelation(userLoginRequest.username, userLoginRequest.authenticationCode);
        return validateAccountUni
                .onItem()
                .invoke(validationResult -> {
                    if (!validationResult.getSuccess()) {
                        validationResult.mapAsResponse();
                    }
                })
                .flatMap(validationResultSet ->
                        this.userLoginService.tryToLoginUser(userLoginRequest)
                                .map(Result::getResultContent)
                );
    }
}
