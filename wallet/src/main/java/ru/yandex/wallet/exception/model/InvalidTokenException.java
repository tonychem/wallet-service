package ru.yandex.wallet.exception.model;

/**
 * Ошибка, возникающая при некорректном JWT токене
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
