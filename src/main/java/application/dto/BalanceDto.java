package application.dto;


import lombok.Value;

import java.math.BigDecimal;

@Value
public class BalanceDto {
    Long id;
    String username;
    BigDecimal balance;
}
