package red.tetracube.accounts;

import io.smallrye.mutiny.Uni;
import red.tetracube.accounts.dto.AccountDTO;
import red.tetracube.accounts.dto.CreateAccountDTO;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/accounts")
public class AccountsResources {

    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<AccountDTO> createAccount(@Valid CreateAccountDTO createAccount) {

    }
}
