package ru.tonychem.service;

import ru.tonychem.domain.dto.AuthenticatedPlayerDto;
import ru.tonychem.exception.model.UnauthorizedOperationException;

import java.util.UUID;

public interface PlayerSessionService {
    boolean exists(UUID id) throws UnauthorizedOperationException;

    UUID open(AuthenticatedPlayerDto authentication);

    void close(UUID id);
}
