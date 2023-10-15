package util;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Утилитарный класс для запуска миграции.
 */
public class MigrationTool {

    /**
     * Метод запускает миграцию БД, извлекая данные о подключении к БД из переменных среды.
     * Должен быть запущен после чтения данных из конфигурационного файла.
     * @param changeLogFilePath Относительный путь до главного changelog файла
     */
    public static void applyMigration(String changeLogFilePath) {
        String url = System.getProperty("jdbc.url");
        String username = System.getProperty("jdbc.username");
        String password = System.getProperty("jdbc.password");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(changeLogFilePath, new ClassLoaderResourceAccessor(), database);

            database.setDefaultSchemaName(System.getProperty("domain.schema.name"));
            database.setLiquibaseSchemaName(System.getProperty("liquibase.schema.name"));

            liquibase.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
