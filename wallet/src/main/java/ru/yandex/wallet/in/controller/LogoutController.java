package ru.yandex.wallet.in.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.wallet.in.dto.UnpackedJwtClaims;
import ru.yandex.wallet.service.PlayerSessionService;

@RestController
@RequestMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LogoutController extends AbstractTokenConsumer {
    private final PlayerSessionService playerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    @Operation(summary = "Удаление пользовательской сессии и деавторизация")
    @ApiResponses(
            {@ApiResponse(responseCode = "204", description = "ОК", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере", content = @Content)}
    )
    @DeleteMapping
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authToken) {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);
        playerSessionService.close(claims.getSessionId());

        return ResponseEntity.noContent().build();
    }
}
