package red.tetracube.authenticationtoken;

import io.smallrye.mutiny.Uni;
import red.tetracube.authenticationtoken.dto.AuthenticationTokenDTO;
import red.tetracube.authenticationtoken.dto.CreateAuthenticationTokenRequest;
import red.tetracube.authenticationtoken.services.CreateAuthenticationTokenService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/authentication-token")
public class AuthenticationTokenAPI {

    private final CreateAuthenticationTokenService createAuthenticationTokenService;

    @Inject
    public AuthenticationTokenAPI(CreateAuthenticationTokenService createAuthenticationTokenService) {
        this.createAuthenticationTokenService = createAuthenticationTokenService;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("")
    public Uni<AuthenticationTokenDTO> createAuthenticationToken(@Valid CreateAuthenticationTokenRequest request) {
        return this.createAuthenticationTokenService.createAuthenticationToken(request);
    }
}
