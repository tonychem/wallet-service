package ru.tonychem.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import ru.tonychem.application.ApplicationController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tonychem.util.JwtUtils;

import java.util.UUID;

@Api(description = "Деавторизация игрока")
@RestController
@RequestMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LogoutController {
    private final ApplicationController controller;

    @ApiOperation(value = "Удаление пользовательской сессии и деавторизация")
    @ApiResponses(
            {@ApiResponse(code = 204, message = "ОК"),
            @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @DeleteMapping
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authToken) {
        String jwt = authToken.substring(7);
        String login = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("login"));
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id")));

        controller.signOut(login, sessionId);

        return ResponseEntity.noContent().build();
    }
}
