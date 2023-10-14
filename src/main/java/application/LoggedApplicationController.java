package application;

import application.dto.AuthenticationDto;
import application.dto.AuthenticationRequest;
import application.dto.BalanceDto;
import application.exception.UnauthorizedOperationException;
import domain.exception.BadCredentialsException;
import domain.model.dto.MoneyTransferRequest;
import domain.model.dto.MoneyTransferResponse;
import domain.model.dto.PlayerCreationRequest;
import domain.model.dto.TransactionDto;
import domain.service.PlayerAction;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

/**
 * Прокси класс для логирования действий пользователя. Фабрика ApplicationController
 * предоставляет UI эту реализацию ApplicationController.
 */
@Slf4j
public class LoggedApplicationController extends ApplicationController {
    @Override
    public AuthenticationDto registerUser(PlayerCreationRequest request) throws BadCredentialsException {
        try {
            log.info("Регистрация пользователя с данными: login={} username={} password={}",
                    request.getLogin(), request.getPassword(), request.getPassword());
            AuthenticationDto dto = super.registerUser(request);
            log.info("Пользователь зарегистрирован: id={}, login={}, username={}, sessionId={}, " +
                            "balance={}", dto.getId(), dto.getLogin(), dto.getUsername(), dto.getSessionId(),
                    dto.getBalance());
            return dto;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    @Override
    public AuthenticationDto authenticate(AuthenticationRequest request) throws BadCredentialsException {
        try {
            log.info("Авторизация пользователя с данными: login={} password={}",
                    request.getLogin(), request.getPassword());
            AuthenticationDto dto = super.authenticate(request);
            log.info("Пользователь авторизован: id={}, login={}, username={}, sessionId={}, " +
                            "balance={}", dto.getId(), dto.getLogin(), dto.getUsername(), dto.getSessionId(),
                    dto.getBalance());
            return dto;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    @Override
    public BalanceDto getBalance(Long id, UUID sessionId) throws UnauthorizedOperationException {
        try {
            log.info("Запрос баланса user_id={} session_id={}",
                    id, sessionId);
            BalanceDto dto = super.getBalance(id, sessionId);
            log.info("Запрос баланса обработан: id={}, username={}, balance={}",
                    dto.getId(), dto.getUsername(), dto.getBalance());
            return dto;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    @Override
    public BalanceDto transferMoney(String sender, String recipient, BigDecimal amount, UUID sessionId, UUID transactionId)
            throws UnauthorizedOperationException {
        try {
            log.info("Инициация перевода денег от username={} к username={} суммой={}. session_id={} transaction_id={}",
                    sender, recipient, amount, sessionId, transactionId);
            BalanceDto dto = super.transferMoney(sender, recipient, amount, sessionId, transactionId);
            log.info("Запрос баланса обработан: id={}, username={}, balance={}",
                    dto.getId(), dto.getUsername(), dto.getBalance());
            return dto;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean requestMoneyFrom(String requester, String donor, BigDecimal amount, UUID sessionId, UUID transactionId)
            throws UnauthorizedOperationException {
        try {
            log.info("Запрос денег исходящий от username={} к username={} суммой={}. session_id={} transaction_id={}",
                    requester, donor, amount, sessionId, transactionId);
            boolean response = super.requestMoneyFrom(requester, donor, amount, sessionId, transactionId);
            return response;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    @Override
    public Collection<MoneyTransferRequest> getPendingMoneyRequests(String login, UUID sessionId) throws UnauthorizedOperationException {
        try {
            log.info("Пользователь username={} запросил список транзакций, ожидающих подтверждения. session_id={}",
                    login, sessionId);
            Collection<MoneyTransferRequest> response = super.getPendingMoneyRequests(login, sessionId);
            return response;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    @Override
    public Collection<TransactionDto> getHistory(String login, PlayerAction action, UUID sessionId) throws UnauthorizedOperationException {
        try {
            log.info("Пользователь login={} запросил историю транзакций типа player_action={}. session_id={}",
                    login, action, sessionId);
            Collection<TransactionDto> response = super.getHistory(login, action, sessionId);
            return response;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    @Override
    public MoneyTransferResponse approvePendingRequest(UUID sessionId, String donorUsername, UUID transactionId) throws UnauthorizedOperationException {
        try {
            log.info("Пользователь username={} пытается подтвердить транзакцию transaction_id={}. session_id={}",
                    donorUsername, transactionId, sessionId);
            MoneyTransferResponse response = super.approvePendingRequest(sessionId, donorUsername, transactionId);
            log.info("Транзакция пользователя username={} подтверждена transaction_id={}, session_id={}",
                    donorUsername, transactionId, sessionId);
            return response;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    @Override
    public void declinePendingRequest(UUID sessionId, String donorUsername, UUID transactionId) throws UnauthorizedOperationException {
        try {
            log.info("Пользователь username={} пытается отклонить транзакцию transaction_id={}. session_id={}",
                    donorUsername, transactionId, sessionId);
            super.declinePendingRequest(sessionId, donorUsername, transactionId);
            log.info("Транзакция пользователя username={} отклонена transaction_id={}, session_id={}",
                    donorUsername, transactionId, sessionId);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    @Override
    public void signOut(String username, UUID sessionId) {
        try {
            super.signOut(username, sessionId);
            log.info("Пользователь username={} закрывает сессию session_id={}",
                    username, sessionId);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }
}
