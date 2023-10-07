package domain.exception;

public class NoSuchTransactionException extends RuntimeException {
    public NoSuchTransactionException(String message) {
        super(message);
    }
}
