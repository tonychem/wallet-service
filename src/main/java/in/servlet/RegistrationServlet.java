package in.servlet;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import application.model.dto.AuthenticationDto;
import domain.dto.PlayerCreationRequest;
import exception.BadCredentialsException;
import exception.dto.ExceptionDto;
import in.dto.UnsecuredPlayerRequestDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.SneakyThrows;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.stream.Collectors;

/**
 * Сервлет регистрации
 */
@WebServlet("/registration")
public class RegistrationServlet extends AbstractServiceServlet {

    private final ApplicationController controller;
    private final MessageDigest messageDigest;

    @SneakyThrows
    public RegistrationServlet() {
        controller = ApplicationControllerFactory.getInstance();
        messageDigest = MessageDigest.getInstance("MD5");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        String requestBody = req.getReader().lines().collect(Collectors.joining());

        try {
            UnsecuredPlayerRequestDto unsecuredPlayerRequestDto = mapper.validateValue(requestBody, UnsecuredPlayerRequestDto.class);
            PlayerCreationRequest playerCreationRequest = new PlayerCreationRequest(unsecuredPlayerRequestDto.getLogin(),
                    messageDigest.digest(unsecuredPlayerRequestDto.getPassword().getBytes()), unsecuredPlayerRequestDto.getUsername());

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
