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
import ru.tonychem.application.model.dto.BalanceDto;
import ru.tonychem.config.JwtTokenFilter;
import ru.tonychem.domain.TransferRequestStatus;
import ru.tonychem.domain.dto.TransactionDto;
import ru.tonychem.exception.GlobalExceptionHandler;
import ru.tonychem.in.controller.WalletController;
import ru.tonychem.service.PlayerAction;
import ru.tonychem.util.JwtUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
public class WalletControllerTest {
    private MockMvc mvc;
    private ApplicationController mockApplicationController;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private String validJwtToken;
    private String invalidJwtToken;

    @BeforeEach
    public void init() {
        mockApplicationController = Mockito.mock(ApplicationController.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new WalletController(mockApplicationController))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new JwtTokenFilter(objectMapper))
                .build();

        validJwtToken = generateValidTestToken();
        invalidJwtToken = "invalid.jwt.token";
    }

    @DisplayName("Should return balance when token is valid")
    @Test
    public void shouldReturnBalanceWhenTokenIsValid() throws Exception {
        BalanceDto expectedBalance = new BalanceDto(1L, "admin", BigDecimal.ONE);

        when(mockApplicationController.getBalance(any(), any()))
                .thenReturn(expectedBalance);

        mvc.perform(get("/player-management/wallet/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedBalance.getId().intValue())))
                .andExpect(jsonPath("$.username", is(expectedBalance.getUsername())))
                .andExpect(jsonPath("$.balance", is(expectedBalance.getBalance().intValue())));

        verify(mockApplicationController).getBalance(any(), any());
    }

    @DisplayName("Should throw forbidden when getting balance when token is invalid")
    @Test
    public void shouldThrowForbiddenWhenGettingBalanceWhenTokenIsInvalid() throws Exception {
        mvc.perform(get("/player-management/wallet/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidJwtToken))
                .andExpect(status().isForbidden());

        verify(mockApplicationController, never()).getBalance(any(), any());
    }

    @DisplayName("Should return a history when token is valid")
    @Test
    public void shouldReturnHistoryWhenTokenIsValid() throws Exception {
        TransactionDto expectedResponse = new TransactionDto(UUID.randomUUID(),
                TransferRequestStatus.PENDING, "sender", "recipient", BigDecimal.ONE);
        Collection<TransactionDto> expectedCollection = new ArrayList<>(List.of(expectedResponse));

        when(mockApplicationController.getHistory(any(), any(), any()))
                .thenReturn(expectedCollection);

        mvc.perform(get("/player-management/wallet/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("action", "CREDIT")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount", is(expectedResponse.getAmount().intValue())))
                .andExpect(jsonPath("$[0].recipient", is(expectedResponse.getRecipient())))
                .andExpect(jsonPath("$.length()", is(1)));

        verify(mockApplicationController).getHistory(any(), any(), any());
    }

    @DisplayName("Should throw forbidden when getting balance when token is invalid")
    @Test
    public void shouldThrowForbiddenWhenGettingHistoryWhenTokenIsInvalid() throws Exception {
        mvc.perform(get("/player-management/wallet/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidJwtToken))
                .andExpect(status().isForbidden());

        verify(mockApplicationController, never()).getHistory(any(), any(), any());
    }

    @DisplayName("Should return history according to transfer action passed in query param")
    @Test
    public void shouldReturnHistoryAccordingToTransactionStatus() throws Exception {
        TransactionDto expectedCreditResponse = new TransactionDto(UUID.randomUUID(),
                TransferRequestStatus.PENDING, "sender", "recipient", BigDecimal.ONE);
        TransactionDto expectedDebitResponse = new TransactionDto(UUID.randomUUID(),
                TransferRequestStatus.PENDING, "recipient", "sender", BigDecimal.TEN);
        UUID sessionId =
                UUID.fromString((String) JwtUtils.extractClaim(validJwtToken, claims -> claims.get("session-id")));
        String login = (String) JwtUtils.extractClaim(validJwtToken, claims -> claims.get("login"));

        when(mockApplicationController.getHistory(login, PlayerAction.CREDIT, sessionId))
                .thenReturn(new ArrayList<>(List.of(expectedCreditResponse)));
        when(mockApplicationController.getHistory(login, PlayerAction.DEBIT, sessionId))
                .thenReturn(new ArrayList<>(List.of(expectedDebitResponse)));

        mvc.perform(get("/player-management/wallet/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("action", "CREDIT")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].amount", is(expectedCreditResponse.getAmount().intValue())))
                .andExpect(jsonPath("$[0].recipient", is(expectedCreditResponse.getRecipient())));

        mvc.perform(get("/player-management/wallet/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("action", "DEBIT")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].amount", is(expectedDebitResponse.getAmount().intValue())))
                .andExpect(jsonPath("$[0].recipient", is(expectedDebitResponse.getRecipient())));

        mvc.perform(get("/player-management/wallet/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].recipient", is(expectedCreditResponse.getRecipient())))
                .andExpect(jsonPath("$[1].recipient", is(expectedDebitResponse.getRecipient())));
    }

    private String generateValidTestToken() {
        Map<String, Object> claims = Map.of("id", 1L,
                "login", "admin",
                "session-id", UUID.randomUUID());

        return JwtUtils.generateToken(claims);
    }
}
