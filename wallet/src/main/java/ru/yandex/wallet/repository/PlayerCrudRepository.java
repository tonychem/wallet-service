package ru.yandex.wallet.repository;

import ru.yandex.wallet.domain.Player;

import java.math.BigDecimal;

/**
 * Базовый интерфейс взаимодействия с базой данных игроков и набором тривиальных методов.
 */
public interface PlayerCrudRepository {
    Player create(Player player);

    void delete(Long id);

    Player getById(Long id);

    Player getByLogin(String login);

    Player getByUsername(String username);

    Player setBalance(String login, BigDecimal newBalance);
}
