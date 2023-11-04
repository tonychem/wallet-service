package ru.tonychem.service.impl;

import org.springframework.stereotype.Service;
import ru.tonychem.domain.dto.AuthenticatedPlayerDto;
import ru.tonychem.exception.model.UnauthorizedOperationException;
import ru.tonychem.service.PlayerSessionService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PlayerSessionServiceImpl implements PlayerSessionService {
    private Map<UUID, AuthenticatedPlayerDto> authentications = new HashMap<>();

    @Override
    public boolean exists(UUID id) throws UnauthorizedOperationException {
        if (authentications.get(id) != null) {
            return true;
        } else {
            throw new UnauthorizedOperationException("Unauthorized access");
        }
    }

    @Override
    public UUID open(AuthenticatedPlayerDto authentication) {
        UUID authenticationId = UUID.randomUUID();
        authentications.put(authenticationId, authentication);
        return authenticationId;
    }

    @Override
    public void close(UUID id) {
        authentications.remove(id);
    }
}
