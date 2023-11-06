package ru.yandex.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.out.BalanceDto;
import model.dto.in.PlayerTransferMoneyRequestDto;
import org.junit.jupiter.api.BeforeEach;
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
import ru.yandex.wallet.in.controller.MoneyTransferController;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;
import ru.yandex.wallet.util.JwtUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MoneyTransferController.class)
@ImportAutoConfiguration(ApplicationConfiguration.class)
public class MoneyTransferControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlayerService mockPlayerService;

    @MockBean
    private PlayerSessionService mockPlayerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    private String validJwtToken;
    private String invalidJwtToken;

    @BeforeEach
    public void init() {
        validJwtToken = generateValidTestToken();
        invalidJwtToken = "invalid.jwt.token";
    }

    @DisplayName("Should transfer money when token and request body are valid")
    @Test
    public void shouldTransferMoneyWhenTokenAndRequestAreValid() throws Exception {
        PlayerTransferMoneyRequestDto moneyRequestDto
                = new PlayerTransferMoneyRequestDto("recipient", 1.0);
        BalanceDto expectedResponse = new BalanceDto(1L, "sender", BigDecimal.ONE);

        when(mockPlayerSessionService.exists(any())).thenReturn(true);
        when(mockPlayerService.transferMoneyTo(any(), any()))
                .thenReturn(expectedResponse);

        mvc.perform(post("/player-management/money-transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(moneyRequestDto))
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedResponse.getId().intValue())))
                .andExpect(jsonPath("$.username", is(expectedResponse.getUsername())))
                .andExpect(jsonPath("$.balance", is(expectedResponse.getBalance().intValue())));

        verify(mockPlayerSessionService).exists(any());
        verify(mockPlayerService).transferMoneyTo(any(), any());
    }

    @DisplayName("Should throw forbidden when transferring money when token is invalid")
    @Test
    public void shouldThrowForbiddenWhenTransferringMoneyWhenTokenIsInvalid() throws Exception {
        mvc.perform(post("/player-management/money-transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidJwtToken))
                .andExpect(status().isForbidden());

        verify(mockPlayerService, never()).transferMoneyTo(any(), any());
    }

    private String generateValidTestToken() {
        Map<String, Object> claims = Map.of("id", 1L,
                "login", "admin",
                "username", "admin",
                "session-id", UUID.randomUUID());

        return JwtUtils.generateToken(secret, claims);
    }
}
