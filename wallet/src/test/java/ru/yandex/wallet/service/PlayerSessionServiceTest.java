package ru.yandex.wallet.service;

import model.dto.out.AuthenticatedPlayerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.wallet.exception.exceptions.UnauthorizedOperationException;
import ru.yandex.wallet.service.impl.PlayerSessionServiceImpl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Player Session Service Test")
public class PlayerSessionServiceTest {
    private PlayerSessionService playerSessionService;
    private Map<UUID, AuthenticatedPlayerDto> authentications;
    private UUID existingId;

    @BeforeEach
    public void init() {
        existingId = UUID.randomUUID();
        authentications = new HashMap<>();
        authentications.put(existingId, new AuthenticatedPlayerDto(null, null, null, null));
        playerSessionService = new PlayerSessionServiceImpl(authentications);
    }

    @Test
    @DisplayName("Should return true when session is present")
    public void shouldCheckExistingId() throws UnauthorizedOperationException {
        assertThat(playerSessionService.exists(existingId)).isTrue();
    }

    @Test
    @DisplayName("Should throw exceptionWhenSessionIsAbsent")
    public void shouldThrowExceptionWhenSessionIsAbsent() throws UnauthorizedOperationException {
        assertThatThrownBy(() -> playerSessionService.exists(UUID.randomUUID()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    @DisplayName("Should add new session")
    public void shouldAddNewSession() {
        AuthenticatedPlayerDto dto
                = new AuthenticatedPlayerDto(1L, "login", "username", BigDecimal.ONE);
        UUID id = playerSessionService.open(dto);
        assertThat(id).isNotNull();
    }

    @Test
    @DisplayName("Should close existing session")
    public void shouldCloseExistingSession() {
        playerSessionService.close(existingId);
        assertThat(authentications).isEmpty();
    }
}
