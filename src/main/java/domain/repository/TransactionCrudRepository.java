package domain.repository;

import domain.model.Transaction;
import domain.model.TransferRequestStatus;
import domain.model.dto.MoneyTransferRequest;

import java.util.Collection;
import java.util.UUID;

public interface TransactionCrudRepository {
    Transaction create(MoneyTransferRequest request);

    Transaction getById(UUID id);

    boolean exists(UUID id);

    Collection<Transaction> getTransactionsBySenderAndRecipientAndStatus(String sender, String recipient,
                                                                         TransferRequestStatus status);

    Collection<Transaction> getDebitingTransactions(String login);

    Collection<Transaction> getCreditingTransactions(String login);
}
