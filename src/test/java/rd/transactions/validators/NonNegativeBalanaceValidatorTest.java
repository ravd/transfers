package rd.transactions.validators;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class NonNegativeBalanaceValidatorTest {

    @Test
    public void negativeBalanceIsNotAllowed() {
        BalanceValidator balanceValidator = new NonNegativeBalanaceValidator();

        assertThat(balanceValidator.isBalanceAllowed(BigDecimal.ONE.negate())).isFalse();
    }

    @Test
    public void nonNegativeBalanceIsAllowed() {
        BalanceValidator balanceValidator = new NonNegativeBalanaceValidator();

        assertThat(balanceValidator.isBalanceAllowed(BigDecimal.ONE)).isTrue();
        assertThat(balanceValidator.isBalanceAllowed(BigDecimal.ZERO)).isTrue();
    }
}