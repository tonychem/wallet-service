package domain.model.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class AuthenticatedPlayerDto {
    Long id;
    String username;
    BigDecimal balance;
}
