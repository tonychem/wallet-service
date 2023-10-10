package domain.service;

import domain.exception.BadCredentialsException;
import domain.model.dto.*;

import java.util.Collection;
import java.util.UUID;

public interface PlayerService {

    AuthenticatedPlayerDto authenticate(String login, byte[] password) throws BadCredentialsException;

    AuthenticatedPlayerDto register(PlayerCreationRequest playerCreationRequest) throws BadCredentialsException;

    AuthenticatedPlayerDto getBalance(Long id);

    /**
     * Перевод денег от одного игрока к другому
     * @param moneyTransferRequest обертка над параметрами запроса
     */
    MoneyTransferResponse transferMoneyTo(MoneyTransferRequest moneyTransferRequest);

    /**
     * Отправить запрос на получение денег от другого пользователя
     * @param moneyTransferRequest обертка над параметрами запроса
     */
    MoneyTransferResponse requestMoneyFrom(MoneyTransferRequest moneyTransferRequest);

    /**
     * Получить список неподтвержденных запросов на отправку денег другим пользователям
     */
    Collection<MoneyTransferRequest> getPendingMoneyRequests(String login);

    /**
     * Подтвердить запрос на перевод денежной суммы игроку
     * @param donorUsername отправитель
     * @param requestId идентификатор транзакции
     */
    MoneyTransferResponse approvePendingMoneyRequest(String donorUsername, UUID requestId);

    void declinePendingRequest(String donorUsername, UUID requestId);

    /**
     * Получить историю транзакций пользователя
     * @param action действия пользователя (списание или зачисление)
     */
    Collection<TransactionDto> getHistory(String login, PlayerAction action);
}
