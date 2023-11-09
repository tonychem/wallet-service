package ru.yandex.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.wallet.config.ApplicationConfiguration;
import ru.yandex.wallet.in.controller.LogoutController;
import ru.yandex.wallet.service.PlayerSessionService;
import ru.yandex.wallet.util.JwtUtils;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = LogoutController.class)
@ImportAutoConfiguration(ApplicationConfiguration.class)
public class LogoutControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PlayerSessionService mockPlayerSessionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String secret;

    @DisplayName("Should sign out user when auth header is valid")
    @Test
    public void shouldSignOutWhenAuthIsValid() throws Exception {
        String login = "admin";
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> claims = Map.of("login", login,
                "session-id", sessionId,
                "id", 1L,
                "username", login);
        String validToken = JwtUtils.generateToken(secret, claims);
        String header = "Bearer " + validToken;

        doNothing().when(mockPlayerSessionService).close(sessionId);

        mvc.perform(delete("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", header))
                .andExpect(status().isNoContent());

        verify(mockPlayerSessionService).close(sessionId);
    }

    @DisplayName("Should throw forbidden when header is empty or invalid")
    @Test
    public void shouldThrowForbiddenWhenHeaderIsEmpty() throws Exception {
        mvc.perform(delete("/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "some.invalid.authtoken"))
                .andExpect(status().isForbidden());

        verify(mockPlayerSessionService, never()).close(any());
    }
}
