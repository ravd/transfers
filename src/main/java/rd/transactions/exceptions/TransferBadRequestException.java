package rd.transactions.exceptions;

public class TransferBadRequestException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Transfer data is not formatted correctly.";
    }
}
