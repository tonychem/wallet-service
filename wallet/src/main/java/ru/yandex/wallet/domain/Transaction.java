package ru.yandex.wallet.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Основная модель транзакции.
 */
@AllArgsConstructor
@Getter
@Builder
public class Transaction {
    private final UUID id;
    @Setter
    private TransferRequestStatus status;
    private final String sender;
    private final String recipient;
    private final BigDecimal amount;
}
