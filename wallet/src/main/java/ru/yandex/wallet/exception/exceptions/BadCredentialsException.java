package ru.yandex.wallet.exception.exceptions;

/**
 * Ошибка, возникающая при некорректной пользовательской связке логин-пароль
 */
public class BadCredentialsException extends Exception {
    public BadCredentialsException(String message) {
        super(message);
    }
}
