package service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.tonychem.domain.Player;
import ru.tonychem.domain.Transaction;
import ru.tonychem.domain.TransferRequestStatus;
import ru.tonychem.domain.dto.*;
import ru.tonychem.exception.model.BadCredentialsException;
import ru.tonychem.exception.model.DeficientBalanceException;
import ru.tonychem.in.dto.*;
import ru.tonychem.repository.PlayerCrudRepository;
import ru.tonychem.repository.TransactionCrudRepository;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.impl.PlayerServiceImpl;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("Player service test")
class PlayerServiceImplTest {

    private PlayerService playerService;
    private PlayerCrudRepository mockPlayerCrudRepository;
    private TransactionCrudRepository mockTransactionCrudRepository;
    private static MessageDigest messageDigest;

    @SneakyThrows
    @BeforeEach
    public void init() {
        mockPlayerCrudRepository = Mockito.mock(PlayerCrudRepository.class);
        mockTransactionCrudRepository = Mockito.mock(TransactionCrudRepository.class);
        messageDigest = MessageDigest.getInstance("MD5");
        playerService = new PlayerServiceImpl(mockPlayerCrudRepository, mockTransactionCrudRepository, messageDigest);
    }

    @DisplayName("Authenticates player when credentials are correct")
    @Test
    void shouldAuthenticateWhenCredentialsAreCorrect() throws Exception {
        UnsecuredAuthenticationRequestDto authenticationRequestDto
                = new UnsecuredAuthenticationRequestDto("admin", "password");

        byte[] password = messageDigest.digest(authenticationRequestDto.getPassword().getBytes());
        Player admin = Player.builder()
                .username(authenticationRequestDto.getLogin())
                .login(authenticationRequestDto.getLogin())
                .password(password)
                .build();
        when(mockPlayerCrudRepository.getByLogin(authenticationRequestDto.getLogin()))
                .thenReturn(admin);

        AuthenticatedPlayerDto dto = playerService.authenticate(authenticationRequestDto);
        assertThat(dto.getUsername()).isEqualTo(admin.getUsername());
        assertThat(dto.getLogin()).isEqualTo(admin.getLogin());

        verify(mockPlayerCrudRepository).getByLogin(authenticationRequestDto.getLogin());
    }

    @DisplayName("Throw error when player offers bad credentials")
    @Test
    void shouldThrowExceptionWhenBadCredentials() throws Exception {
        UnsecuredAuthenticationRequestDto authenticationRequestDto
                = new UnsecuredAuthenticationRequestDto("admin", "incorrect_password");

        byte[] password = messageDigest.digest("correct_password".getBytes());
        Player admin = Player.builder()
                .username("admin")
                .login("admin")
                .password(password)
                .build();

        when(mockPlayerCrudRepository.getByLogin("admin"))
                .thenReturn(admin);

        assertThatThrownBy(() -> playerService.authenticate(authenticationRequestDto))
                .isInstanceOf(BadCredentialsException.class);

        verify(mockPlayerCrudRepository).getByLogin(authenticationRequestDto.getLogin());
    }

    @DisplayName("Should register new player when input data is correct")
    @Test
    void shouldRegisterNewPlayer() throws Exception {
        UnsecuredPlayerCreationRequestDto unsecuredPlayerCreationRequestDto =
                new UnsecuredPlayerCreationRequestDto("admin", "password", "admin");

        Player admin = Player.builder()
                .username(unsecuredPlayerCreationRequestDto.getUsername())
                .login(unsecuredPlayerCreationRequestDto.getLogin())
                .password(messageDigest.digest(unsecuredPlayerCreationRequestDto.getPassword().getBytes()))
                .balance(BigDecimal.ZERO)
                .build();

        when(mockPlayerCrudRepository.create(any()))
                .thenReturn(admin);

        AuthenticatedPlayerDto dto = playerService.register(unsecuredPlayerCreationRequestDto);

        assertThat(dto.getBalance()).isEqualTo("0");
        assertThat(dto.getLogin()).isEqualTo(admin.getLogin());

        verify(mockPlayerCrudRepository).create(any());
    }

    @DisplayName("Should return correct balance when player exists")
    @Test
    void shouldReturnCorrectBalance() {
        Player admin = Player.builder()
                .username("admin")
                .login("admin")
                .password("admin".getBytes())
                .balance(BigDecimal.TEN)
                .build();

        when(mockPlayerCrudRepository.getById(anyLong()))
                .thenReturn(admin);

        BalanceDto balanceDto = playerService.getBalance(anyLong());
        assertThat(balanceDto.getBalance()).isEqualTo(admin.getBalance());

        verify(mockPlayerCrudRepository).getById(any());
    }

    @DisplayName("Should transfer money when player has enough on their balance")
    @Test
    void shouldTransferMoneyWhenBalanceIsProficient() {
        UUID transactionId = UUID.randomUUID();
        MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(transactionId,
                "admin", "user", BigDecimal.ONE);
        Transaction transaction = new Transaction(transactionId, TransferRequestStatus.PENDING,
                moneyTransferRequest.getMoneyFrom(), moneyTransferRequest.getMoneyTo(),
                moneyTransferRequest.getAmount());
        PlayerTransferMoneyRequestDto playerTransferMoneyRequestDto =
                new PlayerTransferMoneyRequestDto("user", 1.0);

        Player admin = Player.builder()
                .username("admin")
                .login("admin")
                .password("admin".getBytes())
                .balance(BigDecimal.TEN)
                .build();

        Player user = Player.builder()
                .username("user")
                .login("user")
                .password("user".getBytes())
                .balance(BigDecimal.ZERO)
                .build();

        BigDecimal balanceAfterWithdrawal = admin.getBalance().subtract(moneyTransferRequest.getAmount());

        when(mockPlayerCrudRepository.getByLogin("admin"))
                .thenReturn(admin);
        when(mockPlayerCrudRepository.getByLogin("user"))
                .thenReturn(user);

        when(mockTransactionCrudRepository.create(any()))
                .thenReturn(transaction);

        when(mockTransactionCrudRepository.approveTransaction(admin.getLogin(), transactionId))
                .thenAnswer((invocation) -> {
                    transaction.setStatus(TransferRequestStatus.APPROVED);
                    return transaction;
                });
        when(mockPlayerCrudRepository.setBalance(any(), any()))
                .thenAnswer(invocationOnMock -> {
                    Player newAdmin = Player.builder()
                            .username(admin.getUsername())
                            .login(admin.getLogin())
                            .password(admin.getPassword())
                            .balance(admin.getBalance().subtract(moneyTransferRequest.getAmount()))
                            .build();
                    return newAdmin;
                });

        BalanceDto balanceDto = playerService.transferMoneyTo("admin", playerTransferMoneyRequestDto);

        assertThat(balanceDto.getBalance())
                .isEqualTo(balanceAfterWithdrawal);
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.APPROVED);

        verify(mockPlayerCrudRepository, times(3)).getByLogin(any());
        verify(mockTransactionCrudRepository).create(any());
    }

    @DisplayName("Should throw an error when transferring money from deficient account")
    @Test
    void shouldNotTransferMoneyWhenBalanceIsDeficient() {
        UUID transactionId = UUID.randomUUID();
        MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(transactionId,
                "admin", "user", BigDecimal.ONE);
        Transaction transaction = new Transaction(transactionId, TransferRequestStatus.PENDING,
                moneyTransferRequest.getMoneyFrom(), moneyTransferRequest.getMoneyTo(),
                moneyTransferRequest.getAmount());
        PlayerTransferMoneyRequestDto playerTransferMoneyRequestDto
                = new PlayerTransferMoneyRequestDto("user", 1.0);

        Player admin = Player.builder()
                .username("admin")
                .login("admin")
                .password("admin".getBytes())
                .balance(BigDecimal.ZERO)
                .build();

        Player user = Player.builder()
                .username("user")
                .login("user")
                .password("user".getBytes())
                .balance(BigDecimal.ZERO)
                .build();

        when(mockPlayerCrudRepository.getByLogin("admin"))
                .thenReturn(admin);
        when(mockPlayerCrudRepository.getByLogin("user"))
                .thenReturn(user);
        when(mockTransactionCrudRepository.create(any()))
                .thenReturn(transaction);

        assertThatThrownBy(() -> playerService.transferMoneyTo(admin.getLogin(), playerTransferMoneyRequestDto))
                .isInstanceOf(DeficientBalanceException.class);
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.FAILED);

        verify(mockPlayerCrudRepository, times(3)).getByLogin(any());
        verify(mockTransactionCrudRepository).create(any());
    }

    @DisplayName("Should get correct response on money request")
    @Test
    void shouldGetResponseWhenAskingMoneyFrom() {
        UUID transactionId = UUID.randomUUID();
        MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(transactionId,
                "admin", "user", BigDecimal.ONE);
        Transaction transaction = new Transaction(transactionId, TransferRequestStatus.PENDING,
                moneyTransferRequest.getMoneyFrom(), moneyTransferRequest.getMoneyTo(),
                moneyTransferRequest.getAmount());
        PlayerRequestMoneyDto playerRequestMoneyDto = new PlayerRequestMoneyDto("admin", 1.0);

        Player admin = Player.builder()
                .username("admin")
                .login("admin")
                .password("admin".getBytes())
                .balance(BigDecimal.ZERO)
                .build();

        Player user = Player.builder()
                .username("user")
                .login("user")
                .password("user".getBytes())
                .balance(BigDecimal.ZERO)
                .build();

        when(mockPlayerCrudRepository.getByLogin("admin"))
                .thenReturn(admin);
        when(mockPlayerCrudRepository.getByLogin("user"))
                .thenReturn(user);
        when(mockTransactionCrudRepository.create(any()))
                .thenReturn(transaction);

        MoneyTransferResponse response = playerService.requestMoneyFrom("user", playerRequestMoneyDto);

        assertThat(response.getRequester().getLogin()).isEqualTo(user.getLogin());
        assertThat(response.getTransactionDto().getAmount()).isEqualTo(moneyTransferRequest.getAmount());

        verify(mockPlayerCrudRepository, times(2)).getByLogin(any());
        verify(mockTransactionCrudRepository).create(any());
    }

    @Test
    @DisplayName("Should approve correct transaction and ignore incorrect transaction when passed as string")
    void shouldApproveCorrectTrannsactionAndIgnoreIncorrectTransactionList() {
        List<String> mixedIdsList = new ArrayList<>();
        mixedIdsList.add("12345-3213123-incorrect");
        mixedIdsList.add(UUID.randomUUID().toString());
        TransactionsListDto transactionsListDto = new TransactionsListDto(mixedIdsList);

        Transaction transaction = new Transaction(UUID.randomUUID(), TransferRequestStatus.PENDING,
                "sender", "recipient", BigDecimal.ONE);
        when(mockTransactionCrudRepository.approveTransaction(any(), any()))
                .thenReturn(transaction);

        Player sender = new Player(1L, "username", "login", "password".getBytes(), BigDecimal.ONE);
        Player recipient = new Player(2L, "username", "login", "password".getBytes(), BigDecimal.ONE);

        when(mockPlayerCrudRepository.getByLogin(any()))
                .thenReturn(sender);
        when(mockPlayerCrudRepository.setBalance(any(), any()))
                .thenReturn(recipient);

        Collection<MoneyTransferResponse> response
                = playerService.approvePendingMoneyRequest("donor", transactionsListDto);

        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should decline correct transaction and ignore incorrect transaction when passed as string")
    void shouldDeclineCorrectTrannsactionAndIgnoreIncorrectTransactionList() {
        List<String> mixedIdsList = new ArrayList<>();
        mixedIdsList.add("12345-3213123-incorrect");
        mixedIdsList.add(UUID.randomUUID().toString());
        mixedIdsList.add(UUID.randomUUID().toString());
        TransactionsListDto transactionsListDto = new TransactionsListDto(mixedIdsList);

        Transaction declinedTransaction = new Transaction(null, null, null, null, null);
        when(mockTransactionCrudRepository.declineTransaction(any(), any()))
                .thenReturn(declinedTransaction);

        playerService.declinePendingRequest("donor", transactionsListDto);

        verify(mockTransactionCrudRepository, times(mixedIdsList.size() - 1))
                .declineTransaction(any(), any());
    }

    @Test
    @DisplayName("Should return history of Debit and Credit when Player action is null")
    void shouldReturnHistoryOfBothTypesOfTransactionsWhenPLayerActionIsNull() {
        Transaction debitingTransaction = new Transaction(UUID.randomUUID(), TransferRequestStatus.PENDING,
                "admin", "user", BigDecimal.TEN);
        Transaction creditingTransaction = new Transaction(UUID.randomUUID(), TransferRequestStatus.PENDING,
                "user", "admin", BigDecimal.TEN);

        when(mockTransactionCrudRepository
                .getTransactionsBySenderAndRecipientAndStatus("admin", null, null))
                .thenReturn(new ArrayList<>(List.of(debitingTransaction)));
        when(mockTransactionCrudRepository
                .getTransactionsBySenderAndRecipientAndStatus(null, "admin", null))
                .thenReturn(new ArrayList<>(List.of(creditingTransaction)));

        Collection<TransactionDto> response = playerService.getHistory("admin", null);
        assertThat(response.size()).isEqualTo(2);
    }
}