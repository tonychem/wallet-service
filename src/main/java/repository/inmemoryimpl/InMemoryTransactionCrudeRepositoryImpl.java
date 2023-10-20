package repository.inmemoryimpl;

import exception.NoSuchTransactionException;
import exception.TransactionAlreadyExistsException;
import exception.TransactionStatusException;
import domain.Transaction;
import domain.TransferRequestStatus;
import domain.dto.MoneyTransferRequest;
import repository.TransactionCrudRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Реализация БД транзакций в оперативной памяти.
 */
public class InMemoryTransactionCrudeRepositoryImpl implements TransactionCrudRepository {

    private final Map<UUID, Transaction> transactions = new HashMap<>();

    /**
     * Создание транзакции
     *
     * @param request обертка над набором входных параметров при составлении запроса на получение денег
     */
    @Override
    public Transaction create(MoneyTransferRequest request) {
        if (transactions.get(request.getId()) != null) throw new TransactionAlreadyExistsException(
                String.format("Транзакция с id=%s уже существует", request.getId())
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
                String.format("Не существует транзакции с id=%s", id)
        );

        return transaction;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Transaction> getDebitingTransactions(String login) {
        return getTransactionsBySenderAndRecipientAndStatus(login, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Transaction> getCreditingTransactions(String login) {
        return getTransactionsBySenderAndRecipientAndStatus(null, login, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction approveTransaction(String donorUsername, UUID id) {
        Transaction transaction = getById(id);

        if (!transaction.getSender().equals(donorUsername))
            throw new TransactionStatusException("Вы не можете подтвердить чужую транзакцию!");

        if (!transaction.getStatus().equals(TransferRequestStatus.PENDING))
            throw new TransactionStatusException("Только транзакции в режиме подтверждения могут быть одобрены");

        transaction.setStatus(TransferRequestStatus.APPROVED);
        return transaction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction declineTransaction(String donorUsername, UUID id) {
        Transaction transaction = getById(id);

        if (!transaction.getSender().equals(donorUsername))
            throw new TransactionStatusException("Вы не можете отклонить чужую транзакцию!");

        if (!transaction.getStatus().equals(TransferRequestStatus.PENDING))
            throw new TransactionStatusException("Только транзакции в режиме подтверждения могут быть отклонены");

        transaction.setStatus(TransferRequestStatus.DECLINED);
        return transaction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction setFailed(UUID id) {
        Transaction transaction = getById(id);
        transaction.setStatus(TransferRequestStatus.FAILED);
        return transaction;
    }
}
