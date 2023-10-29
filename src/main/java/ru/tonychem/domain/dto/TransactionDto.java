package ru.tonychem.domain.dto;

import ru.tonychem.domain.TransferRequestStatus;
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
