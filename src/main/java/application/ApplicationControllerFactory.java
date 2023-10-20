package application;

import application.logging.PGSQLLoggerImpl;

/**
 * Фабрика контроллера уровня приложения.
 */
public class ApplicationControllerFactory {
    private static ApplicationController applicationController;

    private ApplicationControllerFactory() {
    }

    /**
     * @return Singleton объект типа ApplicationController. По умолчанию, возвращает логируемый LoggedApplocationController.
     */
    public static ApplicationController getInstance() {
        if (applicationController == null) {
            applicationController = new LoggedApplicationController(new PGSQLLoggerImpl());
        }
        return applicationController;
    }
}
