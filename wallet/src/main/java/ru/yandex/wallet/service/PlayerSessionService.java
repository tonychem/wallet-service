package ru.yandex.wallet.service;

import model.dto.out.AuthenticatedPlayerDto;
import ru.yandex.wallet.exception.exceptions.UnauthorizedOperationException;

import java.util.UUID;

public interface PlayerSessionService {
    boolean exists(UUID id) throws UnauthorizedOperationException;

    UUID open(AuthenticatedPlayerDto authentication);

    void close(UUID id);
}
