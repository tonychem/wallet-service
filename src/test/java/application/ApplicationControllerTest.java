package application;

import application.dto.AuthenticationDto;
import application.dto.AuthenticationRequest;
import application.dto.BalanceDto;
import application.exception.UnauthorizedOperationException;
import domain.model.dto.AuthenticatedPlayerDto;
import domain.model.dto.PlayerCreationRequest;
import domain.service.PlayerService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationControllerTest {

    private PlayerService mockPlayerService;
    private HashSet<UUID> authorizations;

    private ApplicationController controller;


    @BeforeEach
    public void init() {
        mockPlayerService = Mockito.mock(PlayerService.class);
        authorizations = new HashSet<>();
        controller = new ApplicationController(authorizations, mockPlayerService);
    }

    @SneakyThrows
    @Test
    void shouldRegisterUser() {
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

    @SneakyThrows
    @Test
    void shouldAuthenticateUser() {
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

    @SneakyThrows
    @Test
    void shouldGetBalanceWhenSessionIdIsPresent() {
        AuthenticatedPlayerDto authenticatedPlayerDto = new AuthenticatedPlayerDto(1L, "admin",
                "admin", BigDecimal.TEN);
        when(mockPlayerService.getBalance(any()))
                .thenReturn(authenticatedPlayerDto);
        UUID sessId = UUID.randomUUID();
        authorizations.add(sessId);

        BalanceDto balanceDto = controller.getBalance(anyLong(), sessId);
        assertThat(balanceDto.getBalance()).isEqualTo(authenticatedPlayerDto.getBalance());

        verify(mockPlayerService).getBalance(any());
    }

    @SneakyThrows
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

    @SneakyThrows
    @Test
    void shouldSignOut() {
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