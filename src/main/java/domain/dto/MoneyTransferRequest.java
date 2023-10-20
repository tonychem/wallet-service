package domain.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class MoneyTransferRequest {
    UUID id;
    String moneyFrom;
    String moneyTo;
    BigDecimal amount;
}
