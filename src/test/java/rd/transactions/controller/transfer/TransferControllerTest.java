package rd.transactions.controller.transfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import rd.transactions.config.UUIDGenerator;
import rd.transactions.exceptions.TransferAmountInvalidException;
import rd.transactions.exceptions.TransferBadRequestException;
import rd.transactions.exceptions.TransferNotFoundException;
import rd.transactions.model.AccountId;
import rd.transactions.model.Money;
import rd.transactions.model.Transfer;
import rd.transactions.managers.TransferManager;
import rd.transactions.validators.TransferAmountValidator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rd.transactions.JsonUtils.buildTransferJson;
import static rd.transactions.JsonUtils.buildTransferJsonWithReadOnlyData;

@RunWith(MockitoJUnitRunner.class)
public class TransferControllerTest {

    @Mock
    private TransferManager transferManager;

    @Mock
    private TransferAmountValidator transferAmountValidator;

    @Mock
    private UUIDGenerator uuidGenerator;

    private final TransferConverter transferConverter = new TransferConverter();
    private final static UUID CONSTANT_UUID = UUID.randomUUID();

    @Before
    public void setUpUuids() {
        when(uuidGenerator.generateUUID()).thenReturn(CONSTANT_UUID);
    }

    @Test(expected = TransferBadRequestException.class)
    public void whenTransferRequestIsInvalidExceptionIsThrown() throws JsonProcessingException {
        String transferJson = buildTransferJson(
                "123", "222", "NO_CURRENCy_LIKE_THIS", "23.223");

        TransferController service = new TransferController(
                transferManager, transferAmountValidator, transferConverter, uuidGenerator);

        service.submitTransfer(transferJson);
    }

    @Test(expected = TransferAmountInvalidException.class)
    public void whenTransferredAmountIsInvalidExceptionIsThrown() throws JsonProcessingException {
        when(transferAmountValidator.isTransferAmountValid(any(), any())).thenReturn(false);
        String transferJson = buildTransferJson(
                "123", "222", "USD", "-123");

        TransferController service = new TransferController(
                transferManager, transferAmountValidator, transferConverter, uuidGenerator);

        service.submitTransfer(transferJson);
    }

    @Test
    public void properRequestGetsPassedToTransferService() throws JsonProcessingException, JSONException {
        when(transferAmountValidator.isTransferAmountValid(any(), any())).thenReturn(true);
        String transferJson = buildTransferJson(
                "123", "222", "PLN", "23.22");

        TransferController controller = new TransferController(
                transferManager, transferAmountValidator, transferConverter, uuidGenerator);

        String dtoWithTransferId = controller.submitTransfer(transferJson);

        verify(transferManager).submitTransfer(eq(
                Transfer.of(
                    AccountId.of("123"),
                    AccountId.of("222"),
                    Money.of(new BigDecimal("23.223"), Currency.getInstance("PLN")),
                    CONSTANT_UUID
        )));
        JSONAssert.assertEquals(transferJson, dtoWithTransferId, JSONCompareMode.LENIENT);
        assertThat(dtoWithTransferId).contains(CONSTANT_UUID.toString());
    }

    @Test(expected = TransferNotFoundException.class)
    public void exceptionIsThrownWhenUuidIsNotCorrect() throws JsonProcessingException {

        TransferController controller = new TransferController(
                transferManager, transferAmountValidator, transferConverter, uuidGenerator);

        controller.getTransfer("INAVLID_UUID");
    }

    @Test(expected = TransferNotFoundException.class)
    public void exceptionIsThrownWhenTransactionDoesNotExist() throws JsonProcessingException {

        when(transferManager.getTransfer(CONSTANT_UUID)).thenReturn(Optional.empty());

        TransferController controller = new TransferController(
                transferManager, transferAmountValidator, transferConverter, uuidGenerator);

        controller.getTransfer(CONSTANT_UUID.toString());
    }

    @Test
    public void transferDataIsReturnedIfExists() throws JsonProcessingException, JSONException {

        when(transferManager.getTransfer(CONSTANT_UUID)).thenReturn(Optional.of(
                Transfer.of(
                        AccountId.of("333"),
                        AccountId.of("1111"),
                        Money.euros(new BigDecimal("123")),
                        CONSTANT_UUID
                )
        ));

        TransferController controller = new TransferController(
                transferManager, transferAmountValidator, transferConverter, uuidGenerator);

        String transferData = controller.getTransfer(CONSTANT_UUID.toString());
        String expectedJson = buildTransferJsonWithReadOnlyData(
                "333", "1111", "EUR",
                "123.00", CONSTANT_UUID.toString(), "SUBMITTED");

        JSONAssert.assertEquals(expectedJson, transferData, JSONCompareMode.LENIENT);
    }


    @Test
    public void allTransfersAreReturned() throws JsonProcessingException, JSONException {

        when(transferManager.getTransfers()).thenReturn(Collections.singleton(
                Transfer.of(
                        AccountId.of("333"),
                        AccountId.of("1111"),
                        Money.euros(new BigDecimal("123")),
                        CONSTANT_UUID
                )
        ));

        TransferController controller = new TransferController(
                transferManager, transferAmountValidator, transferConverter, uuidGenerator);

        String transferData = controller.getTransfers();
        String expectedJson = "[" + buildTransferJsonWithReadOnlyData(
                "333", "1111", "EUR", "123.00",
                CONSTANT_UUID.toString(), "SUBMITTED") + "]";

        JSONAssert.assertEquals(expectedJson, transferData, JSONCompareMode.LENIENT);
    }


}