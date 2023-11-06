package ru.yandex.wallet.in.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import model.dto.out.BalanceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.metrics.audit.Audit;
import ru.yandex.metrics.performance.Performance;
import ru.yandex.wallet.domain.dto.TransactionDto;
import ru.yandex.wallet.exception.exceptions.UnauthorizedOperationException;
import ru.yandex.wallet.in.UnpackedJwtClaims;
import ru.yandex.wallet.service.PlayerAction;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;

import java.util.Collection;

@RestController
@RequestMapping(value = "/player-management/wallet", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Performance
@Audit
public class WalletController extends AbstractTokenConsumer {

    private final PlayerService playerService;
    private final PlayerSessionService playerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    @Operation(summary = "Получение баланса игрока")
    @ApiResponses(
            {@ApiResponse(responseCode = "200", description = "OK", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере", content = @Content)}
    )
    @GetMapping("/balance")
    public ResponseEntity<BalanceDto> getBalance(@RequestHeader("Authorization") String authToken)
            throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);

        playerSessionService.exists(claims.getSessionId());
        BalanceDto balance = playerService.getBalance(claims.getUserId());

        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "Получение истории движения денежных средств")
    @ApiResponses(
            {@ApiResponse(responseCode = "200", description = "OK", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере", content = @Content)}
    )
    @GetMapping("/history")
    public ResponseEntity<Collection<TransactionDto>> getHistory(@RequestHeader("Authorization") String authToken,
                                                                 @RequestParam(value = "action", required = false) PlayerAction action)
            throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);

        playerSessionService.exists(claims.getSessionId());
        Collection<TransactionDto> transactionDtos = playerService.getHistory(claims.getLogin(), action);

        return ResponseEntity.ok(transactionDtos);
    }
}
