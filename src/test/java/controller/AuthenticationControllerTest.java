package controller;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import configuration.TestConfiguration;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import ru.tonychem.application.ApplicationController;
//import ru.tonychem.application.model.dto.AuthenticationDto;
//import ru.tonychem.application.model.dto.AuthenticationRequest;
//import ru.tonychem.exception.GlobalExceptionHandler;
//import ru.tonychem.in.controller.AuthenticationController;
//import ru.tonychem.in.dto.UnsecuredAuthenticationRequestDto;
//import ru.tonychem.in.mapper.AuthenticationRequestMapper;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ContextConfiguration(classes = TestConfiguration.class)
//@ExtendWith(SpringExtension.class)
//@Disabled
public class AuthenticationControllerTest {
//    private MockMvc mvc;
//    private ApplicationController mockApplicationController;
//    private static AuthenticationRequestMapper authenticationMapper = AuthenticationRequestMapper.INSTANCE;
//    private static ObjectMapper objectMapper = new ObjectMapper();
//
//    @BeforeEach
//    public void init() {
//        mockApplicationController = Mockito.mock(ApplicationController.class);
//        mvc = MockMvcBuilders
//                .standaloneSetup(new AuthenticationController(mockApplicationController, authenticationMapper))
//                .setControllerAdvice(new GlobalExceptionHandler())
//                .build();
//    }
//
//    @DisplayName("Should authenticate player when data is valid")
//    @Test
//    public void shouldAuthenticatePlayerWhenDataIsCorrect() throws Exception {
//        UnsecuredAuthenticationRequestDto unsecuredAuthenticationRequestDto
//                = new UnsecuredAuthenticationRequestDto("login", "password");
//        AuthenticationRequest authenticationRequest = authenticationMapper.toAuthenticationRequest(unsecuredAuthenticationRequestDto);
//
//        UUID expectedSessionId = UUID.randomUUID();
//        AuthenticationDto expectedResponse = new AuthenticationDto(1L, unsecuredAuthenticationRequestDto.getLogin(),
//                "login", expectedSessionId, BigDecimal.ONE);
//        when(mockApplicationController.authenticate(authenticationRequest))
//                .thenReturn(expectedResponse);
//
//        mvc.perform(post("/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(unsecuredAuthenticationRequestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(expectedResponse.getId().intValue())))
//                .andExpect(jsonPath("$.login", is(expectedResponse.getLogin())))
//                .andExpect(jsonPath("$.balance", is(expectedResponse.getBalance().intValue())));
//
//        verify(mockApplicationController).authenticate(any());
//    }
//
//    @DisplayName("Should be bad request when player login data is invalid")
//    @Test
//    public void shouldThrowExceptionWhenPlayerDataIsInvalid() throws Exception {
//        UnsecuredAuthenticationRequestDto absentLogin
//                = new UnsecuredAuthenticationRequestDto(null, "password");
//
//        mvc.perform(post("/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(absentLogin)))
//                .andExpect(status().isBadRequest());
//
//        verify(mockApplicationController, never()).authenticate(any());
//    }
}
