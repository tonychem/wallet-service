package ru.yandex.wallet.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.wallet.domain.dto.MoneyTransferRequest;
import ru.yandex.wallet.exception.model.UnauthorizedOperationException;
import ru.yandex.wallet.in.dto.PlayerRequestMoneyDto;
import ru.yandex.wallet.in.dto.TransactionsListDto;
import ru.yandex.wallet.in.dto.UnpackedJwtClaims;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;

import java.util.Collection;

@Api(description = "Взаимодействие с денежными запросами")
@RestController
@RequestMapping(value = "/player-management/money-request", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MoneyRequestController extends AbstractTokenConsumer {

    private final PlayerService playerService;
    private final PlayerSessionService playerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    @ApiOperation("Получение списка неподтвержденных денежных запросов")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @GetMapping
    public ResponseEntity<Collection<MoneyTransferRequest>> getPendingMoneyRequests(@RequestHeader("Authorization") String authToken)
            throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);

        playerSessionService.exists(claims.getSessionId());
        Collection<MoneyTransferRequest> moneyTransferRequests =
                playerService.getPendingMoneyRequests(claims.getLogin());

        return ResponseEntity.ok(moneyTransferRequests);
    }

    @ApiOperation("Публикация запроса на получение денежных средств")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Ошибка валидации полей JSON объекта"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @PostMapping
    public ResponseEntity<?> requestMoney(@RequestHeader("Authorization") String authToken,
                                          @RequestBody PlayerRequestMoneyDto moneyRequestDto) throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);

        playerSessionService.exists(claims.getSessionId());
        playerService.requestMoneyFrom(claims.getUsername(), moneyRequestDto);

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Подтверждение входящих запросов на отправку денежных средств")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @PostMapping("/approve")
    public ResponseEntity<?> approvePendingMoneyRequests(@RequestHeader("Authorization") String authToken,
                                                         @RequestBody TransactionsListDto transactions)
            throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);

        playerSessionService.exists(claims.getSessionId());
        playerService.approvePendingMoneyRequest(claims.getUsername(), transactions);

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Отклонение входящих запросов на отправку денежных средств")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @PostMapping("/decline")
    public ResponseEntity<?> declinePendingMoneyRequests(@RequestHeader("Authorization") String authToken,
                                                         @RequestBody TransactionsListDto transactions)
            throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);

        playerSessionService.exists(claims.getSessionId());
        playerService.declinePendingRequest(claims.getUsername(), transactions);

        return ResponseEntity.ok().build();
    }
}
