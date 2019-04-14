package rd.transactions.operation.log;

import org.junit.Test;
import rd.transactions.model.Money;
import rd.transactions.operation.log.AccountAdditionLogEntry;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


public class AccountAdditionLogEntryTest {

    @Test(expected = IllegalArgumentException.class)
    public void failWhenGivenNegativeValue() {
        new AccountAdditionLogEntry(Money.euros(BigDecimal.ONE.negate()));
    }

    @Test
    public void returnSameAmountThatWasGivenAtCreationTime() {
        long initialValue = 1234L;
        AccountAdditionLogEntry entry = new AccountAdditionLogEntry(Money.euros(BigDecimal.valueOf(initialValue)));
        assertThat(entry.getOperationValue().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(initialValue));
    }
}