package ru.yandex.wallet.service;

import ru.yandex.wallet.domain.dto.AuthenticatedPlayerDto;
import ru.yandex.wallet.exception.model.UnauthorizedOperationException;

import java.util.UUID;

public interface PlayerSessionService {
    boolean exists(UUID id) throws UnauthorizedOperationException;

    UUID open(AuthenticatedPlayerDto authentication);

    void close(UUID id);
}
