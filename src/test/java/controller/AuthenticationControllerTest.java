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
import ru.tonychem.domain.dto.AuthenticatedPlayerDto;
import ru.tonychem.exception.GlobalExceptionHandler;
import ru.tonychem.in.controller.AuthenticationController;
import ru.tonychem.in.dto.UnsecuredAuthenticationRequestDto;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.PlayerSessionService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
public class AuthenticationControllerTest {
    private MockMvc mvc;
    private PlayerService mockPlayerService;
    private PlayerSessionService mockPlayerSessionService;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        mockPlayerService = Mockito.mock(PlayerService.class);
        mockPlayerSessionService = Mockito.mock(PlayerSessionService.class);

        mvc = MockMvcBuilders
                .standaloneSetup(new AuthenticationController(mockPlayerService, mockPlayerSessionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("Should authenticate player when data is valid")
    @Test
    public void shouldAuthenticatePlayerWhenDataIsCorrect() throws Exception {
        UnsecuredAuthenticationRequestDto unsecuredAuthenticationRequestDto
                = new UnsecuredAuthenticationRequestDto("login", "password");

        UUID expectedSessionId = UUID.randomUUID();
        AuthenticatedPlayerDto expectedBody
                = new AuthenticatedPlayerDto(1L, "login", "username", BigDecimal.ONE);

        when(mockPlayerSessionService.open(any()))
                .thenReturn(expectedSessionId);
        when(mockPlayerService.authenticate(any()))
                .thenReturn(expectedBody);

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unsecuredAuthenticationRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedBody.getId().intValue())))
                .andExpect(jsonPath("$.login", is(expectedBody.getLogin())))
                .andExpect(jsonPath("$.balance", is(expectedBody.getBalance().intValue())));

        verify(mockPlayerSessionService).open(any());
        verify(mockPlayerService).authenticate(any());
    }

    @DisplayName("Should be bad request when player login data is invalid")
    @Test
    public void shouldThrowExceptionWhenPlayerDataIsInvalid() throws Exception {
        UnsecuredAuthenticationRequestDto absentLogin
                = new UnsecuredAuthenticationRequestDto(null, "password");

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(absentLogin)))
                .andExpect(status().isBadRequest());

        verify(mockPlayerService, never()).authenticate(any());
    }
}
