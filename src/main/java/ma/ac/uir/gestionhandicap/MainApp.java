package ma.ac.uir.gestionhandicap;

import javafx.application.Application;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setMaximized(true);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        SceneNavigator.goToLogin(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}