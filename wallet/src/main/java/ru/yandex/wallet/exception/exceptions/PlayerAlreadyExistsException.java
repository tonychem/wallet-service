package ru.yandex.wallet.exception.exceptions;

/**
 * Ошибка, возникающая при попытке создания пользователя, который имеет такие же логин или никнейм
 */
public class PlayerAlreadyExistsException extends RuntimeException {
    public PlayerAlreadyExistsException(String message) {
        super(message);
    }
}
