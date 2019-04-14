package rd.transactions.validators;

import rd.transactions.model.Money;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * This class checks if transfer amount is allowed, there is no sens in transfering negative or 0 amounts.
 */
public class TransferAmountValidator {
    public boolean isTransferAmountValid(BigDecimal amount, Currency currency) {
        return Money.of(amount, currency).getAmount().compareTo(BigDecimal.ZERO) > 0;
    }
}
