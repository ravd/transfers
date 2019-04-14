package rd.transactions.managers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import rd.transactions.exceptions.*;
import rd.transactions.model.Account;
import rd.transactions.model.AccountId;
import rd.transactions.model.Money;
import rd.transactions.model.Transfer;
import rd.transactions.validators.BalanceValidator;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AccountManagerTest {

    @Mock
    private Money moneyPlaceholder;

    @Mock
    private BalanceValidator balanceValidator;

    @Mock
    private Transfer transfer;

    @Test
    public void initiallyAccountServiceHasNoAccounts() {
        AccountManager accountManager = new AccountManager(balanceValidator);
        assertThat(accountManager.getAccounts()).isEmpty();
    }

    @Test
    public void accountCanBeCreatedAndQueried() {
        AccountManager repository = new AccountManager(balanceValidator);
        Account accountToBeAdded = Account.of(
            AccountId.of("1234"),
            moneyPlaceholder
        );

        repository.createAccount(accountToBeAdded);

        assertThat(repository.getAccountById(AccountId.of("1234"))).isNotEmpty();
        assertThat(repository.getAccounts()).hasSize(1);
    }

    @Test(expected = AccountDoesNotExistException.class)
    public void notExistingSourceAccountInTransferCauseException() throws NotEnoughCreditException, CurrenciesOfAccountsDifferException, CurrencyOfSourceAccountDifferentThanTransferException {
        AccountManager repository = new AccountManager(balanceValidator);

        when(transfer.getSourceAccount()).thenReturn(AccountId.of("123"));

        repository.performTransfer(transfer);
    }

    @Test(expected = AccountDoesNotExistException.class)
    public void notExistingTargetAccountInTransferCauseException() throws NotEnoughCreditException, CurrenciesOfAccountsDifferException, CurrencyOfSourceAccountDifferentThanTransferException {
        AccountManager repository = new AccountManager(balanceValidator);

        Money sourceMoney = Money.euros(new BigDecimal("321.22"));
        AccountId sourceId = AccountId.of("123");
        Account source = Account.of(sourceId, sourceMoney);
        when(transfer.getSourceAccount()).thenReturn(AccountId.of("123"));
        when(transfer.getTargetAccount()).thenReturn(AccountId.of("321"));

        repository.createAccount(source);

        repository.performTransfer(transfer);
    }

    @Test(expected = CurrenciesOfAccountsDifferException.class)
    public void whenAccountsAreInDifferentCurrenciesExceptionIsThrown() throws CurrenciesOfAccountsDifferException, CurrencyOfSourceAccountDifferentThanTransferException, NotEnoughCreditException {
        AccountManager repository = new AccountManager(balanceValidator);

        Money transferMoney = Money.euros(new BigDecimal("322.22"));

        Money sourceMoney = Money.euros(new BigDecimal("321.22"));
        AccountId sourceId = AccountId.of("123");
        Account source = Account.of(sourceId, sourceMoney);

        Money targetMoney = Money.of(new BigDecimal("100.22"), Currency.getInstance("USD"));
        AccountId targetId = AccountId.of("321");
        Account target = Account.of(targetId, targetMoney);

        repository.createAccount(source);
        repository.createAccount(target);

        when(transfer.getSourceAccount()).thenReturn(sourceId);
        when(transfer.getTargetAccount()).thenReturn(targetId);
        when(transfer.getTransferredAmount()).thenReturn(transferMoney);

        repository.performTransfer(transfer);
    }

    @Test(expected = CurrencyOfSourceAccountDifferentThanTransferException.class)
    public void whenSourceAccountCurrencyIsDifferentThanTransferExceptionIsThrown() throws CurrenciesOfAccountsDifferException, CurrencyOfSourceAccountDifferentThanTransferException, NotEnoughCreditException {
        AccountManager repository = new AccountManager(balanceValidator);

        Money transferMoney = Money.of(new BigDecimal("322.22"), Currency.getInstance("USD"));

        Money sourceMoney = Money.euros(new BigDecimal("321.22"));
        AccountId sourceId = AccountId.of("123");
        Account source = Account.of(sourceId, sourceMoney);

        Money targetMoney = Money.euros(new BigDecimal("100.22"));
        AccountId targetId = AccountId.of("321");
        Account target = Account.of(targetId, targetMoney);

        repository.createAccount(source);
        repository.createAccount(target);

        when(transfer.getSourceAccount()).thenReturn(sourceId);
        when(transfer.getTargetAccount()).thenReturn(targetId);
        when(transfer.getTransferredAmount()).thenReturn(transferMoney);

        repository.performTransfer(transfer);
    }

    @Test
    public void whenThereIsNotEnoughMoneyOnSourceAccountExceptionIsThrownAndNoChangeToBalances() throws CurrenciesOfAccountsDifferException, CurrencyOfSourceAccountDifferentThanTransferException {
        when(balanceValidator.isBalanceAllowed(any())).thenReturn(false);
        AccountManager repository = new AccountManager(balanceValidator);

        Money transferMoney = Money.euros(new BigDecimal("322.22"));

        Money sourceMoney = Money.euros(new BigDecimal("321.22"));
        AccountId sourceId = AccountId.of("123");
        Account source = Account.of(sourceId, sourceMoney);

        Money targetMoney = Money.euros(new BigDecimal("100.22"));
        AccountId targetId = AccountId.of("321");
        Account target = Account.of(targetId, targetMoney);

        repository.createAccount(source);
        repository.createAccount(target);

        when(transfer.getSourceAccount()).thenReturn(sourceId);
        when(transfer.getTargetAccount()).thenReturn(targetId);
        when(transfer.getTransferredAmount()).thenReturn(transferMoney);

        boolean exceptionWasThrown = false;
        try {
            repository.performTransfer(transfer);
        } catch (NotEnoughCreditException ex) {
            // expected
            exceptionWasThrown = true;
        }

        assertThat(exceptionWasThrown).isTrue();
        assertThat(repository.getAccountById(sourceId).get().getBalance()).isEqualTo(sourceMoney);
        assertThat(repository.getAccountById(targetId).get().getBalance()).isEqualTo(targetMoney);
    }

    @Test
    public void verifyMoneyCanBeTransferredSucessfully() throws NotEnoughCreditException, CurrenciesOfAccountsDifferException, CurrencyOfSourceAccountDifferentThanTransferException {
        when(balanceValidator.isBalanceAllowed(any())).thenReturn(true);
        AccountManager repository = new AccountManager(balanceValidator);

        Money sourceMoney = Money.euros(new BigDecimal("321.22"));
        Money targetMoney = Money.euros(new BigDecimal("100.00"));
        Money transferMoney = Money.euros(new BigDecimal("121.22"));

        AccountId sourceId = AccountId.of("123");
        Account source = Account.of(sourceId, sourceMoney);

        AccountId targetId = AccountId.of("321");
        Account target = Account.of(targetId, targetMoney);

        repository.createAccount(source);
        repository.createAccount(target);

        when(transfer.getSourceAccount()).thenReturn(sourceId);
        when(transfer.getTargetAccount()).thenReturn(targetId);
        when(transfer.getTransferredAmount()).thenReturn(transferMoney);

        repository.performTransfer(transfer);

        assertThat(repository.getAccountById(sourceId).get().getBalance().getAmount())
                .isEqualTo(new BigDecimal("200.00"));
        assertThat(repository.getAccountById(targetId).get().getBalance().getAmount())
                .isEqualTo(new BigDecimal("221.22"));
    }

    @Test(expected = AccountIdConflictException.class)
    public void addingAnotherAccountWithSameIdCausesConflictAndException() {
        AccountManager repository = new AccountManager(balanceValidator);
        Account account1 = Account.of(
                AccountId.of("1234"),
                moneyPlaceholder
        );

        Account account2 = Account.of(
                AccountId.of("1234"),
                moneyPlaceholder
        );

        repository.createAccount(account1);

        // here exception shall be thrown, indicating that the account already exists
        repository.createAccount(account2);
    }
}