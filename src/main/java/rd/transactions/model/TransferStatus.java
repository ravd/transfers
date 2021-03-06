package rd.transactions.model;

public enum TransferStatus {
    SUBMITTED,
    PROCESSING,
    COMPLETED,
    REJECTED_ONE_OF_ACCOUNTS_DOES_NOT_EXIST,
    REJECTED_NOT_ENOUGH_CREDIT_ON_SOURCE_ACCOUNT,
    REJECTED_DIFFERENT_CURRENCIES,
    REJECTED_UNSUPPORTED_TRANSFER_CURRENCY,
    INTERNAL_ERROR
}
