package red.tetracube.authenticationtoken.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;
import red.tetracube.data.entities.AuthenticationToken;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

public class AuthenticationTokenDTO {

    @JsonProperty
    @NotNull
    @NotEmpty
    public String token;

    @JsonProperty
    @NotNull
    public LocalDateTime validUntil;

    public void mapFromEntity(AuthenticationToken authenticationToken) {
        this.token = authenticationToken.getToken();
        this.validUntil = authenticationToken.getValidUntil().toLocalDateTime();
    }
}
