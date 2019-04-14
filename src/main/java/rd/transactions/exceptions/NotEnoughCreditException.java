package rd.transactions.exceptions;

public class NotEnoughCreditException extends Exception {
    @Override
    public String getMessage() {
        return "Operation rejected, balance after operation is invalid";
    }
}
