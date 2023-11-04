package ru.tonychem.service;

import ru.tonychem.domain.dto.*;
import ru.tonychem.exception.model.BadCredentialsException;
import ru.tonychem.in.dto.*;

import java.util.Collection;

public interface PlayerService {

    /**
     * Аутентификация пользователя
     *
     * @param unsecuredAuthenticationRequest объект, содержащий связку логин-пароль в незашифрованном виде
     * @throws BadCredentialsException
     */
    AuthenticatedPlayerDto authenticate(UnsecuredAuthenticationRequestDto unsecuredAuthenticationRequest)
            throws BadCredentialsException;

    /**
     * Регистрация пользователя
     *
     * @param playerCreationRequest обертка над пользовательскими секретами (логин, пароль, ник) без шифрования
     * @throws BadCredentialsException
     */
    AuthenticatedPlayerDto register(UnsecuredPlayerCreationRequestDto playerCreationRequest) throws BadCredentialsException;

    /**
     * Получение баланса пользователя по идентификатору
     *
     * @param id идентификатор пользователя
     */
    BalanceDto getBalance(Long id);

    /**
     * Перевод денег от одного игрока к другому
     *
     * @param sender       логин отправителя денежных средств
     * @param moneyRequest объект, содержащий имя получателя и сумму
     * @return остаток денежных средств после перевода
     */
    BalanceDto transferMoneyTo(String sender, PlayerTransferMoneyRequestDto moneyRequest);

    /**
     * Отправить запрос на получение денег от другого пользователя
     *
     * @param requester пользователь, запрашивающий денежные средства
     * @param requestMoneyDto объект, содержащий параметры запроса: получателя запроса и сумму
     */
    MoneyTransferResponse requestMoneyFrom(String requester, PlayerRequestMoneyDto requestMoneyDto);

    /**
     * Получить список неподтвержденных запросов на отправку денег другим пользователям
     */
    Collection<MoneyTransferRequest> getPendingMoneyRequests(String login);

    /**
     * Подтвердить запрос на перевод денежной суммы игроку
     *
     * @param donorUsername       отправитель
     * @param transactionsListDto объект, содержащий коллекцию id транзакций в строковом представлении
     */
    Collection<MoneyTransferResponse> approvePendingMoneyRequest(String donorUsername, TransactionsListDto transactionsList);

    void declinePendingRequest(String donorUsername, TransactionsListDto transactionsList);

    /**
     * Получить историю транзакций пользователя
     *
     * @param action действия пользователя (списание или зачисление)
     */
    Collection<TransactionDto> getHistory(String login, PlayerAction action);
}
