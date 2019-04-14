package rd.transactions.operation.log;

import rd.transactions.model.Money;

public class AccountSubtractionLogEntry implements AccountOperationLogEntry {
    private final Money negativeValue;

    /**
     * log entry is created with non-negative value always, even if it is subtraction
     */
    public AccountSubtractionLogEntry(Money nonNegativeValue) {
        if (nonNegativeValue.isNegative()) {
            throw new IllegalArgumentException("Only non-negative values are allowed here.");
        }
        this.negativeValue = nonNegativeValue.negate();
    }

    public Money getOperationValue() {
        return negativeValue;
    }


}
