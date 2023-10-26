package servlet;

import application.model.dto.BalanceDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import exception.UnauthorizedOperationException;
import in.servlet.WalletBalanceServlet;
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

@DisplayName("Wallet Balance Servlet Test")
public class WalletBalanceServletTest extends AbstractServletTest {
    private static String url;
    private WalletBalanceServlet walletBalanceServlet;

    @DisplayName("Fails when passed token is invalid")
    @Test
    public void shouldFailWhenTokenIsInvalid() {
        String invalidToken = "some-invalid-token";
        ResponseDto response = httpClient.getRequest(url, Map.of("Authorization", "Bearer " + invalidToken));
        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @DisplayName("Returns balance when token is valid")
    @Test
    public void shouldReturnBalanceWhenTokenIsValid() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("id", 1L, "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        BalanceDto expectedResponse = new BalanceDto(1L, "admin", BigDecimal.ONE);
        when(mockApplicationController.getBalance(any(), any())).thenReturn(expectedResponse);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        ResponseDto response = httpClient.getRequest(url, headers);

        BalanceDto acquiredBalanceDto = mapper.readValue(response.responseBody(), BalanceDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(acquiredBalanceDto.getBalance()).isEqualTo(expectedResponse.getBalance());
        assertThat(acquiredBalanceDto.getUsername()).isEqualTo(expectedResponse.getUsername());

        verify(mockApplicationController).getBalance(any(), any());
    }

    @BeforeAll
    public static void initiateResourceUrl() {
        url = tomcatRunner.getApplicationUrl() + extractServletResourceUrl(WalletBalanceServlet.class);
    }

    @BeforeEach
    public void refreshMockApplicationController() {
        walletBalanceServlet = new WalletBalanceServlet(mockApplicationController);
        tomcatRunner.registerServlet(walletBalanceServlet);
    }

    @AfterEach
    public void refreshTomcatContext() {
        tomcatRunner.removeServlet(walletBalanceServlet);
    }
}
