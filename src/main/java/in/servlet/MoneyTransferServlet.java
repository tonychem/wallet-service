package in.servlet;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import application.dto.BalanceDto;
import exception.dto.ExceptionDto;
import in.dto.PlayerTransferMoneyRequestDto;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JwtUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервлет перевода денежных средств
 */
@WebServlet("/player-management/money-transfer")
public class MoneyTransferServlet extends AbstractServiceServlet {

    private final ApplicationController controller;

    public MoneyTransferServlet() {
        controller = ApplicationControllerFactory.getInstance();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            String token = extractToken(req);
            String sender = (String) JwtUtils.extractClaim(token, claims -> claims.get("login"));
            UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(token, claims -> claims.get("session-id")));
            UUID transactionId = UUID.randomUUID();

            String requestBody = req.getReader().lines().collect(Collectors.joining());
            PlayerTransferMoneyRequestDto moneyRequest = mapper.validateValue(requestBody, PlayerTransferMoneyRequestDto.class);

            BalanceDto balanceDto = controller.transferMoney(sender, moneyRequest.getRecipient(),
                    BigDecimal.valueOf(moneyRequest.getAmount()), sessionId, transactionId);

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
