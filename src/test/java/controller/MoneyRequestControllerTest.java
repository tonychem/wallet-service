package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.tonychem.config.JwtTokenFilter;
import ru.tonychem.domain.dto.MoneyTransferRequest;
import ru.tonychem.exception.GlobalExceptionHandler;
import ru.tonychem.in.controller.MoneyRequestController;
import ru.tonychem.in.dto.PlayerRequestMoneyDto;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.PlayerSessionService;
import ru.tonychem.util.JwtUtils;

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

@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
@Disabled
public class MoneyRequestControllerTest {
    private MockMvc mvc;
    private PlayerService mockPlayerService;
    private PlayerSessionService mockPlayerSessionService;
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

    private String generateValidTestToken() {
        Map<String, Object> claims = Map.of("id", 1L,
                "login", "admin",
                "username", "username",
                "session-id", UUID.randomUUID());

        return JwtUtils.generateToken(claims);
    }
}
