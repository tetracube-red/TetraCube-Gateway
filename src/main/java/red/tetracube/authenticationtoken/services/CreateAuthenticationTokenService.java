package red.tetracube.authenticationtoken.services;

import io.smallrye.mutiny.Uni;
import red.tetracube.authenticationtoken.payloads.CreateAuthenticationTokenRequest;
import red.tetracube.authenticationtoken.payloads.CreateAuthenticationTokenResponse;
import red.tetracube.core.enumerations.FailureReason;
import red.tetracube.core.models.Result;
import red.tetracube.data.entities.AuthenticationToken;
import red.tetracube.data.entities.House;
import red.tetracube.data.repositories.AuthenticationTokenRepository;
import red.tetracube.data.repositories.HouseRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class CreateAuthenticationTokenService {

    private final AuthenticationTokenRepository authenticationTokenRepository;
    private final HouseRepository houseRepository;

    @Inject
    public CreateAuthenticationTokenService(AuthenticationTokenRepository authenticationTokenRepository,
                                            HouseRepository houseRepository) {
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.houseRepository = houseRepository;
    }

    public Uni<Result<CreateAuthenticationTokenResponse>> createAuthenticationToken(CreateAuthenticationTokenRequest request) {
        return houseRepository.getByName(request.houseName)
                .flatMap(optionalHouse -> {
                    if (optionalHouse.isEmpty()) {
                        return Uni.createFrom().item(Result.failed(FailureReason.NOT_FOUND, "HOUSE_NOT_FOUND"));
                    }
                    return this.createAuthenticationToken(optionalHouse.get())
                            .map(this::mapSuccessfulResult);
                });
    }

    private Result<CreateAuthenticationTokenResponse> mapSuccessfulResult(AuthenticationToken savedAuthenticationToken) {
        var authenticationTokenDTO = new CreateAuthenticationTokenResponse();
        authenticationTokenDTO.mapFromEntity(savedAuthenticationToken);
        return Result.success(authenticationTokenDTO);
    }

    private Uni<AuthenticationToken> createAuthenticationToken(House house) {
        var token = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        var authenticationToken = new AuthenticationToken(token, house);
        return authenticationTokenRepository.save(authenticationToken);
    }
}
