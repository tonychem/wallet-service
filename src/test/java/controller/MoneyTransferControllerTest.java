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
import ru.tonychem.config.JwtTokenFilter;
import ru.tonychem.domain.dto.BalanceDto;
import ru.tonychem.exception.GlobalExceptionHandler;
import ru.tonychem.in.controller.MoneyTransferController;
import ru.tonychem.in.dto.PlayerTransferMoneyRequestDto;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.PlayerSessionService;
import ru.tonychem.util.JwtUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
public class MoneyTransferControllerTest {
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
                .standaloneSetup(new MoneyTransferController(mockPlayerService, mockPlayerSessionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new JwtTokenFilter(objectMapper))
                .build();

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

        return JwtUtils.generateToken(claims);
    }
}
