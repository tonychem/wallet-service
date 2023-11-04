package ru.tonychem.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tonychem.aop.annotations.validation.NotBlank;
import ru.tonychem.aop.annotations.validation.NotNull;
import ru.tonychem.aop.annotations.validation.Validated;

/**
 * Пользовательский запрос на получение денежных средств от другого пользователя
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Validated
public class PlayerRequestMoneyDto {
    @NotBlank
    private String donor;

    @NotNull
    private Double amount;
}
