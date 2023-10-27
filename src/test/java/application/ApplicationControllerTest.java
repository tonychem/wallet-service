package application;

import application.model.dto.AuthenticationDto;
import application.model.dto.AuthenticationRequest;
import application.model.dto.BalanceDto;
import domain.dto.AuthenticatedPlayerDto;
import domain.dto.PlayerCreationRequest;
import exception.UnauthorizedOperationException;
import logging.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Testcontainers;
import repository.AbstractPGSQLRepositoryConsumer;
import service.PlayerService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Application level test class")
@Testcontainers
class ApplicationControllerTest extends AbstractPGSQLRepositoryConsumer {
    private PlayerService mockPlayerService;
    private HashMap<UUID, String> authorizations;
    private ApplicationController controller;

    @BeforeEach
    public void init() throws IOException {
        super.init();
        initiateLogger(properties);
        mockPlayerService = Mockito.mock(PlayerService.class);
        authorizations = new HashMap<>();
        controller = new ApplicationController(authorizations, mockPlayerService);
    }

    private void initiateLogger(Properties properties) {
        LoggerFactory.getLogger(properties);
    }

    @DisplayName("Should return player data when registering new player")
    @Test
    void shouldRegisterUser() throws Exception {
        PlayerCreationRequest playerCreationRequest = new PlayerCreationRequest(
                "admin", "admin".getBytes(), "admin"
        );

        AuthenticatedPlayerDto playerDto = new AuthenticatedPlayerDto(1L,
                playerCreationRequest.getLogin(), playerCreationRequest.getUsername(), BigDecimal.TEN);

        when(mockPlayerService.register(playerCreationRequest))
                .thenReturn(playerDto);

        AuthenticationDto dto = controller.registerUser(playerCreationRequest);
        assertThat(authorizations.size()).isEqualTo(1);
        assertThat(dto.getId()).isEqualTo(playerDto.getId());
        assertThat(dto.getLogin()).isEqualTo(playerDto.getLogin());
        assertThat(dto.getBalance()).isEqualTo(playerDto.getBalance());

        verify(mockPlayerService).register(any());
    }

    @DisplayName("Should return player data when authenticating a player")
    @Test
    void shouldAuthenticateUser() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("admin", "password".getBytes());

        AuthenticatedPlayerDto playerDto = new AuthenticatedPlayerDto(1L,
                request.getLogin(), "admin", BigDecimal.TEN);

        when(mockPlayerService.authenticate(request.getLogin(), request.getPassword()))
                .thenReturn(playerDto);

        AuthenticationDto dto = controller.authenticate(request);
        assertThat(authorizations.size()).isEqualTo(1);
        assertThat(dto.getId()).isEqualTo(playerDto.getId());
        assertThat(dto.getLogin()).isEqualTo(playerDto.getLogin());
        assertThat(dto.getBalance()).isEqualTo(playerDto.getBalance());

        verify(mockPlayerService).authenticate(any(), any());
    }

    @DisplayName("Return balance data when player is authenticated")
    @Test
    void shouldGetBalanceWhenSessionIdIsPresent() throws Exception {
        AuthenticatedPlayerDto authenticatedPlayerDto = new AuthenticatedPlayerDto(1L, "admin",
                "admin", BigDecimal.TEN);
        when(mockPlayerService.getBalance(any()))
                .thenReturn(authenticatedPlayerDto);
        UUID sessId = UUID.randomUUID();
        authorizations.put(sessId, authenticatedPlayerDto.getLogin());

        BalanceDto balanceDto = controller.getBalance(authenticatedPlayerDto.getId(), sessId);
        assertThat(balanceDto.getBalance()).isEqualTo(authenticatedPlayerDto.getBalance());

        verify(mockPlayerService).getBalance(any());
    }

    @DisplayName("Throws exception when balance is requested without authentication")
    @Test
    void shouldThrowExceptionWhenSessionIdIsAbsent() {
        AuthenticatedPlayerDto authenticatedPlayerDto = new AuthenticatedPlayerDto(1L, "admin",
                "admin", BigDecimal.TEN);
        when(mockPlayerService.getBalance(any()))
                .thenReturn(authenticatedPlayerDto);
        UUID sessId = UUID.randomUUID();

        assertThatThrownBy(() -> controller.getBalance(authenticatedPlayerDto.getId(), sessId))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @DisplayName("Should remove authorization when player is signed out")
    @Test
    void shouldSignOut() throws Exception {
        PlayerCreationRequest playerCreationRequest = new PlayerCreationRequest(
                "admin", "admin".getBytes(), "admin"
        );
        AuthenticatedPlayerDto playerDto = new AuthenticatedPlayerDto(1L,
                playerCreationRequest.getLogin(), playerCreationRequest.getUsername(), BigDecimal.TEN);
        when(mockPlayerService.register(playerCreationRequest))
                .thenReturn(playerDto);

        AuthenticationDto dto = controller.registerUser(playerCreationRequest);
        assertThat(authorizations.size()).isEqualTo(1);

        controller.signOut(playerDto.getUsername(), dto.getSessionId());
        assertThat(authorizations.isEmpty()).isTrue();
    }
}