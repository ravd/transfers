package rd.transactions.controller.transfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rd.transactions.config.ObjectMapperFactory;
import rd.transactions.config.UUIDGenerator;
import rd.transactions.dto.TransferDto;
import rd.transactions.exceptions.TransferAmountInvalidException;
import rd.transactions.exceptions.TransferBadRequestException;
import rd.transactions.exceptions.TransferNotFoundException;
import rd.transactions.model.Transfer;
import rd.transactions.managers.TransferManager;
import rd.transactions.validators.TransferAmountValidator;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransferController {
    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);
    private final ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

    private final TransferManager transferManager;
    private final TransferConverter transferConverter;
    private final TransferAmountValidator transferAmountValidator;
    private final UUIDGenerator uuidGenerator;

    public TransferController(
            TransferManager transferManager,
            TransferAmountValidator transferAmountValidator,
            TransferConverter transferConverter, UUIDGenerator uuidGenerator) {
        this.transferManager = transferManager;
        this.transferConverter = transferConverter;
        this.transferAmountValidator = transferAmountValidator;
        this.uuidGenerator = uuidGenerator;
    }


    public String submitTransfer(String transferJson) throws JsonProcessingException {
        TransferDto transferDto = parseTransfer(transferJson);

        if (!transferAmountValidator.isTransferAmountValid(transferDto.getAmount(), transferDto.getCurrency())) {
            throw new TransferAmountInvalidException();
        }

        Transfer transfer = transferConverter.toModel(transferDto, uuidGenerator.generateUUID());
        transferManager.submitTransfer(transfer);

        return mapper.writeValueAsString(transferConverter.toDto(transfer));
    }

    private TransferDto parseTransfer(String transferJson) {
        try {
            return mapper.readValue(transferJson, TransferDto.class);
        } catch (IOException e) {
            logger.error("Can't parse the request", e);
            throw new TransferBadRequestException();
        }
    }

    public String getTransfers() throws JsonProcessingException {
        List<TransferDto> transferDtos = transferManager.getTransfers()
                .stream()
                .map(transferConverter::toDto)
                .collect(Collectors.toList());
        return mapper.writeValueAsString(transferDtos);
    }

    public String getTransfer(String transferId) throws JsonProcessingException {
        UUID transferUUID = parseUuid(transferId);

        TransferDto transferDto = transferManager.getTransfer(transferUUID)
                .map(transferConverter::toDto)
                .orElseThrow(TransferNotFoundException::new);

        return mapper.writeValueAsString(transferDto);
    }

    private UUID parseUuid(String transferId) {
        try {
            return UUID.fromString(transferId);
        } catch (Exception ex) {
            logger.error("Failed to parse UUID", ex);
            throw new TransferNotFoundException();
        }
    }

}
