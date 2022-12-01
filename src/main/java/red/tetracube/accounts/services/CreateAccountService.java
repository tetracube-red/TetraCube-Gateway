package red.tetracube.accounts.services;

import io.smallrye.mutiny.Uni;
import red.tetracube.accounts.dto.AccountDTO;
import red.tetracube.accounts.dto.CreateAccountDTO;
import red.tetracube.data.repositories.AccountRepository;
import red.tetracube.data.repositories.AuthenticationTokenRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateAccountService {

    private final AccountRepository accountRepository;
    private final AuthenticationTokenRepository authenticationTokenRepository;

    public CreateAccountService(AccountRepository accountRepository,
                                AuthenticationTokenRepository authenticationTokenRepository) {
        this.accountRepository = accountRepository;
        this.authenticationTokenRepository = authenticationTokenRepository;
    }

    public Uni<AccountDTO> createAccount(CreateAccountDTO createAccountDTO) {
        // search
    }
}
