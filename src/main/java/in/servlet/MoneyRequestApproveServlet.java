package in.servlet;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import exception.dto.ExceptionDto;
import in.dto.TransactionsDto;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JwtUtils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервлет подтверждения запросов на перевод денежных средств
 */
@WebServlet("/player-management/money-request/approve")
public class MoneyRequestApproveServlet extends AbstractServiceServlet {

    private final ApplicationController controller;

    public MoneyRequestApproveServlet() {
        controller = ApplicationControllerFactory.getInstance();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            String token = extractToken(req);
            String username = (String) JwtUtils.extractClaim(token, claims -> claims.get("username"));
            UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(token, claims -> claims.get("session-id")));

            String requestBody = req.getReader().lines().collect(Collectors.joining());
            TransactionsDto transactionsDto = mapper.validateValue(requestBody, TransactionsDto.class);
            List<UUID> transactionsToApprove = extractValidUUIDsFromString(transactionsDto.getIds());

            for (UUID id : transactionsToApprove) {
                controller.approvePendingRequest(sessionId, username, id);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
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
