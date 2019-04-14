package rd.transactions.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rd.transactions.model.Transfer;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class TransferManager {
    private static final Logger logger = LoggerFactory.getLogger(TransferManager.class);
    private final AccountManager accountManager;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<UUID, Transfer> transfers = new ConcurrentHashMap<>();

    public TransferManager(AccountManager accountManager, ExecutorService executorService) {
        this.accountManager = accountManager;
        this.executorService = executorService;
    }

    /**
     * This method schedules transfer to be performed utilizing underlying executor service.
     * This is in case performTransfer method in accountManager is long running,
     *  e.g. DB or other network services are being used.
     * In current situation performTransfer is instantaneous.
     * @param transfer
     */
    public void submitTransfer(Transfer transfer) {
        Runnable transferPerformer = new TransferPerformer(transfer, accountManager);
        executorService.submit(transferPerformer);
        transfers.put(transfer.getTransferId(), transfer);
    }

    public Optional<Transfer> getTransfer(UUID transferId) {
        return Optional.ofNullable(transfers.get(transferId));
    }

    public Collection<Transfer> getTransfers() {
        return transfers.values();
    }
}
