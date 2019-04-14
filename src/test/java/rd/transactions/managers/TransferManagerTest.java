package rd.transactions.managers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import rd.transactions.model.Transfer;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransferManagerTest {

    @Mock
    private ExecutorService executorService;

    @Mock
    private AccountManager accountManager;

    @Mock
    private Transfer transfer;

    @Test
    public void whenTransferIsSubmittedItIsAddedToExecutorAndToTransferDb() {
        TransferManager manager = new TransferManager(accountManager, executorService);

        UUID transferUuid = UUID.randomUUID();

        when(transfer.getTransferId()).thenReturn(transferUuid);

        manager.submitTransfer(transfer);

        Optional<Transfer> transferFromRepo = manager.getTransfer(transferUuid);
        assertThat(transferFromRepo).isNotEmpty();
        assertThat(transferFromRepo.get()).isEqualTo(transfer);
        verify(executorService, times(1)).submit((TransferPerformer)any());
    }
}