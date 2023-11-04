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
import ru.tonychem.in.controller.RegistrationController;
import ru.tonychem.in.dto.UnsecuredPlayerCreationRequestDto;
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
public class RegistrationControllerTest {
    private MockMvc mvc;
    private PlayerService mockPlayerService;
    private PlayerSessionService mockPlayerSessionService;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        mockPlayerService = Mockito.mock(PlayerService.class);
        mockPlayerSessionService = Mockito.mock(PlayerSessionService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new RegistrationController(mockPlayerService, mockPlayerSessionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("Should register player when data is valid")
    @Test
    public void shouldRegisterPlayerWhenDataIsCorrect() throws Exception {
        UnsecuredPlayerCreationRequestDto unsecuredPlayerCreationRequestDto
                = new UnsecuredPlayerCreationRequestDto("login", "password", "username");
        UUID expectedSessionId = UUID.randomUUID();
        AuthenticatedPlayerDto expectedBody =
                new AuthenticatedPlayerDto(1L, "login", "username", BigDecimal.ONE);

        when(mockPlayerSessionService.open(any()))
                .thenReturn(expectedSessionId);
        when(mockPlayerService.register(any()))
                .thenReturn(expectedBody);

        mvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unsecuredPlayerCreationRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedBody.getId().intValue())))
                .andExpect(jsonPath("$.login", is(expectedBody.getLogin())))
                .andExpect(jsonPath("$.balance", is(expectedBody.getBalance().intValue())));

        verify(mockPlayerSessionService).open(any());
        verify(mockPlayerService).register(any());
    }

    @DisplayName("Should be bad request when player creation data is invalid")
    @Test
    public void shouldThrowExceptionWhenPlayerDataIsInvalid() throws Exception {
        UnsecuredPlayerCreationRequestDto absentLoginPlayerCreationRequest
                = new UnsecuredPlayerCreationRequestDto(null, "password", "username");

        mvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(absentLoginPlayerCreationRequest)))
                .andExpect(status().isBadRequest());

        verify(mockPlayerService, never()).register(any());
        verify(mockPlayerSessionService, never()).open(any());
    }
}
