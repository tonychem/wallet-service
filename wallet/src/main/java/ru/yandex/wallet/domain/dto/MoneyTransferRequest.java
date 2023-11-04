package ru.yandex.wallet.domain.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Обертка над параметрами запроса дененжных средств
 */
@Value
public class MoneyTransferRequest {
    UUID id;
    String moneyFrom;
    String moneyTo;
    BigDecimal amount;
}
