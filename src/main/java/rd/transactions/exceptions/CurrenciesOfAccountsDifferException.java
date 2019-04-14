package rd.transactions.exceptions;

/**
 * If there were automatic conversion service in the system, this sort of situation could be addressed
 * For the sake of this app there is no conversion
 */
public class CurrenciesOfAccountsDifferException extends Exception {
    @Override
    public String getMessage() {
        return "Source and target accounts have different currencies";
    }
}
