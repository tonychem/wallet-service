package ru.yandex.wallet.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Основная модель игрока.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Player {
    private Long id;
    private String username;
    private String login;
    private byte[] password;
    private BigDecimal balance;
}
