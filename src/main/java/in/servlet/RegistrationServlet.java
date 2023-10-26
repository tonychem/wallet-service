package in.servlet;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import application.model.dto.AuthenticationDto;
import domain.dto.PlayerCreationRequest;
import exception.BadCredentialsException;
import exception.dto.ExceptionDto;
import in.dto.UnsecuredPlayerRequestDto;
import in.mapper.PlayerRequestMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Сервлет регистрации
 */
@WebServlet("/registration")
public class RegistrationServlet extends AbstractServiceServlet {

    private final ApplicationController controller;
    private final PlayerRequestMapper playerRequestMapper;

    @SneakyThrows
    public RegistrationServlet() {
        controller = ApplicationControllerFactory.getInstance();
        playerRequestMapper = PlayerRequestMapper.INSTANCE;
    }

    public RegistrationServlet(ApplicationController controller) {
        this.controller = controller;
        playerRequestMapper = PlayerRequestMapper.INSTANCE;
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        String requestBody = req.getReader().lines().collect(Collectors.joining());

        try {
            UnsecuredPlayerRequestDto unsecuredPlayerRequestDto =
                    mapper.validateValue(requestBody, UnsecuredPlayerRequestDto.class);
            PlayerCreationRequest playerCreationRequest =
                    playerRequestMapper.toPlayerCreationRequest(unsecuredPlayerRequestDto);

            AuthenticationDto authenticatedPlayerDto = controller.registerUser(playerCreationRequest);
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
