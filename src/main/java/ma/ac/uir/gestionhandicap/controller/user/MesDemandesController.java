package ma.ac.uir.gestionhandicap.controller.user;

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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;
import java.awt.Desktop;
import java.io.File;
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.PieceJustificative;
import ma.ac.uir.gestionhandicap.model.Saison;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;
import ma.ac.uir.gestionhandicap.model.enums.TypeDemande;
import ma.ac.uir.gestionhandicap.service.DemandeService;
import ma.ac.uir.gestionhandicap.service.PieceJustificativeService;
import ma.ac.uir.gestionhandicap.service.SaisonService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.ExportService;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;

public class MesDemandesController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private Label statTotalLabel;

    @FXML
    private Label statEnCoursLabel;

    @FXML
    private Label statAccepteesLabel;

    @FXML
    private Label statRefuseesLabel;

    @FXML
    private ComboBox<String> filtreTypeCombo;

    @FXML
    private ComboBox<String> filtreStatutCombo;

    @FXML
    private ComboBox<String> filtreSaisonCombo;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Demande> demandesTable;

    @FXML
    private TableColumn<Demande, String> colId;

    @FXML
    private TableColumn<Demande, String> colType;

    @FXML
    private TableColumn<Demande, String> colObjet;

    @FXML
    private TableColumn<Demande, String> colDate;

    @FXML
    private TableColumn<Demande, String> colStatut;

    @FXML
    private TableColumn<Demande, Void> colActions;

    private DemandeService demandeService = new DemandeService();
    private PieceJustificativeService pieceService = new PieceJustificativeService();
    private SaisonService saisonService = new SaisonService();

    private ObservableList<Demande> demandes = FXCollections.observableArrayList();
    private List<Demande> allDemandes = new ArrayList<>();
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

        filtreTypeCombo.getItems().add("Tous");
        filtreTypeCombo.getItems().add("Aménagement examen");
        filtreTypeCombo.getItems().add("Accessibilité");
        filtreTypeCombo.getItems().add("Accompagnement");
        filtreTypeCombo.getItems().add("Autre");
        filtreTypeCombo.setValue("Tous");
        filtreTypeCombo.setOnAction(e -> refresh());

        filtreStatutCombo.getItems().add("Tous");
        filtreStatutCombo.getItems().add("En cours");
        filtreStatutCombo.getItems().add("Acceptées");
        filtreStatutCombo.getItems().add("Refusées");
        filtreStatutCombo.setValue("Tous");
        filtreStatutCombo.setOnAction(e -> applyFilters());

        filtreTypeCombo.setOnAction(e -> applyFilters());

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
        demandesTable.setItems(demandes);
        demandesTable.setPlaceholder(new Label("Aucune demande à afficher"));

        refresh();
    }

    private void setupColumns() {
        colId.setCellValueFactory(d -> new SimpleStringProperty("#D-" + d.getValue().getIdDemande()));
        colType.setCellValueFactory(d -> {
            TypeDemande t = d.getValue().getTypeDemande();
            return new SimpleStringProperty(t == null ? "—" : t.getLibelle());
        });
        colObjet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObjet()));
        colDate.setCellValueFactory(d -> {
            if (d.getValue().getDateCreation() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(d.getValue().getDateCreation().format(dateFmt));
        });
        colStatut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatutDemande().name()));

        colStatut.setCellFactory(col -> new TableCell<Demande, String>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.getStyleClass().clear();
                    if (item.equals("EN_COURS")) {
                        badge.setText("En cours");
                        badge.getStyleClass().add("statut-badge-warning");
                    } else if (item.equals("ACCEPTEE")) {
                        badge.setText("Acceptée");
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

        colActions.setCellFactory(col -> new TableCell<Demande, Void>() {
            private final Button voirBtn = new Button("Voir");
            private final Button modifierBtn = new Button("Modifier");
            private final Button supprimerBtn = new Button("Suppr.");
            private final HBox box = new HBox(6);
            {
                voirBtn.getStyleClass().add("secondary-button");
                voirBtn.setPrefHeight(28);
                voirBtn.setPrefWidth(60);

                modifierBtn.getStyleClass().add("primary-button");
                modifierBtn.setPrefHeight(28);
                modifierBtn.setPrefWidth(72);

                supprimerBtn.getStyleClass().add("danger-button");
                supprimerBtn.setPrefHeight(28);
                supprimerBtn.setPrefWidth(70);

                box.setAlignment(Pos.CENTER_LEFT);

                voirBtn.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());
                    onVoir(d);
                });
                modifierBtn.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());
                    onModifier(d);
                });
                supprimerBtn.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());
                    onSupprimer(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Demande d = getTableView().getItems().get(getIndex());
                box.getChildren().clear();
                box.getChildren().add(voirBtn);
                if (d.getStatutDemande() == StatutDemande.EN_COURS) {
                    box.getChildren().add(modifierBtn);
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
            allDemandes = demandeService.findByUtilisateur(user.getIdUtilisateur());
            applyFilters();

            int total = demandeService.countByUtilisateur(user.getIdUtilisateur());
            int enCours = demandeService.countByUtilisateurAndStatut(user.getIdUtilisateur(), StatutDemande.EN_COURS);
            int acceptees = demandeService.countByUtilisateurAndStatut(user.getIdUtilisateur(), StatutDemande.ACCEPTEE);
            int refusees = demandeService.countByUtilisateurAndStatut(user.getIdUtilisateur(), StatutDemande.REFUSEE);

            statTotalLabel.setText(String.valueOf(total));
            statEnCoursLabel.setText(String.valueOf(enCours));
            statAccepteesLabel.setText(String.valueOf(acceptees));
            statRefuseesLabel.setText(String.valueOf(refusees));
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger les demandes : " + e.getMessage());
        }
    }

    private void applyFilters() {
        String typeF = filtreTypeCombo.getValue();
        String statF = filtreStatutCombo.getValue();
        String saisonF = filtreSaisonCombo.getValue();
        String search = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Demande> filtered = new ArrayList<>();
        for (Demande d : allDemandes) {
            if (typeF != null && !typeF.equals("Tous")) {
                if (d.getTypeDemande() == null || !d.getTypeDemande().getLibelle().equals(typeF)) continue;
            }
            if (statF != null && !statF.equals("Tous")) {
                String s = d.getStatutDemande().name();
                if (statF.equals("En cours") && !s.equals("EN_COURS")) continue;
                if (statF.equals("Acceptées") && !s.equals("ACCEPTEE")) continue;
                if (statF.equals("Refusées") && !s.equals("REFUSEE")) continue;
            }
            if (saisonF != null && !saisonF.equals("Toutes")) {
                Integer idS = saisonsMap.get(saisonF);
                if (idS == null) continue;
                if (d.getIdSaison() == null || !d.getIdSaison().equals(idS)) continue;
            }
            if (!search.isEmpty()) {
                String objet = d.getObjet() == null ? "" : d.getObjet().toLowerCase();
                String desc = d.getDescription() == null ? "" : d.getDescription().toLowerCase();
                String id = String.valueOf(d.getIdDemande());
                if (!objet.contains(search) && !desc.contains(search) && !id.contains(search)) continue;
            }
            filtered.add(d);
        }
        demandes.setAll(filtered);
    }

    private void onVoir(Demande d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Type : ").append(d.getTypeDemande() == null ? "—" : d.getTypeDemande().getLibelle()).append("\n");
        sb.append("Objet : ").append(d.getObjet()).append("\n");
        sb.append("Description :\n").append(d.getDescription()).append("\n\n");
        sb.append("Statut : ").append(d.getStatutDemande() == null ? "—" : d.getStatutDemande().getLibelle()).append("\n");
        if (d.getDateCreation() != null) {
            sb.append("Date création : ").append(d.getDateCreation().format(dateFmt)).append("\n");
        }
        if (d.getCommentaireAdmin() != null && !d.getCommentaireAdmin().isEmpty()) {
            sb.append("\nCommentaire admin : ").append(d.getCommentaireAdmin());
        }

        PieceJustificative piece = null;
        try {
            piece = pieceService.findDemandeDocument(d.getIdDemande());
        } catch (Exception e) {
        }

        if (piece == null) {
            sb.append("\n\nPièce jointe : aucune");
            AlertUtil.showInfo("Demande #D-" + d.getIdDemande(), sb.toString());
            return;
        }

        sb.append("\n\nPièce jointe : ").append(piece.getNomFichier());

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Demande #D-" + d.getIdDemande());
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

    private void onModifier(Demande d) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Modifier la demande");
        dialog.setHeaderText("Modifier la demande #D-" + d.getIdDemande());
        dialog.setContentText("La modification complète sera disponible bientôt. Pour l'instant, vous pouvez seulement supprimer puis recréer.");
        dialog.showAndWait();
    }

    private void onSupprimer(Demande d) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer");
        confirm.setHeaderText("Supprimer la demande #D-" + d.getIdDemande() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                demandeService.supprimerDemande(d.getIdDemande());
                AlertUtil.showInfo("Supprimée", "La demande a été supprimée.");
                refresh();
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExport() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("Type");
        headers.add("Objet");
        headers.add("Description");
        headers.add("Date création");
        headers.add("Statut");
        headers.add("Commentaire admin");

        List<List<String>> rows = new ArrayList<>();
        for (Demande d : demandes) {
            List<String> row = new ArrayList<>();
            row.add("D-" + d.getIdDemande());
            row.add(d.getTypeDemande() == null ? "" : d.getTypeDemande().getLibelle());
            row.add(d.getObjet() == null ? "" : d.getObjet());
            row.add(d.getDescription() == null ? "" : d.getDescription());
            row.add(d.getDateCreation() == null ? "" : d.getDateCreation().format(dateFmt));
            row.add(d.getStatutDemande() == null ? "" : d.getStatutDemande().getLibelle());
            row.add(d.getCommentaireAdmin() == null ? "" : d.getCommentaireAdmin());
            rows.add(row);
        }

        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        ExportService.export(stage, "mes_demandes.csv", "Mes demandes", headers, rows);
    }

    @FXML
    private void handleAccueil() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToUserDashboard(stage);
    }

    @FXML
    private void handleMesDemandes() {
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
