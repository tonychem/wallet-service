package servlet;

import application.model.dto.AuthenticationDto;
import application.model.dto.AuthenticationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import exception.BadCredentialsException;
import in.dto.UnsecuredAuthenticationRequestDto;
import in.mapper.AuthenticationRequestMapper;
import in.servlet.AuthenticationServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import util.JwtUtils;
import utils.ResponseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("Authentication Servlet Test")
public class AuthenticationServletTest extends AbstractServletTest {
    private static String url;
    private AuthenticationServlet authenticationServlet;

    @Test
    @DisplayName("Should let user authorize when data is correct")
    public void shouldAuthorizeWhenUserIsCorrect() throws BadCredentialsException, JsonProcessingException {
        UnsecuredAuthenticationRequestDto unsecuredAuthenticationRequestDto
                = new UnsecuredAuthenticationRequestDto("admin", "admin");
        AuthenticationRequest request =
                AuthenticationRequestMapper.INSTANCE.toAuthenticationRequest(unsecuredAuthenticationRequestDto);
        when(mockApplicationController.authenticate(request))
                .thenReturn(new AuthenticationDto(1L, unsecuredAuthenticationRequestDto.getLogin(), "admin",
                        UUID.randomUUID(), BigDecimal.ONE));

        ResponseDto serverResponse =
                httpClient.postRequest(url,
                        mapper.writeValueAsString(unsecuredAuthenticationRequestDto), null);

        assertThat(serverResponse.statusCode()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(serverResponse.headers()).isNotEmpty();

        List<String> authorizationTokenHeader = serverResponse.headers().get("Authorization");
        assertThat(authorizationTokenHeader).isNotEmpty();
        assertThat(authorizationTokenHeader.get(0).startsWith("Bearer")).isTrue();

        String token = authorizationTokenHeader.get(0).substring(7);
        assertDoesNotThrow(() -> JwtUtils.extractClaim(token, claims -> claims.get("login")));

        verify(mockApplicationController).authenticate(request);
    }

    @DisplayName("Should not let user authorize when input data does not correspond to constraints")
    @Test
    public void shouldNotAuthorizeWhenValidationFails() throws JsonProcessingException {
        UnsecuredAuthenticationRequestDto unsecuredAuthenticationRequestDto
                = new UnsecuredAuthenticationRequestDto("", "");
        AuthenticationRequest request =
                AuthenticationRequestMapper.INSTANCE.toAuthenticationRequest(unsecuredAuthenticationRequestDto);

        ResponseDto serverResponse =
                httpClient.postRequest(url,
                        mapper.writeValueAsString(unsecuredAuthenticationRequestDto), null);

        assertThat(serverResponse.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        assertThat(serverResponse.headers()).isNotEmpty();

        List<String> authorizationTokenHeader = serverResponse.headers().get("Authorization");
        assertThat(authorizationTokenHeader).isNull();
    }

    @BeforeAll
    public static void initiateResourceUrl() {
        url = tomcatRunner.getApplicationUrl() + extractServletResourceUrl(AuthenticationServlet.class);
    }

    @BeforeEach
    public void refreshMockApplicationController() {
        authenticationServlet = new AuthenticationServlet(mockApplicationController);
        tomcatRunner.registerServlet(authenticationServlet);
    }

    @AfterEach
    public void refreshTomcatContext() {
        tomcatRunner.removeServlet(authenticationServlet);
    }
}
