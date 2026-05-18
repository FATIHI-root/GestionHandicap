package ma.ac.uir.gestionhandicap.controller.user;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.exception.ValidationException;
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.TypeDemande;
import ma.ac.uir.gestionhandicap.service.DemandeService;
import ma.ac.uir.gestionhandicap.service.PieceJustificativeService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;

public class NouvelleDemandeController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private ComboBox<TypeDemande> typeCombo;

    @FXML
    private TextField objetField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private CheckBox confirmCheck;

    @FXML
    private Label fileNameLabel;

    private List<File> selectedFiles = new ArrayList<>();

    private DemandeService demandeService = new DemandeService();
    private PieceJustificativeService pieceService = new PieceJustificativeService();

    @FXML
    private void initialize() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNomComplet());
            if (user.getPrenom() != null && user.getPrenom().length() > 0) {
                avatarInitialLabel.setText(user.getPrenom().substring(0, 1).toUpperCase());
            }
        }

        typeCombo.getItems().add(TypeDemande.AMENAGEMENT_EXAMEN);
        typeCombo.getItems().add(TypeDemande.ACCESSIBILITE);
        typeCombo.getItems().add(TypeDemande.ACCOMPAGNEMENT);
        typeCombo.getItems().add(TypeDemande.AUTRE);
    }

    @FXML
    private void handleEnvoyer() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user == null) {
            AlertUtil.showError("Session", "Vous devez être connecté.");
            return;
        }

        TypeDemande type = typeCombo.getValue();
        String objet = objetField.getText();
        String description = descriptionField.getText();

        if (type == null) {
            AlertUtil.showWarning("Type manquant", "Veuillez sélectionner un type de demande.");
            return;
        }
        if (objet == null || objet.trim().isEmpty()) {
            AlertUtil.showWarning("Objet manquant", "Veuillez saisir un objet.");
            return;
        }
        if (description == null || description.trim().isEmpty()) {
            AlertUtil.showWarning("Description manquante", "Veuillez décrire votre demande.");
            return;
        }
        if (!confirmCheck.isSelected()) {
            AlertUtil.showWarning("Confirmation requise", "Veuillez confirmer que les informations sont exactes.");
            return;
        }

        try {
            Demande created = demandeService.creerDemande(user.getIdUtilisateur(), type, objet, description);

            for (File f : selectedFiles) {
                if (f != null && f.exists()) {
                    try {
                        pieceService.storeDemandeFile(f, created.getIdDemande());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            AlertUtil.showInfo("Demande envoyée", "Votre demande a été enregistrée avec succès.");
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            SceneNavigator.goToMesDemandes(stage);
        } catch (ValidationException e) {
            AlertUtil.showWarning("Validation", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'enregistrer la demande : " + e.getMessage());
        }
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un ou plusieurs documents");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF, JPG, PNG", "*.pdf", "*.jpg", "*.jpeg", "*.png"));
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        List<File> files = fc.showOpenMultipleDialog(stage);
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
                AlertUtil.showWarning("Format non supporté", "Seuls les fichiers PDF, JPG et PNG sont acceptés.");
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
    private void handleAnnuler() {
        typeCombo.setValue(null);
        objetField.clear();
        descriptionField.clear();
        confirmCheck.setSelected(false);
        selectedFiles.clear();
        updateFileNameLabel();
    }

    @FXML
    private void handleAccueil() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToUserDashboard(stage);
    }

    @FXML
    private void handleMesDemandes() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToMesDemandes(stage);
    }

    @FXML
    private void handleMesReclamations() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToMesReclamations(stage);
    }

    @FXML
    private void handleNouvelleDemande() {
    }

    @FXML
    private void handleNouvelleReclamation() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToNouvelleReclamation(stage);
    }

    @FXML
    private void handleProfil() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToProfilUser(stage);
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToLogin(stage);
    }
}
