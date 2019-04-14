package rd.transactions.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rd.transactions.exceptions.AccountDoesNotExistException;
import rd.transactions.exceptions.CurrenciesOfAccountsDifferException;
import rd.transactions.exceptions.CurrencyOfSourceAccountDifferentThanTransferException;
import rd.transactions.exceptions.NotEnoughCreditException;
import rd.transactions.model.Transfer;
import rd.transactions.model.TransferStatus;

public class TransferPerformer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TransferPerformer.class);
    private final Transfer transfer;
    private final AccountManager accountManager;

    public TransferPerformer(Transfer transfer, AccountManager accountManager) {
        this.transfer = transfer;
        this.accountManager = accountManager;
    }

    @Override
    public void run() {
        transfer.updateTransferStatus(TransferStatus.PROCESSING);
        try {
            accountManager.performTransfer(transfer);
            transfer.updateTransferStatus(TransferStatus.COMPLETED);
        } catch (AccountDoesNotExistException ex) {
            logger.error("Account from transfer" + transfer.toString() + " does not exist", ex);
            transfer.updateTransferStatus(TransferStatus.REJECTED_ONE_OF_ACCOUNTS_DOES_NOT_EXIST);
        } catch (NotEnoughCreditException ex) {
            logger.error("Transfer" + transfer.toString() + " not enough credit", ex);
            transfer.updateTransferStatus(TransferStatus.REJECTED_NOT_ENOUGH_CREDIT_ON_SOURCE_ACCOUNT);
        } catch (CurrenciesOfAccountsDifferException ex) {
            logger.error("Transfer" + transfer.toString() + " currencies differ", ex);
            transfer.updateTransferStatus(TransferStatus.REJECTED_DIFFERENT_CURRENCIES);
        } catch (CurrencyOfSourceAccountDifferentThanTransferException ex) {
            logger.error("Transfer" + transfer.toString() + " unsupported transfer currency", ex);
            transfer.updateTransferStatus(TransferStatus.REJECTED_UNSUPPORTED_TRANSFER_CURRENCY);
        } catch (Throwable ex) {
            logger.error("Error during transfer processing.", ex);
            transfer.updateTransferStatus(TransferStatus.INTERNAL_ERROR);
        }
    }

    public Transfer getTransfer() {
        return transfer;
    }
}
