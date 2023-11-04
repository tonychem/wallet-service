package ru.tonychem.exception.model;

/**
 * Ошибка, возникающая при некорретной работе с пользовательскими транзакциями
 */
public class TransactionStatusException extends RuntimeException {
    public TransactionStatusException(String message) {
        super(message);
    }
}
