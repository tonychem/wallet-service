package servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import domain.TransferRequestStatus;
import domain.dto.TransactionDto;
import exception.UnauthorizedOperationException;
import in.servlet.WalletHistoryServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import service.PlayerAction;
import util.JwtUtils;
import utils.ResponseDto;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Wallet History Test")
public class WalletHistoryServletTest extends AbstractServletTest {
    private static String url;
    private WalletHistoryServlet walletHistoryServlet;

    @DisplayName("Should return collection of transactions when valid request param is present")
    @Test
    public void shouldReturnDebitHistoryWhenCorrectRequestParamIsPassed() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("login", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        UUID transactionId = UUID.randomUUID();
        TransferRequestStatus status = TransferRequestStatus.PENDING;
        String sender = (String) jwtPayload.get("login");
        String recipient = "user";
        BigDecimal amount = BigDecimal.ONE;
        Collection<TransactionDto> dtos = List.of(new TransactionDto(transactionId, status, sender, recipient, amount));

        when(mockApplicationController.getHistory((String) jwtPayload.get("login"), PlayerAction.CREDIT, sessionId))
                .thenReturn(dtos);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        ResponseDto response = httpClient.getRequest(url + "?action=CREDIT", headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_OK);

        Collection<TransactionDto> acquiredTransactionDtos =
                mapper.validateValue(response.responseBody(), Collection.class);

        assertThat(acquiredTransactionDtos.isEmpty()).isFalse();
        verify(mockApplicationController).getHistory(any(), any(), any());
    }

    @DisplayName("Should fail when invalid request param name is passed")
    @Test
    public void shouldFailWhenInvalidRequestParamIsPassed() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("login", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        ResponseDto response = httpClient.getRequest(url + "?bullshit=CREDIT", headers);
        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @DisplayName("Should fetch both CREDIT and DEBIT when action request param is absent")
    @Test
    public void shouldFetchBothActionsWhenRequestParamIsAbsent() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("login", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        UUID transactionId = UUID.randomUUID();
        TransferRequestStatus status = TransferRequestStatus.PENDING;
        String sender = (String) jwtPayload.get("login");
        String recipient = "user";
        BigDecimal amount = BigDecimal.ONE;
        Collection<TransactionDto> dtos =
                new ArrayList<>(List.of(new TransactionDto(transactionId, status, sender, recipient, amount)));

        when(mockApplicationController.getHistory(any(), any(), any()))
                .thenReturn(dtos);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        ResponseDto response = httpClient.getRequest(url, headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_OK);

        Collection<TransactionDto> acquiredTransactionDtos =
                mapper.validateValue(response.responseBody(), Collection.class);

        assertThat(acquiredTransactionDtos.isEmpty()).isFalse();

        verify(mockApplicationController, times(2)).getHistory(any(), any(), any());
    }

    @DisplayName("Fails when passed token is invalid")
    @Test
    public void shouldFailWhenTokenIsInvalid() {
        String invalidToken = "some-invalid-token";
        ResponseDto response = httpClient.getRequest(url, Map.of("Authorization", "Bearer " + invalidToken));
        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @BeforeAll
    public static void initiateResourceUrl() {
        url = tomcatRunner.getApplicationUrl() + extractServletResourceUrl(WalletHistoryServlet.class);
    }

    @BeforeEach
    public void refreshMockApplicationController() {
        walletHistoryServlet = new WalletHistoryServlet(mockApplicationController);
        tomcatRunner.registerServlet(walletHistoryServlet);
    }

    @AfterEach
    public void refreshTomcatContext() {
        tomcatRunner.removeServlet(walletHistoryServlet);
    }
}
