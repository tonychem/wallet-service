package domain.repository;

import domain.exception.NoSuchPlayerException;
import domain.exception.PlayerAlreadyExistsException;
import domain.model.Player;
import domain.repository.inmemoryimpl.InMemoryPlayerCrudRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("In-memory player repository test class")
class InMemoryPlayerCrudRepositoryImplTest {

    private InMemoryPlayerCrudRepositoryImpl inMemoryPlayerCrudRepository;
    private Player adminPlayer;

    @BeforeEach
    public void init() {
        inMemoryPlayerCrudRepository = new InMemoryPlayerCrudRepositoryImpl();
        adminPlayer = Player.builder()
                .login("login")
                .password("password".getBytes())
                .username("username")
                .build();
    }

    @DisplayName("Player creation when input data is correct")
    @Test
    void shouldCreateUser() {
        Player player = inMemoryPlayerCrudRepository.create(adminPlayer);
        assertThat(player.getId()).isEqualTo(1L);
        assertThat(player.getUsername()).isEqualTo(adminPlayer.getUsername());
        assertThat(player.getBalance()).isEqualTo("0");
    }

    @DisplayName("Fetching absent player throws error")
    @Test
    void shouldThrowErrorWhenUserIsAbsent() {
        assertThatThrownBy(() -> inMemoryPlayerCrudRepository.getById(0L))
                .isInstanceOf(NoSuchPlayerException.class);
        assertThatThrownBy(() -> inMemoryPlayerCrudRepository.getByLogin("absent"))
                .isInstanceOf(NoSuchPlayerException.class);
        assertThatThrownBy(() -> inMemoryPlayerCrudRepository.getByUsername("present"))
                .isInstanceOf(NoSuchPlayerException.class);
    }

    @DisplayName("Fetching present user returns its data")
    @Test
    void shouldFindByUsername() {
        Player player = inMemoryPlayerCrudRepository.create(adminPlayer);

        Player playerFromDbByUsername = inMemoryPlayerCrudRepository.getByUsername(adminPlayer.getUsername());
        Player playerFromDbByLogin = inMemoryPlayerCrudRepository.getByLogin(adminPlayer.getLogin());

        assertThat(player.getUsername()).isEqualTo(playerFromDbByUsername.getUsername());
        assertThat(player.getLogin()).isEqualTo(playerFromDbByLogin.getLogin());
    }

    @DisplayName("Creating player with already existing username or login throws error")
    @Test
    void shouldThrowExceptionWhenDbAlreadyHasThisUsername() {
        inMemoryPlayerCrudRepository.create(adminPlayer);

        Player playerSameLogin = Player.builder()
                .login(adminPlayer.getLogin())
                .password("pwd".getBytes())
                .username("newUsername")
                .build();

        Player playerSameUsername = Player.builder()
                .login("someLogin")
                .password("pwd".getBytes())
                .username(adminPlayer.getUsername())
                .build();

        assertThatThrownBy(() -> inMemoryPlayerCrudRepository.create(playerSameLogin))
                .isInstanceOf(PlayerAlreadyExistsException.class);
        assertThatThrownBy(() -> inMemoryPlayerCrudRepository.create(playerSameUsername))
                .isInstanceOf(PlayerAlreadyExistsException.class);
    }

}