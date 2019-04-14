package rd.transactions.managers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import rd.transactions.exceptions.AccountDoesNotExistException;
import rd.transactions.exceptions.CurrenciesOfAccountsDifferException;
import rd.transactions.exceptions.CurrencyOfSourceAccountDifferentThanTransferException;
import rd.transactions.exceptions.NotEnoughCreditException;
import rd.transactions.model.AccountId;
import rd.transactions.model.Money;
import rd.transactions.model.Transfer;
import rd.transactions.model.TransferStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransferPerformerTest {

    @Mock
    AccountManager accountManager;

    private Transfer getNewTransfer() {
        return Transfer.of(
                AccountId.of("A"),
                AccountId.of("B"),
                Money.euros(new BigDecimal("123.22")),
                UUID.randomUUID()
        );
    }

    @Test
    public void performTransferSunnyDay() throws NotEnoughCreditException, CurrencyOfSourceAccountDifferentThanTransferException, CurrenciesOfAccountsDifferException {
        Transfer transfer = getNewTransfer();
        TransferPerformer performer = new TransferPerformer(transfer, accountManager);

        performer.run();

        verify(accountManager, times(1)).performTransfer(eq(transfer));
        assertThat(transfer.getTransferStatus()).isEqualTo(TransferStatus.COMPLETED);
    }

    @Test
    public void performTransferNotEnoughMoney() throws NotEnoughCreditException, CurrencyOfSourceAccountDifferentThanTransferException, CurrenciesOfAccountsDifferException {
        Transfer transfer = getNewTransfer();
        TransferPerformer performer = new TransferPerformer(transfer, accountManager);
        doThrow(new NotEnoughCreditException()).when(accountManager).performTransfer(any());

        performer.run();

        assertThat(transfer.getTransferStatus()).isEqualTo(TransferStatus.REJECTED_NOT_ENOUGH_CREDIT_ON_SOURCE_ACCOUNT);
    }

    @Test
    public void performTransferNoAccount() throws NotEnoughCreditException, CurrencyOfSourceAccountDifferentThanTransferException, CurrenciesOfAccountsDifferException {
        Transfer transfer = getNewTransfer();
        TransferPerformer performer = new TransferPerformer(transfer, accountManager);
        doThrow(new AccountDoesNotExistException()).when(accountManager).performTransfer(any());

        performer.run();

        assertThat(transfer.getTransferStatus()).isEqualTo(TransferStatus.REJECTED_ONE_OF_ACCOUNTS_DOES_NOT_EXIST);
    }

    @Test
    public void performTransferUnsupportedCurrency() throws NotEnoughCreditException, CurrencyOfSourceAccountDifferentThanTransferException, CurrenciesOfAccountsDifferException {
        Transfer transfer = getNewTransfer();
        TransferPerformer performer = new TransferPerformer(transfer, accountManager);
        doThrow(new CurrencyOfSourceAccountDifferentThanTransferException()).when(accountManager).performTransfer(any());

        performer.run();

        assertThat(transfer.getTransferStatus()).isEqualTo(TransferStatus.REJECTED_UNSUPPORTED_TRANSFER_CURRENCY);
    }

    @Test
    public void performTransferCurrenciesDiffer() throws NotEnoughCreditException, CurrencyOfSourceAccountDifferentThanTransferException, CurrenciesOfAccountsDifferException {
        Transfer transfer = getNewTransfer();
        TransferPerformer performer = new TransferPerformer(transfer, accountManager);
        doThrow(new CurrenciesOfAccountsDifferException()).when(accountManager).performTransfer(any());

        performer.run();

        assertThat(transfer.getTransferStatus()).isEqualTo(TransferStatus.REJECTED_DIFFERENT_CURRENCIES);
    }

    @Test
    public void performTransferInternalError() throws NotEnoughCreditException, CurrencyOfSourceAccountDifferentThanTransferException, CurrenciesOfAccountsDifferException {
        Transfer transfer = getNewTransfer();
        TransferPerformer performer = new TransferPerformer(transfer, accountManager);
        doThrow(new NullPointerException()).when(accountManager).performTransfer(any());

        performer.run();

        assertThat(transfer.getTransferStatus()).isEqualTo(TransferStatus.INTERNAL_ERROR);
    }
}