package rd.transactions.exceptions;

public class AccountIdConflictException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Account with same id already exists.";
    }
}
