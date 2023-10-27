package exception.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * DTO класс для передачи сообщения об ошибке
 */
@Value
public class ExceptionDto {
    @JsonProperty("error")
    String message;
}
