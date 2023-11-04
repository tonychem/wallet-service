package ru.yandex.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.wallet.config.JwtTokenFilter;
import ru.yandex.wallet.domain.dto.MoneyTransferRequest;
import ru.yandex.wallet.domain.dto.MoneyTransferResponse;
import ru.yandex.wallet.exception.GlobalExceptionHandler;
import ru.yandex.wallet.in.controller.MoneyRequestController;
import ru.yandex.wallet.in.dto.PlayerRequestMoneyDto;
import ru.yandex.wallet.in.dto.TransactionsListDto;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;
import ru.yandex.wallet.util.JwtUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
public class MoneyRequestControllerTest {
    private MockMvc mvc;
    private PlayerService mockPlayerService;
    private PlayerSessionService mockPlayerSessionService;

    @Value("${jwt.secret}")
    private String secret;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private String validJwtToken;
    private String invalidJwtToken;

    @BeforeEach
    public void init() {
        mockPlayerService = Mockito.mock(PlayerService.class);
        mockPlayerSessionService = Mockito.mock(PlayerSessionService.class);

        mvc = MockMvcBuilders
                .standaloneSetup(new MoneyRequestController(mockPlayerService, mockPlayerSessionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new JwtTokenFilter(objectMapper))
                .build();

        validJwtToken = generateValidTestToken();
        invalidJwtToken = "invalid.jwt.token";
    }

    @DisplayName("Should return collection of pending money requests when token is valid")
    @Test
    public void shouldReturnCollectionOfPendingMoneyRequestsWhenTokenIsValid() throws Exception {
        MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(UUID.randomUUID(),
                "sender", "recipient", BigDecimal.ONE);
        Collection<MoneyTransferRequest> expectedCollectionResponse = List.of(moneyTransferRequest);

        when(mockPlayerSessionService.exists(any()))
                .thenReturn(true);
        when(mockPlayerService.getPendingMoneyRequests(any()))
                .thenReturn(expectedCollectionResponse);

        mvc.perform(get("/player-management/money-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(moneyTransferRequest.getId().toString())))
                .andExpect(jsonPath("$[0].moneyFrom", is(moneyTransferRequest.getMoneyFrom())))
                .andExpect(jsonPath("$[0].moneyTo", is(moneyTransferRequest.getMoneyTo())));

        verify(mockPlayerService).getPendingMoneyRequests(any());
        verify(mockPlayerSessionService).exists(any());
    }

    @DisplayName("Should post money request when token is valid")
    @Test
    public void shouldPostMoneyRequestTokenIsValid() throws Exception {
        PlayerRequestMoneyDto playerRequestMoneyDto = new PlayerRequestMoneyDto("donor", 1.0);

        when(mockPlayerSessionService.exists(any()))
                .thenReturn(true);
        when(mockPlayerService.requestMoneyFrom(any(), any()))
                .thenReturn(any());

        mvc.perform(post("/player-management/money-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(playerRequestMoneyDto))
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk());

        verify(mockPlayerService).requestMoneyFrom(any(), any());
    }

    @DisplayName("Should throw forbidden when fetching pending money requests when token is invalid")
    @Test
    public void shouldThrowForbiddenWhenFetchingPendingMoneyRequestsWhenTokenIsInvalid() throws Exception {
        mvc.perform(get("/player-management/money-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidJwtToken))
                .andExpect(status().isForbidden());

        verify(mockPlayerSessionService, never()).exists(any());
        verify(mockPlayerService, never()).getPendingMoneyRequests(any());
    }

    @DisplayName("Should throw forbidden when requesting money when token is invalid")
    @Test
    public void shouldThrowForbiddenWhenRequestingMoneyWhenTokenIsInvalid() throws Exception {
        mvc.perform(post("/player-management/money-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidJwtToken))
                .andExpect(status().isForbidden());

        verify(mockPlayerService, never()).requestMoneyFrom(any(), any());
    }

    @DisplayName("Should throw forbidden when accepting money request when token is invalid")
    @Test
    public void shouldThrowForbiddenWhenAcceptingMoneyRequestWhenTokenIsInvalid() throws Exception {
        mvc.perform(post("/player-management/money-request/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidJwtToken))
                .andExpect(status().isForbidden());

        verify(mockPlayerService, never()).approvePendingMoneyRequest(any(), any());
    }

    @DisplayName("Should throw forbidden when declining money request when token is invalid")
    @Test
    public void shouldThrowForbiddenWhenDecliningMoneyRequestWhenTokenIsInvalid() throws Exception {
        mvc.perform(post("/player-management/money-request/decline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidJwtToken))
                .andExpect(status().isForbidden());

        verify(mockPlayerService, never()).declinePendingRequest(any(), any());
    }

    @Test
    @DisplayName("Should approve transactions when token is valid")
    public void shouldApproveTransactionsWhenTokenIsValid() throws Exception {
        List<String> stringIds = List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        TransactionsListDto transactionsListDto = new TransactionsListDto(stringIds);

        when(mockPlayerSessionService.exists(any()))
                .thenReturn(true);
        when(mockPlayerService.approvePendingMoneyRequest(any(), any()))
                .thenReturn(List.of(new MoneyTransferResponse(null, null)));


        mvc.perform(post("/player-management/money-request/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(transactionsListDto))
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk());

        verify(mockPlayerSessionService).exists(any());
        verify(mockPlayerService).approvePendingMoneyRequest(any(), any());
    }

    @Test
    @DisplayName("Should decline transactions when token is valid")
    public void shouldDeclineTransactionsWhenTokenIsValid() throws Exception {
        List<String> stringIds = List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        TransactionsListDto transactionsListDto = new TransactionsListDto(stringIds);

        when(mockPlayerSessionService.exists(any()))
                .thenReturn(true);
        doNothing().when(mockPlayerService).declinePendingRequest(any(), any());

        mvc.perform(post("/player-management/money-request/decline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(transactionsListDto))
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk());

        verify(mockPlayerSessionService).exists(any());
        verify(mockPlayerService).declinePendingRequest(any(), any());
    }

    private String generateValidTestToken() {
        Map<String, Object> claims = Map.of("id", 1L,
                "login", "admin",
                "username", "username",
                "session-id", UUID.randomUUID());

        return JwtUtils.generateToken(secret, claims);
    }
}
