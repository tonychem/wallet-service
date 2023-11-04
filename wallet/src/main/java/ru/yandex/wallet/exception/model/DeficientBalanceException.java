package ru.yandex.wallet.exception.model;

/**
 * Ошибка, возникающая при недостатке баланса у пользователя
 */
public class DeficientBalanceException extends RuntimeException {
    public DeficientBalanceException(String message) {
        super(message);
    }
}
