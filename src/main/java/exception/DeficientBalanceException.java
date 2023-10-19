package exception;

public class DeficientBalanceException extends RuntimeException {
    public DeficientBalanceException(String message) {
        super(message);
    }
}
