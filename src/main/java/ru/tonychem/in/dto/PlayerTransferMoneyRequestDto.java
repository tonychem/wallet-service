package ru.tonychem.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tonychem.aop.annotations.validation.NotBlank;
import ru.tonychem.aop.annotations.validation.NotNull;
import ru.tonychem.aop.annotations.validation.Validated;

/**
 * Пользовательский запрос на отправление денежных средств
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class PlayerTransferMoneyRequestDto {
    @NotBlank
    private String recipient;
    @NotNull
    private Double amount;
}
