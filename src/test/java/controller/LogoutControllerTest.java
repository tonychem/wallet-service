package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.tonychem.application.ApplicationController;
import ru.tonychem.config.JwtTokenFilter;
import ru.tonychem.exception.GlobalExceptionHandler;
import ru.tonychem.in.controller.LogoutController;
import ru.tonychem.util.JwtUtils;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
public class LogoutControllerTest {
    private MockMvc mvc;
    private ApplicationController mockApplicationController;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        mockApplicationController = Mockito.mock(ApplicationController.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new LogoutController(mockApplicationController))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new JwtTokenFilter(objectMapper))
                .build();
    }

    @DisplayName("Should sign out user when auth header is valid")
    @Test
    public void shouldSignOutWhenAuthIsValid() throws Exception {
        String login = "admin";
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> claims = Map.of("login", login,
                "session-id", sessionId);
        String validToken = JwtUtils.generateToken(claims);
        String header = "Bearer " + validToken;

        doNothing().when(mockApplicationController).signOut(login, sessionId);

        mvc.perform(delete("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", header))
                .andExpect(status().isNoContent());

        verify(mockApplicationController).signOut(any(), any());
    }

    @DisplayName("Should throw forbidden when header is empty or invalid")
    @Test
    public void test() throws Exception {
        mvc.perform(delete("/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "some.invalid.authtoken"))
                .andExpect(status().isForbidden());

        verify(mockApplicationController, never()).signOut(any(), any());
    }
}
