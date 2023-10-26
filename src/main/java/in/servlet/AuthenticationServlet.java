package in.servlet;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import application.model.dto.AuthenticationDto;
import application.model.dto.AuthenticationRequest;
import config.ValidatingObjectMapper;
import exception.BadCredentialsException;
import exception.dto.ExceptionDto;
import in.dto.UnsecuredAuthenticationRequestDto;
import in.mapper.AuthenticationRequestMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Сервлет аутентификации.
 */
@WebServlet("/login")
public class AuthenticationServlet extends AbstractServiceServlet {

    private final ApplicationController controller;
    private final ValidatingObjectMapper mapper;

    private final AuthenticationRequestMapper authenticationRequestMapper;

    @SneakyThrows
    public AuthenticationServlet() {
        controller = ApplicationControllerFactory.getInstance();
        mapper = new ValidatingObjectMapper();
        authenticationRequestMapper = AuthenticationRequestMapper.INSTANCE;
    }

    @SneakyThrows
    public AuthenticationServlet(ApplicationController controller) {
        this.controller = controller;
        mapper = new ValidatingObjectMapper();
        authenticationRequestMapper = AuthenticationRequestMapper.INSTANCE;
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        String requestBody = req.getReader().lines().collect(Collectors.joining());

        try {
            UnsecuredAuthenticationRequestDto unsecuredPlayerRequestDto =
                    mapper.validateValue(requestBody, UnsecuredAuthenticationRequestDto.class);
            AuthenticationRequest authenticationRequest =
                    authenticationRequestMapper.toAuthenticationRequest(unsecuredPlayerRequestDto);

            AuthenticationDto authenticatedPlayerDto = controller.authenticate(authenticationRequest);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setHeader("Authorization", "Bearer " + produceJwt(authenticatedPlayerDto));
            resp.getOutputStream().write(mapper.writeValueAsBytes(authenticatedPlayerDto));
        } catch (BadCredentialsException | ConstraintViolationException e) {
            ExceptionDto exceptionDto = new ExceptionDto(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getOutputStream().write(mapper.writeValueAsBytes(exceptionDto));
        }
    }
}
