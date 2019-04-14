package rd.transactions.operation.log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import rd.transactions.model.Account;
import rd.transactions.model.AccountId;
import rd.transactions.model.Money;
import rd.transactions.operation.log.AccountAdditionLogEntry;
import rd.transactions.operation.log.AccountOperationLog;
import rd.transactions.operation.log.AccountSubtractionLogEntry;
import rd.transactions.validators.BalanceValidator;

import java.math.BigDecimal;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountOperationLogTest {

    @Mock
    private Account initialAccount;

    @Mock
    private BalanceValidator balanceValidator;

    @Test
    public void canAddAndSubtractFromAccount() {
        // super-permissive validator
        when(balanceValidator.isBalanceAllowed(any())).thenReturn(true);

        AccountOperationLog accountOperationLog = createInitialLog("120.33");

        accountOperationLog.addEntry(
                new AccountAdditionLogEntry(euros("9.67")));

        // we expect that 0.000123 gets discarded
        accountOperationLog.addEntry(
                new AccountSubtractionLogEntry(euros("17.230123")),
                balanceValidator);

        accountOperationLog.addEntry(
                new AccountAdditionLogEntry(euros("0.25")),
                balanceValidator);

        Account account = accountOperationLog.materializeAsAccount();
        assertThat(account.getBalance().getAmount()).isEqualByComparingTo("113.02");
    }

    @Test
    public void doNotAllowOperationsMakingBalanceInvalid() {
        // disallow all operations
        when(balanceValidator.isBalanceAllowed(any())).thenReturn(false);

        String initialAccountBalance = "100.00";

        AccountOperationLog accountOperationLog = createInitialLog(initialAccountBalance);

        boolean operationResult = accountOperationLog.addEntry(
                new AccountSubtractionLogEntry(euros("17.230123")),
                balanceValidator);

        assertThat(operationResult).isFalse();
        Account account = accountOperationLog.materializeAsAccount();
        assertThat(account.getBalance().getAmount()).isEqualByComparingTo(new BigDecimal(initialAccountBalance));
    }

    @Test
    public void allowedOperationsReturnTrueUponAdditionToLog() {
        // allow all operations
        when(balanceValidator.isBalanceAllowed(any())).thenReturn(true);

        AccountOperationLog accountOperationLog = createInitialLog("100.00");
        boolean operationResult = accountOperationLog.addEntry(
                new AccountSubtractionLogEntry(euros("17.230123")),
                balanceValidator);

        assertThat(operationResult).isTrue();
    }

    private AccountOperationLog createInitialLog(String initialValue) {
        when(initialAccount.getId()).thenReturn(AccountId.of("1234"));
        when(initialAccount.getBalance()).thenReturn(euros(initialValue));
        return new AccountOperationLog(initialAccount);
    }

    private Money euros(String value) {
        return Money.euros(new BigDecimal(value));
    }
}