package ru.tonychem.exception.model;

/**
 * Ошибка, возникающая при отсутствии предоставленной транзакции в базе данных
 */
public class NoSuchTransactionException extends RuntimeException {
    public NoSuchTransactionException(String message) {
        super(message);
    }
}