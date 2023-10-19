package domain.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class AuthenticatedPlayerDto {
    Long id;
    String login;
    String username;
    BigDecimal balance;
}
