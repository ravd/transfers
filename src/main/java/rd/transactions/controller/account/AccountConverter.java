package rd.transactions.controller.account;

import rd.transactions.dto.AccountDto;
import rd.transactions.model.Account;
import rd.transactions.model.AccountId;
import rd.transactions.model.Money;

public class AccountConverter {
    AccountDto toDto(Account account) {
        return new AccountDto(
                account.getId().getId(),
                account.getCurrency(),
                account.getBalance().getAmount()
        );
    }

    Account toModel(AccountDto accountDto) {
        return Account.of(
                AccountId.of(accountDto.getAccountId()),
                Money.of(accountDto.getAmount(), accountDto.getCurrency())
        );
    }
}
