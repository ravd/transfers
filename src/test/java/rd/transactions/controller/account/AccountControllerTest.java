package rd.transactions.controller.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import rd.transactions.exceptions.AccountBadRequestException;
import rd.transactions.exceptions.AccountDoesNotExistException;
import rd.transactions.exceptions.BalanceInvalidException;
import rd.transactions.model.Account;
import rd.transactions.model.AccountId;
import rd.transactions.model.Money;
import rd.transactions.managers.AccountManager;
import rd.transactions.validators.BalanceValidator;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rd.transactions.JsonUtils.buildAccountJson;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {

    @Mock
    private AccountManager repository;

    @Mock
    private BalanceValidator balanceValidator;

    @Test
    public void whenRequestIsValidAccountIsCreated() throws JsonProcessingException, JSONException {
        when(balanceValidator.isBalanceAllowed(any())).thenReturn(true);
        AccountController service = new AccountController(repository, balanceValidator, new AccountConverter());
        String requestBody = buildAccountJson("123A", "USD", "100.00");

        String addedAccount = service.addAccount(requestBody);

        Account expectedAccount = Account.of(
                AccountId.of("123A"),
                Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"))
        );
        verify(repository).createAccount(eq(expectedAccount));
        JSONAssert.assertEquals(requestBody, addedAccount, JSONCompareMode.LENIENT);
    }

    @Test
    public void whenRequestedBalanceHasHigherPrecisionThanCurrencyAllowsItWillBeRounded() throws JsonProcessingException, JSONException {
        when(balanceValidator.isBalanceAllowed(any())).thenReturn(true);
        AccountController service = new AccountController(repository, balanceValidator, new AccountConverter());
        String requestBody = buildAccountJson("123A", "USD", "12.12345");

        String addedAccount = service.addAccount(requestBody);

        String expectedBodyWithRoundedBalance = buildAccountJson("123A", "USD", "12.12");
        JSONAssert.assertEquals(expectedBodyWithRoundedBalance, addedAccount, JSONCompareMode.LENIENT);
    }

    @Test(expected = BalanceInvalidException.class)
    public void addAccountWithInvalidBalanceResultsInException() throws JsonProcessingException {
        when(balanceValidator.isBalanceAllowed(any())).thenReturn(false);
        AccountController service = new AccountController(repository, balanceValidator, new AccountConverter());
        String requestBody = buildAccountJson("123A", "USD", "100.0");

        service.addAccount(requestBody);
    }

    @Test(expected = AccountBadRequestException.class)
    public void missingPropertyResultsInException() throws JsonProcessingException {
        AccountController service = new AccountController(repository, balanceValidator, new AccountConverter());
        String requestBody = "{ \"accountId\": \"123A\"," +
                "\"amount\": \"100.00\"}";

        // no currency
        service.addAccount(requestBody);
    }

    @Test(expected = AccountBadRequestException.class)
    public void invalidCurrencyCodeResultsInException() throws JsonProcessingException {
        AccountController service = new AccountController(repository, balanceValidator, new AccountConverter());
        String requestBody = buildAccountJson("123A", "USDki", "100.0");

        service.addAccount(requestBody);
    }

    @Test(expected = AccountBadRequestException.class)
    public void invalidBalanceResultsInException() throws JsonProcessingException {
        AccountController service = new AccountController(repository, balanceValidator, new AccountConverter());
        String requestBody = buildAccountJson("123A", "USD", "100.x0");

        service.addAccount(requestBody);
    }

    @Test(expected = AccountDoesNotExistException.class)
    public void exceptionIsThrownWhenQueriedForAccountWhichDoesNotExist() throws JsonProcessingException {
        AccountController service = new AccountController(repository, balanceValidator, new AccountConverter());

        when(repository.getAccountById(any())).thenReturn(Optional.empty());

        service.getAccount("unknownId");
    }

    @Test
    public void whenQueriedForExistingAccountTheDataIsReturned() throws JsonProcessingException, JSONException {
        AccountController service = new AccountController(repository, balanceValidator, new AccountConverter());

        when(repository.getAccountById(eq(AccountId.of("124")))).thenReturn(
                Optional.of(
                        Account.of(AccountId.of("124"),
                                Money.euros(BigDecimal.ONE))));

        String accountJson = service.getAccount("124");
        String expectedJson= buildAccountJson("124", "EUR", "1.00");
        JSONAssert.assertEquals(expectedJson, accountJson, JSONCompareMode.LENIENT);
    }

}