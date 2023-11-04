package ru.tonychem.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tonychem.aop.annotations.validation.NotEmpty;
import ru.tonychem.aop.annotations.validation.Validated;

import java.util.Collection;

/**
 * Пользовательский список id транзакций в строковом представлении для подтверждения/отклонения
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Validated
public class TransactionsListDto {
    @NotEmpty
    private Collection<String> ids;
}
