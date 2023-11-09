package ru.yandex.wallet.exception.exceptions;

/**
 * Ошибка, возникающая при некорретной работе с пользовательскими транзакциями
 */
public class TransactionStatusException extends RuntimeException {
    public TransactionStatusException(String message) {
        super(message);
    }
}
