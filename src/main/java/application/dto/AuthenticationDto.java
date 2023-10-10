package application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class AuthenticationDto {
    private final Long id;
    private final String login;
    private final String username;
    private final UUID sessionId;
    private BigDecimal balance;
}
