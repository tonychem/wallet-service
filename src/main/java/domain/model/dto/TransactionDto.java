package domain.model.dto;

import domain.model.TransferRequestStatus;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class TransactionDto {
    UUID id;
    TransferRequestStatus status;
    String sender;
    String recipient;
    BigDecimal amount;
}
