package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.tonychem.domain.Player;
import ru.tonychem.domain.Transaction;
import ru.tonychem.domain.TransferRequestStatus;
import ru.tonychem.domain.dto.AuthenticatedPlayerDto;
import ru.tonychem.domain.dto.MoneyTransferRequest;
import ru.tonychem.domain.dto.MoneyTransferResponse;
import ru.tonychem.domain.dto.PlayerCreationRequest;
import ru.tonychem.domain.mapper.PlayerMapper;
import ru.tonychem.domain.mapper.TransactionMapper;
import ru.tonychem.exception.model.BadCredentialsException;
import ru.tonychem.exception.model.DeficientBalanceException;
import ru.tonychem.repository.PlayerCrudRepository;
import ru.tonychem.repository.TransactionCrudRepository;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.PlayerServiceImpl;

import java.math.BigDecimal;
import java.security.MessageDigest;
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

    private static PlayerMapper playerMapper = PlayerMapper.INSTANCE;
    private static TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    @BeforeEach
    public void init() {
        mockPlayerCrudRepository = Mockito.mock(PlayerCrudRepository.class);
        mockTransactionCrudRepository = Mockito.mock(TransactionCrudRepository.class);
        playerService = new PlayerServiceImpl(mockPlayerCrudRepository, mockTransactionCrudRepository, playerMapper,
                transactionMapper);
    }

    @DisplayName("Authenticates player when credentials are correct")
    @Test
    void shouldAuthenticateWhenCredentialsAreCorrect() throws Exception {
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

    @DisplayName("Throw error when player offers bad credentials")
    @Test
    void shouldThrowExceptionWhenBadCredentials() throws Exception {
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

    @DisplayName("Should register new player when input data is correct")
    @Test
    void shouldRegisterNewPlayer() throws Exception {
        Player newPlayer = Player.builder()
                .login("admin")
                .password("password".getBytes())
                .username("admin")
                .build();

        Player admin = Player.builder()
                .username(newPlayer.getUsername())
                .login(newPlayer.getLogin())
                .password(newPlayer.getPassword())
                .balance(BigDecimal.ZERO)
                .build();

        PlayerCreationRequest request = new PlayerCreationRequest(newPlayer.getLogin(), newPlayer.getPassword(),
                newPlayer.getUsername());

        when(mockPlayerCrudRepository.create(newPlayer))
                .thenReturn(admin);

        AuthenticatedPlayerDto dto = playerService.register(request);

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

        AuthenticatedPlayerDto dto = playerService.getBalance(anyLong());
        assertThat(dto.getBalance()).isEqualTo(admin.getBalance());

        verify(mockPlayerCrudRepository).getById(any());
    }

    @DisplayName("should transfer money when player has enough on their balance")
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
        when(mockTransactionCrudRepository.approveTransaction(admin.getLogin(), transactionId))
                .thenAnswer((invocation) -> {
                    transaction.setStatus(TransferRequestStatus.APPROVED);
                    return transaction;
                });
        when(mockTransactionCrudRepository.create(moneyTransferRequest))
                .thenReturn(transaction);
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

        MoneyTransferResponse response = playerService.transferMoneyTo(moneyTransferRequest);

        assertThat(response.getRequester().getBalance())
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
}