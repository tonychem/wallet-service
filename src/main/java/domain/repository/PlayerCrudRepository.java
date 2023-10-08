package domain.repository;

import domain.model.Player;
import domain.model.dto.PlayerCreationRequest;

public interface PlayerCrudRepository {
    Player create(PlayerCreationRequest playerCreationRequest);

    void delete(Long id);

    Player getById(Long id);

    Player getByLogin(String login);

    Player getByUsername(String username);
}
