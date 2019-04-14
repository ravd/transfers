package rd.transactions.validators;

import java.math.BigDecimal;

public interface BalanceValidator {
    boolean isBalanceAllowed(BigDecimal balanceValue);
}
