package domain.repository.db;

import domain.exception.NoSuchPlayerException;
import domain.exception.PlayerAlreadyExistsException;
import domain.model.Player;
import domain.repository.jdbcimpl.PGJDBCPlayerCrudRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dockerized Postgresql Player Repository test class")
@Testcontainers
class PGJDBCPlayerCrudRepositoryImplTest {
    @Container
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    private PGJDBCPlayerCrudRepositoryImpl playerRepository;


    @DisplayName("Should create a new unique player")
    @Test
    public void shouldReturnPlayerWhenCreatingUniquePlayer() {
        Player player = Player.builder()
                .login("unique_player")
                .username("unique_player")
                .password("unique_player".getBytes())
                .build();

        Player playerFromDb = playerRepository.create(player);

        assertThat(playerFromDb.getId()).isEqualTo(3L);
        assertThat(playerFromDb.getLogin()).isEqualTo(player.getLogin());
        assertThat(playerFromDb.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("Should fail when creating a player with same login")
    @Test
    public void shouldThrowExceptionWhenCreatingPlayerWithAlreadyExistingLogin() {
        Player player = Player.builder()
                .login("admin")
                .username("admin_another")
                .password("admin_another".getBytes())
                .build();

        assertThatThrownBy(() -> playerRepository.create(player))
                .isInstanceOf(PlayerAlreadyExistsException.class);
    }

    @DisplayName("Should fail when creating a player with same username")
    @Test
    public void shouldThrowExceptionWhenCreatingPlayerWithAlreadyExistingUsername() {
        Player player = Player.builder()
                .login("admin_another")
                .username("admin")
                .password("admin_another".getBytes())
                .build();

        assertThatThrownBy(() -> playerRepository.create(player))
                .isInstanceOf(PlayerAlreadyExistsException.class);
    }

    @DisplayName("Should retrieve user by id")
    @Test
    public void shouldReturnPlayerByIdWhenPlayerExists() {
        Player player = playerRepository.getById(1L);

        assertThat(player.getId()).isEqualTo(1L);
        assertThat(player.getLogin()).isEqualTo("user");
        assertThat(player.getBalance().longValue()).isEqualTo(10L);
    }

    @DisplayName("Should throw exception when passing id for user that does not exist")
    @Test
    public void shouldThrowExceptionWhenPlayerByIdDoesNotExist() {
        assertThatThrownBy(() -> playerRepository.getById(100L))
                .isInstanceOf(NoSuchPlayerException.class);
    }

    @DisplayName("Should retrieve user by login")
    @Test
    public void shouldReturnPlayerByLoginWhenPlayerExists() {
        Player player = playerRepository.getByLogin("admin");

        assertThat(player.getId()).isEqualTo(2L);
        assertThat(player.getLogin()).isEqualTo("admin");
        assertThat(player.getBalance().longValue()).isEqualTo(5L);
    }

    @DisplayName("Should throw exception when passing login for user that does not exist")
    @Test
    public void shouldThrowExceptionWhenPlayerByLoginDoesNotExist() {
        assertThatThrownBy(() -> playerRepository.getByLogin("no-op"))
                .isInstanceOf(NoSuchPlayerException.class);
    }

    @DisplayName("Should retrieve user by username")
    @Test
    public void shouldReturnPlayerByUsernameWhenPlayerExists() {
        Player player = playerRepository.getByUsername("user");

        assertThat(player.getId()).isEqualTo(1L);
        assertThat(player.getLogin()).isEqualTo("user");
        assertThat(player.getBalance().longValue()).isEqualTo(10L);
    }

    @DisplayName("Should throw exception when passing username for user that does not exist")
    @Test
    public void shouldThrowExceptionWhenPlayerByUsernameDoesNotExist() {
        assertThatThrownBy(() -> playerRepository.getByUsername("no-op"))
                .isInstanceOf(NoSuchPlayerException.class);
    }

    @DisplayName("Should set balance for existing user")
    @Test
    public void shouldReturnPlayerWithUpdatedBalanceIfPlayerExists() {
        Player player = playerRepository.setBalance("admin", BigDecimal.TEN);

        assertThat(player.getLogin()).isEqualTo("admin");
        assertThat(player.getBalance().longValue()).isEqualTo(10L);
    }

    @DisplayName("Should throw exception when setting balance for non-existing user")
    @Test
    public void shouldThrowExceptionWhenUpdatingBalanceForNonExistingPlayer() {
        assertThatThrownBy(() -> playerRepository.setBalance("no-op", BigDecimal.TEN))
                .isInstanceOf(NoSuchPlayerException.class);
    }

    /**
     * Метод подготоваливает БД для тестирования: создает таблицу players,
     * и вносит двух пользователей (user со счетом 10 и admin со счетом 5);
     */
    @BeforeEach
    public void init() {
        assert postgres.isRunning();

        String url = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        String createTablePlayersQuery = """
                CREATE TABLE IF NOT EXISTS players (
                    id int8 PRIMARY KEY UNIQUE NOT NULL,
                    username varchar(255) UNIQUE NOT NULL,
                    login varchar(50) UNIQUE NOT NULL,
                    password bytea NOT NULL,
                    balance numeric(15,3) NOT NULL
                )
                """;

        String createSequenceQuery = """
                CREATE SEQUENCE IF NOT EXISTS player_id_sequence
                """;

        String insertUserPlayerQuery = """
                INSERT INTO players(id, username, login, password, balance)
                    VALUES (nextval('player_id_sequence'), 'user', 'user', '\\xee11cbb19052e40b07aac0ca060c23ee', 10.000)
                """;

        String insertAdminPlayerQuery = """
                INSERT INTO players(id, username, login, password, balance)
                    VALUES (nextval('player_id_sequence'), 'admin', 'admin', '\\x21232f297a57a5a743894a0e4a801fc3', 5.000)
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Statement statement = connection.createStatement();
            statement.execute(createTablePlayersQuery);
            statement.execute(createSequenceQuery);
            statement.execute(insertUserPlayerQuery);
            statement.execute(insertAdminPlayerQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        playerRepository = new PGJDBCPlayerCrudRepositoryImpl(url, username, password, "public");
    }

    @AfterEach
    public void destruct() {
        String url = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        String dropTablePlayersQuery = """
                DROP TABLE players
                """;

        String dropSequenceQuery = """
                DROP SEQUENCE player_id_sequence
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Statement statement = connection.createStatement();
            statement.execute(dropTablePlayersQuery);
            statement.execute(dropSequenceQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}