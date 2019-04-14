package rd.transactions.exceptions;

public class AccountDoesNotExistException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Account with this id does not exist.";
    }
}
