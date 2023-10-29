package ru.tonychem.application;

import ru.tonychem.aop.annotations.Audit;
import ru.tonychem.aop.annotations.Performance;
import ru.tonychem.application.model.Authentication;
import ru.tonychem.application.model.dto.AuthenticationDto;
import ru.tonychem.application.model.dto.AuthenticationRequest;
import ru.tonychem.application.model.dto.BalanceDto;
import ru.tonychem.application.model.mapper.AuthenticationMapper;
import ru.tonychem.exception.model.BadCredentialsException;
import ru.tonychem.exception.model.UnauthorizedOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tonychem.domain.dto.*;
import ru.tonychem.service.PlayerAction;
import ru.tonychem.service.PlayerService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Класс-контроллер, ответственный за распределение запросов от UI к сервисам.
 * В классе хранится множество авторизованных сессий. Обслуживание запросов пользователя невозможно без
 * предоставления идентификатора сессии. Чтобы получить идентификатор сессии необходимо зарегистрировать
 * нового пользователя или пройти авторизацию.
 */
@Performance
@Audit
@Service
@RequiredArgsConstructor
public class ApplicationController {

    private final PlayerService playerService;

    private final AuthenticationMapper authenticationMapper;

    private Map<UUID, String> authentications = new HashMap<>();


    /**
     * Регистрация пользователя. Метод автоматически сохраняет в множество текущих сессий зарегистрированного пользователя.
     *
     * @param request класс-обертка над пользовательскими секретами (логин, пароль, ник)
     */
    public AuthenticationDto registerUser(PlayerCreationRequest request) throws BadCredentialsException {
        AuthenticatedPlayerDto authenticatedPlayerDto = playerService.register(request);
        UUID sessionId = UUID.randomUUID();

        Authentication authentication = new Authentication(authenticatedPlayerDto.getId(),
                authenticatedPlayerDto.getLogin(), authenticatedPlayerDto.getUsername(), sessionId);

        authentications.put(sessionId, authenticatedPlayerDto.getLogin());
        return authenticationMapper.toAuthenticationDto(authentication, authenticatedPlayerDto.getBalance());
    }

    /**
     * Аутентификация пользователя. Метод сохраняет в множество текущих сессий авторизованного пользователя.
     *
     * @param request класс-обертка над пользовательскими секретами (логин, пароль)
     */
    public AuthenticationDto authenticate(AuthenticationRequest request) throws BadCredentialsException {
        AuthenticatedPlayerDto authenticatedPlayerDto = playerService.authenticate(request.getLogin(), request.getPassword());
        UUID sessionId = UUID.randomUUID();

        authentications.put(sessionId, authenticatedPlayerDto.getLogin());

        return authenticationMapper.toAuthenticationDto(sessionId, authenticatedPlayerDto);
    }

    /**
     * Возвращает баланс пользователя. Доступен только для авторизованных пользователей.
     *
     * @param id        идентификатор пользователя
     * @param sessionId идентификатор текущей сессии
     * @return баланс пользователя
     * @throws UnauthorizedOperationException
     */
    public BalanceDto getBalance(Long id, UUID sessionId) throws UnauthorizedOperationException {
        if (authentications.get(sessionId) == null) throw new UnauthorizedOperationException("Unauthorized access");
        AuthenticatedPlayerDto playerDto = playerService.getBalance(id);
        return authenticationMapper.toBalanceDto(playerDto);
    }

    /**
     * Перевод денежных средств между игроками.
     *
     * @param sender        логин отправителя средств
     * @param recipient     логин получателя средств
     * @param amount        сумма для перевода
     * @param sessionId     идентификатор сессии
     * @param transactionId идентификатор транзакции
     * @return остаток на балансе после перевода
     */
    public BalanceDto transferMoney(String sender, String recipient, BigDecimal amount, UUID sessionId, UUID transactionId) throws UnauthorizedOperationException {
        if (authentications.get(sessionId) == null) throw new UnauthorizedOperationException("Unauthorized access");

        MoneyTransferResponse response = playerService.transferMoneyTo(new MoneyTransferRequest(transactionId, sender, recipient, amount));

        return new BalanceDto(response.getRequester().getId(), response.getRequester().getUsername(), response.getRequester().getBalance());
    }

    /**
     * Запрос денежных средств от другого пользователя
     *
     * @param requester     логин пользователя, запрашивающего денежные средства
     * @param donor         логин пользователя, который получит запрос на перевод денежных средств
     * @param amount        сумма
     * @param sessionId     идентификатор сессии
     * @param transactionId идентификатор транзакции
     */
    public boolean requestMoneyFrom(String requester, String donor, BigDecimal amount, UUID sessionId, UUID transactionId) throws UnauthorizedOperationException {
        if (authentications.get(sessionId) == null) throw new UnauthorizedOperationException("Unauthorized access");

        MoneyTransferResponse response = playerService.requestMoneyFrom(new MoneyTransferRequest(transactionId, donor, requester, amount));

        return response != null;
    }

    /**
     * Получение списка непринятых заявок на перевод денежных средств другим пользователям.
     *
     * @param login     логин пользователя
     * @param sessionId идентификтор сессии
     */
    public Collection<MoneyTransferRequest> getPendingMoneyRequests(String login, UUID sessionId) throws UnauthorizedOperationException {
        if (authentications.get(sessionId) == null) throw new UnauthorizedOperationException("Unauthorized access");

        return playerService.getPendingMoneyRequests(login);
    }

    /**
     * Получение истории о переводах денежных средств.
     *
     * @param login     логин игрока, который запрашивает историю
     * @param action    действие (кредит, дебит)
     * @param sessionId идентификатор сессии
     */
    public Collection<TransactionDto> getHistory(String login, PlayerAction action, UUID sessionId) throws UnauthorizedOperationException {
        if (authentications.get(sessionId) == null) throw new UnauthorizedOperationException("Unauthorized access");
        Collection<TransactionDto> history = playerService.getHistory(login, action);
        return history;
    }

    /**
     * Подтверждение денежных запросов от других пользователей на перевод денежных средств
     *
     * @param sessionId     идентификатор сессии
     * @param donorUsername никнейм пользователя, со счета которого будут списаны деньги
     * @param transactionId идентификатор транзакции
     */
    public MoneyTransferResponse approvePendingRequest(UUID sessionId, String donorUsername, UUID transactionId) throws UnauthorizedOperationException {
        if (authentications.get(sessionId) == null) throw new UnauthorizedOperationException("Unauthorized access");
        return playerService.approvePendingMoneyRequest(donorUsername, transactionId);
    }

    /**
     * Отклонение денежных запросов от других пользователей на перевод денежных средств
     *
     * @param sessionId     идентификатор сесии
     * @param donorUsername никнейм пользователя, на счет которого поступил запрос
     * @param transactionId идентификатор транзакции
     */
    public void declinePendingRequest(UUID sessionId, String donorUsername, UUID transactionId) throws UnauthorizedOperationException {
        if (authentications.get(sessionId) == null) throw new UnauthorizedOperationException("Unauthorized access");
        playerService.declinePendingRequest(donorUsername, transactionId);
    }

    /**
     * Удаление текущей сессии
     *
     * @param login     логин пользователя
     * @param sessionId идентификатор сессии
     */
    public void signOut(String login, UUID sessionId) {
        authentications.remove(sessionId);
    }
}
