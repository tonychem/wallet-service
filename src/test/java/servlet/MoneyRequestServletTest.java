package servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import domain.dto.MoneyTransferRequest;
import exception.UnauthorizedOperationException;
import in.dto.PlayerRequestMoneyDto;
import in.servlet.MoneyRequestServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import util.JwtUtils;
import utils.ResponseDto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Money Request Servlet Test")
public class MoneyRequestServletTest extends AbstractServletTest {
    private static String url;
    private MoneyRequestServlet moneyRequestServlet;

    @DisplayName("Fails when passed token is invalid")
    @Test
    public void shouldFailWhenTokenIsInvalid() {
        String invalidToken = "some-invalid-token";
        ResponseDto responsePost =
                httpClient.postRequest(url, "some-body", Map.of("Authorization", "Bearer " + invalidToken));
        ResponseDto responseGet =
                httpClient.getRequest(url, Map.of("Authorization", "Bearer " + invalidToken));
        assertThat(responsePost.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @DisplayName("Should return pending money request for current player")
    @Test
    public void shouldRetrievePendingRequestForPlayer() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("login", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        MoneyTransferRequest expectedMoneyTransferRequest = new MoneyTransferRequest(UUID.randomUUID(), "sender",
                "reciever", BigDecimal.ONE);
        Collection<MoneyTransferRequest> expectedMoneyTransferRequestCollection =
                List.of(expectedMoneyTransferRequest);

        when(mockApplicationController.getPendingMoneyRequests(any(), any()))
                .thenReturn(expectedMoneyTransferRequestCollection);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        ResponseDto response = httpClient.getRequest(url, headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_OK);

        Collection<MoneyTransferRequest> acquiredMoneyTransferRequestCollection
                = mapper.readValue(response.responseBody(), Collection.class);

        assertThat(acquiredMoneyTransferRequestCollection.isEmpty()).isFalse();
        verify(mockApplicationController).getPendingMoneyRequests(any(), any());
    }

    @DisplayName("Should post a money request when player data is valid")
    @Test
    public void shouldPostMoneyRequestWhenUserDataIsValid() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("login", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        PlayerRequestMoneyDto playerRequestMoneyDto = new PlayerRequestMoneyDto("admin", 1.0);

        when(mockApplicationController.requestMoneyFrom(any(), any(), any(), any(), any()))
                .thenReturn(true);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        String requestBody = mapper.writeValueAsString(playerRequestMoneyDto);
        ResponseDto response = httpClient.postRequest(url, requestBody, headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_OK);

        verify(mockApplicationController).requestMoneyFrom(any(), any(), any(), any(), any());
    }

    @DisplayName("Should fail when posting money request with invalid player data")
    @Test
    public void shouldFailWhenPostingMoneyRequestWithInvalidPlayerData() throws JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("login", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        PlayerRequestMoneyDto playerRequestMoneyDto = new PlayerRequestMoneyDto(null, null);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        String requestBody = mapper.writeValueAsString(playerRequestMoneyDto);
        ResponseDto response = httpClient.postRequest(url, requestBody, headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @BeforeAll
    public static void initiateResourceUrl() {
        url = tomcatRunner.getApplicationUrl() + extractServletResourceUrl(MoneyRequestServlet.class);
    }

    @BeforeEach
    public void refreshMockApplicationController() {
        moneyRequestServlet = new MoneyRequestServlet(mockApplicationController);
        tomcatRunner.registerServlet(moneyRequestServlet);
    }

    @AfterEach
    public void refreshTomcatContext() {
        tomcatRunner.removeServlet(moneyRequestServlet);
    }
}
