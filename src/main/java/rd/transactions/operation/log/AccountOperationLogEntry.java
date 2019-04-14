package rd.transactions.operation.log;

import rd.transactions.model.Money;

public interface AccountOperationLogEntry {
    /**
     * This method returns value ready to be used in aggregation operations.
     */
    Money getOperationValue();
}
