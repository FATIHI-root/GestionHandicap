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
import ma.ac.uir.gestionhandicap.model.Reclamation;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.PrioriteReclamation;
import ma.ac.uir.gestionhandicap.service.PieceJustificativeService;
import ma.ac.uir.gestionhandicap.service.ReclamationService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;

public class NouvelleReclamationController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private TextField objetField;

    @FXML
    private ComboBox<String> categorieCombo;

    @FXML
    private ComboBox<PrioriteReclamation> prioriteCombo;

    @FXML
    private TextArea descriptionField;

    @FXML
    private CheckBox confirmCheck;

    @FXML
    private Label fileNameLabel;

    private List<File> selectedFiles = new ArrayList<>();

    private ReclamationService reclamationService = new ReclamationService();
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

        prioriteCombo.getItems().add(PrioriteReclamation.BASSE);
        prioriteCombo.getItems().add(PrioriteReclamation.NORMALE);
        prioriteCombo.getItems().add(PrioriteReclamation.HAUTE);
        prioriteCombo.setValue(PrioriteReclamation.NORMALE);

        categorieCombo.getItems().add("Accessibilité");
        categorieCombo.getItems().add("Service administratif");
        categorieCombo.getItems().add("Enseignement");
        categorieCombo.getItems().add("Équipement");
        categorieCombo.getItems().add("Autre");
    }

    @FXML
    private void handleEnvoyer() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user == null) {
            AlertUtil.showError("Session", "Vous devez être connecté.");
            return;
        }

        String objet = objetField.getText();
        PrioriteReclamation priorite = prioriteCombo.getValue();
        String description = descriptionField.getText();

        if (objet == null || objet.trim().isEmpty()) {
            AlertUtil.showWarning("Objet manquant", "Veuillez saisir un objet.");
            return;
        }
        if (priorite == null) {
            AlertUtil.showWarning("Priorité manquante", "Veuillez sélectionner une priorité.");
            return;
        }
        if (description == null || description.trim().isEmpty()) {
            AlertUtil.showWarning("Description manquante", "Veuillez décrire votre réclamation.");
            return;
        }
        if (!confirmCheck.isSelected()) {
            AlertUtil.showWarning("Confirmation requise", "Veuillez confirmer que les informations sont exactes.");
            return;
        }

        try {
            reclamationService.creerReclamation(user.getIdUtilisateur(), objet, description, priorite);

            if (!selectedFiles.isEmpty()) {
                Reclamation last = null;
                try {
                    List<Reclamation> all = reclamationService.getMesReclamations(user.getIdUtilisateur());
                    if (!all.isEmpty()) {
                        last = all.get(0);
                    }
                } catch (Exception ex) {
                }
                if (last != null) {
                    for (File f : selectedFiles) {
                        if (f != null && f.exists()) {
                            try {
                                pieceService.storeReclamationFile(f, last.getIdReclamation());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }

            AlertUtil.showInfo("Réclamation envoyée", "Votre réclamation a été enregistrée avec succès.");
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            SceneNavigator.goToMesReclamations(stage);
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Validation", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'enregistrer la réclamation : " + e.getMessage());
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
        objetField.clear();
        descriptionField.clear();
        prioriteCombo.setValue(PrioriteReclamation.NORMALE);
        categorieCombo.setValue(null);
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
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToNouvelleDemande(stage);
    }

    @FXML
    private void handleNouvelleReclamation() {
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
