package red.tetracube.guest;

import io.smallrye.mutiny.Uni;
import red.tetracube.core.models.BusinessValidationResult;
import red.tetracube.guest.payloads.GuestSubscriptionRequest;
import red.tetracube.guest.payloads.GuestSubscriptionResponse;
import red.tetracube.guest.payloads.UserLoginRequest;
import red.tetracube.guest.payloads.UserLoginResponse;
import red.tetracube.guest.services.GuestSubscriptionService;
import red.tetracube.guest.services.UserLoginService;

import javax.enterprise.context.RequestScoped;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/guests")
@RequestScoped
public class GuestAPI {

    private final UserLoginService userLoginService;
    private final GuestSubscriptionService guestSubscriptionService;

    public GuestAPI(UserLoginService userLoginService,
            GuestSubscriptionService guestSubscriptionService) {
        this.userLoginService = userLoginService;
        this.guestSubscriptionService = guestSubscriptionService;
    }

    @POST
    @Path("/subscribe")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<GuestSubscriptionResponse> subscribeGuest(@Valid GuestSubscriptionRequest request) {
        return this.guestSubscriptionService.validateSubscriptionRequest(request)
            .invoke(validationResult -> {
                if (!validationResult.getSuccess()) {
                    validationResult.mapAsResponse();
                }
            })
            .flatMap(ignored -> {
                var guestSubscriptionResponseUni = this.guestSubscriptionService.doGuestSubcription(request);
                return guestSubscriptionResponseUni;
            });
    }

    /*
     * @POST
     * 
     * @Path("/login")
     * 
     * @Produces(MediaType.APPLICATION_JSON)
     * 
     * @Consumes(MediaType.APPLICATION_JSON)
     * public Uni<UserLoginResponse> doUserLogin(@Valid UserLoginRequest
     * userLoginRequest) {
     * var validateUserUni =
     * this.userLoginService.validateUserAndAuthenticationTokenRelation(
     * userLoginRequest.username, userLoginRequest.authenticationCode);
     * return validateUserUni
     * .onItem()
     * .invoke(validationResult -> {
     * if (!validationResult.getSuccess()) {
     * validationResult.mapAsResponse();
     * }
     * })
     * .flatMap(validationResultSet ->
     * this.userLoginService.tryToLoginUser(userLoginRequest)
     * .map(Result::getResultContent)
     * );
     * }
     */
}
