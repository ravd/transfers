package rd.transactions.model;

import java.util.Currency;
import java.util.Objects;

public class Account {
    private final AccountId id;
    private final Money balance;

    public static Account of(AccountId id, Money balance) {
        return new Account(id, balance);
    }

    private Account(AccountId id, Money balance) {
        this.id = id;
        this.balance = balance;
    }

    public AccountId getId() {
        return id;
    }

    public Currency getCurrency() { return balance.getCurrency(); }

    public Money getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id.equals(account.id) &&
                balance.equals(account.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", balance=" + balance +
                '}';
    }
}
