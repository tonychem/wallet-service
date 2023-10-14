import application.ApplicationController;
import application.ApplicationControllerFactory;
import domain.model.Player;
import domain.repository.inmemoryimpl.InMemoryPlayerCrudRepositoryImpl;
import domain.service.PlayerServiceImpl;
import infrastructure.ControllerUI;
import util.ConfigFileReader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class MainApplication {
    public static void main(String[] args) throws NoSuchAlgorithmException,
            NoSuchFieldException, IllegalAccessException, IOException {
//        populateBd();
        ConfigFileReader.read("application.properties");
        new ControllerUI().beginInteraction();
    }

    /**
     * Метод для заселения InMemory базы данных двумя пользователями: user(login=user, pwd=user) с балансом 2.0 и
     * admin(login=admin, pwd=admin) с балансом 10.0.
     * <b>ТОЛЬКО ДЛЯ ТЕСТИРОВАНИЯ IN MEMORY ПРИЛОЖЕНИЯ<b/>
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws NoSuchAlgorithmException
     */
    public static void populateBd() throws NoSuchFieldException, IllegalAccessException, NoSuchAlgorithmException {
        ApplicationController controller = ApplicationControllerFactory.getInstance();

        Field playerServiceField = ApplicationController.class
                .getDeclaredField("playerService");
        playerServiceField.setAccessible(true);

        PlayerServiceImpl playerService = (PlayerServiceImpl) playerServiceField.get(controller);

        Field playerCrudRepository = PlayerServiceImpl.class
                .getDeclaredField("playerRepository");
        playerCrudRepository.setAccessible(true);

        InMemoryPlayerCrudRepositoryImpl inMemoryPlayerCrudRepository = (InMemoryPlayerCrudRepositoryImpl)
                playerCrudRepository.get(playerService);

        Field playerMapField = InMemoryPlayerCrudRepositoryImpl.class
                .getDeclaredField("players");
        playerMapField.setAccessible(true);

        Field sequenceField = InMemoryPlayerCrudRepositoryImpl.class
                .getDeclaredField("sequenceGenerator");
        sequenceField.setAccessible(true);

        Long generator = (Long) sequenceField.get(inMemoryPlayerCrudRepository);
        Map<Long, Player> map = (Map<Long, Player>) playerMapField.get(inMemoryPlayerCrudRepository);

        map.put(generator,
                Player.builder()
                        .id(generator)
                        .login("admin")
                        .password(MessageDigest.getInstance("MD5").digest("admin".getBytes()))
                        .username("admin")
                        .balance(BigDecimal.TEN)
                        .build()
        );

        sequenceField.set(inMemoryPlayerCrudRepository, ++generator);

        map.put(generator,
                Player.builder()
                        .id(generator)
                        .login("user")
                        .password(MessageDigest.getInstance("MD5").digest("user".getBytes()))
                        .username("user")
                        .balance(BigDecimal.valueOf(2L))
                        .build()
        );

        sequenceField.set(inMemoryPlayerCrudRepository, ++generator);
    }
}
