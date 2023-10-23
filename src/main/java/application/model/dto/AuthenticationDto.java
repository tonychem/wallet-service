package application.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
public class AuthenticationDto {
    private final Long id;
    private final String login;
    private final String username;
    @JsonIgnore
    private final UUID sessionId;
    private final BigDecimal balance;
}
