package ma.ac.uir.gestionhandicap.controller.auth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.exception.ValidationException;
import ma.ac.uir.gestionhandicap.service.AuthService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.ValidatorUtil;

public class RegisterController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private ComboBox<String> typeHandicapComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label fileNameLabel;

    private List<File> selectedFiles = new ArrayList<>();

    private AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        typeHandicapComboBox.getItems().add("Visuel");
        typeHandicapComboBox.getItems().add("Auditif");
        typeHandicapComboBox.getItems().add("Moteur");
        typeHandicapComboBox.getItems().add("Autre");

        confirmPasswordField.setOnAction(e -> handleRegister());
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un ou plusieurs documents justificatifs");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF, JPG, PNG", "*.pdf", "*.jpg", "*.jpeg", "*.png"));

        Stage stage = (Stage) emailField.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);

        if (files != null && !files.isEmpty()) {
            selectedFiles.clear();
            selectedFiles.addAll(files);
            updateFileNameLabel();
        }
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> valid = new ArrayList<>();
            for (File f : db.getFiles()) {
                String n = f.getName().toLowerCase();
                if (n.endsWith(".pdf") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")) {
                    valid.add(f);
                }
            }
            if (!valid.isEmpty()) {
                selectedFiles.clear();
                selectedFiles.addAll(valid);
                updateFileNameLabel();
                success = true;
            } else {
                AlertUtil.showWarning("Format non supporté",
                        "Seuls les fichiers PDF, JPG et PNG sont acceptés.");
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void updateFileNameLabel() {
        if (selectedFiles.isEmpty()) {
            fileNameLabel.setText("Aucun fichier sélectionné");
            return;
        }
        int n = selectedFiles.size();
        StringBuilder sb = new StringBuilder();
        sb.append(n).append(n == 1 ? " fichier : " : " fichiers : ");
        int max = Math.min(n, 3);
        for (int i = 0; i < max; i++) {
            if (i > 0) sb.append(", ");
            sb.append(selectedFiles.get(i).getName());
        }
        if (n > max) {
            sb.append(", +").append(n - max).append(" autre").append(n - max > 1 ? "s" : "");
        }
        fileNameLabel.setText(sb.toString());
    }

    @FXML
    private void handleRegister() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String telephone = telephoneField.getText();
        String typeHandicap = typeHandicapComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (ValidatorUtil.isEmpty(nom) || ValidatorUtil.isEmpty(prenom) || ValidatorUtil.isEmpty(email) || ValidatorUtil.isEmpty(password) || ValidatorUtil.isEmpty(confirmPassword)) {
            AlertUtil.showWarning("Champs vides", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (!ValidatorUtil.isValidName(nom)) {
            AlertUtil.showWarning("Nom invalide", "Le nom doit contenir au moins 2 caractères.");
            return;
        }

        if (!ValidatorUtil.isValidName(prenom)) {
            AlertUtil.showWarning("Prénom invalide", "Le prénom doit contenir au moins 2 caractères.");
            return;
        }

        if (!ValidatorUtil.isValidEmail(email)) {
            AlertUtil.showWarning("Email invalide", "Veuillez saisir un email valide.");
            return;
        }

        if (!ValidatorUtil.isUirEmail(email)) {
            AlertUtil.showWarning("Email UIR obligatoire", "Veuillez utiliser un email UIR.");
            return;
        }

        if (!ValidatorUtil.isValidPhone(telephone)) {
            AlertUtil.showWarning("Téléphone invalide", "Veuillez saisir un numéro de téléphone valide.");
            return;
        }

        if (typeHandicap == null) {
            AlertUtil.showWarning("Type de handicap", "Veuillez sélectionner un type de handicap.");
            return;
        }

        if (!ValidatorUtil.isValidPassword(password)) {
            AlertUtil.showWarning("Mot de passe invalide", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!ValidatorUtil.samePassword(password, confirmPassword)) {
            AlertUtil.showWarning("Confirmation incorrecte", "Les mots de passe ne sont pas identiques.");
            return;
        }

        if (selectedFiles.isEmpty()) {
            AlertUtil.showWarning("Document manquant", "Veuillez importer au moins un document justificatif.");
            return;
        }

        try {
            authService.register(nom, prenom, email, telephone, password, confirmPassword, selectedFiles);

            AlertUtil.showInfo("Inscription réussie", "Votre compte a été créé. Il doit être validé par l'administration.");

            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneNavigator.goToLogin(stage);

        } catch (ValidationException e) {
            AlertUtil.showWarning("Erreur de validation", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de créer le compte.");
        }
    }

    @FXML
    private void handleGoToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.goToLogin(stage);
    }
}
