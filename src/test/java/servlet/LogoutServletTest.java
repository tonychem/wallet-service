package servlet;

import in.servlet.LogoutServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import util.JwtUtils;
import utils.ResponseDto;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Logout Servlet Test")
public class LogoutServletTest extends AbstractServletTest {
    private static String url;
    private LogoutServlet logoutServlet;

    @DisplayName("Should let user delete authorization when JWT is correct")
    @Test
    public void shouldDeleteAuthorizationWhenTokenIsCorrect() {
        UUID sessionId = UUID.randomUUID();
        String validToken = JwtUtils.generateToken(Map.of("login", "admin", "session-id", sessionId.toString()));
        doNothing().when(mockApplicationController).signOut("admin", sessionId);
        Map<String, String> headers = Map.of("Authorization", "Bearer " + validToken);

        ResponseDto responseDto = httpClient.deleteRequest(url, headers);
        assertThat(responseDto.statusCode()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
        verify(mockApplicationController).signOut(any(), any());
    }

    @DisplayName("Should not let user delete authorization when JWT is incorrect")
    @Test
    public void shouldNotDeleteAuthorizationWhenTokenIsInCorrect() {
        UUID sessionId = UUID.randomUUID();
        String invalidToken = "Invalid token";
        doNothing().when(mockApplicationController).signOut("admin", sessionId);
        Map<String, String> headers = Map.of("Authorization", "Bearer " + invalidToken);

        ResponseDto responseDto = httpClient.deleteRequest(url, headers);
        assertThat(responseDto.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @BeforeAll
    public static void initiateResourceUrl() {
        url = tomcatRunner.getApplicationUrl() + extractServletResourceUrl(LogoutServlet.class);
    }

    @BeforeEach
    public void refreshMockApplicationController() {
        logoutServlet = new LogoutServlet(mockApplicationController);
        tomcatRunner.registerServlet(logoutServlet);
    }

    @AfterEach
    public void refreshTomcatContext() {
        tomcatRunner.removeServlet(logoutServlet);
    }
}
