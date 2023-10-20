package domain.dto;

import lombok.Value;

@Value
public class MoneyTransferResponse {
    AuthenticatedPlayerDto requester;
    TransactionDto transactionDto;
}
