package rd.transactions.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Currency;

public class AccountDto {
    private final String accountId;
    private final Currency currency;
    private final BigDecimal amount;

    @JsonCreator
    public AccountDto(
            @JsonProperty(value = "accountId", required = true) String accountId,
            @JsonProperty(value = "currency", required = true) Currency currencyCode,
            @JsonProperty(value = "amount", required = true) BigDecimal amount) {
        this.accountId = accountId;
        this.currency = currencyCode;
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "AccountDto{" +
                "accountId='" + accountId + '\'' +
                ", currencyCode='" + currency + '\'' +
                ", amount=" + amount +
                '}';
    }
}
