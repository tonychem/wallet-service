package ru.tonychem.in.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tonychem.application.ApplicationController;
import ru.tonychem.application.model.dto.AuthenticationDto;
import ru.tonychem.domain.dto.PlayerCreationRequest;
import ru.tonychem.exception.model.BadCredentialsException;
import ru.tonychem.in.dto.UnsecuredPlayerCreationRequestDto;
import ru.tonychem.in.mapper.PlayerRequestMapper;

@Api(description = "Регистрация новых пользователей")
@RestController
@RequestMapping(value = "/registration")
@RequiredArgsConstructor
public class RegistrationController extends AbstractTokenProducer {
    private final ApplicationController controller;

    private final PlayerRequestMapper playerRequestMapper;

    @ApiOperation("Регистрация пользователя")
    @ApiResponses(
            {@ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Ошибка валидации полей JSON объекта или такой пользователь уже существует"),
                    @ApiResponse(code = 403, message = "Отсутствует токен авторизации либо пользовательская сессия отсутвует/закрыта на сервере")}
    )
    @PostMapping
    public ResponseEntity<AuthenticationDto> registerPlayer(@RequestBody UnsecuredPlayerCreationRequestDto
                                                                    unsecuredPlayerCreationRequestDto)
            throws BadCredentialsException {
        PlayerCreationRequest creationRequest = playerRequestMapper.toPlayerCreationRequest(unsecuredPlayerCreationRequestDto);
        AuthenticationDto authenticationDto = controller.registerUser(creationRequest);

        return ResponseEntity.ok()
                .header("Authorization", generateJwt(authenticationDto))
                .body(authenticationDto);
    }
}
