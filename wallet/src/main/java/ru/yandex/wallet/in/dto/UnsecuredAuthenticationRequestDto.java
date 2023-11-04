package ru.yandex.wallet.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Входящий пользовательский запрос на аутентификацию с незашифрованным паролем
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnsecuredAuthenticationRequestDto {
    private String login;
    private String password;
}
