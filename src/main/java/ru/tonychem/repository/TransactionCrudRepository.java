package ru.tonychem.repository;

import ru.tonychem.domain.Transaction;
import ru.tonychem.domain.TransferRequestStatus;
import ru.tonychem.domain.dto.MoneyTransferRequest;

import java.util.Collection;
import java.util.UUID;

/**
 * Базовый интерфейс взаимодействия с базой данных транзакций и набором тривиальных методов.
 */
public interface TransactionCrudRepository {
    Transaction create(MoneyTransferRequest request);

    Transaction getById(UUID id);

    /**
     * Метод для получения списка транзакций
     * @param sender отправитель денег (если null - то логин отправителя игнорируется)
     * @param recipient получатель денег (если null - то логин получателя игнорируется)
     * @param status статус транзакции (если null - то статус любой)
     */
    Collection<Transaction> getTransactionsBySenderAndRecipientAndStatus(String sender, String recipient,
                                                                         TransferRequestStatus status);

    /**
     * Получить все дебитовые транзакции пользователя
     * @param login
     */
    Collection<Transaction> getDebitingTransactions(String login);

    /**
     * Получить все кредитовые транзакции пользователя
     * @param login
     */
    Collection<Transaction> getCreditingTransactions(String login);

    /**
     * Подтвердить транзакцию (перевод денег)
     * @param donorUsername логин отправителя
     * @param id идентификатор транзакции
     */
    Transaction approveTransaction(String donorUsername, UUID id);

    /**
     * Отклонить транзакцию (перевод денег)
     * @param donorUsername логин отправителя
     * @param id идентификатор транзакции
     */
    Transaction declineTransaction(String donorUsername, UUID id);

    /**
     * Установить статус транзакции в состояние FAILED
     * @param id идентификатор транзакции
     * @return
     */
    Transaction setFailed(UUID id);
}
