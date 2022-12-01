package red.tetracube.accounts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.common.constraint.NotNull;
import red.tetracube.data.entities.Account;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

public class AccountDTO {

    @JsonProperty
    @NotNull
    public UUID id;

    @JsonProperty
    @NotNull
    @NotEmpty
    public String name;

    public void fromEntity(Account account) {
        this.id = account.getId();
        this.name = account.getName();
    }
}
