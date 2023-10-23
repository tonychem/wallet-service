package application.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class AuthenticationRequest {
    String login;
    byte[] password;
}
