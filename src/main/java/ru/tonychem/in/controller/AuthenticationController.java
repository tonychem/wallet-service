package ru.tonychem.in.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tonychem.aop.annotations.validation.Validated;
import ru.tonychem.application.ApplicationController;
import ru.tonychem.application.model.dto.AuthenticationDto;
import ru.tonychem.application.model.dto.AuthenticationRequest;
import ru.tonychem.exception.model.BadCredentialsException;
import ru.tonychem.in.dto.UnsecuredAuthenticationRequestDto;
import ru.tonychem.in.mapper.AuthenticationRequestMapper;

@RestController
@RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthenticationController extends AbstractTokenProducer {
    private final ApplicationController applicationController;
    private final AuthenticationRequestMapper authenticationRequestMapper;

    @PostMapping
    public ResponseEntity<AuthenticationDto> authenticate(@RequestBody UnsecuredAuthenticationRequestDto requestDto)
            throws BadCredentialsException {
        AuthenticationRequest authenticationRequest = authenticationRequestMapper.toAuthenticationRequest(requestDto);
        AuthenticationDto authenticationDto = applicationController.authenticate(authenticationRequest);

        return ResponseEntity.ok()
                .header("Authorization", generateJwt(authenticationDto))
                .body(authenticationDto);
    }
}
