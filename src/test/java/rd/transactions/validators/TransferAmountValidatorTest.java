package rd.transactions.validators;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferAmountValidatorTest {

    @Test
    public void nonPositiveTransferAmountIsNotAllowed() {
        TransferAmountValidator validator = new TransferAmountValidator();

        assertThat(validator.isTransferAmountValid(new BigDecimal("0.003"), Currency.getInstance("EUR"))).isFalse();
        assertThat(validator.isTransferAmountValid(BigDecimal.ONE.negate(), Currency.getInstance("EUR"))).isFalse();
    }

    @Test
    public void positiveAmountIsAllowed() {
        TransferAmountValidator validator = new TransferAmountValidator();

        assertThat(validator.isTransferAmountValid(new BigDecimal("0.01"), Currency.getInstance("EUR"))).isTrue();
    }
}