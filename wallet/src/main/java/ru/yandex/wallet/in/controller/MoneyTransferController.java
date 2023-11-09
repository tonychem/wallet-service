package ru.yandex.wallet.in.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import model.dto.out.BalanceDto;
import model.dto.in.PlayerTransferMoneyRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.metrics.audit.Audit;
import ru.yandex.metrics.performance.Performance;
import ru.yandex.wallet.exception.exceptions.UnauthorizedOperationException;
import ru.yandex.wallet.in.UnpackedJwtClaims;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;

@RestController
@RequestMapping(value = "/player-management/money-transfer")
@RequiredArgsConstructor
@Performance
@Audit
public class MoneyTransferController extends AbstractTokenConsumer {

    private final PlayerService playerService;
    private final PlayerSessionService playerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    @Operation(summary = "Перевод денежных средств")
    @ApiResponses(
            {@ApiResponse(responseCode = "200", description = "OK", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации полей JSON объекта", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере", content = @Content)}
    )
    @PostMapping
    public ResponseEntity<BalanceDto> transferMoney(@RequestHeader("Authorization") String authToken,
                                                    @RequestBody PlayerTransferMoneyRequestDto moneyRequest)
            throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);

        playerSessionService.exists(claims.getSessionId());
        BalanceDto balanceDto = playerService.transferMoneyTo(claims.getLogin(), moneyRequest);

        return ResponseEntity.ok(balanceDto);
    }
}
