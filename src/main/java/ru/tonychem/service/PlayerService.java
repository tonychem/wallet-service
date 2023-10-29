package ru.tonychem.service;

import ru.tonychem.domain.dto.*;
import ru.tonychem.exception.model.BadCredentialsException;

import java.util.Collection;
import java.util.UUID;

public interface PlayerService {

    /**
     * Аутентификация пользователя
     *
     * @param login    логин пользователя для аутентификации
     * @param password пароль в зашифрованном виде (MD5 алгоритм)
     * @throws BadCredentialsException
     */
    AuthenticatedPlayerDto authenticate(String login, byte[] password) throws BadCredentialsException;

    /**
     * Регистрация пользователя
     *
     * @param playerCreationRequest обертка над пользовательскими секретами (логин, пароль, ник)
     * @throws BadCredentialsException
     */
    AuthenticatedPlayerDto register(PlayerCreationRequest playerCreationRequest) throws BadCredentialsException;

    /**
     * Получение баланса пользователя по идентификатору
     *
     * @param id идентификатор пользователя
     */
    AuthenticatedPlayerDto getBalance(Long id);

    /**
     * Перевод денег от одного игрока к другому
     *
     * @param moneyTransferRequest обертка над параметрами запроса
     */
    MoneyTransferResponse transferMoneyTo(MoneyTransferRequest moneyTransferRequest);

    /**
     * Отправить запрос на получение денег от другого пользователя
     *
     * @param moneyTransferRequest обертка над параметрами запроса
     */
    MoneyTransferResponse requestMoneyFrom(MoneyTransferRequest moneyTransferRequest);

    /**
     * Получить список неподтвержденных запросов на отправку денег другим пользователям
     */
    Collection<MoneyTransferRequest> getPendingMoneyRequests(String login);

    /**
     * Подтвердить запрос на перевод денежной суммы игроку
     *
     * @param donorUsername отправитель
     * @param requestId     идентификатор транзакции
     */
    MoneyTransferResponse approvePendingMoneyRequest(String donorUsername, UUID requestId);

    void declinePendingRequest(String donorUsername, UUID requestId);

    /**
     * Получить историю транзакций пользователя
     *
     * @param action действия пользователя (списание или зачисление)
     */
    Collection<TransactionDto> getHistory(String login, PlayerAction action);
}
