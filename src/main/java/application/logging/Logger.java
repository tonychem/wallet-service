package application.logging;

/**
 * Простой логгер, поддерживающий различный уровень логгирования
 */
public interface Logger {
    default void trace(String message) {
        logMessage(LoggingLevel.TRACE, message);
    }

    default void debug(String message) {
        logMessage(LoggingLevel.DEBUG, message);

    }

    default void info(String message) {
        logMessage(LoggingLevel.INFO, message);

    }

    default void warn(String message) {
        logMessage(LoggingLevel.WARN, message);
    }

    default void error(String message) {
        logMessage(LoggingLevel.ERROR, message);
    }

    void logMessage(LoggingLevel level, String message);
}
