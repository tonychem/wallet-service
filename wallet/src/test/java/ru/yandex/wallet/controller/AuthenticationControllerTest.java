package ru.yandex.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.in.UnsecuredAuthenticationRequestDto;
import model.dto.out.AuthenticatedPlayerDto;
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
import ru.yandex.wallet.in.controller.AuthenticationController;
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

@WebMvcTest(AuthenticationController.class)
@ImportAutoConfiguration(ApplicationConfiguration.class)
public class AuthenticationControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PlayerService mockPlayerService;

    @MockBean
    private PlayerSessionService mockPlayerSessionService;

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
    @Disabled("Перенести в интеграционные тесты, на уровне mockmvctest валидация не работает")
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
