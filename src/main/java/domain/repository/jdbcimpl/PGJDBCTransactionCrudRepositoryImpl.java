package domain.repository.jdbcimpl;

import domain.model.Transaction;
import domain.model.TransferRequestStatus;
import domain.model.dto.MoneyTransferRequest;
import domain.repository.TransactionCrudRepository;

import java.util.Collection;
import java.util.UUID;

public class PGJDBCTransactionCrudRepositoryImpl implements TransactionCrudRepository {
    @Override
    public Transaction create(MoneyTransferRequest request) {
        return null;
    }

    @Override
    public Transaction getById(UUID id) {
        return null;
    }

    @Override
    public Collection<Transaction> getTransactionsBySenderAndRecipientAndStatus(String sender, String recipient, TransferRequestStatus status) {
        return null;
    }

    @Override
    public Collection<Transaction> getDebitingTransactions(String login) {
        return null;
    }

    @Override
    public Collection<Transaction> getCreditingTransactions(String login) {
        return null;
    }

    @Override
    public Transaction approveTransaction(String donorUsername, UUID id) {
        return null;
    }

    @Override
    public Transaction declineTransaction(String donorUsername, UUID id) {
        return null;
    }
}
