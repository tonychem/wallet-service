package domain.repository;

import domain.exception.NoSuchPlayerException;
import domain.exception.PlayerAlreadyExistsException;
import domain.model.Player;
import domain.model.dto.PlayerCreationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("In-memory player repository test class")
class InMemoryPlayerCrudRepositoryImplTest {

    private InMemoryPlayerCrudRepositoryImpl inMemoryPlayerCrudRepository;
    private PlayerCreationRequest adminPlayer;

    @BeforeEach
    public void init() {
        inMemoryPlayerCrudRepository = new InMemoryPlayerCrudRepositoryImpl();
        adminPlayer = new PlayerCreationRequest("login", "password".getBytes(), "username");
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

        PlayerCreationRequest playerCreationRequestSameLogin = new PlayerCreationRequest(adminPlayer.getLogin(),
                "pwd".getBytes(), "newUsername");
        PlayerCreationRequest playerCreationRequestSameUsername = new PlayerCreationRequest("someLogin",
                "pwd".getBytes(), adminPlayer.getUsername());

        assertThatThrownBy(() -> inMemoryPlayerCrudRepository.create(playerCreationRequestSameLogin))
                .isInstanceOf(PlayerAlreadyExistsException.class);
        assertThatThrownBy(() -> inMemoryPlayerCrudRepository.create(playerCreationRequestSameUsername))
                .isInstanceOf(PlayerAlreadyExistsException.class);
    }

}