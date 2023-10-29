package ru.tonychem.in.controller;

import ru.tonychem.application.ApplicationController;
import ru.tonychem.application.model.dto.BalanceDto;
import ru.tonychem.domain.dto.TransactionDto;
import ru.tonychem.exception.model.UnauthorizedOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tonychem.service.PlayerAction;
import ru.tonychem.util.JwtUtils;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping(value = "/player-management/wallet", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class WalletController {
    private final ApplicationController controller;

    @GetMapping("/balance")
    public ResponseEntity<BalanceDto> getBalance(@RequestHeader("Authorization") String authToken)
            throws UnauthorizedOperationException {
        String jwt = authToken.substring(7);
        Long userId = Long.valueOf((Integer) JwtUtils.extractClaim(jwt, claims -> claims.get("id")));
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id")));

        BalanceDto balanceDto = controller.getBalance(userId, sessionId);
        return ResponseEntity.ok(balanceDto);
    }

    @GetMapping("/history")
    public ResponseEntity<Collection<TransactionDto>> getHistory(@RequestHeader("Authorization") String authToken,
                                                                 @RequestParam(value = "action", required = false) PlayerAction action)
            throws UnauthorizedOperationException {
        String jwt = authToken.substring(7);
        String login = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("login"));
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id")));

        Collection<TransactionDto> transactionDtos;

        if (action != null) {
            transactionDtos = controller.getHistory(login, action, sessionId);
        } else {
            transactionDtos = controller.getHistory(login, PlayerAction.CREDIT, sessionId);
            transactionDtos.addAll(controller.getHistory(login, PlayerAction.DEBIT, sessionId));
        }

        return ResponseEntity.ok(transactionDtos);
    }
}
