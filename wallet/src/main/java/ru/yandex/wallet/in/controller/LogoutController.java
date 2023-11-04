package ru.yandex.wallet.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

@Api(description = "Деавторизация игрока")
@RestController
@RequestMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LogoutController extends AbstractTokenConsumer {
    private final PlayerSessionService playerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    @ApiOperation(value = "Удаление пользовательской сессии и деавторизация")
    @ApiResponses(
            {@ApiResponse(code = 204, message = "ОК"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @DeleteMapping
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authToken) {
        UnpackedJwtClaims claims = unpackJwtClaims(authToken, secret);
        playerSessionService.close(claims.getSessionId());

        return ResponseEntity.noContent().build();
    }
}
