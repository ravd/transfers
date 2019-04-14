package rd.transactions.model;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyTest {

    @Test
    public void roundingScenariosHalfEven() {
        Money a = Money.euros(new BigDecimal("0.005"));
        Money b = Money.euros(new BigDecimal("0.015"));
        Money c = Money.euros(new BigDecimal("0.025"));

        assertThat(a.getAmount()).isEqualTo(new BigDecimal("0.00"));
        assertThat(b.getAmount()).isEqualTo(new BigDecimal("0.02"));
        assertThat(c.getAmount()).isEqualTo(new BigDecimal("0.02"));
    }

    @Test
    public void addsAmountsOfMoney() {
        Money a = Money.euros(new BigDecimal("123.12"));
        Money b = Money.euros(new BigDecimal("100.11"));
        Money sum = a.add(b);

        assertThat(sum.getAmount()).isEqualByComparingTo(new BigDecimal("223.23"));
    }

    @Test
    public void isNegativeIsTrueForNegativeValues() {
        Money a = Money.euros(new BigDecimal("-123.12"));
        assertThat(a.isNegative()).isTrue();
    }

    @Test
    public void isNegativeIsFalseForZero() {
        Money a = Money.euros(new BigDecimal("0"));

        assertThat(a.isNegative()).isFalse();
    }

    @Test
    public void isNegativeIsTrueForPositiveValue() {
        Money a = Money.euros(new BigDecimal("100.11"));

        assertThat(a.isNegative()).isFalse();
    }

    @Test
    public void negationReturnsValueWithOppositeSignAndSameAbsoluteValue() {
        Money a = Money.euros(new BigDecimal("100.11"));

        assertThat(a.isNegative()).isFalse();
        assertThat(a.negate().isNegative()).isTrue();

        assertThat(a.negate().negate()).isEqualTo(a);
    }
}