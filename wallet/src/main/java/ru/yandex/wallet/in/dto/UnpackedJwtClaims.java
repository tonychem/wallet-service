package ru.yandex.wallet.in.dto;

import lombok.Value;

import java.util.UUID;

/**
 * Класс-обертка над декодированными данными из JWT, полученного от пользователя
 */
@Value
public class UnpackedJwtClaims {
    Long userId;
    String login;
    String username;
    UUID sessionId;
}
