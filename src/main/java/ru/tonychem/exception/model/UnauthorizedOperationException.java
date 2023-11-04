package ru.tonychem.exception.model;

/**
 * Ошибка, возникающая при попытке обратиться к приложению с некорректным идентификатором сессии (или его отсутствии)
 */
public class UnauthorizedOperationException extends Exception {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
