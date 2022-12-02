package red.tetracube.accounts.services;

import io.smallrye.mutiny.Uni;
import red.tetracube.accounts.dto.AccountDTO;
import red.tetracube.accounts.dto.CreateAccountDTO;
import red.tetracube.data.entities.Account;
import red.tetracube.data.entities.AuthenticationToken;
import red.tetracube.data.entities.House;
import red.tetracube.data.repositories.AccountRepository;
import red.tetracube.data.repositories.AuthenticationTokenRepository;
import red.tetracube.data.repositories.HouseRepository;
import red.tetracube.exceptions.ConflictsRequestException;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.sql.Timestamp;
import java.time.Instant;

@ApplicationScoped
public class CreateAccountService {

    private final AccountRepository accountRepository;
    private final AuthenticationTokenRepository authenticationTokenRepository;
    private final HouseRepository houseRepository;

    public CreateAccountService(AccountRepository accountRepository,
                                AuthenticationTokenRepository authenticationTokenRepository,
                                HouseRepository houseRepository) {
        this.accountRepository = accountRepository;
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.houseRepository = houseRepository;
    }

    public Uni<AccountDTO> createAccount(CreateAccountDTO createAccountDTO) {
        var authenticationTokenUni = authenticationTokenRepository.getByToken(createAccountDTO.authenticationToken);
        var userExistsUni = accountRepository.existsByName(createAccountDTO.username);
        var houseUni = houseRepository.getById(createAccountDTO.houseId);
        return Uni.combine().all().unis(authenticationTokenUni, userExistsUni, houseUni)
                .collectFailures()
                .asTuple()
                .invoke(unis ->  {
                    validateAuthenticationToken(unis.getItem1());
                    validateAccountExists(unis.getItem2());
                    validateHouse(unis.getItem3());
                })
                .map(unis -> createAccountEntity(createAccountDTO.username, unis.getItem1(), unis.getItem3()))
                .flatMap(accountRepository::save)
                .call(account -> {
                    var linkedAuthenticationToken = account.getAuthenticationToken();
                    linkedAuthenticationToken.setAsInUse();
                    return authenticationTokenRepository.save(linkedAuthenticationToken);
                })
                .map(accountEntity -> {
                    var accountDTO = new AccountDTO();
                    accountDTO.fromEntity(accountEntity);
                    return accountDTO;
                });
    }

    private void validateAuthenticationToken(AuthenticationToken authenticationToken) {
        if (authenticationToken == null) {
            throw new NotFoundException("TOKEN_NOT_FOUND");
        }
        if (authenticationToken.getValidUntil().before(Timestamp.from(Instant.now()))) {
            throw new BadRequestException("TOKEN_EXPIRED");
        }
        if (authenticationToken.getInUse()) {
            throw new BadRequestException("TOKEN_ALREADY_IN_USE");
        }
    }

    private void validateAccountExists(boolean exists) {
        if (exists) {
            throw new ConflictsRequestException("ACCOUNT_ALREADY_EXISTS");
        }
    }

    private void validateHouse(House house) {
        if (house == null) {
            throw new NotFoundException("HOUSE_NOT_FOUND");
        }
    }

    private Account createAccountEntity(String username, AuthenticationToken authenticationToken, House house) {
        authenticationToken.setAsInUse();
        return new Account(
                username,
                house,
                authenticationToken
        );
    }
}
