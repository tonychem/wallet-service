package ru.tonychem.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tonychem.domain.dto.AuthenticatedPlayerDto;
import ru.tonychem.exception.model.BadCredentialsException;
import ru.tonychem.in.dto.UnsecuredAuthenticationRequestDto;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.PlayerSessionService;

import java.util.UUID;

@Api(description = "Авторизация игроков")
@RestController
@RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthenticationController extends AbstractTokenProducer {

    private final PlayerService playerService;
    private final PlayerSessionService playerSessionService;

    @ApiOperation(value = "Получение токена авторизации")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Часть требуемых данных отсутствует/не существует связка login-password")}
    )
    @PostMapping
    public ResponseEntity<AuthenticatedPlayerDto> authenticate(@RequestBody UnsecuredAuthenticationRequestDto requestDto)
            throws BadCredentialsException {
        AuthenticatedPlayerDto authenticationDto = playerService.authenticate(requestDto);
        UUID sessionId = playerSessionService.open(authenticationDto);

        return ResponseEntity.ok()
                .header("Authorization", generateJwt(authenticationDto, sessionId))
                .body(authenticationDto);
    }
}
