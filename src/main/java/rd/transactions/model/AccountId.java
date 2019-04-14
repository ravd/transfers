package rd.transactions.model;

import java.util.Objects;

public class AccountId {
    private final String id;

    public static AccountId of(String id) {
        return new AccountId(id);
    }

    private AccountId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "AccountId{" +
                "id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountId accountId = (AccountId) o;
        return id.equals(accountId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
