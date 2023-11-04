package ru.tonychem.domain.dto;

import lombok.Value;

/**
 * Класс-обертка над параметрами ответа на запрос дененжных средств
 */
@Value
public class MoneyTransferResponse {
    AuthenticatedPlayerDto requester;
    TransactionDto transactionDto;
}
