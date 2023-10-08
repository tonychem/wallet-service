package application;

public class ApplicationControllerFactory {
    private static ApplicationController applicationController;

    private ApplicationControllerFactory() {
    }

    public static ApplicationController getInstance() {
        if (applicationController == null) {
            applicationController = new ApplicationController();
        }
        return applicationController;
    }
}
