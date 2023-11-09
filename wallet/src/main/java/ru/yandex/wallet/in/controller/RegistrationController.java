package ru.yandex.wallet.in.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import model.dto.out.AuthenticatedPlayerDto;
import model.dto.in.UnsecuredPlayerCreationRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.metrics.audit.Audit;
import ru.yandex.metrics.performance.Performance;
import ru.yandex.wallet.exception.exceptions.BadCredentialsException;
import ru.yandex.wallet.service.PlayerService;
import ru.yandex.wallet.service.PlayerSessionService;

import java.util.UUID;

@RestController
@RequestMapping(value = "/registration")
@RequiredArgsConstructor
@Performance
@Audit
public class RegistrationController extends AbstractTokenProducer {

    private final PlayerService playerService;
    private final PlayerSessionService playerSessionService;

    @Value("${jwt.secret}")
    private String secret;

    @Operation(summary = "Регистрация пользователя")
    @ApiResponses(
            {@ApiResponse(responseCode = "200", description = "OK", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации полей JSON объекта или такой пользователь уже существует", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере", content = @Content)}
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
