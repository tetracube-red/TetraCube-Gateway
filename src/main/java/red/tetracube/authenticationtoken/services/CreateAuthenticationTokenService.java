package red.tetracube.authenticationtoken.services;

import io.smallrye.mutiny.Uni;
import red.tetracube.authenticationtoken.dto.AuthenticationTokenDTO;
import red.tetracube.data.entities.AuthenticationToken;
import red.tetracube.data.repositories.AuthenticationTokenRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class CreateAuthenticationTokenService {

    private final AuthenticationTokenRepository authenticationTokenRepository;

    @Inject
    public CreateAuthenticationTokenService(AuthenticationTokenRepository authenticationTokenRepository) {
        this.authenticationTokenRepository = authenticationTokenRepository;
    }

    public Uni<AuthenticationTokenDTO> createAuthenticationToken() {
        var token = UUID.randomUUID().toString().replace("-", "");
        var authenticationToken = new AuthenticationToken(token);
        return this.authenticationTokenRepository.save(authenticationToken)
                .map(createdToken -> {
                    var authenticationTokenDTO = new AuthenticationTokenDTO();
                    authenticationTokenDTO.mapFromEntity(createdToken);
                    return authenticationTokenDTO;
                });
    }
}
