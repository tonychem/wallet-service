package ru.yandex.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.wallet.exception.model.*;

import java.security.SignatureException;

/**
 * Класс, ответственный за глобальную обработку ошибок приложения.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {NoSuchPlayerException.class, NoSuchTransactionException.class})
    public ResponseEntity<ApiException> handleMissingEntityExceptions(Exception exception) {
        return new ResponseEntity<>(new ApiException(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {PlayerAlreadyExistsException.class, TransactionAlreadyExistsException.class})
    public ResponseEntity<ApiException> handleAlreadyExistingExceptions(Exception exception) {
        return new ResponseEntity<>(new ApiException(exception.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {BadCredentialsException.class, DeficientBalanceException.class,
            ConstraintViolationException.class})
    public ResponseEntity<ApiException> handleInvalidUserRequests(Exception exception) {
        return new ResponseEntity<>(new ApiException(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {InvalidTokenException.class, UnauthorizedOperationException.class,
            SignatureException.class})
    public ResponseEntity<ApiException> handleUnauthorizedOperations(Exception exception) {
        return new ResponseEntity<>(new ApiException(exception.getMessage()), HttpStatus.FORBIDDEN);
    }
}
