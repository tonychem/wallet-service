package ru.yandex.wallet.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * DTO класс для передачи сообщения об ошибке
 */
@Value
public class ApiException {
    @JsonProperty("error")
    String message;
}
