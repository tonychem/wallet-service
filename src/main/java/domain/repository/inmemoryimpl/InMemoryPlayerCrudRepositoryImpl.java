package domain.repository.inmemoryimpl;

import domain.exception.NoSuchPlayerException;
import domain.exception.PlayerAlreadyExistsException;
import domain.model.Player;
import domain.repository.PlayerCrudRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Реализация БД игроков в оперативной памяти.
 */
public class InMemoryPlayerCrudRepositoryImpl implements PlayerCrudRepository {

    private final Map<Long, Player> players = new HashMap<>();
    private long sequenceGenerator = 1;

    /**
     * Создание пользователя.
     *
     * @param newPlayer новый игрок, содержащий поля username, login, password.
     */
    @Override
    public Player create(Player newPlayer) {
        boolean loginAlreadyExists = players.values().stream()
                .anyMatch(player -> player.getLogin().equals(newPlayer.getLogin()));

        boolean usernameAlreadyExists = players.values().stream()
                .anyMatch(player -> player.getUsername().equals(newPlayer.getUsername()));

        if (loginAlreadyExists) throw new PlayerAlreadyExistsException(
                String.format("Пользователь с таким логином login=%s уже существует", newPlayer.getLogin())
        );

        if (usernameAlreadyExists) throw new PlayerAlreadyExistsException(
                String.format("Пользователь с таким именем username=%s уже существует", newPlayer.getUsername())
        );

        newPlayer.setId(sequenceGenerator);
        newPlayer.setBalance(BigDecimal.ZERO);

        players.put(sequenceGenerator, newPlayer);
        sequenceGenerator++;

        return newPlayer;
    }

    @Override
    public void delete(Long id) {
        players.remove(id);
    }

    @Override
    public Player getById(Long id) {
        Player player = players.get(id);

        if (player == null) throw new NoSuchPlayerException(
                String.format("Пользователь с id=%d не существует", id)
        );

        return player;
    }

    @Override
    public Player getByUsername(String username) {
        Optional<Player> playerOp = players.values().stream()
                .filter(player -> player.getUsername().equals(username))
                .findAny();

        return playerOp.orElseThrow(
                () -> new NoSuchPlayerException(
                        String.format("Пользователь с именем пользователя username=%s не существует", username)
                )
        );
    }

    @Override
    public Player getByLogin(String login) {
        Optional<Player> playerOp = players.values().stream()
                .filter(player -> player.getLogin().equals(login))
                .findAny();

        return playerOp.orElseThrow(
                () -> new NoSuchPlayerException(
                        String.format("Пользователь с логином login=%s не существует", login)
                )
        );
    }

    @Override
    public Player setBalance(String login, BigDecimal newBalance) {
        Player player = getByLogin(login);
        player.setBalance(newBalance);
        return player;
    }
}
