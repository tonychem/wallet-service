package ru.yandex.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.out.BalanceDto;
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
import ru.yandex.wallet.domain.TransferRequestStatus;
import ru.yandex.wallet.domain.dto.TransactionDto;
import ru.yandex.wallet.in.controller.WalletController;
import ru.yandex.wallet.service.PlayerAction;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;
import ru.yandex.wallet.util.JwtUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@ImportAutoConfiguration(ApplicationConfiguration.class)
public class WalletControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PlayerService mockPlayerService;

    @MockBean
    private PlayerSessionService mockPlayerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private ObjectMapper objectMapper;

    private String validJwtToken;

    private String invalidJwtToken;

    @BeforeEach
    public void init() {
        validJwtToken = generateValidTestToken();
        invalidJwtToken = "invalid.jwt.token";
    }

    @DisplayName("Should return balance when token is valid")
    @Test
    public void shouldReturnBalanceWhenTokenIsValid() throws Exception {
        BalanceDto expectedBody = new BalanceDto(1L, "admin", BigDecimal.ONE);

        when(mockPlayerSessionService.exists(any()))
                .thenReturn(true);
        when(mockPlayerService.getBalance(any()))
                .thenReturn(expectedBody);

        mvc.perform(get("/player-management/wallet/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedBody.getId().intValue())))
                .andExpect(jsonPath("$.username", is(expectedBody.getUsername())))
                .andExpect(jsonPath("$.balance", is(expectedBody.getBalance().intValue())));

        verify(mockPlayerSessionService).exists(any());
        verify(mockPlayerService).getBalance(any());
    }

    @DisplayName("Should throw forbidden when getting balance when token is invalid")
    @Test
    public void shouldThrowForbiddenWhenGettingBalanceWhenTokenIsInvalid() throws Exception {
        mvc.perform(get("/player-management/wallet/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidJwtToken))
                .andExpect(status().isForbidden());

        verify(mockPlayerService, never()).getBalance(any());
        verify(mockPlayerSessionService, never()).exists(any());
    }

    @DisplayName("Should return a history when token is valid")
    @Test
    public void shouldReturnHistoryWhenTokenIsValid() throws Exception {
        TransactionDto expectedTransaction = new TransactionDto(UUID.randomUUID(),
                TransferRequestStatus.PENDING, "sender", "recipient", BigDecimal.ONE);
        Collection<TransactionDto> expectedCollection = new ArrayList<>(List.of(expectedTransaction));

        when(mockPlayerSessionService.exists(any()))
                .thenReturn(true);
        when(mockPlayerService.getHistory(any(), any()))
                .thenReturn(expectedCollection);

        mvc.perform(get("/player-management/wallet/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("action", "CREDIT")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount", is(expectedTransaction.getAmount().intValue())))
                .andExpect(jsonPath("$[0].recipient", is(expectedTransaction.getRecipient())))
                .andExpect(jsonPath("$.length()", is(1)));

        verify(mockPlayerSessionService).exists(any());
        verify(mockPlayerService).getHistory(any(), any());
    }

    @DisplayName("Should throw forbidden when getting balance when token is invalid")
    @Test
    public void shouldThrowForbiddenWhenGettingHistoryWhenTokenIsInvalid() throws Exception {
        mvc.perform(get("/player-management/wallet/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidJwtToken))
                .andExpect(status().isForbidden());

        verify(mockPlayerService, never()).getHistory(any(), any());
        verify(mockPlayerSessionService, never()).exists(any());
    }

    @DisplayName("Should return history according to transfer action passed in query param")
    @Test
    public void shouldReturnHistoryAccordingToTransactionStatus() throws Exception {
        TransactionDto expectedDebitResponse = new TransactionDto(UUID.randomUUID(),
                TransferRequestStatus.PENDING, "recipient", "sender", BigDecimal.TEN);
        String login = (String) JwtUtils.extractClaim(validJwtToken, claims -> claims.get("login"), secret);

        when(mockPlayerSessionService.exists(any()))
                .thenReturn(true);
        when(mockPlayerService.getHistory(login, PlayerAction.DEBIT))
                .thenReturn(new ArrayList<>(List.of(expectedDebitResponse)));


        mvc.perform(get("/player-management/wallet/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("action", "DEBIT")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].amount", is(expectedDebitResponse.getAmount().intValue())))
                .andExpect(jsonPath("$[0].recipient", is(expectedDebitResponse.getRecipient())));

        verify(mockPlayerService).getHistory(any(), any());
        verify(mockPlayerSessionService).exists(any());
    }

    @DisplayName("Should return full history (debit + credit) when query param is missing")
    @Test
    public void shouldReturnHistoryWhenActionQueryParamIsMissing() throws Exception {
        TransactionDto expectedCreditResponse = new TransactionDto(UUID.randomUUID(),
                TransferRequestStatus.PENDING, "sender", "recipient", BigDecimal.ONE);
        TransactionDto expectedDebitResponse = new TransactionDto(UUID.randomUUID(),
                TransferRequestStatus.PENDING, "recipient", "sender", BigDecimal.TEN);
        String login = (String) JwtUtils.extractClaim(validJwtToken, claims -> claims.get("login"), secret);

        when(mockPlayerSessionService.exists(any()))
                .thenReturn(true);
        when(mockPlayerService.getHistory(login, null))
                .thenReturn(List.of(expectedCreditResponse, expectedDebitResponse));

        mvc.perform(get("/player-management/wallet/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].recipient", is(expectedCreditResponse.getRecipient())))
                .andExpect(jsonPath("$[1].recipient", is(expectedDebitResponse.getRecipient())));

        verify(mockPlayerService).getHistory(any(), any());
        verify(mockPlayerSessionService).exists(any());
    }

    private String generateValidTestToken() {
        Map<String, Object> claims = Map.of("id", 1L,
                "login", "admin",
                "username", "username",
                "session-id", UUID.randomUUID());

        return JwtUtils.generateToken(secret, claims);
    }
}
