package in.servlet;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import exception.dto.ExceptionDto;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JwtUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * Сервлет де-авторизации.
 */
@WebServlet("/logout")
public class LogoutServlet extends AbstractServiceServlet {

    private final ApplicationController controller;

    public LogoutServlet() {
        controller = ApplicationControllerFactory.getInstance();
    }

    public LogoutServlet(ApplicationController controller) {
        this.controller = controller;
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        try {
            String token = extractToken(req);
            String login = (String) JwtUtils.extractClaim(token, claims -> claims.get("login"));
            String sessionId = (String) JwtUtils.extractClaim(token, claims -> claims.get("session-id"));
            controller.signOut(login, UUID.fromString(sessionId));
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SignatureException e) {
            ExceptionDto dto = new ExceptionDto(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(mapper.writeValueAsBytes(dto));
        } catch (Exception e) {
            ExceptionDto dto = new ExceptionDto(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getOutputStream().write(mapper.writeValueAsBytes(dto));
        }
    }
}
