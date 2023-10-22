package exception.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ExceptionDto {
    @JsonProperty("error")
    String message;
}
