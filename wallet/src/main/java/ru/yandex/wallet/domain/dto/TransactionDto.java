package ru.yandex.wallet.domain.dto;

import ru.yandex.wallet.domain.TransferRequestStatus;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO-класс транзакции
 */
@Value
public class TransactionDto {
    UUID id;
    TransferRequestStatus status;
    String sender;
    String recipient;
    BigDecimal amount;
}
