package rd.transactions.operation.log;

import rd.transactions.model.Money;

public class AccountAdditionLogEntry implements AccountOperationLogEntry {
    private final Money nonNegativeValue;

    /**
     * log entry is created with non-negative value always
     */
    public AccountAdditionLogEntry(Money nonNegativeValue) {
        if (nonNegativeValue.isNegative()) {
            throw new IllegalArgumentException("Only non-negative values are allowed here.");
        }
        this.nonNegativeValue = nonNegativeValue;
    }

    public Money getOperationValue() {
        return nonNegativeValue;
    }


}
