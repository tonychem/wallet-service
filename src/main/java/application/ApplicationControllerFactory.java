package application;

/**
 * Фабрика контроллера уровня приложения.
 */
public class ApplicationControllerFactory {
    private static ApplicationController applicationController;

    private ApplicationControllerFactory() {
    }

    /**
     * @return Singleton объект типа ApplicationController.
     */
    public static ApplicationController getInstance() {
        if (applicationController == null) {
            applicationController = new ApplicationController();
        }
        return applicationController;
    }
}
