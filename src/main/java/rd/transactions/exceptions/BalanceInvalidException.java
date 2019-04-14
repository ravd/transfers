package rd.transactions.exceptions;

public class BalanceInvalidException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Balance for the operation is invalid";
    }
}
