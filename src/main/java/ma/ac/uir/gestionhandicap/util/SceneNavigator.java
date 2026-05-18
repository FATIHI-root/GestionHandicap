package ma.ac.uir.gestionhandicap.util;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneNavigator {

    public static void changeScene(Stage stage, String fxmlPath, String title) {
        try {
            boolean wasMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            stage.setTitle(title);
            stage.setMaximized(false);
            stage.setScene(scene);
            stage.show();

            if (wasMaximized) {
                Platform.runLater(() -> stage.setMaximized(true));
            }

            FadeTransition fade = new FadeTransition(Duration.millis(220), scene.getRoot());
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();

        } catch (IOException e) {
            AlertUtil.showError("Erreur", "Impossible de charger la page : " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void goToLogin(Stage stage) {
        changeScene(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    public static void goToRegister(Stage stage) {
        changeScene(stage, "/fxml/auth/register.fxml", "Inscription");
    }

    public static void goToAdminDashboard(Stage stage) {
        changeScene(stage, "/fxml/admin/admin_dashboard.fxml", "Dashboard Administrateur");
    }

    public static void goToUserDashboard(Stage stage) {
        changeScene(stage, "/fxml/user/user_dashboard.fxml", "Dashboard Utilisateur");
    }

    public static void goToMesDemandes(Stage stage) {
        changeScene(stage, "/fxml/user/mes_demandes.fxml", "Mes demandes");
    }

    public static void goToNouvelleDemande(Stage stage) {
        changeScene(stage, "/fxml/user/nouvelle_demande.fxml", "Nouvelle demande");
    }

    public static void goToNouvelleReclamation(Stage stage) {
        changeScene(stage, "/fxml/user/nouvelle_reclamation.fxml", "Nouvelle réclamation");
    }

    public static void goToValidationComptes(Stage stage) {
        changeScene(stage, "/fxml/admin/validation_comptes.fxml", "Validation des comptes");
    }

    public static void goToProfilUser(Stage stage) {
        changeScene(stage, "/fxml/user/profil.fxml", "Mon profil");
    }

    public static void goToProfilAdmin(Stage stage) {
        changeScene(stage, "/fxml/admin/profil.fxml", "Mon profil");
    }

    public static void goToAdminDemandes(Stage stage) {
        changeScene(stage, "/fxml/admin/admin_demandes.fxml", "Gestion des demandes");
    }

    public static void goToMesReclamations(Stage stage) {
        changeScene(stage, "/fxml/user/mes_reclamations.fxml", "Mes réclamations");
    }

    public static void goToAdminReclamations(Stage stage) {
        changeScene(stage, "/fxml/admin/admin_reclamations.fxml", "Gestion des réclamations");
    }

    public static void goToStatistiques(Stage stage) {
        changeScene(stage, "/fxml/admin/statistiques.fxml", "Statistiques");
    }

    public static void goToArchivage(Stage stage) {
        changeScene(stage, "/fxml/admin/archives.fxml", "Archivage");
    }

    public static void goToSaisons(Stage stage) {
        changeScene(stage, "/fxml/admin/saisons.fxml", "Saisons académiques");
    }
}