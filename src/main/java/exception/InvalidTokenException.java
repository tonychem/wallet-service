package exception;

/**
 * Ошибка, возникающая при некорректном JWT токене
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
