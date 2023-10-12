package domain.repository;

import domain.exception.NoSuchTransactionException;
import domain.exception.TransactionAlreadyExistsException;
import domain.model.Transaction;
import domain.model.TransferRequestStatus;
import domain.model.dto.MoneyTransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("In-memory transaction repository test class")
class InMemoryTransactionCrudeRepositoryImplTest {

    private InMemoryTransactionCrudeRepositoryImpl inMemoryTransactionCrudeRepository;
    private MoneyTransferRequest moneyTransferRequest;

    @BeforeEach
    public void init() {
        inMemoryTransactionCrudeRepository = new InMemoryTransactionCrudeRepositoryImpl();
        moneyTransferRequest = new MoneyTransferRequest(
                UUID.fromString("d1a8662a-66bc-11ee-8c99-0242ac120002"),
                "sender",
                "reciever",
                BigDecimal.TEN
        );
    }

    @DisplayName("Transaction creation when transaction data is correct")
    @Test
    void shouldCreateTransaction() {
        Transaction transaction = inMemoryTransactionCrudeRepository.create(moneyTransferRequest);
        assertThat(transaction.getId()).isEqualByComparingTo(moneyTransferRequest.getId());
        assertThat(transaction.getSender()).isEqualTo(moneyTransferRequest.getMoneyFrom());
        assertThat(transaction.getRecipient()).isEqualTo(moneyTransferRequest.getMoneyTo());
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.PENDING);
    }

    @DisplayName("Creating transaction with already existing id throws error")
    @Test
    void shouldFailWhenSameTransactionId() {
        Transaction transaction = inMemoryTransactionCrudeRepository.create(moneyTransferRequest);
        assertThatThrownBy(() -> inMemoryTransactionCrudeRepository.create(
                new MoneyTransferRequest(moneyTransferRequest.getId(),
                        "another", "another", BigDecimal.ONE)
        )).isInstanceOf(TransactionAlreadyExistsException.class);
    }

    @DisplayName("Fetching transaction by id when id is correct")
    @Test
    void shouldGetTransactionById() {
        inMemoryTransactionCrudeRepository.create(moneyTransferRequest);

        Transaction transactionFromDb = inMemoryTransactionCrudeRepository.getById(moneyTransferRequest.getId());
        assertThat(transactionFromDb.getSender()).isEqualTo(moneyTransferRequest.getMoneyFrom());
        assertThat(transactionFromDb.getRecipient()).isEqualTo(moneyTransferRequest.getMoneyTo());
    }

    @DisplayName("Fetching absent transaction throws error")
    @Test
    void shouldThrowExceptionWhenIdDoesnotExist() {
        assertThatThrownBy(() -> inMemoryTransactionCrudeRepository.getById(moneyTransferRequest.getId()))
                .isInstanceOf(NoSuchTransactionException.class);
    }

    @DisplayName("Fetching transactions by sender, by recipient and current status")
    @Test
    void shouldReturnListOfAppropriateTransactions() {
        MoneyTransferRequest requestFromAdminToUser1 = new MoneyTransferRequest(
                UUID.randomUUID(), "admin", "user", BigDecimal.ONE
        );

        MoneyTransferRequest requestFromAdminToUser10 = new MoneyTransferRequest(
                UUID.randomUUID(), "admin", "user", BigDecimal.TEN
        );

        MoneyTransferRequest requestFromUserToAdmin10 = new MoneyTransferRequest(
                UUID.randomUUID(), "user", "admin", BigDecimal.TEN
        );

        inMemoryTransactionCrudeRepository.create(requestFromAdminToUser1);
        inMemoryTransactionCrudeRepository.create(requestFromAdminToUser10);
        inMemoryTransactionCrudeRepository.create(requestFromUserToAdmin10);

        Collection<Transaction> adminTransactionsPending = inMemoryTransactionCrudeRepository
                .getTransactionsBySenderAndRecipientAndStatus("admin", "user", TransferRequestStatus.PENDING);
        Collection<Transaction> adminTransactionsApproved = inMemoryTransactionCrudeRepository
                .getTransactionsBySenderAndRecipientAndStatus("admin", "user", TransferRequestStatus.APPROVED);
        Collection<Transaction> adminTransactionsDeclined = inMemoryTransactionCrudeRepository
                .getTransactionsBySenderAndRecipientAndStatus("admin", "user", TransferRequestStatus.DECLINED);
        Collection<Transaction> userTransactionsPending = inMemoryTransactionCrudeRepository
                .getTransactionsBySenderAndRecipientAndStatus("user", "admin", TransferRequestStatus.PENDING);

        Collection<Transaction> adminDebitingTransactions = inMemoryTransactionCrudeRepository
                .getDebitingTransactions("admin");
        Collection<Transaction> adminCrediting = inMemoryTransactionCrudeRepository
                .getCreditingTransactions("admin");

        assertThat(adminTransactionsPending.size()).isEqualTo(2);
        assertThat(adminTransactionsApproved.size()).isEqualTo(0);
        assertThat(adminTransactionsDeclined.size()).isEqualTo(0);
        assertThat(userTransactionsPending.size()).isEqualTo(1);
        assertThat(adminDebitingTransactions.size()).isEqualTo(2);
        assertThat(adminCrediting.size()).isEqualTo(1);
    }

    @DisplayName("Declining existing transaction")
    @Test
    void shouldDeclineTransaction() {
        Transaction transaction = inMemoryTransactionCrudeRepository.create(moneyTransferRequest);
        inMemoryTransactionCrudeRepository.declineTransaction(moneyTransferRequest.getMoneyFrom(),
                transaction.getId());
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.DECLINED);
    }

    @DisplayName("Approving existing transaction")
    @Test
    void shouldApproveTransaction() {
        Transaction transaction = inMemoryTransactionCrudeRepository.create(moneyTransferRequest);
        inMemoryTransactionCrudeRepository.approveTransaction(moneyTransferRequest.getMoneyFrom(),
                transaction.getId());
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.APPROVED);
    }
}