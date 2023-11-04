package ru.yandex.wallet.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Пользовательский запрос на отправление денежных средств
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerTransferMoneyRequestDto {
    private String recipient;
    private Double amount;
}
