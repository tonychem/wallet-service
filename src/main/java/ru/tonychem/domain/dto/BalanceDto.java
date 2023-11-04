package ru.tonychem.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Информация о балансе пользователя
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceDto {
    private Long id;
    private String username;
    private BigDecimal balance;
}
