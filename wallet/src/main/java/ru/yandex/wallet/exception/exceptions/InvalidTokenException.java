package ru.yandex.wallet.exception.exceptions;

/**
 * Ошибка, возникающая при некорректном JWT токене
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
