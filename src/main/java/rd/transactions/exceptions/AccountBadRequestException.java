package rd.transactions.exceptions;

public class AccountBadRequestException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Account data is not formatted correctly.";
    }
}
