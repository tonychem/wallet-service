package ru.tonychem.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tonychem.aop.annotations.Audit;
import ru.tonychem.domain.dto.BalanceDto;
import ru.tonychem.domain.dto.TransactionDto;
import ru.tonychem.exception.model.UnauthorizedOperationException;
import ru.tonychem.in.dto.UnpackedJwtClaims;
import ru.tonychem.service.PlayerAction;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.PlayerSessionService;

import java.util.Collection;

@Api(description = "Действия с кошельком")
@RestController
@RequestMapping(value = "/player-management/wallet", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Audit
public class WalletController extends AbstractTokenConsumer {

    private final PlayerService playerService;
    private final PlayerSessionService playerSessionService;

    @ApiOperation("Получение баланса игрока")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @GetMapping("/balance")
    public ResponseEntity<BalanceDto> getBalance(@RequestHeader("Authorization") String authToken)
            throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken);

        playerSessionService.exists(claims.getSessionId());
        BalanceDto balance = playerService.getBalance(claims.getUserId());

        return ResponseEntity.ok(balance);
    }

    @ApiOperation("Получение истории движения денежных средств")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @GetMapping("/history")
    public ResponseEntity<Collection<TransactionDto>> getHistory(@RequestHeader("Authorization") String authToken,
                                                                 @RequestParam(value = "action", required = false) PlayerAction action)
            throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken);

        playerSessionService.exists(claims.getSessionId());
        Collection<TransactionDto> transactionDtos = playerService.getHistory(claims.getLogin(), action);

        return ResponseEntity.ok(transactionDtos);
    }
}
