package ru.tonychem.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tonychem.aop.annotations.validation.NotEmpty;
import ru.tonychem.aop.annotations.validation.Validated;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Validated
public class TransactionsListDto {
    @NotEmpty
    private Collection<String> ids;
}
