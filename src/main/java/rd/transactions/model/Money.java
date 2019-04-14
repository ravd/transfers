package rd.transactions.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * This class represents amount of money to be used in operations.
 * It may represent negative amount.
 */
public class Money {

    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    private static final Currency EURO = Currency.getInstance("EUR");

    private final BigDecimal amount;
    private final Currency currency;

    public static Money euros(BigDecimal amount) {
        return new Money(amount, EURO);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    private Money(BigDecimal amount, Currency currency) {
        this.currency = currency;
        this.amount = amount.setScale(currency.getDefaultFractionDigits(), DEFAULT_ROUNDING);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Money add(Money value) {
        return new Money(
                amount.add(value.getAmount()),
                currency);
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.equals(money.amount) &&
                currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return "Money{" +
                "amount=" + amount +
                ", currency=" + currency +
                '}';
    }
}
