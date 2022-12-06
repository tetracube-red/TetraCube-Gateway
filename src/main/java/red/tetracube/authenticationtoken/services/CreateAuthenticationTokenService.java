package red.tetracube.authenticationtoken.services;

import io.smallrye.mutiny.Uni;
import red.tetracube.authenticationtoken.dto.AuthenticationTokenDTO;
import red.tetracube.authenticationtoken.dto.CreateAuthenticationTokenRequest;
import red.tetracube.data.entities.AuthenticationToken;
import red.tetracube.data.entities.House;
import red.tetracube.data.repositories.AuthenticationTokenRepository;
import red.tetracube.data.repositories.HouseRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
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

    public Uni<AuthenticationTokenDTO> createAuthenticationToken(CreateAuthenticationTokenRequest request) {
        var token = UUID.randomUUID().toString().replace("-", "");
        return houseRepository.getByName(request.houseName)
                .invoke(this::validateHouse)
                .map(house -> new AuthenticationToken(token, house))
                .flatMap(this.authenticationTokenRepository::save)
                .map(createdToken -> {
                    var authenticationTokenDTO = new AuthenticationTokenDTO();
                    authenticationTokenDTO.mapFromEntity(createdToken);
                    return authenticationTokenDTO;
                });
    }

    private void validateHouse(House house) {
        if (house == null) {
            throw new NotFoundException("HOUSE_NOT_FOUND");
        }
    }
}
