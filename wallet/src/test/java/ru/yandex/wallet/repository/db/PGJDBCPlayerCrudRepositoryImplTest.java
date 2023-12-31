package ru.yandex.wallet.repository.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.wallet.domain.Player;
import ru.yandex.wallet.exception.exceptions.NoSuchPlayerException;
import ru.yandex.wallet.exception.exceptions.PlayerAlreadyExistsException;
import ru.yandex.wallet.repository.jdbcimpl.PGJDBCPlayerCrudRepositoryImpl;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Postgres Player Repository test")
@Transactional
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class PGJDBCPlayerCrudRepositoryImplTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PGJDBCPlayerCrudRepositoryImpl playerRepository;

    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void initRepository() {
        playerRepository = new PGJDBCPlayerCrudRepositoryImpl(jdbcTemplate);
    }

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
        assertThat(player.getLogin()).isEqualTo("admin");
        assertThat(player.getBalance().longValue()).isEqualTo(5L);
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

        assertThat(player.getId()).isEqualTo(1L);
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

        assertThat(player.getId()).isEqualTo(2L);
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
}