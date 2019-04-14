package rd.transactions.operation.log;

import org.junit.Test;
import rd.transactions.model.Money;
import rd.transactions.operation.log.AccountSubtractionLogEntry;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountSubtractionLogEntryTest {

    @Test(expected = IllegalArgumentException.class)
    public void failWhenGivenNegativeValue() {
        new AccountSubtractionLogEntry(Money.euros(BigDecimal.ONE.negate()));
    }

    @Test
    public void returnNegativeAmountInOperationValue() {
        AccountSubtractionLogEntry entry = new AccountSubtractionLogEntry(Money.euros(BigDecimal.ONE));
        assertThat(entry.getOperationValue().isNegative()).isTrue();
    }
}