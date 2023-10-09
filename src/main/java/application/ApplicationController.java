package application;

import application.exception.UnauthorizedOperationException;
import domain.exception.BadCredentialsException;
import domain.model.dto.*;
import domain.service.PlayerAction;
import domain.service.PlayerService;
import domain.service.PlayerServiceImpl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ApplicationController {

    private final Set<UUID> authentications;
    private final PlayerService playerService;

    public ApplicationController() {
        this.playerService = new PlayerServiceImpl();
        authentications = new HashSet<>();
    }

    public ApplicationController(Set<UUID> authentications, PlayerService playerService) {
        this.authentications = authentications;
        this.playerService = playerService;
    }

    public AuthenticationDto registerUser(PlayerCreationRequest request) throws BadCredentialsException {
        AuthenticatedPlayerDto authenticatedPlayerDto = playerService.register(request);
        UUID sessionId = UUID.randomUUID();

        Authentication authentication = new Authentication(authenticatedPlayerDto.getId(),
                authenticatedPlayerDto.getLogin(), authenticatedPlayerDto.getUsername(), sessionId);
        authentications.add(sessionId);
        return new AuthenticationDto(authentication.getId(), authentication.getLogin(),
                authentication.getUsername(),
                authentication.getSessionID(), authenticatedPlayerDto.getBalance());
    }

    public AuthenticationDto authenticate(AuthenticationRequest request) throws BadCredentialsException {
        AuthenticatedPlayerDto authenticatedPlayerDto = playerService.authenticate(request.getLogin(),
                request.getPassword());
        UUID sessionId = UUID.randomUUID();

        authentications.add(sessionId);

        return new AuthenticationDto(authenticatedPlayerDto.getId(), authenticatedPlayerDto.getLogin(),
                authenticatedPlayerDto.getUsername(),
                sessionId, authenticatedPlayerDto.getBalance());
    }

    public BalanceDto getBalance(Long id, UUID sessionId) throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");
        AuthenticatedPlayerDto playerDto = playerService.getBalance(id);
        return new BalanceDto(playerDto.getId(), playerDto.getUsername(), playerDto.getBalance());
    }

    public BalanceDto transferMoney(String sender, String recipient, BigDecimal amount, UUID sessionId,
                                    UUID transactionId)
            throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");

        MoneyTransferResponse response = playerService.transferMoneyTo(new MoneyTransferRequest(
                transactionId, sender, recipient, amount
        ));

        return new BalanceDto(response.getRequester().getId(), response.getRequester().getUsername(),
                response.getRequester().getBalance());
    }

    public boolean requestMoneyFrom(String requester, String donor, BigDecimal amount, UUID sessionId,
                                    UUID transactionId) throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");

        MoneyTransferResponse response = playerService.requestMoneyFrom(new MoneyTransferRequest(
                transactionId, donor, requester, amount
        ));

        return response != null;
    }

    public Collection<MoneyTransferRequest> getPendingMoneyRequests(String login, UUID sessionId)
            throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");

        return playerService.getPendingMoneyRequests(login);
    }

    public Collection<TransactionDto> getHistory(String login, PlayerAction action, UUID sessionId)
            throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");
        Collection<TransactionDto> history = playerService.getHistory(login, action);
        return history;
    }

    public MoneyTransferResponse approvePendingRequest(UUID sessionId, UUID transactionId)
            throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");
        return playerService.approvePendingMoneyRequest(transactionId);
    }

    public void declinePendingRequest(UUID sessionId, UUID transactionId)
            throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");
        playerService.declinePendingRequest(transactionId);
    }

    public void signOut(UUID sessionId) {
        authentications.remove(sessionId);
    }
}
