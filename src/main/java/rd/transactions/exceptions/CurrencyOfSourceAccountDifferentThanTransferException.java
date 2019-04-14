package rd.transactions.exceptions;

/**
 * If there was automatic conversion service in the system, this sort of situation could be addressed
 * For the sake of this app there is no conversion
 */
public class CurrencyOfSourceAccountDifferentThanTransferException extends Exception {
    @Override
    public String getMessage() {
        return "Source account is in different currency than transfer.";
    }
}
