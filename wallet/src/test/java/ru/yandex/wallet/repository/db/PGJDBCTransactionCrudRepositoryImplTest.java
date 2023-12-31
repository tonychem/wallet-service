package ru.yandex.wallet.repository.db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.wallet.domain.Transaction;
import ru.yandex.wallet.domain.TransferRequestStatus;
import ru.yandex.wallet.exception.exceptions.TransactionStatusException;
import ru.yandex.wallet.repository.jdbcimpl.PGJDBCTransactionCrudRepositoryImpl;

import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Postgres Transaction Repository test")
@Transactional
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class PGJDBCTransactionCrudRepositoryImplTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static UUID transactionIdFromUserToAdmin;
    private static UUID transactionIdFromAdminToUserPending;
    private static UUID transactionIdFromAdminToUserApproved;

    private PGJDBCTransactionCrudRepositoryImpl transactionRepository;

    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void initRepository() {
        transactionRepository = new PGJDBCTransactionCrudRepositoryImpl(jdbcTemplate);
    }

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

    @BeforeEach
    public void initTransactionRepository() {
        transactionRepository = new PGJDBCTransactionCrudRepositoryImpl(jdbcTemplate);
    }

    @BeforeAll
    public static void initiateUUIDs() {
        transactionIdFromUserToAdmin = UUID.fromString("e97668d0-96a0-47bc-9202-fc04982a0b5e");
        transactionIdFromAdminToUserPending = UUID.fromString("7b65505d-f04f-4b4d-a179-8277c288081a");
        transactionIdFromAdminToUserApproved = UUID.fromString("dd49e541-45af-47ff-827c-b4f8f828857b");
    }
}