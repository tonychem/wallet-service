package domain.repository;

import domain.exception.NoSuchPlayerException;
import domain.exception.UserAlreadyExistsException;
import domain.model.Player;
import domain.model.dto.PlayerCreationRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryPlayerCrudRepositoryImpl implements PlayerCrudRepository {

    private final Map<Long, Player> players = new HashMap<>();
    private long sequenceGenerator = 1;

    @Override
    public Player create(PlayerCreationRequest playerCreationRequest) {
        String newPlayerLogin = playerCreationRequest.getLogin();
        String newPlayerUsername = playerCreationRequest.getUsername();

        boolean loginAlreadyExists = players.values().stream()
                .anyMatch(player -> player.getLogin().equals(newPlayerLogin));

        boolean usernameAlreadyExists = players.values().stream()
                .anyMatch(player -> player.getUsername().equals(newPlayerUsername));

        if (loginAlreadyExists) throw new UserAlreadyExistsException(
                String.format("User with such login=%s already exists", newPlayerLogin)
        );

        if (usernameAlreadyExists) throw new UserAlreadyExistsException(
                String.format("User with such username=%s already exists", newPlayerUsername)
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
                String.format("User(id=%d) does not exist", id)
        );

        return player;
    }

    @Override
    public Player getByLogin(String login) {
        Optional<Player> playerOp = players.values().stream()
                .filter(player -> player.getLogin().equals(login))
                .findAny();

        return playerOp.orElseThrow(
                () -> new NoSuchPlayerException(
                        String.format("User(login=%s) does not exist", login)
                )
        );
    }
}
