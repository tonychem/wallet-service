package domain.repository;

import domain.model.Player;
import domain.model.dto.PlayerCreationRequest;

/**
 * Базовый интерфейс взаимодействия с базой данных игроков и набором тривиальных методов.
 */
public interface PlayerCrudRepository {
    Player create(Player player);

    void delete(Long id);

    Player getById(Long id);

    Player getByLogin(String login);

    Player getByUsername(String username);
}
