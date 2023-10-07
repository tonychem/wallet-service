package domain.repository;

import domain.exception.NoSuchTransactionException;
import domain.exception.TransactionAlreadyExistsException;
import domain.model.Transaction;
import domain.model.TransferRequestStatus;
import domain.model.dto.MoneyTransferRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryTransactionCrudeRepositoryImpl implements TransactionCrudRepository {

    private final Map<UUID, Transaction> transactions = new HashMap<>();

    @Override
    public Transaction create(MoneyTransferRequest request) {
        if (transactions.get(request.getId()) != null) throw new TransactionAlreadyExistsException(
                String.format("Transaction with id=%s already exists", request.getId())
        );

        Transaction transaction = Transaction.builder()
                .id(request.getId())
                .status(TransferRequestStatus.PENDING)
                .sender(request.getMoneyFrom())
                .recipient(request.getMoneyTo())
                .amount(request.getAmount())
                .build();

        transactions.put(request.getId(), transaction);

        return transaction;
    }

    @Override
    public Transaction getById(UUID id) {
        Transaction transaction = transactions.get(id);

        if (transaction == null) throw new NoSuchTransactionException(
                String.format("No transaction id=%s exists", id)
        );

        return transaction;
    }

    @Override
    public boolean exists(UUID id) {
        return transactions.get(id) != null;
    }

    @Override
    public Collection<Transaction> getTransactionsBySenderAndRecipientAndStatus(String sender, String recipient,
                                                                                TransferRequestStatus status) {
        Stream<Transaction> baseStream = transactions.values().stream();

        if (sender != null) {
            baseStream = baseStream
                    .filter(transaction -> transaction.getSender().equals(sender));
        }

        if (recipient != null) {
            baseStream = baseStream
                    .filter(transaction -> transaction.getRecipient().equals(recipient));
        }

        if (status != null) {
            baseStream = baseStream
                    .filter(transaction -> transaction.getStatus().equals(status));
        }

        return baseStream.collect(Collectors.toList());
    }

    @Override
    public Collection<Transaction> getDebitingTransactions(String login) {
        return getTransactionsBySenderAndRecipientAndStatus(login, null, null);
    }

    @Override
    public Collection<Transaction> getCreditingTransactions(String login) {
        return getTransactionsBySenderAndRecipientAndStatus(null, login, null);
    }
}
