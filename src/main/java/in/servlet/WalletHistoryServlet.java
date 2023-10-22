package in.servlet;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import domain.dto.TransactionDto;
import exception.dto.ExceptionDto;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.PlayerAction;
import util.JwtUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 * Сервлет для работы с историями транзакций пользователя
 */
@WebServlet("/player-management/wallet/history")
public class WalletHistoryServlet extends AbstractServiceServlet {

    private final ApplicationController controller;

    public WalletHistoryServlet() {
        this.controller = ApplicationControllerFactory.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            String token = extractToken(req);
            String login = (String) JwtUtils.extractClaim(token, claims -> claims.get("login"));
            UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(token, claims -> claims.get("session-id")));

            PlayerAction action = parsePlayerAction(req.getQueryString());
            Collection<TransactionDto> transactionDtos;

            if (action != null) {
                transactionDtos = controller.getHistory(login, action, sessionId);
            } else {
                transactionDtos = controller.getHistory(login, PlayerAction.CREDIT, sessionId);
                transactionDtos.addAll(controller.getHistory(login, PlayerAction.DEBIT, sessionId));
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getOutputStream().write(mapper.writeValueAsBytes(transactionDtos));
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

    private PlayerAction parsePlayerAction(String query) {
        if (query == null) {
            return null;
        }

        String actionInQuery = query.split("=")[1].toUpperCase();

        try {
            PlayerAction action = PlayerAction.valueOf(actionInQuery);
            return action;
        } catch (Exception e) {
            return null;
        }
    }
}
