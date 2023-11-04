package ru.yandex.wallet.repository.jdbcimpl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.wallet.domain.Transaction;
import ru.yandex.wallet.domain.TransferRequestStatus;
import ru.yandex.wallet.domain.dto.MoneyTransferRequest;
import ru.yandex.wallet.exception.model.NoSuchTransactionException;
import ru.yandex.wallet.exception.model.TransactionAlreadyExistsException;
import ru.yandex.wallet.exception.model.TransactionStatusException;
import ru.yandex.wallet.repository.TransactionCrudRepository;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class PGJDBCTransactionCrudRepositoryImpl implements TransactionCrudRepository {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> {
        Transaction transaction = Transaction.builder()
                .id(rs.getObject("id", UUID.class))
                .status(TransferRequestStatus.valueOf(rs.getString("status")))
                .sender(rs.getString("sender"))
                .recipient(rs.getString("recipient"))
                .amount(rs.getBigDecimal("amount"))
                .build();
        return transaction;
    };

    @Override
    public Transaction create(MoneyTransferRequest request) {
        checkTransactionExists(request);

        String creationQuery = "INSERT INTO transactions (id, status, sender, recipient, amount) VALUES (?,?,?,?,?)";

        jdbcTemplate.update(creationQuery, request.getId(), TransferRequestStatus.PENDING, request.getMoneyFrom(),
                request.getMoneyTo(), request.getAmount());

        Transaction transaction = Transaction.builder()
                .id(request.getId())
                .status(TransferRequestStatus.PENDING)
                .sender(request.getMoneyFrom())
                .recipient(request.getMoneyTo())
                .amount(request.getAmount())
                .build();

        return transaction;
    }

    @Override
    public Transaction getById(UUID id) {
        String selectQuery = "SELECT * FROM transactions WHERE id = ?";
        try {
            Transaction transaction = jdbcTemplate.queryForObject(selectQuery, transactionRowMapper, id);
            return transaction;
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchTransactionException(
                    String.format("Не существует транзакции с id=%s", id)
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Transaction> getTransactionsBySenderAndRecipientAndStatus(String sender,
                                                                                String recipient,
                                                                                TransferRequestStatus status) {
        String selectQuery = buildDynamicQuery(sender, recipient, status);

        PreparedStatementCreator psc = con -> {
            PreparedStatement preparedStatement = con.prepareStatement(selectQuery);

            int columnIndex = 1;

            if (sender != null) {
                preparedStatement.setString(columnIndex, sender);
                columnIndex++;
            }

            if (recipient != null) {
                preparedStatement.setString(columnIndex, recipient);
                columnIndex++;
            }

            if (status != null) {
                preparedStatement.setString(columnIndex, status.name());
            }

            return preparedStatement;
        };

        Stream<Transaction> transactionStream = jdbcTemplate.queryForStream(psc, transactionRowMapper);
        return transactionStream.collect(Collectors.toList());
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

        String updateQuery = "UPDATE transactions SET status = ? WHERE id = ?";

        jdbcTemplate.update(updateQuery, TransferRequestStatus.APPROVED.name(), id);
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

        String updateQuery = "UPDATE transactions SET status = ? WHERE id = ?";

        jdbcTemplate.update(updateQuery, TransferRequestStatus.DECLINED.name(), id);
        transaction.setStatus(TransferRequestStatus.DECLINED);
        return transaction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction setFailed(UUID id) {
        Transaction transaction = getById(id);
        String updateQuery = "UPDATE transactions SET status = ? WHERE id = ?";

        jdbcTemplate.update(updateQuery, TransferRequestStatus.FAILED.name(), id);
        transaction.setStatus(TransferRequestStatus.FAILED);
        return transaction;
    }

    /**
     * Метод проверяет, существует ли транзакция с id, указанным в запросе. Если такая транзакция существует,
     * метод завершает работу с ошибкой.
     *
     * @param request запрос денежных средств
     * @throws TransactionAlreadyExistsException
     */
    private void checkTransactionExists(MoneyTransferRequest request) {
        String checkTransactionExistsQuery = "SELECT * FROM transactions WHERE id = ?";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(checkTransactionExistsQuery, request.getId());

        if (rowSet.next()) {
            throw new TransactionAlreadyExistsException(
                    String.format("Транзакция с id=%s уже существует", request.getId())
            );
        }
    }

    /**
     * Вспомогательнй метод для формирования динамического sql запроса для извлечения списка транзакций
     *
     * @param sender    отправитель денежных средств
     * @param recipient получатель денежных средств
     * @param status    статус транзакции
     */
    private String buildDynamicQuery(String sender, String recipient, TransferRequestStatus status) {
        String baseSelectQuery = "SELECT * FROM transactions";
        boolean noQueryConditionsAdded = true;

        if (sender != null) {
            baseSelectQuery += " WHERE sender = ?";
            noQueryConditionsAdded = false;
        }

        if (recipient != null) {
            if (noQueryConditionsAdded) {
                baseSelectQuery += " WHERE recipient = ?";
                noQueryConditionsAdded = false;
            } else {
                baseSelectQuery += " AND recipient = ?";
            }
        }

        if (status != null) {
            if (noQueryConditionsAdded) {
                baseSelectQuery += " WHERE status = ?";
            } else {
                baseSelectQuery += " AND status = ?";
            }
        }

        return baseSelectQuery;
    }
}
