package servlet;

import application.model.dto.BalanceDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import exception.UnauthorizedOperationException;
import in.dto.PlayerTransferMoneyRequestDto;
import in.servlet.MoneyTransferServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import util.JwtUtils;
import utils.ResponseDto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Money Transfer Servlet Test")
public class MoneyTransferServletTest extends AbstractServletTest {
    private static String url;
    private MoneyTransferServlet moneyTransferServlet;

    @DisplayName("Fails when passed token is invalid")
    @Test
    public void shouldFailWhenTokenIsInvalid() {
        String invalidToken = "some-invalid-token";
        ResponseDto response =
                httpClient.postRequest(url, "some-body", Map.of("Authorization", "Bearer " + invalidToken));
        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @DisplayName("Should transfer money when player data is valid")
    @Test
    public void shouldTransferMoneyWhenPlayerDataIsValid() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("login", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        PlayerTransferMoneyRequestDto requestDto = new PlayerTransferMoneyRequestDto("user", 1.0);

        BalanceDto expectedBalanceResponse = new BalanceDto(1L, "admin", BigDecimal.ONE);
        when(mockApplicationController.transferMoney(any(), any(), any(), any(), any()))
                .thenReturn(expectedBalanceResponse);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        String requestBody = mapper.writeValueAsString(requestDto);
        ResponseDto response = httpClient.postRequest(url, requestBody, headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_OK);

        BalanceDto acquiredBalanceDto = mapper.readValue(response.responseBody(), BalanceDto.class);

        assertThat(acquiredBalanceDto.getBalance()).isEqualTo(expectedBalanceResponse.getBalance());
        assertThat(acquiredBalanceDto.getUsername()).isEqualTo(expectedBalanceResponse.getUsername());

        verify(mockApplicationController).transferMoney(any(), any(), any(), any(), any());
    }

    @DisplayName("Should fail when player data is incomplete")
    @Test
    public void shouldFailWhenPlayerDataIsNotComplete() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("login", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        PlayerTransferMoneyRequestDto requestDto = new PlayerTransferMoneyRequestDto("user", null);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        String requestBody = mapper.writeValueAsString(requestDto);
        ResponseDto response = httpClient.postRequest(url, requestBody, headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @BeforeAll
    public static void initiateResourceUrl() {
        url = tomcatRunner.getApplicationUrl() + extractServletResourceUrl(MoneyTransferServlet.class);
    }

    @BeforeEach
    public void refreshMockApplicationController() {
        moneyTransferServlet = new MoneyTransferServlet(mockApplicationController);
        tomcatRunner.registerServlet(moneyTransferServlet);
    }

    @AfterEach
    public void refreshTomcatContext() {
        tomcatRunner.removeServlet(moneyTransferServlet);
    }
}
