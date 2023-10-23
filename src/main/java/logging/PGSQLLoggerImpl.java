package logging;

import java.sql.*;

public class PGSQLLoggerImpl implements Logger {
    private final String username;
    private final String password;
    private final String URL;
    private final String schema;

    public PGSQLLoggerImpl() {
        this.schema = System.getProperty("domain.schema.name");
        this.username = System.getProperty("jdbc.username");
        this.password = System.getProperty("jdbc.password");
        this.URL = System.getProperty("jdbc.url") + "?currentSchema=" + schema;
    }

    public PGSQLLoggerImpl(String username, String password, String URL, String schema) {
        this.username = username;
        this.password = password;
        this.schema = schema;
        this.URL = URL + "?currentSchema=" + schema;
    }

    @Override
    public void logMessage(LoggingLevel level, String message) {
        String insertLogQuery = """
                INSERT INTO logs(datetime, level, message) VALUES (?,?,?)
                """;
        try (Connection connection = DriverManager.getConnection(URL, username, password);
             PreparedStatement statement = connection.prepareStatement(insertLogQuery)) {
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setString(2, level.name());
            statement.setString(3, message);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
