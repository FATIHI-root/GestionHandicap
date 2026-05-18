package ma.ac.uir.gestionhandicap.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.exception.AuthenticationException;
import ma.ac.uir.gestionhandicap.exception.ValidationException;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.Role;
import ma.ac.uir.gestionhandicap.service.AuthService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;
import ma.ac.uir.gestionhandicap.util.ValidatorUtil;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        passwordField.setOnAction(e -> handleLogin());
        emailField.setOnAction(e -> passwordField.requestFocus());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (ValidatorUtil.isEmpty(email) || ValidatorUtil.isEmpty(password)) {
            AlertUtil.showWarning("Champs vides", "Veuillez remplir tous les champs.");
            return;
        }

        if (!ValidatorUtil.isValidEmail(email)) {
            AlertUtil.showWarning("Email invalide", "Veuillez saisir un email valide.");
            return;
        }

        try {
            Utilisateur utilisateur = authService.login(email, password);

            SessionManager.setCurrentUser(utilisateur);

            Stage stage = (Stage) emailField.getScene().getWindow();

            if (utilisateur.getRole() == Role.ADMIN) {
                SceneNavigator.goToAdminDashboard(stage);
            } else {
                SceneNavigator.goToUserDashboard(stage);
            }

        } catch (AuthenticationException e) {
            AlertUtil.showError("Erreur de connexion", e.getMessage());
        } catch (ValidationException e) {
            AlertUtil.showWarning("Erreur de validation", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Une erreur est survenue.");
        }
    }

    @FXML
    private void handleGoToRegister() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.goToRegister(stage);
    }
}