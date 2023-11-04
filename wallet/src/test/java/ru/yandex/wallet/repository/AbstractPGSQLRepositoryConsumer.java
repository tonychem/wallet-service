package ru.yandex.wallet.repository;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Абстрактный класс, обязанностью которого является создание тест-контейнера, инициализации таблиц средствами миграции
 * и удаление всех тестовых данных после выполнения юнит-тестов в классах-наследниках
 */
@Testcontainers
//@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
public abstract class AbstractPGSQLRepositoryConsumer {

    @Value("${liquibase.changeLogFile}")
    private String changelogFile;

    @Value("${schema.domain.name}")
    protected String businessSchema;

    @Value("${liquibase.schema}")
    private String liquibaseSchema;

    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    /**
     * Метод уничтожает пользовательские и системные таблицы liquibase
     */
    @AfterEach
    public void dropTables() {
        try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(),
                postgres.getPassword());
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
}
