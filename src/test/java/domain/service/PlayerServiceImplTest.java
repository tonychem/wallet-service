package domain.service;

import domain.exception.BadCredentialsException;
import domain.exception.DeficientBalanceException;
import domain.model.Player;
import domain.model.Transaction;
import domain.model.TransferRequestStatus;
import domain.model.dto.AuthenticatedPlayerDto;
import domain.model.dto.MoneyTransferRequest;
import domain.model.dto.MoneyTransferResponse;
import domain.model.dto.PlayerCreationRequest;
import domain.repository.PlayerCrudRepository;
import domain.repository.TransactionCrudRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class PlayerServiceImplTest {

    private PlayerService playerService;
    private PlayerCrudRepository mockPlayerCrudRepository;
    private TransactionCrudRepository mockTransactionCrudRepository;

    @BeforeEach
    public void init() {
        mockPlayerCrudRepository = Mockito.mock(PlayerCrudRepository.class);
        mockTransactionCrudRepository = Mockito.mock(TransactionCrudRepository.class);
        playerService = new PlayerServiceImpl(mockPlayerCrudRepository, mockTransactionCrudRepository);
    }

    @SneakyThrows
    @Test
    void shouldAuthenticateWhenCredentialsAreCorrect() {
        byte[] password = MessageDigest.getInstance("MD5")
                .digest("password".getBytes());
        Player admin = Player.builder()
                .username("admin")
                .login("admin")
                .password(password)
                .build();

        when(mockPlayerCrudRepository.getByLogin("admin"))
                .thenReturn(admin);

        AuthenticatedPlayerDto dto = playerService.authenticate("admin", password);
        assertThat(dto.getUsername()).isEqualTo(admin.getUsername());
        assertThat(dto.getLogin()).isEqualTo(admin.getLogin());

        verify(mockPlayerCrudRepository).getByLogin("admin");
    }

    @SneakyThrows
    @Test
    void shouldThrowExceptionWhenBadCredentials() {
        byte[] password = MessageDigest.getInstance("MD5")
                .digest("password".getBytes());
        Player admin = Player.builder()
                .username("admin")
                .login("admin")
                .password(password)
                .build();

        when(mockPlayerCrudRepository.getByLogin("admin"))
                .thenReturn(admin);

        assertThatThrownBy(() -> playerService.authenticate("admin", "incorrect".getBytes()))
                .isInstanceOf(BadCredentialsException.class);

        verify(mockPlayerCrudRepository).getByLogin("admin");
    }

    @SneakyThrows
    @Test
    void shouldRegisterNewPlayer() {
        PlayerCreationRequest request = new PlayerCreationRequest("admin", "password".getBytes(),
                "admin");
        Player admin = Player.builder()
                .username(request.getUsername())
                .login(request.getLogin())
                .password(request.getPassword())
                .balance(BigDecimal.ZERO)
                .build();

        when(mockPlayerCrudRepository.create(request))
                .thenReturn(admin);

        AuthenticatedPlayerDto dto = playerService.register(request);

        assertThat(dto.getBalance()).isEqualTo("0");
        assertThat(dto.getLogin()).isEqualTo(admin.getLogin());

        verify(mockPlayerCrudRepository).create(any());
    }

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

        AuthenticatedPlayerDto dto = playerService.getBalance(anyLong());
        assertThat(dto.getBalance()).isEqualTo(admin.getBalance());

        verify(mockPlayerCrudRepository).getById(any());
    }

    @Test
    void shouldTransferMoneyWhenBalanceIsProficient() {
        UUID transactionId = UUID.randomUUID();
        MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(transactionId,
                "admin", "user", BigDecimal.ONE);
        Transaction transaction = new Transaction(transactionId, TransferRequestStatus.PENDING,
                moneyTransferRequest.getMoneyFrom(), moneyTransferRequest.getMoneyTo(),
                moneyTransferRequest.getAmount());

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
        when(mockTransactionCrudRepository.create(moneyTransferRequest))
                .thenReturn(transaction);

        MoneyTransferResponse response = playerService.transferMoneyTo(moneyTransferRequest);

        assertThat(response.getRequester().getBalance())
                .isEqualTo(balanceAfterWithdrawal);
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.APPROVED);

        verify(mockPlayerCrudRepository, times(2)).getByLogin(any());
        verify(mockTransactionCrudRepository).create(any());
    }

    @Test
    void shouldTransferMoneyWhenBalanceIsDeficient() {
        UUID transactionId = UUID.randomUUID();
        MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(transactionId,
                "admin", "user", BigDecimal.ONE);
        Transaction transaction = new Transaction(transactionId, TransferRequestStatus.PENDING,
                moneyTransferRequest.getMoneyFrom(), moneyTransferRequest.getMoneyTo(),
                moneyTransferRequest.getAmount());

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
        when(mockTransactionCrudRepository.create(moneyTransferRequest))
                .thenReturn(transaction);

        assertThatThrownBy(() -> playerService.transferMoneyTo(moneyTransferRequest))
                .isInstanceOf(DeficientBalanceException.class);
        assertThat(transaction.getStatus()).isEqualTo(TransferRequestStatus.FAILED);

        verify(mockPlayerCrudRepository, times(2)).getByLogin(any());
        verify(mockTransactionCrudRepository).create(any());
    }

    @Test
    void shouldGetResponseWhenAskingMoneyFrom() {
        UUID transactionId = UUID.randomUUID();
        MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(transactionId,
                "admin", "user", BigDecimal.ONE);
        Transaction transaction = new Transaction(transactionId, TransferRequestStatus.PENDING,
                moneyTransferRequest.getMoneyFrom(), moneyTransferRequest.getMoneyTo(),
                moneyTransferRequest.getAmount());

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
        when(mockTransactionCrudRepository.create(moneyTransferRequest))
                .thenReturn(transaction);

        MoneyTransferResponse response = playerService.requestMoneyFrom(moneyTransferRequest);

        assertThat(response.getRequester().getLogin()).isEqualTo(user.getLogin());
        assertThat(response.getTransactionDto().getAmount()).isEqualTo(moneyTransferRequest.getAmount());

        verify(mockPlayerCrudRepository, times(2)).getByLogin(any());
        verify(mockTransactionCrudRepository).create(any());
    }

    @Test
    void getPendingMoneyRequests() {

    }

    @Test
    void approvePendingMoneyRequest() {
    }


    @Test
    void getHistory() {
    }
}