package ru.yandex.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.out.AuthenticatedPlayerDto;
import model.dto.in.UnsecuredPlayerCreationRequestDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.wallet.config.ApplicationConfiguration;
import ru.yandex.wallet.in.controller.RegistrationController;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@ImportAutoConfiguration(ApplicationConfiguration.class)
public class RegistrationControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PlayerService mockPlayerService;

    @MockBean
    private PlayerSessionService mockPlayerSessionService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @Disabled("Перенести в интеграционные тесты, на уровне mockmvctest валидация не работает")
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
