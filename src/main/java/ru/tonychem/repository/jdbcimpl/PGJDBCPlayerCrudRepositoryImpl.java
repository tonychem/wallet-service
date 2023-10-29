package ru.tonychem.repository.jdbcimpl;

import ru.tonychem.domain.Player;
import ru.tonychem.exception.model.NoSuchPlayerException;
import ru.tonychem.exception.model.PlayerAlreadyExistsException;
import ru.tonychem.repository.PlayerCrudRepository;

import java.math.BigDecimal;
import java.sql.*;

public class PGJDBCPlayerCrudRepositoryImpl implements PlayerCrudRepository {

    private final String URL;
    private final String username;
    private final String password;

    public PGJDBCPlayerCrudRepositoryImpl(String URL, String username, String password, String schema) {
        this.username = username;
        this.password = password;
        this.URL = URL + "?currentSchema=" + schema;
    }

    @Override
    public Player create(Player player) {
        String nextValQuery = "SELECT nextval('player_id_sequence')";
        String creationQuery = "INSERT INTO players (id, username, login, password, balance) VALUES (?,?,?,?,?)";

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             Statement generatorStatement = connection.createStatement();
             PreparedStatement creationStatement = connection.prepareStatement(creationQuery)) {
            checkPlayerExists(connection, player);

            ResultSet generatorValueResultSet = generatorStatement.executeQuery(nextValQuery);

            long nextId = 0;
            if (generatorValueResultSet.next()) {
                nextId = generatorValueResultSet.getLong(1);
            }

            player.setId(nextId);
            player.setBalance(BigDecimal.ZERO);

            creationStatement.setLong(1, nextId);
            creationStatement.setString(2, player.getUsername());
            creationStatement.setString(3, player.getLogin());
            creationStatement.setBytes(4, player.getPassword());
            creationStatement.setBigDecimal(5, player.getBalance());
            creationStatement.executeUpdate();

            return player;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Long id) {
        String deletePlayerQuery = "DELETE FROM players WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement deleteStatement = connection.prepareStatement(deletePlayerQuery)) {
            deleteStatement.setLong(1, id);
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player getById(Long id) {
        String searchByIdQuery = "SELECT * FROM players WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement searchStatement = connection.prepareStatement(searchByIdQuery)) {
            searchStatement.setLong(1, id);
            ResultSet resultSet = searchStatement.executeQuery();

            if (resultSet.next()) {
                Player player = Player.builder()
                        .id(resultSet.getLong("id"))
                        .login(resultSet.getString("login"))
                        .password(resultSet.getBytes("password"))
                        .username(resultSet.getString("username"))
                        .balance(resultSet.getBigDecimal("balance"))
                        .build();
                return player;
            }

            throw new NoSuchPlayerException(
                    String.format("Пользователь с id=%d не существует", id)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player getByLogin(String login) {
        String searchByLoginQuery = "SELECT * from players WHERE login = ?";

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement searchStatement = connection.prepareStatement(searchByLoginQuery)) {
            searchStatement.setString(1, login);
            ResultSet resultSet = searchStatement.executeQuery();

            if (resultSet.next()) {
                Player player = Player.builder()
                        .id(resultSet.getLong("id"))
                        .login(resultSet.getString("login"))
                        .password(resultSet.getBytes("password"))
                        .username(resultSet.getString("username"))
                        .balance(resultSet.getBigDecimal("balance"))
                        .build();
                return player;
            }

            throw new NoSuchPlayerException(
                    String.format("Пользователь с логином login=%s не существует", login)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player getByUsername(String playerName) {
        String searchByUsername = "SELECT * FROM players WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement searchStatement = connection.prepareStatement(searchByUsername)) {
            searchStatement.setString(1, playerName);
            ResultSet resultSet = searchStatement.executeQuery();

            if (resultSet.next()) {
                Player player = Player.builder()
                        .id(resultSet.getLong("id"))
                        .login(resultSet.getString("login"))
                        .password(resultSet.getBytes("password"))
                        .username(resultSet.getString("username"))
                        .balance(resultSet.getBigDecimal("balance"))
                        .build();
                return player;
            }

            throw new NoSuchPlayerException(
                    String.format("Пользователь с именем пользователя username=%s не существует", username)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player setBalance(String login, BigDecimal newBalance) {
        String updateQuery = "UPDATE players SET balance = ? WHERE login = ?";

        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setBigDecimal(1, newBalance);
            updateStatement.setString(2, login);
            updateStatement.executeUpdate();
            return getByLogin(login);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод проверяет, существует ли пользователь с таким логином или ником в БД.
     * Если существует, метод завершает свою работу с ошибкой.
     *
     * @param connection соединение с БД
     * @param player     новый игрок
     * @throws PlayerAlreadyExistsException
     */
    private void checkPlayerExists(Connection connection, Player player) {
        String loginSearchQuery = "SELECT * FROM players WHERE login = ?";
        String usernameSearchQuery = "SELECT * FROM players WHERE username = ?";

        try (PreparedStatement loginSearchStatement = connection.prepareStatement(loginSearchQuery);
             PreparedStatement usernameSearchStatement = connection.prepareStatement(usernameSearchQuery)) {
            loginSearchStatement.setString(1, player.getLogin());
            boolean loginAlreadyExists = loginSearchStatement.executeQuery().next();

            usernameSearchStatement.setString(1, player.getUsername());
            boolean usernameAlreadyExists = usernameSearchStatement.executeQuery().next();

            if (loginAlreadyExists) throw new PlayerAlreadyExistsException(
                    String.format("Пользователь с таким логином login=%s уже существует", player.getLogin())
            );

            if (usernameAlreadyExists) throw new PlayerAlreadyExistsException(
                    String.format("Пользователь с таким именем username=%s уже существует", player.getUsername())
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
