package ru.yandex.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.wallet.config.JwtTokenFilter;
import ru.yandex.wallet.exception.GlobalExceptionHandler;
import ru.yandex.wallet.in.controller.LogoutController;
import ru.yandex.wallet.service.PlayerSessionService;
import ru.yandex.wallet.util.JwtUtils;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
public class LogoutControllerTest {
    private MockMvc mvc;
    private PlayerSessionService mockPlayerSessionService;

    @Value("${jwt.secret}")
    private String secret;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        mockPlayerSessionService = Mockito.mock(PlayerSessionService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new LogoutController(mockPlayerSessionService))
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
    public void test() throws Exception {
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