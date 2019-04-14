package rd.transactions.validators;

import java.math.BigDecimal;

/**
 * This validator checks if amount is non-negative,
 *  though if requirements allowed for negative balance it could be implemented differently
 */
public class NonNegativeBalanaceValidator implements BalanceValidator {
    public boolean isBalanceAllowed(BigDecimal balanceValue) {
        return balanceValue.compareTo(BigDecimal.ZERO) >= 0;
    }
}
