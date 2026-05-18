package ma.ac.uir.gestionhandicap.controller.user;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;
import ma.ac.uir.gestionhandicap.model.Reclamation;
import ma.ac.uir.gestionhandicap.model.Saison;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;
import java.awt.Desktop;
import java.io.File;
import ma.ac.uir.gestionhandicap.model.PieceJustificative;
import ma.ac.uir.gestionhandicap.service.PieceJustificativeService;
import ma.ac.uir.gestionhandicap.service.ReclamationService;
import ma.ac.uir.gestionhandicap.service.SaisonService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.ExportService;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;

public class MesReclamationsController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private Label statTotalLabel;

    @FXML
    private Label statEnregistreesLabel;

    @FXML
    private Label statTraiteesLabel;

    @FXML
    private Label statRefuseesLabel;

    @FXML
    private ComboBox<String> filtreStatutCombo;

    @FXML
    private ComboBox<String> filtreSaisonCombo;

    @FXML
    private javafx.scene.control.TextField searchField;

    @FXML
    private TableView<Reclamation> reclamationsTable;

    @FXML
    private TableColumn<Reclamation, String> colId;

    @FXML
    private TableColumn<Reclamation, String> colObjet;

    @FXML
    private TableColumn<Reclamation, String> colPriorite;

    @FXML
    private TableColumn<Reclamation, String> colDate;

    @FXML
    private TableColumn<Reclamation, String> colStatut;

    @FXML
    private TableColumn<Reclamation, Void> colActions;

    private ReclamationService reclamationService = new ReclamationService();
    private SaisonService saisonService = new SaisonService();
    private PieceJustificativeService pieceService = new PieceJustificativeService();

    private ObservableList<Reclamation> reclamations = FXCollections.observableArrayList();
    private List<Reclamation> allReclamations = new ArrayList<>();
    private Map<String, Integer> saisonsMap = new HashMap<>();

    private DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNomComplet());
            if (user.getPrenom() != null && user.getPrenom().length() > 0) {
                avatarInitialLabel.setText(user.getPrenom().substring(0, 1).toUpperCase());
            }
        }

        filtreStatutCombo.getItems().add("Tous");
        filtreStatutCombo.getItems().add("Enregistrées");
        filtreStatutCombo.getItems().add("En cours");
        filtreStatutCombo.getItems().add("Traitées");
        filtreStatutCombo.getItems().add("Refusées");
        filtreStatutCombo.setValue("Tous");
        filtreStatutCombo.setOnAction(e -> applyFilters());

        filtreSaisonCombo.getItems().add("Toutes");
        try {
            for (Saison s : saisonService.findAll()) {
                filtreSaisonCombo.getItems().add(s.getLibelle());
                saisonsMap.put(s.getLibelle(), s.getIdSaison());
            }
        } catch (Exception e) {
        }
        filtreSaisonCombo.setValue("Toutes");
        filtreSaisonCombo.setOnAction(e -> applyFilters());

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        }

        setupColumns();
        reclamationsTable.setItems(reclamations);
        reclamationsTable.setPlaceholder(new Label("Aucune réclamation à afficher"));

        refresh();
    }

    private void setupColumns() {
        colId.setCellValueFactory(r -> new SimpleStringProperty("#R-" + r.getValue().getIdReclamation()));
        colObjet.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getObjet()));
        colPriorite.setCellValueFactory(r ->
                new SimpleStringProperty(r.getValue().getPriorite() == null ? "—" : r.getValue().getPriorite().getLibelle()));
        colDate.setCellValueFactory(r -> {
            if (r.getValue().getDateCreation() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(r.getValue().getDateCreation().format(dateFmt));
        });
        colStatut.setCellValueFactory(r ->
                new SimpleStringProperty(r.getValue().getStatutReclamation().name()));

        colStatut.setCellFactory(col -> new TableCell<Reclamation, String>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.getStyleClass().clear();
                    if (item.equals("ENREGISTREE")) {
                        badge.setText("Enregistrée");
                        badge.getStyleClass().add("statut-badge-warning");
                    } else if (item.equals("EN_COURS")) {
                        badge.setText("En cours");
                        badge.getStyleClass().add("statut-badge-warning");
                    } else if (item.equals("TRAITEE")) {
                        badge.setText("Traitée");
                        badge.getStyleClass().add("statut-badge-success");
                    } else {
                        badge.setText("Refusée");
                        badge.getStyleClass().add("statut-badge-danger");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<Reclamation, Void>() {
            private final Button voirBtn = new Button("Voir");
            private final Button supprimerBtn = new Button("Suppr.");
            private final HBox box = new HBox(6);
            {
                voirBtn.getStyleClass().add("secondary-button");
                voirBtn.setPrefHeight(28);
                voirBtn.setPrefWidth(60);

                supprimerBtn.getStyleClass().add("danger-button");
                supprimerBtn.setPrefHeight(28);
                supprimerBtn.setPrefWidth(70);

                box.setAlignment(Pos.CENTER_LEFT);

                voirBtn.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    onVoir(r);
                });
                supprimerBtn.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    onSupprimer(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Reclamation r = getTableView().getItems().get(getIndex());
                box.getChildren().clear();
                box.getChildren().add(voirBtn);
                StatutReclamation s = r.getStatutReclamation();
                if (s == StatutReclamation.ENREGISTREE || s == StatutReclamation.EN_COURS) {
                    box.getChildren().add(supprimerBtn);
                }
                setGraphic(box);
            }
        });
    }

    private void refresh() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user == null) return;
        try {
            allReclamations = reclamationService.getMesReclamations(user.getIdUtilisateur());
            applyFilters();

            int total = allReclamations.size();
            int enregistrees = 0, traitees = 0, refusees = 0;
            for (Reclamation r : allReclamations) {
                StatutReclamation s = r.getStatutReclamation();
                if (s == StatutReclamation.ENREGISTREE || s == StatutReclamation.EN_COURS) enregistrees++;
                else if (s == StatutReclamation.TRAITEE) traitees++;
                else if (s == StatutReclamation.REFUSEE) refusees++;
            }

            statTotalLabel.setText(String.valueOf(total));
            statEnregistreesLabel.setText(String.valueOf(enregistrees));
            statTraiteesLabel.setText(String.valueOf(traitees));
            statRefuseesLabel.setText(String.valueOf(refusees));
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les réclamations : " + e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    private void applyFilters() {
        String statF = filtreStatutCombo.getValue();
        String saisonF = filtreSaisonCombo == null ? null : filtreSaisonCombo.getValue();
        String search = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Reclamation> filtered = new ArrayList<>();
        for (Reclamation r : allReclamations) {
            String s = r.getStatutReclamation().name();
            if (statF != null && !statF.equals("Tous")) {
                if (statF.equals("Enregistrées") && !s.equals("ENREGISTREE")) continue;
                if (statF.equals("En cours") && !s.equals("EN_COURS")) continue;
                if (statF.equals("Traitées") && !s.equals("TRAITEE")) continue;
                if (statF.equals("Refusées") && !s.equals("REFUSEE")) continue;
            }
            if (saisonF != null && !saisonF.equals("Toutes")) {
                Integer idS = saisonsMap.get(saisonF);
                if (idS == null) continue;
                if (r.getIdSaison() == null || !r.getIdSaison().equals(idS)) continue;
            }
            if (!search.isEmpty()) {
                String objet = r.getObjet() == null ? "" : r.getObjet().toLowerCase();
                String desc = r.getDescription() == null ? "" : r.getDescription().toLowerCase();
                String id = String.valueOf(r.getIdReclamation());
                if (!objet.contains(search) && !desc.contains(search) && !id.contains(search)) continue;
            }
            filtered.add(r);
        }
        reclamations.setAll(filtered);
    }

    private void onVoir(Reclamation r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Objet : ").append(r.getObjet()).append("\n");
        sb.append("Priorité : ").append(r.getPriorite() == null ? "—" : r.getPriorite().getLibelle()).append("\n");
        sb.append("Statut : ").append(r.getStatutReclamation() == null ? "—" : r.getStatutReclamation().getLibelle()).append("\n");
        if (r.getDateCreation() != null) {
            sb.append("Créée le : ").append(r.getDateCreation().format(dateFmt)).append("\n");
        }
        sb.append("\nDescription :\n").append(r.getDescription());
        if (r.getCommentaireAdmin() != null && !r.getCommentaireAdmin().isEmpty()) {
            sb.append("\n\nRéponse admin : ").append(r.getCommentaireAdmin());
        }

        PieceJustificative piece = null;
        try {
            piece = pieceService.findReclamationDocument(r.getIdReclamation());
        } catch (Exception e) {
        }

        if (piece == null) {
            sb.append("\n\nPièce jointe : aucune");
            AlertUtil.showInfo("Réclamation #R-" + r.getIdReclamation(), sb.toString());
            return;
        }

        sb.append("\n\nPièce jointe : ").append(piece.getNomFichier());

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Réclamation #R-" + r.getIdReclamation());
        dialog.setHeaderText(null);
        dialog.setContentText(sb.toString() + "\n\nOuvrir la pièce jointe ?");
        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            ouvrirFichier(piece.getCheminFichier());
        }
    }

    private void ouvrirFichier(String chemin) {
        if (chemin == null || chemin.isEmpty()) {
            AlertUtil.showWarning("Pièce jointe", "Aucun fichier disponible.");
            return;
        }
        File f = new File(chemin);
        if (!f.exists()) {
            AlertUtil.showError("Fichier introuvable", "Le fichier n'existe plus :\n" + f.getAbsolutePath());
            return;
        }
        try {
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le fichier : " + ex.getMessage());
        }
    }

    private void onSupprimer(Reclamation r) {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer");
        confirm.setHeaderText("Supprimer la réclamation #R-" + r.getIdReclamation() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                reclamationService.supprimerReclamation(r.getIdReclamation(), user.getIdUtilisateur());
                AlertUtil.showInfo("Supprimée", "La réclamation a été supprimée.");
                refresh();
            } catch (IllegalArgumentException e) {
                AlertUtil.showWarning("Refusé", e.getMessage());
            } catch (SQLException e) {
                AlertUtil.showError("Erreur", "Impossible de supprimer : " + e.getMessage());
            } catch (Exception e) {
                AlertUtil.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleExport() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("Objet");
        headers.add("Description");
        headers.add("Priorité");
        headers.add("Date création");
        headers.add("Statut");
        headers.add("Commentaire admin");

        List<List<String>> rows = new ArrayList<>();
        for (Reclamation r : reclamations) {
            List<String> row = new ArrayList<>();
            row.add("R-" + r.getIdReclamation());
            row.add(r.getObjet() == null ? "" : r.getObjet());
            row.add(r.getDescription() == null ? "" : r.getDescription());
            row.add(r.getPriorite() == null ? "" : r.getPriorite().getLibelle());
            row.add(r.getDateCreation() == null ? "" : r.getDateCreation().format(dateFmt));
            row.add(r.getStatutReclamation() == null ? "" : r.getStatutReclamation().getLibelle());
            row.add(r.getCommentaireAdmin() == null ? "" : r.getCommentaireAdmin());
            rows.add(row);
        }

        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        ExportService.export(stage, "mes_reclamations.csv", "Mes réclamations", headers, rows);
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
    }

    @FXML
    private void handleNouvelleDemande() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToNouvelleDemande(stage);
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
