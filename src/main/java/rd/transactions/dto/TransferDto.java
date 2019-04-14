package rd.transactions.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class TransferDto {
    private String sourceAccountId;
    private String targetAccountId;
    private Currency currency;
    private BigDecimal amount;
    private UUID transferId;
    private String transferStatus;

    @JsonCreator
    public TransferDto(
            @JsonProperty(value = "sourceAccountId", required = true) String sourceAccount,
            @JsonProperty(value = "targetAccountId", required = true) String targetAccountId,
            @JsonProperty(value = "currency", required = true) Currency currency,
            @JsonProperty(value = "amount", required = true) BigDecimal amount,
            @JsonProperty(value = "transferId") UUID transferId,
            @JsonProperty(value = "transferStatus") String transferStatus
            ) {
        this.sourceAccountId = sourceAccount;
        this.targetAccountId = targetAccountId;
        this.currency = currency;
        this.amount = amount;
        this.transferId = transferId;
        this.transferStatus = transferStatus;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public String getTargetAccountId() {
        return targetAccountId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getTransferId() {
        return transferId;
    }

    @Override
    public String toString() {
        return "TransferDto{" +
                "sourceAccountId='" + sourceAccountId + '\'' +
                ", targetAccountId='" + targetAccountId + '\'' +
                ", currency=" + currency +
                ", amount=" + amount +
                '}';
    }


    public String getTransferStatus() {
        return transferStatus;
    }
}
