package ru.yandex.wallet.domain.dto;

import lombok.Value;

import java.math.BigDecimal;

/**
 * Информация аутентифицированного пользователя
 */
@Value
public class AuthenticatedPlayerDto {
    Long id;
    String login;
    String username;
    BigDecimal balance;
}
