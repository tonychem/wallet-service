package ru.tonychem.logging;

import java.sql.*;

public class PGSQLLoggerImpl implements Logger {
    private final String username;
    private final String password;
    private final String URL;

    public PGSQLLoggerImpl(String username, String password, String URL, String schema) {
        this.username = username;
        this.password = password;
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
