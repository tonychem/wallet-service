package in.servlet;

import application.ApplicationController;
import application.ApplicationControllerFactory;
import domain.dto.MoneyTransferRequest;
import exception.dto.ExceptionDto;
import in.dto.PlayerRequestMoneyDto;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JwtUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервлет, ответственный за денежные запросы. Можно получить текущие неодобренные заявки по глаголу GET, или опубликовать новую
 * по глаголу POST
 */
@WebServlet("/player-management/money-request")
public class MoneyRequestServlet extends AbstractServiceServlet {
    private final ApplicationController controller;

    public MoneyRequestServlet() {
        controller = ApplicationControllerFactory.getInstance();
    }

    public MoneyRequestServlet(ApplicationController controller) {
        this.controller = controller;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            String token = extractToken(req);
            String requester = (String) JwtUtils.extractClaim(token, claims -> claims.get("login"));
            UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(token, claims -> claims.get("session-id")));
            UUID transactionId = UUID.randomUUID();

            String requestBody = req.getReader().lines().collect(Collectors.joining());
            PlayerRequestMoneyDto moneyRequest = mapper.validateValue(requestBody, PlayerRequestMoneyDto.class);

            controller.requestMoneyFrom(requester, moneyRequest.getDonor(), BigDecimal.valueOf(moneyRequest.getAmount()),
                    sessionId, transactionId);
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            String token = extractToken(req);
            String login = (String) JwtUtils.extractClaim(token, claims -> claims.get("login"));
            UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(token, claims -> claims.get("session-id")));

            Collection<MoneyTransferRequest> moneyTransferResponses =
                    controller.getPendingMoneyRequests(login, sessionId);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getOutputStream().write(mapper.writeValueAsBytes(moneyTransferResponses));
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
