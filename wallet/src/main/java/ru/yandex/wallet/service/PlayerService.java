package ru.yandex.wallet.service;

import model.dto.in.*;
import model.dto.out.AuthenticatedPlayerDto;
import model.dto.out.BalanceDto;
import ru.yandex.wallet.domain.dto.MoneyTransferRequest;
import ru.yandex.wallet.domain.dto.MoneyTransferResponse;
import ru.yandex.wallet.domain.dto.TransactionDto;
import ru.yandex.wallet.exception.exceptions.BadCredentialsException;

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
     * @param transactionsList объект, содержащий коллекцию id транзакций в строковом представлении
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
