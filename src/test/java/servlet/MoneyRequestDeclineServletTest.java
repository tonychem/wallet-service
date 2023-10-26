package servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import exception.UnauthorizedOperationException;
import in.dto.TransactionsListDto;
import in.servlet.MoneyRequestDeclineServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import util.JwtUtils;
import utils.ResponseDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Money Request Decline Test")
public class MoneyRequestDeclineServletTest extends AbstractServletTest {
    private static String url;
    private MoneyRequestDeclineServlet moneyRequestDeclineServlet;

    @DisplayName("Fails when passed token is invalid")
    @Test
    public void shouldFailWhenTokenIsInvalid() {
        String invalidToken = "some-invalid-token";
        ResponseDto response =
                httpClient.postRequest(url, "some-body", Map.of("Authorization", "Bearer " + invalidToken));
        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @DisplayName("Should decline a list of pending requests one by one as provided by player")
    @Test
    public void shouldDeclineListOfPendingRequestsOneByOne() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("username", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        List<String> offeredIdsList = List.of(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        TransactionsListDto requestDto = new TransactionsListDto(offeredIdsList);

        doNothing().when(mockApplicationController).declinePendingRequest(any(), anyString(), any());

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        String requestBody = mapper.writeValueAsString(requestDto);
        ResponseDto response = httpClient.postRequest(url, requestBody, headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_OK);

        verify(mockApplicationController, times(offeredIdsList.size()))
                .declinePendingRequest(any(), any(), any());
    }

    @DisplayName("Should decline a list of pending requests one by one with mistakes as provided by player")
    @Test
    public void shouldNotFailWhenSomeUUIDsAreIncorrectInProvidedList() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("username", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        List<String> offeredIdsList = List.of(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "mistake-is-here");
        TransactionsListDto requestDto = new TransactionsListDto(offeredIdsList);

        doNothing().when(mockApplicationController).declinePendingRequest(any(), anyString(), any());

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        String requestBody = mapper.writeValueAsString(requestDto);
        ResponseDto response = httpClient.postRequest(url, requestBody, headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_OK);

        verify(mockApplicationController, times(offeredIdsList.size() - 1))
                .declinePendingRequest(any(), any(), any());
    }

    @DisplayName("Should fail when declining list is empty")
    @Test
    public void shouldFailWhenIdsListIsEmpty() throws UnauthorizedOperationException, JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> jwtPayload = Map.of("username", "admin", "session-id", sessionId);
        String validToken = JwtUtils.generateToken(jwtPayload);

        List<String> offeredIdsList = Collections.EMPTY_LIST;
        TransactionsListDto requestDto = new TransactionsListDto(offeredIdsList);

        doNothing().when(mockApplicationController).declinePendingRequest(any(), anyString(), any());

        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);
        String requestBody = mapper.writeValueAsString(requestDto);
        ResponseDto response = httpClient.postRequest(url, requestBody, headers);

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);

        verify(mockApplicationController, never()).declinePendingRequest(any(), anyString(), any());
    }

    @BeforeAll
    public static void initiateResourceUrl() {
        url = tomcatRunner.getApplicationUrl() + extractServletResourceUrl(MoneyRequestDeclineServlet.class);
    }

    @BeforeEach
    public void refreshMockApplicationController() {
        moneyRequestDeclineServlet = new MoneyRequestDeclineServlet(mockApplicationController);
        tomcatRunner.registerServlet(moneyRequestDeclineServlet);
    }

    @AfterEach
    public void refreshTomcatContext() {
        tomcatRunner.removeServlet(moneyRequestDeclineServlet);
    }
}
