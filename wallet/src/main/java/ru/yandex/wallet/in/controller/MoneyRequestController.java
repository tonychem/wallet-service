package ru.yandex.wallet.in.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import model.dto.in.PlayerRequestMoneyDto;
import model.dto.in.TransactionsListDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.metrics.audit.Audit;
import ru.yandex.metrics.performance.Performance;
import ru.yandex.wallet.domain.dto.MoneyTransferRequest;
import ru.yandex.wallet.exception.exceptions.UnauthorizedOperationException;
import ru.yandex.wallet.in.UnpackedJwtClaims;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;

import java.util.Collection;

@RestController
@RequestMapping(value = "/player-management/money-request", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Performance
@Audit
public class MoneyRequestController extends AbstractTokenConsumer {

    private final PlayerService playerService;
    private final PlayerSessionService playerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    @Operation(summary = "Получение списка неподтвержденных денежных запросов")
    @ApiResponses(
            {@ApiResponse(responseCode = "200", description = "OK", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере", content = @Content)}
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

    @Operation(summary = "Публикация запроса на получение денежных средств")
    @ApiResponses(
            {@ApiResponse(responseCode = "200", description = "OK", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации полей JSON объекта", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере", content = @Content)}
    )
    @PostMapping
    public ResponseEntity<?> requestMoney(@RequestHeader("Authorization") String authToken,
                                          @RequestBody PlayerRequestMoneyDto moneyRequestDto) throws UnauthorizedOperationException {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);

        playerSessionService.exists(claims.getSessionId());
        playerService.requestMoneyFrom(claims.getUsername(), moneyRequestDto);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Подтверждение входящих запросов на отправку денежных средств")
    @ApiResponses(
            {@ApiResponse(responseCode = "200", description = "OK", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере", content = @Content)}
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

    @Operation(summary = "Отклонение входящих запросов на отправку денежных средств")
    @ApiResponses(
            {@ApiResponse(responseCode = "200", description = "OK", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере", content = @Content)}
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
