package servlet;

import application.model.dto.AuthenticationDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import exception.BadCredentialsException;
import in.dto.UnsecuredPlayerRequestDto;
import in.servlet.RegistrationServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import utils.ResponseDto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Registration Servlet Test")
public class RegistrationServletTest extends AbstractServletTest {
    private static String url;
    private RegistrationServlet registrationServlet;

    @DisplayName("Should register player when credentials are correct")
    @Test
    public void shouldRegisterUserWhenCredentialsAreNotBlank() throws JsonProcessingException, BadCredentialsException {
        UnsecuredPlayerRequestDto unsecuredPlayerRequestDto =
                new UnsecuredPlayerRequestDto("login", "password", "username");
        AuthenticationDto expectedResponse = new AuthenticationDto(1L, unsecuredPlayerRequestDto.getLogin(),
                unsecuredPlayerRequestDto.getUsername(), UUID.randomUUID(), BigDecimal.ONE);

        when(mockApplicationController.registerUser(any()))
                .thenReturn(expectedResponse);

        String requestBody = mapper.writeValueAsString(unsecuredPlayerRequestDto);
        ResponseDto response = httpClient.postRequest(url, requestBody, Collections.emptyMap());

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_OK);

        AuthenticationDto responseAuthenticationDto = mapper.readValue(response.responseBody(), AuthenticationDto.class);
        assertThat(responseAuthenticationDto.getLogin()).isEqualTo(expectedResponse.getLogin());
        assertThat(responseAuthenticationDto.getUsername()).isEqualTo(expectedResponse.getUsername());

        verify(mockApplicationController).registerUser(any());
    }

    @DisplayName("Should fail when passing empty credentials")
    @Test
    public void shouldFailWhenCredentialsAreBlank() throws JsonProcessingException, BadCredentialsException {
        UnsecuredPlayerRequestDto unsecuredPlayerRequestDto =
                new UnsecuredPlayerRequestDto("", "", "");

        String requestBody = mapper.writeValueAsString(unsecuredPlayerRequestDto);
        ResponseDto response = httpClient.postRequest(url, requestBody, Collections.emptyMap());

        assertThat(response.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);

        verify(mockApplicationController, never()).registerUser(any());
    }


    @BeforeAll
    public static void initiateResourceUrl() {
        url = tomcatRunner.getApplicationUrl() + extractServletResourceUrl(RegistrationServlet.class);
    }

    @BeforeEach
    public void refreshMockApplicationController() {
        registrationServlet = new RegistrationServlet(mockApplicationController);
        tomcatRunner.registerServlet(registrationServlet);
    }

    @AfterEach
    public void refreshTomcatContext() {
        tomcatRunner.removeServlet(registrationServlet);
    }
}
