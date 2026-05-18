package ma.ac.uir.gestionhandicap.controller.admin;

import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.exception.AuthenticationException;
import ma.ac.uir.gestionhandicap.exception.ValidationException;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.service.UtilisateurService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;

public class ProfilController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField telephoneField;

    @FXML
    private PasswordField ancienPasswordField;

    @FXML
    private PasswordField nouveauPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label emailLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label statutLabel;

    private UtilisateurService utilisateurService = new UtilisateurService();

    private DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user == null) return;

        userNameLabel.setText(user.getNomComplet());
        if (user.getPrenom() != null && user.getPrenom().length() > 0) {
            avatarInitialLabel.setText(user.getPrenom().substring(0, 1).toUpperCase());
        }

        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        telephoneField.setText(user.getTelephone() == null ? "" : user.getTelephone());

        emailLabel.setText(user.getEmail());
        roleLabel.setText(user.getRole() == null ? "—" : user.getRole().name());
        dateLabel.setText(user.getDateInscription() == null ? "—" : user.getDateInscription().format(dateFmt));
        statutLabel.setText(user.getStatutCompte() == null ? "—" : user.getStatutCompte().name());
    }

    @FXML
    private void handleSaveProfile() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user == null) return;

        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String tel = telephoneField.getText();

        try {
            utilisateurService.updateProfile(user.getIdUtilisateur(), nom, prenom, tel);
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setTelephone(tel);
            userNameLabel.setText(user.getNomComplet());
            if (prenom != null && prenom.length() > 0) {
                avatarInitialLabel.setText(prenom.substring(0, 1).toUpperCase());
            }
            AlertUtil.showInfo("Profil mis à jour", "Vos informations ont été enregistrées.");
        } catch (ValidationException e) {
            AlertUtil.showWarning("Validation", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de mettre à jour le profil.");
        }
    }

    @FXML
    private void handleChangePassword() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user == null) return;

        String ancien = ancienPasswordField.getText();
        String nouveau = nouveauPasswordField.getText();
        String confirmation = confirmPasswordField.getText();

        try {
            utilisateurService.changePassword(user.getIdUtilisateur(), ancien, nouveau, confirmation);
            ancienPasswordField.clear();
            nouveauPasswordField.clear();
            confirmPasswordField.clear();
            AlertUtil.showInfo("Mot de passe modifié", "Votre mot de passe a été mis à jour.");
        } catch (ValidationException e) {
            AlertUtil.showWarning("Validation", e.getMessage());
        } catch (AuthenticationException e) {
            AlertUtil.showWarning("Erreur", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de changer le mot de passe.");
        }
    }

    @FXML
    private void handleAccueil() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToAdminDashboard(stage);
    }

    @FXML
    private void handleDemandes() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToAdminDemandes(stage);
    }

    @FXML
    private void handleReclamations() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToAdminReclamations(stage);
    }

    @FXML
    private void handleArchivage() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToArchivage(stage);
    }

    @FXML
    private void handleValidationComptes() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToValidationComptes(stage);
    }

    @FXML
    private void handleStatistiques() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToStatistiques(stage);
    }

    @FXML
    private void handleSaisons() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToSaisons(stage);
    }

    @FXML
    private void handleProfil() {
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToLogin(stage);
    }
}
