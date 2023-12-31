package ru.yandex.wallet.exception.exceptions;

/**
 * Ошибка, возникающая при попытке создания транзакции с существующим ID
 */
public class TransactionAlreadyExistsException extends RuntimeException {
    public TransactionAlreadyExistsException(String message) {
        super(message);
    }
}
