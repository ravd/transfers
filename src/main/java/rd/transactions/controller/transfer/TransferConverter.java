package rd.transactions.controller.transfer;

import rd.transactions.dto.TransferDto;
import rd.transactions.model.AccountId;
import rd.transactions.model.Money;
import rd.transactions.model.Transfer;

import java.util.UUID;

public class TransferConverter {

    TransferDto toDto(Transfer transfer) {
        return new TransferDto(
                transfer.getSourceAccount().getId(),
                transfer.getTargetAccount().getId(),
                transfer.getTransferredAmount().getCurrency(),
                transfer.getTransferredAmount().getAmount(),
                transfer.getTransferId(),
                transfer.getTransferStatus().name()
        );
    }

    Transfer toModel(TransferDto transferDto, UUID uuid) {
        return Transfer.of(
                AccountId.of(transferDto.getSourceAccountId()),
                AccountId.of(transferDto.getTargetAccountId()),
                Money.of(transferDto.getAmount(), transferDto.getCurrency()),
                uuid
        );
    }
}
