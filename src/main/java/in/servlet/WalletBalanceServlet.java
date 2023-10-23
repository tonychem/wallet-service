package in.servlet;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import application.model.dto.BalanceDto;
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
 * Сервлет для работы с балансом на счете пользователя
 */
@WebServlet("/player-management/wallet/balance")
public class WalletBalanceServlet extends AbstractServiceServlet {
    private final ApplicationController controller;

    public WalletBalanceServlet() {
        controller = ApplicationControllerFactory.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            String token = extractToken(req);
            Long userId = Long.valueOf((Integer) JwtUtils.extractClaim(token, claims -> claims.get("id")));
            UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(token, claims -> claims.get("session-id")));

            BalanceDto balanceDto = controller.getBalance(userId, sessionId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getOutputStream().write(mapper.writeValueAsBytes(balanceDto));
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
