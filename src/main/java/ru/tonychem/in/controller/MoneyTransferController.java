package ru.tonychem.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tonychem.domain.dto.BalanceDto;
import ru.tonychem.exception.model.UnauthorizedOperationException;
import ru.tonychem.in.dto.PlayerTransferMoneyRequestDto;
import ru.tonychem.in.dto.UnpackedJwtClaims;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.PlayerSessionService;

@Api(description = "Контроллер переводов денежных средств")
@RestController
@RequestMapping(value = "/player-management/money-transfer")
@RequiredArgsConstructor
public class MoneyTransferController extends AbstractTokenConsumer {

    private final PlayerService playerService;
    private final PlayerSessionService playerSessionService;

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
        UnpackedJwtClaims claims = unpackJwtClaims(authToken);

        playerSessionService.exists(claims.getSessionId());
        BalanceDto balanceDto = playerService.transferMoneyTo(claims.getLogin(), moneyRequest);

        return ResponseEntity.ok(balanceDto);
    }
}
