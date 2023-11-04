package ru.yandex.wallet.exception.model;

/**
 * Ошибка, возникающая при отсутствии пользователя в базе данных
 */
public class NoSuchPlayerException extends RuntimeException {
    public NoSuchPlayerException(String message) {
        super(message);
    }
}
