package ru.tonychem.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tonychem.application.ApplicationController;
import ru.tonychem.domain.dto.MoneyTransferRequest;
import ru.tonychem.exception.model.UnauthorizedOperationException;
import ru.tonychem.in.dto.PlayerRequestMoneyDto;
import ru.tonychem.in.dto.TransactionsListDto;
import ru.tonychem.util.JwtUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Api(description = "Взаимодействие с денежными запросами")
@RestController
@RequestMapping(value = "/player-management/money-request", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MoneyRequestController {
    private final ApplicationController controller;

    @ApiOperation("Получение списка неподтвержденных денежных запросов")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @GetMapping
    public ResponseEntity<Collection<MoneyTransferRequest>> getPendingMoneyRequests(@RequestHeader("Authorization") String authToken)
            throws UnauthorizedOperationException {
        String jwt = authToken.substring(7);
        String login = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("login"));
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id")));

        Collection<MoneyTransferRequest> moneyTransferRequests = controller.getPendingMoneyRequests(login, sessionId);

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
        String jwt = authToken.substring(7);
        String requester = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("login"));
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id")));
        UUID transactionId = UUID.randomUUID();

        controller.requestMoneyFrom(requester, moneyRequestDto.getDonor(), BigDecimal.valueOf(moneyRequestDto.getAmount()),
                sessionId, transactionId);
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
        String jwt = authToken.substring(7);
        String username = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("username"));
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id")));
        List<UUID> validIds = extractValidUUIDs(transactions.getIds());

        for (UUID id : validIds) {
            controller.approvePendingRequest(sessionId, username, id);
        }

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
        String jwt = authToken.substring(7);
        String username = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("username"));
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id")));
        List<UUID> validIds = extractValidUUIDs(transactions.getIds());

        for (UUID id : validIds) {
            controller.declinePendingRequest(sessionId, username, id);
        }

        return ResponseEntity.ok().build();
    }

    private List<UUID> extractValidUUIDs(Collection<String> ids) {
        List<UUID> result = new ArrayList<>(ids.size());

        for (String id : ids) {
            try {
                result.add(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
            }
        }
        return result;
    }
}
