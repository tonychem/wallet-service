package ru.tonychem.exception;

import ru.tonychem.exception.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {NoSuchPlayerException.class, NoSuchTransactionException.class})
    public ResponseEntity<ApiException> handleMissingEntityExceptions(RuntimeException exception) {
        return new ResponseEntity<>(new ApiException(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {PlayerAlreadyExistsException.class, TransactionAlreadyExistsException.class})
    public ResponseEntity<ApiException> handleAlreadyExistingExceptions(RuntimeException exception) {
        return new ResponseEntity<>(new ApiException(exception.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {BadCredentialsException.class, ConstraintViolationException.class,
            DeficientBalanceException.class})
    public ResponseEntity<ApiException> handleInvalidUserRequests(RuntimeException exception) {
        return new ResponseEntity<>(new ApiException(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {InvalidTokenException.class, UnauthorizedOperationException.class})
    public ResponseEntity<ApiException> handleUnauthorizedOperations(RuntimeException exception) {
        return new ResponseEntity<>(new ApiException(exception.getMessage()), HttpStatus.FORBIDDEN);
    }


}
