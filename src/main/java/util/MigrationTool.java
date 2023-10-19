package util;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Утилитарный класс для запуска миграции.
 */
public class MigrationTool {

    /**
     * Метод запускает миграцию БД, извлекая данные о подключении к БД из переменных среды.
     * Должен быть запущен после чтения данных из конфигурационного файла.
     *
     * @param properties объект, содержащий все пары ключ-значений свойств,
     *                   извлеченных из пользовательского файла-конфигурации
     */
    public static void applyMigration(Properties properties) {
        String url = properties.getProperty("jdbc.url");
        String username = properties.getProperty("jdbc.username");
        String password = properties.getProperty("jdbc.password");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(properties.getProperty("liquibase.changelogFile.path"),
                    new ClassLoaderResourceAccessor(), database);

            database.setDefaultSchemaName(properties.getProperty("domain.schema.name"));
            database.setLiquibaseSchemaName(properties.getProperty("liquibase.schema.name"));

            liquibase.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
