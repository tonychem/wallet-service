package domain.repository;

import domain.exception.NoSuchPlayerException;
import domain.exception.PlayerAlreadyExistsException;
import domain.model.Player;
import domain.model.dto.PlayerCreationRequest;

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
     * @param playerCreationRequest обертка над входящими секретами - логин, ник, пароль. Логин и пароль должны быть уникальны.
     */
    @Override
    public Player create(PlayerCreationRequest playerCreationRequest) {
        String newPlayerLogin = playerCreationRequest.getLogin();
        String newPlayerUsername = playerCreationRequest.getUsername();

        boolean loginAlreadyExists = players.values().stream()
                .anyMatch(player -> player.getLogin().equals(newPlayerLogin));

        boolean usernameAlreadyExists = players.values().stream()
                .anyMatch(player -> player.getUsername().equals(newPlayerUsername));

        if (loginAlreadyExists) throw new PlayerAlreadyExistsException(
                String.format("Пользователь с таким логином login=%s уже существует", newPlayerLogin)
        );

        if (usernameAlreadyExists) throw new PlayerAlreadyExistsException(
                String.format("Пользователь с таким именем username=%s уже существует", newPlayerUsername)
        );

        Player newPlayer = Player.builder()
                .id(sequenceGenerator)
                .username(newPlayerUsername)
                .login(newPlayerLogin)
                .password(playerCreationRequest.getPassword())
                .balance(BigDecimal.ZERO)
                .build();

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
                        String.format("Пользователь с логином login=%s не существует", username)
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


}
