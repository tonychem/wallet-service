package repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import util.ConfigFileReader;
import util.MigrationTool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Абстрактный класс, обязанностью которого является создание тест-контейнера, инициализации таблиц средствами миграции
 * и удаление всех тестовых данных после выполнения юнит-тестов в классах-наследниках
 */
@Testcontainers
public abstract class AbstractPGSQLRepositoryConsumer {

    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    protected Properties properties;

    /**
     * Метод подготоваливает БД для тестирования: создает схемы и применяет актуальную миграцию
     * Тестовой схемой для таблиц liquibase и бизнес-моделей является public
     */
    @BeforeEach
    public void init() throws IOException {
        assert postgres.isRunning();

        properties = ConfigFileReader.read("application-test.properties");
        setTestDbCredentialsToProperties();
        applyRecentMigration(properties);
    }

    @AfterEach
    public void destruct() {
        String url = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            dropCustomTables(statement);
            dropLiquibaseTables(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void dropLiquibaseTables(Statement statement) throws SQLException {
        String dropLockTableQuery = """
                DROP TABLE databasechangeloglock
                """;

        String dropChangeLogTableQuery = """
                DROP TABLE databasechangelog
                """;

        statement.execute(dropLockTableQuery);
        statement.execute(dropChangeLogTableQuery);
    }

    private void dropCustomTables(Statement statement) throws SQLException {
        String dropTablePlayersQuery = """
                DROP TABLE players CASCADE
                """;

        String dropPlayerSequenceQuery = """
                DROP SEQUENCE player_id_sequence
                """;

        String dropTransactionQuery = """
                DROP TABLE transactions
                 """;

        String dropLogsTableQuery = """
                DROP TABLE logs
                """;

        statement.execute(dropTablePlayersQuery);
        statement.execute(dropPlayerSequenceQuery);
        statement.execute(dropTransactionQuery);
        statement.execute(dropLogsTableQuery);
    }

    /**
     * Метод применяет последнюю актуальную миграцию.
     */
    private void applyRecentMigration(Properties properties) {
        MigrationTool.applyMigration(properties);
    }

    /**
     * Выставляет url, username и password тестовой БД для объекта properties.
     * Тестовой схемой для таблиц liquibase и бизнес-моделей является public
     */
    private void setTestDbCredentialsToProperties() {
        String url = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        properties.setProperty("jdbc.url", url);
        properties.setProperty("jdbc.username", username);
        properties.setProperty("jdbc.password", password);

        properties.setProperty("domain.schema.name", "public");
        properties.setProperty("liquibase.schema.name", "public");
    }
}
