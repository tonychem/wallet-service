package ru.yandex.wallet.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.wallet.domain.dto.AuthenticatedPlayerDto;
import ru.yandex.wallet.exception.model.BadCredentialsException;
import ru.yandex.wallet.in.dto.UnsecuredPlayerCreationRequestDto;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;

import java.util.UUID;

@Api(description = "Регистрация новых пользователей")
@RestController
@RequestMapping(value = "/registration")
@RequiredArgsConstructor
public class RegistrationController extends AbstractTokenProducer {

    private final PlayerService playerService;
    private final PlayerSessionService playerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    @ApiOperation("Регистрация пользователя")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Ошибка валидации полей JSON объекта или такой пользователь уже существует"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @PostMapping
    public ResponseEntity<AuthenticatedPlayerDto> registerPlayer(@RequestBody UnsecuredPlayerCreationRequestDto
                                                                         unsecuredPlayerCreationRequestDto)
            throws BadCredentialsException {
        AuthenticatedPlayerDto authentication = playerService.register(unsecuredPlayerCreationRequestDto);
        UUID sessionId = playerSessionService.open(authentication);
        return ResponseEntity.ok()
                .header("Authorization", generateJwt(authentication, sessionId, secret))
                .body(authentication);
    }
}
