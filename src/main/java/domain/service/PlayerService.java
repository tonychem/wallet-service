package domain.service;

import domain.exception.BadCredentialsException;
import domain.model.dto.*;

import java.util.Collection;
import java.util.UUID;

public interface PlayerService {

    AuthenticatedPlayerDto authenticate(String login, byte[] password) throws BadCredentialsException;

    AuthenticatedPlayerDto register(PlayerCreationRequest playerCreationRequest) throws BadCredentialsException;

    AuthenticatedPlayerDto getBalance(Long id);

    MoneyTransferResponse transferMoneyTo(MoneyTransferRequest moneyTransferRequest);

    MoneyTransferResponse requestMoneyFrom(MoneyTransferRequest moneyTransferRequest);

    Collection<MoneyTransferRequest> getPendingMoneyRequests(String login);

    MoneyTransferResponse approvePendingMoneyRequest(String donorUsername, UUID requestId);

    void declinePendingRequest(String donorUsername, UUID requestId);

    Collection<TransactionDto> getHistory(String login, PlayerAction action);
}
