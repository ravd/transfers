package rd.transactions.exceptions;

public class TransferNotFoundException extends RuntimeException {
    @Override
    public String getMessage() {
        return "There is no transfer registered under this identifier.";
    }
}
