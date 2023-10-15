package domain.repository.db;

import domain.exception.TransactionStatusException;
import domain.model.Transaction;
import domain.model.TransferRequestStatus;
import domain.repository.jdbcimpl.PGJDBCPlayerCrudRepositoryImpl;
import domain.repository.jdbcimpl.PGJDBCTransactionCrudRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dockerized Postgresql Transaction Repository test class")
@Testcontainers
class PGJDBCTransactionCrudRepositoryImplTest {
    @Container
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    private PGJDBCTransactionCrudRepositoryImpl transactionRepository;
    private PGJDBCPlayerCrudRepositoryImpl playerRepository;

    private UUID transactionIdFromUserToAdmin;
    private UUID transactionIdFromAdminToUserPending;
    private UUID transactionIdFromAdminToUserApproved;


    @DisplayName("Should return existing transaction by id")
    @Test
    public void shouldReturnExistingTransactionById() {
        Transaction transaction = transactionRepository.getById(transactionIdFromUserToAdmin);

        assertThat(transaction.getId()).isEqualTo(transactionIdFromUserToAdmin);
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.PENDING);
        assertThat(transaction.getSender()).isEqualTo("user");
        assertThat(transaction.getRecipient()).isEqualTo("admin");
        assertThat(transaction.getAmount().longValue()).isEqualTo(1L);
    }

    @DisplayName("Should throw exception when retrieving non-existinh transaction")
    @Test
    public void shouldThrowExceptionWhenTransactionIsAbsentById() {
        assertThatThrownBy(() -> transactionRepository.getById(UUID.randomUUID()));
    }

    @DisplayName("Should return a list of transaction by sender, recipient and status")
    @Test
    public void shouldReturnListOfTransactionsBySenderByRecipientByStatus() {
        Collection<Transaction> transactionsByUserSender =
                transactionRepository.getTransactionsBySenderAndRecipientAndStatus("user", null, null);
        Collection<Transaction> transactionsByUserReceiver =
                transactionRepository.getTransactionsBySenderAndRecipientAndStatus(null, "user", null);
        Collection<Transaction> pendingTransactions =
                transactionRepository.getTransactionsBySenderAndRecipientAndStatus(null, null,
                        TransferRequestStatus.PENDING);
        Collection<Transaction> allTransactions =
                transactionRepository.getTransactionsBySenderAndRecipientAndStatus(null, null, null);

        assertThat(transactionsByUserSender.size()).isEqualTo(1);
        assertThat(transactionsByUserSender).allMatch(transaction -> transaction.getSender().equals("user"));

        assertThat(transactionsByUserReceiver.size()).isEqualTo(2);
        assertThat(transactionsByUserReceiver).allMatch(transaction -> transaction.getRecipient().equals("user"));

        assertThat(allTransactions.size()).isEqualTo(3);
    }

    @DisplayName("Should return all debiting transactions by existing user")
    @Test
    public void shouldReturnListOfDebitingTransactionsByExistingUser() {
        Collection<Transaction> transactionsByAdmin = transactionRepository.getDebitingTransactions("admin");
        Collection<Transaction> transactionsByUser = transactionRepository.getDebitingTransactions("user");

        assertThat(transactionsByAdmin.size()).isEqualTo(2);
        assertThat(transactionsByUser.size()).isEqualTo(1);
    }

    @DisplayName("Should return all crediting transactions by existing user")
    @Test
    public void shouldReturnListOfCreditingTransactionsByExistingUser() {
        Collection<Transaction> transactionsToUser = transactionRepository.getCreditingTransactions("user");
        assertThat(transactionsToUser.size()).isEqualTo(2);
    }

    @DisplayName("Should approve transactions")
    @Test
    public void shouldApproveTransaction() {
        Transaction transaction =
                transactionRepository.approveTransaction("admin", transactionIdFromAdminToUserPending);

        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.APPROVED);
        assertThat(transaction.getRecipient()).isEqualTo("user");
    }

    @DisplayName("Should fail when approving somebody else's transactions")
    @Test
    public void shouldThrowExceptionWhenApprovingOthersTransactions() {
        assertThatThrownBy(() ->
                transactionRepository.approveTransaction("user", transactionIdFromAdminToUserPending))
                .isInstanceOf(TransactionStatusException.class);
    }

    @DisplayName("Should decline transactions")
    @Test
    public void shouldDeclineTransaction() {
        Transaction transaction =
                transactionRepository.declineTransaction("admin", transactionIdFromAdminToUserPending);

        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.DECLINED);
        assertThat(transaction.getRecipient()).isEqualTo("user");
    }

    @DisplayName("Should fail when declining somebody else's transactions")
    @Test
    public void shouldThrowExceptionWhenDecliningOthersTransactions() {
        assertThatThrownBy(() ->
                transactionRepository.declineTransaction("user", transactionIdFromAdminToUserPending))
                .isInstanceOf(TransactionStatusException.class);
    }

    @DisplayName("Should fail when declining transaction in approved state")
    @Test
    public void shouldThrowExceptionWhenDecliningApprovedTransaction() {
        assertThatThrownBy(() ->
                transactionRepository.declineTransaction("admin", transactionIdFromAdminToUserApproved))
                .isInstanceOf(TransactionStatusException.class);
    }

    @DisplayName("Should fail transaction")
    @Test
    public void shouldFailTransaction() {
        Transaction transaction =
                transactionRepository.setFailed(transactionIdFromUserToAdmin);
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.FAILED);
    }

    /**
     * Метод подготоваливает БД для тестирования: создает две таблицы (players и transactions),
     * вносит двух пользователей (user со счетом 10 и admin со счетом 5),
     * и 3 транзакции: от admin к user (1 подтвержденная на сумму 1.0 и 1 неподтвержденная на сумму 2.0),
     * и от user к admin (неподтвержденная на сумму 1.0)
     */
    @BeforeEach
    public void init() {
        assert postgres.isRunning();

        String url = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        String createTablePlayersQuery = """
                CREATE TABLE IF NOT EXISTS players (
                    id int8 PRIMARY KEY UNIQUE NOT NULL,
                    username varchar(255) UNIQUE NOT NULL,
                    login varchar(50) UNIQUE NOT NULL,
                    password bytea NOT NULL,
                    balance numeric(15,3) NOT NULL
                )
                """;

        String createTableTransactionQuery = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id uuid PRIMARY KEY UNIQUE NOT NULL,
                    status varchar(20) NOT NULL,
                    sender varchar(50) REFERENCES players(login),
                    recipient varchar(50) REFERENCES players(login),
                    amount numeric(15,3) NOT NULL
                )
                """;

        String createSequenceQuery = """
                CREATE SEQUENCE IF NOT EXISTS player_id_sequence
                """;

        String insertUserPlayerQuery = """
                INSERT INTO players(id, username, login, password, balance)
                    VALUES (nextval('player_id_sequence'), 'user', 'user', '\\xee11cbb19052e40b07aac0ca060c23ee', 10.000)
                """;

        String insertAdminPlayerQuery = """
                INSERT INTO players(id, username, login, password, balance)
                    VALUES (nextval('player_id_sequence'), 'admin', 'admin', '\\x21232f297a57a5a743894a0e4a801fc3', 5.000)
                """;

        String insertPendingTransactionFromAdminToUserQuery = """
                INSERT INTO transactions(id, status, sender, recipient, amount)
                    VALUES (?, 'PENDING', 'admin', 'user', 2.000)
                """;

        String insertApprovedTransactionFromAdminToUserQuery = """
                INSERT INTO transactions(id, status, sender, recipient, amount)
                    VALUES (?, 'APPROVED', 'admin', 'user', 1.000)
                """;

        String insertPendingTransactionFromUserToAdminQuery = """
                INSERT INTO transactions(id, status, sender, recipient, amount)
                    VALUES (?, 'PENDING', 'user', 'admin', 1.000)
                """;

        transactionIdFromAdminToUserPending = UUID.randomUUID();
        transactionIdFromAdminToUserApproved = UUID.randomUUID();
        transactionIdFromUserToAdmin = UUID.randomUUID();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Statement statement = connection.createStatement();
            statement.execute(createTablePlayersQuery);
            statement.execute(createTableTransactionQuery);
            statement.execute(createSequenceQuery);
            statement.execute(insertUserPlayerQuery);
            statement.execute(insertAdminPlayerQuery);

            PreparedStatement insertPendingTransactionFromAdminToUser =
                    connection.prepareStatement(insertPendingTransactionFromAdminToUserQuery);
            PreparedStatement insertApprovedTransactionFromAdminToUser =
                    connection.prepareStatement(insertApprovedTransactionFromAdminToUserQuery);
            PreparedStatement insertPendingTransactionFromUserToAdmin =
                    connection.prepareStatement(insertPendingTransactionFromUserToAdminQuery);

            PGobject uuidFromAdminToUserPending = new PGobject();
            uuidFromAdminToUserPending.setType("uuid");
            uuidFromAdminToUserPending.setValue(transactionIdFromAdminToUserPending.toString());

            PGobject uuidFromAdminToUserApproved = new PGobject();
            uuidFromAdminToUserApproved.setType("uuid");
            uuidFromAdminToUserApproved.setValue(transactionIdFromAdminToUserApproved.toString());

            PGobject uuidFromUserToAdmin = new PGobject();
            uuidFromUserToAdmin.setType("uuid");
            uuidFromUserToAdmin.setValue(transactionIdFromUserToAdmin.toString());

            insertPendingTransactionFromAdminToUser.setObject(1, uuidFromAdminToUserPending);
            insertApprovedTransactionFromAdminToUser.setObject(1, uuidFromAdminToUserApproved);
            insertPendingTransactionFromUserToAdmin.setObject(1, uuidFromUserToAdmin);

            insertPendingTransactionFromAdminToUser.executeUpdate();
            insertApprovedTransactionFromAdminToUser.executeUpdate();
            insertPendingTransactionFromUserToAdmin.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        playerRepository = new PGJDBCPlayerCrudRepositoryImpl(url, username, password, "public");
        transactionRepository = new PGJDBCTransactionCrudRepositoryImpl(url, username, password, "public");
    }

    @AfterEach
    public void destruct() {
        String url = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        String dropTableTransactionQuery = """
                DROP TABLE transactions
                """;

        String dropTablePlayersQuery = """
                DROP TABLE players
                """;

        String dropSequenceQuery = """
                DROP SEQUENCE player_id_sequence
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Statement statement = connection.createStatement();
            statement.execute(dropTableTransactionQuery);
            statement.execute(dropTablePlayersQuery);
            statement.execute(dropSequenceQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}