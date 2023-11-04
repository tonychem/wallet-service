package ru.tonychem.repository.jdbcimpl;

import ru.tonychem.domain.Transaction;
import ru.tonychem.domain.TransferRequestStatus;
import ru.tonychem.domain.dto.MoneyTransferRequest;
import ru.tonychem.exception.model.NoSuchTransactionException;
import ru.tonychem.exception.model.TransactionAlreadyExistsException;
import ru.tonychem.exception.model.TransactionStatusException;
import org.postgresql.util.PGobject;
import ru.tonychem.repository.TransactionCrudRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PGJDBCTransactionCrudRepositoryImpl implements TransactionCrudRepository {
    private final String URL;
    private final String username;
    private final String password;

    public PGJDBCTransactionCrudRepositoryImpl(String URL, String username, String password, String schema) {
        this.username = username;
        this.password = password;
        this.URL = URL + "?currentSchema=" + schema;
    }

    @Override
    public Transaction create(MoneyTransferRequest request) {
        String creationQuery = "INSERT INTO transactions (id, status, sender, recipient, amount) VALUES (?,?,?,?,?)";

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement creationStatement = connection.prepareStatement(creationQuery)) {
            checkTransactionExists(connection, request);

            Transaction transaction = Transaction.builder()
                    .id(request.getId())
                    .status(TransferRequestStatus.PENDING)
                    .sender(request.getMoneyFrom())
                    .recipient(request.getMoneyTo())
                    .amount(request.getAmount())
                    .build();

            PGobject uuid = new PGobject();
            uuid.setType("uuid");
            uuid.setValue(request.getId().toString());

            creationStatement.setObject(1, uuid);
            creationStatement.setString(2, transaction.getStatus().name());
            creationStatement.setString(3, transaction.getSender());
            creationStatement.setString(4, transaction.getRecipient());
            creationStatement.setBigDecimal(5, transaction.getAmount());
            creationStatement.executeUpdate();

            return transaction;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transaction getById(UUID id) {
        String selectQuery = "SELECT * FROM transactions WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {

            selectStatement.setObject(1, id, Types.OTHER);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                Transaction transaction = Transaction.builder()
                        .id(UUID.fromString(resultSet.getObject("id", PGobject.class).getValue()))
                        .status(TransferRequestStatus.valueOf(resultSet.getString("status")))
                        .sender(resultSet.getString("sender"))
                        .recipient(resultSet.getString("recipient"))
                        .amount(resultSet.getBigDecimal("amount"))
                        .build();
                return transaction;
            }

            throw new NoSuchTransactionException(
                    String.format("Не существует транзакции с id=%s", id)
            );

        } catch (SQLException e) {
            throw new RuntimeException(e);
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

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            int columnIndex = 1;

            if (sender != null) {
                selectStatement.setString(columnIndex, sender);
                columnIndex++;
            }

            if (recipient != null) {
                selectStatement.setString(columnIndex, recipient);
                columnIndex++;
            }

            if (status != null) {
                selectStatement.setString(columnIndex, status.name());
            }

            ResultSet resultSet = selectStatement.executeQuery();
            List<Transaction> transactions = new ArrayList<>();
            while (resultSet.next()) {
                transactions.add(
                        new Transaction(
                                UUID.fromString(resultSet.getObject(1, PGobject.class).toString()),
                                TransferRequestStatus.valueOf(resultSet.getString("status")),
                                resultSet.getString("sender"),
                                resultSet.getString("recipient"),
                                resultSet.getBigDecimal("amount")
                        )
                );
            }
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            PGobject uuid = new PGobject();
            uuid.setType("uuid");
            uuid.setValue(transaction.getId().toString());

            updateStatement.setString(1, TransferRequestStatus.APPROVED.name());
            updateStatement.setObject(2, uuid);
            updateStatement.executeUpdate();

            transaction.setStatus(TransferRequestStatus.APPROVED);
            return transaction;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            PGobject uuid = new PGobject();
            uuid.setType("uuid");
            uuid.setValue(transaction.getId().toString());

            updateStatement.setString(1, TransferRequestStatus.DECLINED.name());
            updateStatement.setObject(2, uuid);
            updateStatement.executeUpdate();

            transaction.setStatus(TransferRequestStatus.DECLINED);
            return transaction;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction setFailed(UUID id) {
        Transaction transaction = getById(id);
        String updateQuery = "UPDATE transactions SET status = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            PGobject uuid = new PGobject();
            uuid.setType("uuid");
            uuid.setValue(id.toString());

            updateStatement.setString(1, TransferRequestStatus.FAILED.name());
            updateStatement.setObject(2, uuid);
            updateStatement.executeUpdate();

            transaction.setStatus(TransferRequestStatus.FAILED);
            return transaction;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод проверяет, существует ли транзакция с id, указанным в запросе. Если такая транзакция существует,
     * метод завершает работу с ошибкой.
     *
     * @param connection соединение с БД
     * @param request    запрос денежных средств
     * @throws TransactionAlreadyExistsException
     */
    private void checkTransactionExists(Connection connection, MoneyTransferRequest request) {
        String checkTransactionExistsQuery = "SELECT * FROM transactions WHERE id = ?";

        try (PreparedStatement checkExistenceStatement = connection.prepareStatement(checkTransactionExistsQuery)) {
            PGobject uuid = new PGobject();
            uuid.setType("uuid");
            uuid.setValue(request.getId().toString());

            checkExistenceStatement.setObject(1, uuid);
            boolean transactionAlreadyExists = checkExistenceStatement.executeQuery().next();

            if (transactionAlreadyExists) {
                throw new TransactionAlreadyExistsException(
                        String.format("Транзакция с id=%s уже существует", request.getId())
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
