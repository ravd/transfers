package rd.transactions.model;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Transfer {
    private final AccountId sourceAccount;
    private final AccountId targetAccount;
    private final Money transferredAmount;
    private final UUID transferId;
    private AtomicReference<TransferStatus> transferStatus = new AtomicReference<>(TransferStatus.SUBMITTED);

    public static Transfer of(
            AccountId sourceAccount, AccountId targetAccount, Money transferredAmount, UUID transferId) {
        return new Transfer(sourceAccount, targetAccount, transferredAmount, transferId);
    }

    private Transfer(AccountId sourceAccount, AccountId targetAccount, Money transferredAmount, UUID transferId) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.transferredAmount = transferredAmount;
        this.transferId = transferId;
    }

    public AccountId getSourceAccount() {
        return sourceAccount;
    }

    public AccountId getTargetAccount() {
        return targetAccount;
    }

    public Money getTransferredAmount() {
        return transferredAmount;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public TransferStatus getTransferStatus() {
        return transferStatus.get();
    }

    public void updateTransferStatus(TransferStatus transferStatus) {
        this.transferStatus.set(transferStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return sourceAccount.equals(transfer.sourceAccount) &&
                targetAccount.equals(transfer.targetAccount) &&
                transferredAmount.equals(transfer.transferredAmount) &&
                transferId.equals(transfer.transferId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAccount, targetAccount, transferredAmount, transferId);
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "sourceAccount=" + sourceAccount +
                ", targetAccount=" + targetAccount +
                ", transferredAmount=" + transferredAmount +
                '}';
    }
}
