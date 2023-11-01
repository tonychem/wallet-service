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
import ru.tonychem.application.model.dto.AuthenticationDto;
import ru.tonychem.domain.dto.PlayerCreationRequest;
import ru.tonychem.exception.GlobalExceptionHandler;
import ru.tonychem.in.controller.RegistrationController;
import ru.tonychem.in.dto.UnsecuredPlayerCreationRequestDto;
import ru.tonychem.in.mapper.PlayerRequestMapper;

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
    private ApplicationController mockApplicationController;
    private static PlayerRequestMapper playerRequestMapper = PlayerRequestMapper.INSTANCE;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        mockApplicationController = Mockito.mock(ApplicationController.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new RegistrationController(mockApplicationController, playerRequestMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("Should register player when data is valid")
    @Test
    public void shouldRegisterPlayerWhenDataIsCorrect() throws Exception {
        UnsecuredPlayerCreationRequestDto unsecuredPlayerCreationRequestDto
                = new UnsecuredPlayerCreationRequestDto("login", "password", "username");
        PlayerCreationRequest securedPlayerCreationRequest =
                PlayerRequestMapper.INSTANCE.toPlayerCreationRequest(unsecuredPlayerCreationRequestDto);

        AuthenticationDto expectedResponse = new AuthenticationDto(1L, unsecuredPlayerCreationRequestDto.getLogin(),
                unsecuredPlayerCreationRequestDto.getUsername(), UUID.randomUUID(), BigDecimal.ONE);
        when(mockApplicationController.registerUser(securedPlayerCreationRequest))
                .thenReturn(expectedResponse);

        mvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unsecuredPlayerCreationRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedResponse.getId().intValue())))
                .andExpect(jsonPath("$.login", is(expectedResponse.getLogin())))
                .andExpect(jsonPath("$.balance", is(expectedResponse.getBalance().intValue())));

        verify(mockApplicationController).registerUser(any());
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

        verify(mockApplicationController, never()).registerUser(any());
    }
}
