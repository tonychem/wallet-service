package ru.yandex.wallet.repository.jdbcimpl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.wallet.domain.Player;
import ru.yandex.wallet.exception.model.NoSuchPlayerException;
import ru.yandex.wallet.exception.model.PlayerAlreadyExistsException;
import ru.yandex.wallet.repository.PlayerCrudRepository;

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class PGJDBCPlayerCrudRepositoryImpl implements PlayerCrudRepository {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<Player> playerRowMapper = (rs, rowNum) -> {
        Player player = Player.builder()
                .id(rs.getLong("id"))
                .login(rs.getString("login"))
                .password(rs.getBytes("password"))
                .username(rs.getString("username"))
                .balance(rs.getBigDecimal("balance"))
                .build();
        return player;
    };

    @Override
    public Player create(Player player) {
        checkPlayerExists(player);

        String nextValQuery = "SELECT nextval('player_id_sequence')";
        String creationQuery = "INSERT INTO players (id, username, login, password, balance) VALUES (?,?,?,?,?)";

        long nextId = jdbcTemplate.queryForObject(nextValQuery, Long.class);
        player.setId(nextId);
        player.setBalance(BigDecimal.ZERO);

        jdbcTemplate.update(creationQuery, nextId, player.getUsername(), player.getLogin(),
                player.getPassword(), player.getBalance());

        return player;
    }

    @Override
    public void delete(Long id) {
        String deletePlayerQuery = "DELETE FROM players WHERE id = ?";
        jdbcTemplate.update(deletePlayerQuery, id);
    }

    @Override
    public Player getById(Long id) {
        String searchByIdQuery = "SELECT * FROM players WHERE id = ?";
        try {
            Player player = jdbcTemplate.queryForObject(searchByIdQuery, playerRowMapper, id);
            return player;
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchPlayerException(String.format("Пользователь с id=%d не существует", id));
        }
    }

    @Override
    public Player getByLogin(String login) {
        String searchByLoginQuery = "SELECT * from players WHERE login = ?";
        try {
            Player player = jdbcTemplate.queryForObject(searchByLoginQuery, playerRowMapper, login);
            return player;
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchPlayerException(String.format("Пользователь с логином login=%s не существует", login));
        }
    }

    @Override
    public Player getByUsername(String playerName) {
        String searchByUsername = "SELECT * FROM players WHERE username = ?";
        try {
            Player player = jdbcTemplate.queryForObject(searchByUsername, playerRowMapper, playerName);
            return player;
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchPlayerException(
                    String.format("Пользователь с именем username=%s не существует", playerName));
        }
    }

    @Override
    public Player setBalance(String login, BigDecimal newBalance) {
        String updateQuery = "UPDATE players SET balance = ? WHERE login = ?";
        jdbcTemplate.update(updateQuery, newBalance, login);
        return getByLogin(login);
    }

    /**
     * Метод проверяет, существует ли пользователь с таким логином или ником в БД.
     * Если существует, метод завершает свою работу с ошибкой.
     *
     * @param player новый игрок
     * @throws PlayerAlreadyExistsException
     */
    private void checkPlayerExists(Player player) {
        String loginSearchQuery = "SELECT * FROM players WHERE login = ?";
        String usernameSearchQuery = "SELECT * FROM players WHERE username = ?";

        SqlRowSet loginSearchRowSet = jdbcTemplate.queryForRowSet(loginSearchQuery, player.getLogin());
        SqlRowSet usernameSearchRowSet = jdbcTemplate.queryForRowSet(usernameSearchQuery, player.getUsername());

        if (loginSearchRowSet.next()) {
            throw new PlayerAlreadyExistsException(
                    String.format("Пользователь с таким логином login=%s уже существует", player.getLogin())
            );
        }

        if (usernameSearchRowSet.next()) {
            throw new PlayerAlreadyExistsException(
                    String.format("Пользователь с таким именем username=%s уже существует", player.getUsername())
            );
        }
    }
}
