package application;

import application.dto.AuthenticationDto;
import application.dto.AuthenticationRequest;
import application.dto.BalanceDto;
import application.exception.UnauthorizedOperationException;
import application.logging.Logger;
import domain.dto.MoneyTransferRequest;
import domain.dto.MoneyTransferResponse;
import domain.dto.PlayerCreationRequest;
import domain.dto.TransactionDto;
import exception.BadCredentialsException;
import service.PlayerAction;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 * Прокси класс для логирования действий пользователя. Фабрика ApplicationController
 * предоставляет UI эту реализацию ApplicationController.
 */
public class LoggedApplicationController extends ApplicationController {
    private final Logger log;

    public LoggedApplicationController(Logger log) {
        super();
        this.log = log;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationDto registerUser(PlayerCreationRequest request) throws BadCredentialsException {
        try {
            log.info(String.format("Регистрация пользователя с данными: login=%s username=%s password=%s",
                    request.getLogin(), request.getUsername(), Arrays.toString(request.getPassword())));
            AuthenticationDto dto = super.registerUser(request);
            log.info(String.format("Пользователь зарегистрирован: id=%s, login=%s, username=%s, sessionId=%s, " +
                            "balance=%s", dto.getId(), dto.getLogin(), dto.getUsername(), dto.getSessionId(),
                    dto.getBalance()));
            return dto;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationDto authenticate(AuthenticationRequest request) throws BadCredentialsException {
        try {
            log.info(String.format("Авторизация пользователя с данными: login=%s password=%s",
                    request.getLogin(), Arrays.toString(request.getPassword())));
            AuthenticationDto dto = super.authenticate(request);
            log.info(String.format("Пользователь авторизован: id=%s, login=%s, username=%s, sessionId=%s, " +
                            "balance=%s", dto.getId(), dto.getLogin(), dto.getUsername(), dto.getSessionId(),
                    dto.getBalance()));
            return dto;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BalanceDto getBalance(Long id, UUID sessionId) throws UnauthorizedOperationException {
        try {
            log.info(String.format("Запрос баланса user_id=%s session_id=%s",
                    id, sessionId));
            BalanceDto dto = super.getBalance(id, sessionId);
            log.info(String.format("Запрос баланса обработан: id=%s, username=%s, balance=%s",
                    dto.getId(), dto.getUsername(), dto.getBalance()));
            return dto;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BalanceDto transferMoney(String sender, String recipient, BigDecimal amount, UUID sessionId, UUID transactionId)
            throws UnauthorizedOperationException {
        try {
            log.info(String.format("Инициация перевода денег от username=%s к username=%s суммой=%s. " +
                            "session_id=%s transaction_id=%s",
                    sender, recipient, amount, sessionId, transactionId));
            BalanceDto dto = super.transferMoney(sender, recipient, amount, sessionId, transactionId);
            log.info(String.format("Запрос баланса обработан: id=%s, username=%s, balance=%s",
                    dto.getId(), dto.getUsername(), dto.getBalance()));
            return dto;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestMoneyFrom(String requester, String donor, BigDecimal amount, UUID sessionId, UUID transactionId)
            throws UnauthorizedOperationException {
        try {
            log.info(String.format("Запрос денег исходящий от username=%s к username=%s суммой=%s. session_id=%s " +
                    "transaction_id=%s", requester, donor, amount, sessionId, transactionId));
            return super.requestMoneyFrom(requester, donor, amount, sessionId, transactionId);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MoneyTransferRequest> getPendingMoneyRequests(String login, UUID sessionId) throws UnauthorizedOperationException {
        try {
            log.info(String.format("Пользователь username=%s запросил список транзакций, ожидающих подтверждения. " +
                    "session_id=%s", login, sessionId));
            Collection<MoneyTransferRequest> response = super.getPendingMoneyRequests(login, sessionId);
            return response;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TransactionDto> getHistory(String login, PlayerAction action, UUID sessionId) throws UnauthorizedOperationException {
        try {
            log.info(String.format("Пользователь login=%s запросил историю транзакций типа player_action=%s. " +
                    "session_id=%s", login, action, sessionId));
            return super.getHistory(login, action, sessionId);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MoneyTransferResponse approvePendingRequest(UUID sessionId, String donorUsername, UUID transactionId) throws UnauthorizedOperationException {
        try {
            log.info(String.format("Пользователь username=%s пытается подтвердить транзакцию transaction_id=%s. " +
                    "session_id=%s", donorUsername, transactionId, sessionId));
            MoneyTransferResponse response = super.approvePendingRequest(sessionId, donorUsername, transactionId);
            log.info(String.format("Транзакция пользователя username=%s подтверждена transaction_id=%s, session_id=%s",
                    donorUsername, transactionId, sessionId));
            return response;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void declinePendingRequest(UUID sessionId, String donorUsername, UUID transactionId)
            throws UnauthorizedOperationException {
        try {
            log.info(String.format("Пользователь username=%s пытается отклонить транзакцию transaction_id=%s. " +
                    "session_id=%s", donorUsername, transactionId, sessionId));
            super.declinePendingRequest(sessionId, donorUsername, transactionId);
            log.info(String.format("Транзакция пользователя username=%s отклонена transaction_id=%s, session_id=%s",
                    donorUsername, transactionId, sessionId));
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void signOut(String username, UUID sessionId) {
        try {
            super.signOut(username, sessionId);
            log.info(String.format("Пользователь username=%s закрывает сессию session_id=%s",
                    username, sessionId));
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }
}
