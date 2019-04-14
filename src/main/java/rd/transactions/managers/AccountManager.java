package rd.transactions.managers;

import rd.transactions.exceptions.*;
import rd.transactions.model.Account;
import rd.transactions.model.AccountId;
import rd.transactions.model.Transfer;
import rd.transactions.operation.log.AccountAdditionLogEntry;
import rd.transactions.operation.log.AccountOperationLog;
import rd.transactions.operation.log.AccountSubtractionLogEntry;
import rd.transactions.validators.BalanceValidator;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

/**
 * The class managing account operations
 * It is thread-safe
 */
public class AccountManager {
    private final ConcurrentHashMap<AccountId, AccountOperationLog> accounts = new ConcurrentHashMap<>();
    private final BalanceValidator balanceValidator;

    public AccountManager(BalanceValidator balanceValidator) {
        this.balanceValidator = balanceValidator;
    }

    public Collection<Account> getAccounts() {
        return accounts.values().stream()
                .map(AccountOperationLog::materializeAsAccount)
                .collect(toList());
    }

    public Optional<Account> getAccountById(AccountId accountId) {
        return Optional.ofNullable(accounts.get(accountId))
                .map(AccountOperationLog::materializeAsAccount);
    }

    /**
     * Adds account to managers.
     *  if there already exists account with same account identifier, exception is thrown and account is not added
     */
    public void createAccount(Account account) {
        AccountOperationLog accountInMap = accounts.putIfAbsent(account.getId(), new AccountOperationLog(account));
        if (accountInMap != null) {
            throw new AccountIdConflictException();
        }
    }

    /**
     * This method transfers money from one account to another.
     * If any account does not exist, the transfer is not performed.
     * If source account has no credit, the transfer is not performed.
     * The assumption for this code is that accounts are not removable.
     */
    public void performTransfer(Transfer transfer) throws
            AccountDoesNotExistException,
            NotEnoughCreditException,
            CurrenciesOfAccountsDifferException,
            CurrencyOfSourceAccountDifferentThanTransferException {
        AccountOperationLog source = Optional.ofNullable(accounts.get(transfer.getSourceAccount()))
            .orElseThrow(AccountDoesNotExistException::new);
        AccountOperationLog target = Optional.ofNullable(accounts.get(transfer.getTargetAccount()))
                .orElseThrow(AccountDoesNotExistException::new);

        if (!source.getCurrency().equals(transfer.getTransferredAmount().getCurrency())) {
            throw new CurrencyOfSourceAccountDifferentThanTransferException();
        }

        if (!source.getCurrency().equals(target.getCurrency())) {
            throw new CurrenciesOfAccountsDifferException();
        }


        if (source.addEntry(new AccountSubtractionLogEntry(transfer.getTransferredAmount()), balanceValidator)) {
            target.addEntry(new AccountAdditionLogEntry(transfer.getTransferredAmount()));
        } else {
            throw new NotEnoughCreditException();
        }
    }
}
