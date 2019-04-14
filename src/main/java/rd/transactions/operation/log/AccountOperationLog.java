package rd.transactions.operation.log;

import rd.transactions.model.Account;
import rd.transactions.model.AccountId;
import rd.transactions.model.Money;
import rd.transactions.validators.BalanceValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

/**
 * This class represents transaction log for particular account
 * Entry addition is checked against balance validator to guarantee that the account is always in valid state
 */
public class AccountOperationLog {
    private final AccountId accountId;
    private final Currency currency;
    private final List<AccountOperationLogEntry> entries;

    public AccountOperationLog(Account account) {
        this.accountId = account.getId();
        this.currency = account.getCurrency();
        this.entries = new ArrayList<>(
                Collections.singleton(
                        new AccountAdditionLogEntry(account.getBalance())));
    }

    public Account materializeAsAccount() {
        return Account.of(accountId, getBalance());
    }

    /**
     * @return true if operation is successfully applied, false if amount of subtractive operation exceeds balance
     */
    public synchronized boolean addEntry(
            AccountOperationLogEntry operation, BalanceValidator balanceValidator) {
        if (!isBalanceAllowedAfterOperation(operation, balanceValidator)) {
            return false;
        }
        entries.add(operation);
        return true;
    }

    /**
     * Addition of money is always allowed, and guaranteed to be successful
     */
    public synchronized void addEntry(
            AccountAdditionLogEntry operation) {
        entries.add(operation);
    }

    public Currency getCurrency() {
        return currency;
    }

    private boolean isBalanceAllowedAfterOperation(AccountOperationLogEntry operation, BalanceValidator balanceValidator) {
        Money balance = getBalance();
        BigDecimal balanceAfterOperation = balance.add(operation.getOperationValue()).getAmount();
        return balanceValidator.isBalanceAllowed(balanceAfterOperation);
    }

    private Money getBalance() {
        return entries.stream()
                .map(AccountOperationLogEntry::getOperationValue)
                .reduce(Money::add)
                .orElseThrow(() -> new RuntimeException("Account history should have at least one element."));
    }
}
