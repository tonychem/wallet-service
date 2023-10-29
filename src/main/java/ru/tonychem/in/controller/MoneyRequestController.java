package ru.tonychem.in.controller;

import ru.tonychem.application.ApplicationController;
import ru.tonychem.exception.model.UnauthorizedOperationException;
import ru.tonychem.in.dto.PlayerRequestMoneyDto;
import ru.tonychem.in.dto.TransactionsListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tonychem.util.JwtUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/player-management/money-request", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MoneyRequestController {
    private final ApplicationController controller;

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
