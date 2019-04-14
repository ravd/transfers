package rd.transactions.controller.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rd.transactions.config.ObjectMapperFactory;
import rd.transactions.dto.AccountDto;
import rd.transactions.exceptions.AccountBadRequestException;
import rd.transactions.exceptions.AccountDoesNotExistException;
import rd.transactions.exceptions.BalanceInvalidException;
import rd.transactions.model.Account;
import rd.transactions.model.AccountId;
import rd.transactions.managers.AccountManager;
import rd.transactions.validators.BalanceValidator;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
    private final AccountManager accountManager;
    private final BalanceValidator balanceValidator;
    private final AccountConverter accountConverter;

    public AccountController(
            AccountManager accountManager,
            BalanceValidator balanceValidator, AccountConverter accountConverter) {
        this.accountManager = accountManager;
        this.balanceValidator = balanceValidator;
        this.accountConverter = accountConverter;
    }

    public String getAccounts() throws JsonProcessingException {
        List<AccountDto> accountList = accountManager.getAccounts().stream()
                .map(accountConverter::toDto)
                .collect(toList());
        return mapper.writeValueAsString(accountList);
    }

    public String getAccount(String accountId) throws JsonProcessingException {
        AccountDto accountDto = accountManager.getAccountById(AccountId.of(accountId))
                .map(accountConverter::toDto)
                .orElseThrow(AccountDoesNotExistException::new);
        return mapper.writeValueAsString(accountDto);
    }

    public String addAccount(String accountJson) throws JsonProcessingException {
        AccountDto accountDto = parseAccount(accountJson);
        if (!balanceValidator.isBalanceAllowed(accountDto.getAmount())) {
            throw new BalanceInvalidException();
        }
        Account account = accountConverter.toModel(accountDto);
        accountManager.createAccount(account);
        return mapper.writeValueAsString(accountConverter.toDto(account));
    }

    private AccountDto parseAccount(String accountJson) {
        try {
            return mapper.readValue(accountJson, AccountDto.class);
        } catch (IOException e) {
            logger.error("Can't parse the request", e);
            throw new AccountBadRequestException();
        }
    }


}
