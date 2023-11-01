package ru.tonychem.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tonychem.application.ApplicationController;
import ru.tonychem.application.model.dto.BalanceDto;
import ru.tonychem.exception.model.UnauthorizedOperationException;
import ru.tonychem.in.dto.PlayerTransferMoneyRequestDto;
import ru.tonychem.util.JwtUtils;

import java.math.BigDecimal;
import java.util.UUID;

@Api(description = "Контроллер переводов денежных средств")
@RestController
@RequestMapping(value = "/player-management/money-transfer")
@RequiredArgsConstructor
public class MoneyTransferController {

    private final ApplicationController controller;

    @ApiOperation("Перевод денежных средств")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Ошибка валидации полей JSON объекта"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @PostMapping
    public ResponseEntity<BalanceDto> transferMoney(@RequestHeader("Authorization") String authToken,
                                                    @RequestBody PlayerTransferMoneyRequestDto moneyRequest)
            throws UnauthorizedOperationException {
        String jwt = authToken.substring(7);
        String sender = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("login"));
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id")));
        UUID transactionId = UUID.randomUUID();

        BalanceDto remainingBalance = controller.transferMoney(sender, moneyRequest.getRecipient(),
                BigDecimal.valueOf(moneyRequest.getAmount()), sessionId, transactionId);
        return ResponseEntity.ok(remainingBalance);
    }
}
