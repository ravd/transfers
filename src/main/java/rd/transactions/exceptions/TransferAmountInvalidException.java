package rd.transactions.exceptions;

public class TransferAmountInvalidException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Transfer amount is invalid.";
    }
}
