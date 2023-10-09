package domain.repository;

import domain.exception.NoSuchTransactionException;
import domain.exception.TransactionAlreadyExistsException;
import domain.model.Transaction;
import domain.model.TransferRequestStatus;
import domain.model.dto.MoneyTransferRequest;
import domain.model.dto.MoneyTransferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void shouldCreateTransaction() {
        Transaction transaction = inMemoryTransactionCrudeRepository.create(moneyTransferRequest);
        assertThat(transaction.getId()).isEqualByComparingTo(moneyTransferRequest.getId());
        assertThat(transaction.getSender()).isEqualTo(moneyTransferRequest.getMoneyFrom());
        assertThat(transaction.getRecipient()).isEqualTo(moneyTransferRequest.getMoneyTo());
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.PENDING);
    }

    @Test
    void shouldFailWhenSameTransactionId() {
        Transaction transaction = inMemoryTransactionCrudeRepository.create(moneyTransferRequest);
        assertThatThrownBy(() -> inMemoryTransactionCrudeRepository.create(
                new MoneyTransferRequest(moneyTransferRequest.getId(),
                        "another", "another", BigDecimal.ONE)
        )).isInstanceOf(TransactionAlreadyExistsException.class);
    }

    @Test
    void shouldGetTransactionById() {
        inMemoryTransactionCrudeRepository.create(moneyTransferRequest);

        Transaction transactionFromDb = inMemoryTransactionCrudeRepository.getById(moneyTransferRequest.getId());
        assertThat(transactionFromDb.getSender()).isEqualTo(moneyTransferRequest.getMoneyFrom());
        assertThat(transactionFromDb.getRecipient()).isEqualTo(moneyTransferRequest.getMoneyTo());
    }

    @Test
    void shouldThrowExceptionWhenIdDoesnotExist() {
        assertThatThrownBy(() -> inMemoryTransactionCrudeRepository.getById(moneyTransferRequest.getId()))
                .isInstanceOf(NoSuchTransactionException.class);
    }

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

    @Test
    void shouldDeclineTransaction() {
        Transaction transaction = inMemoryTransactionCrudeRepository.create(moneyTransferRequest);
        inMemoryTransactionCrudeRepository.declineTransaction(transaction.getId());
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.DECLINED);
    }

    @Test
    void shouldApproveTransaction() {
        Transaction transaction = inMemoryTransactionCrudeRepository.create(moneyTransferRequest);
        inMemoryTransactionCrudeRepository.approveTransaction(transaction.getId());
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.APPROVED);
    }
}