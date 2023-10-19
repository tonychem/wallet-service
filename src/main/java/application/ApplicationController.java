package application;

import application.dto.AuthenticationDto;
import application.dto.AuthenticationRequest;
import application.dto.BalanceDto;
import application.exception.UnauthorizedOperationException;
import domain.dto.*;
import exception.BadCredentialsException;
import service.PlayerAction;
import service.PlayerService;
import service.PlayerServiceImpl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Класс-контроллер, ответственный за распределение запросов от UI к сервисам.
 * В классе хранится множество авторизованных сессий. Обслуживание запросов пользователя невозможно без
 * предоставления идентификатора сессии. Чтобы получить идентификатор сессии необходимо зарегистрировать
 * нового пользователя или пройти авторизацию.
 */
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
        authentications.add(sessionId);
        return new AuthenticationDto(authentication.getId(), authentication.getLogin(),
                authentication.getUsername(),
                authentication.getSessionID(), authenticatedPlayerDto.getBalance());
    }

    /**
     * Аутентификация пользователя. Метод сохраняет в множество текущих сессий авторизованного пользователя.
     *
     * @param request класс-обертка над пользовательскими секретами (логин, пароль)
     */
    public AuthenticationDto authenticate(AuthenticationRequest request) throws BadCredentialsException {
        AuthenticatedPlayerDto authenticatedPlayerDto = playerService.authenticate(request.getLogin(),
                request.getPassword());
        UUID sessionId = UUID.randomUUID();

        authentications.add(sessionId);

        return new AuthenticationDto(authenticatedPlayerDto.getId(), authenticatedPlayerDto.getLogin(),
                authenticatedPlayerDto.getUsername(),
                sessionId, authenticatedPlayerDto.getBalance());
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
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");
        AuthenticatedPlayerDto playerDto = playerService.getBalance(id);
        return new BalanceDto(playerDto.getId(), playerDto.getUsername(), playerDto.getBalance());
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

    /**
     * Запрос денежных средств от другого пользователя
     *
     * @param requester     логин пользователя, запрашивающего денежные средства
     * @param donor         логин пользователя, который получит запрос на перевод денежных средств
     * @param amount        сумма
     * @param sessionId     идентификатор сессии
     * @param transactionId идентификатор транзакции
     */
    public boolean requestMoneyFrom(String requester, String donor, BigDecimal amount, UUID sessionId,
                                    UUID transactionId) throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");

        MoneyTransferResponse response = playerService.requestMoneyFrom(new MoneyTransferRequest(
                transactionId, donor, requester, amount
        ));

        return response != null;
    }

    /**
     * Получение списка непринятых заявок на перевод денежных средств другим пользователям.
     *
     * @param login     логин пользователя
     * @param sessionId идентификтор сессии
     */
    public Collection<MoneyTransferRequest> getPendingMoneyRequests(String login, UUID sessionId)
            throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");

        return playerService.getPendingMoneyRequests(login);
    }

    /**
     * Получение истории о переводах денежных средств.
     *
     * @param login     логин игрока, который запрашивает историю
     * @param action    действие (кредит, дебит)
     * @param sessionId идентификатор сессии
     */
    public Collection<TransactionDto> getHistory(String login, PlayerAction action, UUID sessionId)
            throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");
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
    public MoneyTransferResponse approvePendingRequest(UUID sessionId,
                                                       String donorUsername,
                                                       UUID transactionId)
            throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");
        return playerService.approvePendingMoneyRequest(donorUsername, transactionId);
    }

    /**
     * Отклонение денежных запросов от других пользователей на перевод денежных средств
     *
     * @param sessionId     идентификатор сесии
     * @param donorUsername никнейм пользователя, на счет которого поступил запрос
     * @param transactionId идентификатор транзакции
     */
    public void declinePendingRequest(UUID sessionId, String donorUsername, UUID transactionId)
            throws UnauthorizedOperationException {
        if (!authentications.contains(sessionId)) throw new UnauthorizedOperationException("Unauthorized access");
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
