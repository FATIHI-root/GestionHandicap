package ma.ac.uir.gestionhandicap.controller.admin;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.dao.UtilisateurDAO;
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.HistoriqueDemande;
import ma.ac.uir.gestionhandicap.model.HistoriqueReclamation;
import ma.ac.uir.gestionhandicap.model.Reclamation;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.service.ArchivageService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.ExportService;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;
import java.util.ArrayList;

public class ArchivesController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private Label statDemandesLabel;

    @FXML
    private Label statReclamationsLabel;

    @FXML
    private TableView<Demande> demandesTable;

    @FXML
    private TableColumn<Demande, String> colDemId;

    @FXML
    private TableColumn<Demande, String> colDemType;

    @FXML
    private TableColumn<Demande, String> colDemObjet;

    @FXML
    private TableColumn<Demande, String> colDemEtudiant;

    @FXML
    private TableColumn<Demande, String> colDemDate;

    @FXML
    private TableColumn<Demande, String> colDemStatut;

    @FXML
    private TableColumn<Demande, Void> colDemActions;

    @FXML
    private TableView<Reclamation> reclamationsTable;

    @FXML
    private TableColumn<Reclamation, String> colRecId;

    @FXML
    private TableColumn<Reclamation, String> colRecObjet;

    @FXML
    private TableColumn<Reclamation, String> colRecEtudiant;

    @FXML
    private TableColumn<Reclamation, String> colRecPriorite;

    @FXML
    private TableColumn<Reclamation, String> colRecDate;

    @FXML
    private TableColumn<Reclamation, String> colRecStatut;

    @FXML
    private TableColumn<Reclamation, Void> colRecActions;

    @FXML
    private javafx.scene.control.ComboBox<String> filtreSaisonCombo;

    @FXML
    private javafx.scene.control.TextField searchField;

    private ArchivageService archivageService = new ArchivageService();
    private ma.ac.uir.gestionhandicap.service.SaisonService saisonService = new ma.ac.uir.gestionhandicap.service.SaisonService();
    private List<Demande> allDemandes = new ArrayList<>();
    private List<Reclamation> allReclamations = new ArrayList<>();
    private HashMap<String, Integer> saisonsMap = new HashMap<>();
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private Map<Integer, String> userNameCache = new HashMap<>();

    private ObservableList<Demande> demandes = FXCollections.observableArrayList();
    private ObservableList<Reclamation> reclamations = FXCollections.observableArrayList();

    private DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNomComplet());
            if (user.getPrenom() != null && user.getPrenom().length() > 0) {
                avatarInitialLabel.setText(user.getPrenom().substring(0, 1).toUpperCase());
            }
        }

        setupDemandeColumns();
        setupReclamationColumns();

        demandesTable.setItems(demandes);
        demandesTable.setPlaceholder(new Label("Aucune demande archivée"));
        reclamationsTable.setItems(reclamations);
        reclamationsTable.setPlaceholder(new Label("Aucune réclamation archivée"));

        if (filtreSaisonCombo != null) {
            filtreSaisonCombo.getItems().add("Toutes");
            try {
                for (ma.ac.uir.gestionhandicap.model.Saison s : saisonService.findAll()) {
                    filtreSaisonCombo.getItems().add(s.getLibelle());
                    saisonsMap.put(s.getLibelle(), s.getIdSaison());
                }
            } catch (Exception e) {
            }
            filtreSaisonCombo.setValue("Toutes");
            filtreSaisonCombo.setOnAction(e -> applyFilters());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        }

        loadData();
    }

    private void applyFilters() {
        String saisonF = filtreSaisonCombo == null ? null : filtreSaisonCombo.getValue();
        String search = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Integer idS = saisonF == null || saisonF.equals("Toutes") ? null : saisonsMap.get(saisonF);

        List<Demande> dFiltered = new ArrayList<>();
        for (Demande d : allDemandes) {
            if (idS != null && (d.getIdSaison() == null || !d.getIdSaison().equals(idS))) continue;
            if (!search.isEmpty()) {
                String objet = d.getObjet() == null ? "" : d.getObjet().toLowerCase();
                String etu = getUserName(d.getIdUtilisateur()).toLowerCase();
                String id = String.valueOf(d.getIdDemande());
                if (!objet.contains(search) && !etu.contains(search) && !id.contains(search)) continue;
            }
            dFiltered.add(d);
        }
        demandes.setAll(dFiltered);

        List<Reclamation> rFiltered = new ArrayList<>();
        for (Reclamation r : allReclamations) {
            if (idS != null && (r.getIdSaison() == null || !r.getIdSaison().equals(idS))) continue;
            if (!search.isEmpty()) {
                String objet = r.getObjet() == null ? "" : r.getObjet().toLowerCase();
                String etu = getUserName(r.getIdUtilisateur()).toLowerCase();
                String id = String.valueOf(r.getIdReclamation());
                if (!objet.contains(search) && !etu.contains(search) && !id.contains(search)) continue;
            }
            rFiltered.add(r);
        }
        reclamations.setAll(rFiltered);

        statDemandesLabel.setText(String.valueOf(dFiltered.size()));
        statReclamationsLabel.setText(String.valueOf(rFiltered.size()));
    }

    private String getUserName(int id) {
        if (userNameCache.containsKey(id)) {
            return userNameCache.get(id);
        }
        try {
            Utilisateur u = utilisateurDAO.findById(id);
            String name = u == null ? "Utilisateur #" + id : u.getNomComplet();
            userNameCache.put(id, name);
            return name;
        } catch (Exception e) {
            return "Utilisateur #" + id;
        }
    }

    private void setupDemandeColumns() {
        colDemId.setCellValueFactory(d -> new SimpleStringProperty("#D-" + d.getValue().getIdDemande()));
        colDemType.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTypeDemande() == null ? "—" : d.getValue().getTypeDemande().getLibelle()));
        colDemObjet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObjet()));
        colDemEtudiant.setCellValueFactory(d -> new SimpleStringProperty(getUserName(d.getValue().getIdUtilisateur())));
        colDemDate.setCellValueFactory(d -> {
            if (d.getValue().getDateCreation() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(d.getValue().getDateCreation().format(dateFmt));
        });
        colDemStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatutDemande().name()));

        colDemStatut.setCellFactory(col -> new TableCell<Demande, String>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.getStyleClass().clear();
                    if (item.equals("ACCEPTEE")) {
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

        colDemActions.setCellFactory(col -> new TableCell<Demande, Void>() {
            private final Button histBtn = new Button("Historique");
            {
                histBtn.getStyleClass().add("secondary-button");
                histBtn.setPrefHeight(28);
                histBtn.setPrefWidth(110);
                histBtn.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());
                    showDemandeHistorique(d);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : histBtn);
            }
        });
    }

    private void setupReclamationColumns() {
        colRecId.setCellValueFactory(r -> new SimpleStringProperty("#R-" + r.getValue().getIdReclamation()));
        colRecObjet.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getObjet()));
        colRecEtudiant.setCellValueFactory(r -> new SimpleStringProperty(getUserName(r.getValue().getIdUtilisateur())));
        colRecPriorite.setCellValueFactory(r ->
                new SimpleStringProperty(r.getValue().getPriorite() == null ? "—" : r.getValue().getPriorite().getLibelle()));
        colRecDate.setCellValueFactory(r -> {
            if (r.getValue().getDateCreation() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(r.getValue().getDateCreation().format(dateFmt));
        });
        colRecStatut.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getStatutReclamation().name()));

        colRecStatut.setCellFactory(col -> new TableCell<Reclamation, String>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.getStyleClass().clear();
                    if (item.equals("TRAITEE")) {
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

        colRecActions.setCellFactory(col -> new TableCell<Reclamation, Void>() {
            private final Button histBtn = new Button("Historique");
            {
                histBtn.getStyleClass().add("secondary-button");
                histBtn.setPrefHeight(28);
                histBtn.setPrefWidth(110);
                histBtn.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    showReclamationHistorique(r);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : histBtn);
            }
        });
    }

    private void loadData() {
        try {
            allDemandes = archivageService.findDemandesArchivees();
            allReclamations = archivageService.findReclamationsArchivees();
            applyFilters();
        } catch (Exception e) {
            statDemandesLabel.setText("—");
            statReclamationsLabel.setText("—");
            AlertUtil.showError("Erreur", "Impossible de charger les archives : " + e.getMessage());
        }
    }

    private void showDemandeHistorique(Demande d) {
        try {
            List<HistoriqueDemande> hist = archivageService.historiqueDemande(d.getIdDemande());

            TableView<HistoriqueDemande> table = new TableView<>();
            table.setPlaceholder(new Label("Aucun historique enregistré"));

            TableColumn<HistoriqueDemande, String> cDate = new TableColumn<>("Date");
            cDate.setPrefWidth(150);
            cDate.setCellValueFactory(h -> {
                if (h.getValue().getDateAction() == null) return new SimpleStringProperty("—");
                return new SimpleStringProperty(h.getValue().getDateAction().format(dateTimeFmt));
            });

            TableColumn<HistoriqueDemande, String> cAdmin = new TableColumn<>("Admin");
            cAdmin.setPrefWidth(170);
            cAdmin.setCellValueFactory(h -> new SimpleStringProperty(getUserName(h.getValue().getIdAdmin())));

            TableColumn<HistoriqueDemande, String> cAncien = new TableColumn<>("Ancien statut");
            cAncien.setPrefWidth(120);
            cAncien.setCellValueFactory(h ->
                    new SimpleStringProperty(h.getValue().getAncienStatut() == null ? "—" : h.getValue().getAncienStatut().getLibelle()));

            TableColumn<HistoriqueDemande, String> cNouveau = new TableColumn<>("Nouveau statut");
            cNouveau.setPrefWidth(130);
            cNouveau.setCellValueFactory(h ->
                    new SimpleStringProperty(h.getValue().getNouveauStatut() == null ? "—" : h.getValue().getNouveauStatut().getLibelle()));

            TableColumn<HistoriqueDemande, String> cComment = new TableColumn<>("Commentaire");
            cComment.setPrefWidth(280);
            cComment.setCellValueFactory(h ->
                    new SimpleStringProperty(h.getValue().getCommentaire() == null ? "" : h.getValue().getCommentaire()));

            table.getColumns().add(cDate);
            table.getColumns().add(cAdmin);
            table.getColumns().add(cAncien);
            table.getColumns().add(cNouveau);
            table.getColumns().add(cComment);
            table.setItems(FXCollections.observableArrayList(hist));

            openHistoriqueWindow("Historique — Demande #D-" + d.getIdDemande(),
                    "Type : " + (d.getTypeDemande() == null ? "—" : d.getTypeDemande().getLibelle()) + "   •   Objet : " + d.getObjet(),
                    table);
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger l'historique : " + e.getMessage());
        }
    }

    private void showReclamationHistorique(Reclamation r) {
        try {
            List<HistoriqueReclamation> hist = archivageService.historiqueReclamation(r.getIdReclamation());

            TableView<HistoriqueReclamation> table = new TableView<>();
            table.setPlaceholder(new Label("Aucun historique enregistré"));

            TableColumn<HistoriqueReclamation, String> cDate = new TableColumn<>("Date");
            cDate.setPrefWidth(150);
            cDate.setCellValueFactory(h -> {
                if (h.getValue().getDateAction() == null) return new SimpleStringProperty("—");
                return new SimpleStringProperty(h.getValue().getDateAction().format(dateTimeFmt));
            });

            TableColumn<HistoriqueReclamation, String> cAdmin = new TableColumn<>("Admin");
            cAdmin.setPrefWidth(170);
            cAdmin.setCellValueFactory(h -> new SimpleStringProperty(getUserName(h.getValue().getIdAdmin())));

            TableColumn<HistoriqueReclamation, String> cAncien = new TableColumn<>("Ancien statut");
            cAncien.setPrefWidth(120);
            cAncien.setCellValueFactory(h ->
                    new SimpleStringProperty(h.getValue().getAncienStatut() == null ? "—" : h.getValue().getAncienStatut().getLibelle()));

            TableColumn<HistoriqueReclamation, String> cNouveau = new TableColumn<>("Nouveau statut");
            cNouveau.setPrefWidth(130);
            cNouveau.setCellValueFactory(h ->
                    new SimpleStringProperty(h.getValue().getNouveauStatut() == null ? "—" : h.getValue().getNouveauStatut().getLibelle()));

            TableColumn<HistoriqueReclamation, String> cComment = new TableColumn<>("Commentaire");
            cComment.setPrefWidth(280);
            cComment.setCellValueFactory(h ->
                    new SimpleStringProperty(h.getValue().getCommentaire() == null ? "" : h.getValue().getCommentaire()));

            table.getColumns().add(cDate);
            table.getColumns().add(cAdmin);
            table.getColumns().add(cAncien);
            table.getColumns().add(cNouveau);
            table.getColumns().add(cComment);
            table.setItems(FXCollections.observableArrayList(hist));

            openHistoriqueWindow("Historique — Réclamation #R-" + r.getIdReclamation(),
                    "Priorité : " + (r.getPriorite() == null ? "—" : r.getPriorite().getLibelle()) + "   •   Objet : " + r.getObjet(),
                    table);
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger l'historique : " + e.getMessage());
        }
    }

    private void openHistoriqueWindow(String title, String subtitle, TableView<?> table) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("section-subtitle");

        VBox header = new VBox(6, titleLabel, subtitleLabel);
        header.setPadding(new Insets(20, 24, 12, 24));

        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("outline-button");
        closeBtn.setPrefWidth(110);
        closeBtn.setPrefHeight(36);

        HBox footer = new HBox(closeBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 24, 20, 24));

        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");
        root.setTop(header);
        root.setCenter(table);
        root.setBottom(footer);

        Stage stage = new Stage();
        stage.setTitle(title);
        Scene scene = new Scene(root, 900, 500);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        stage.setScene(scene);

        closeBtn.setOnAction(e -> stage.close());
        stage.show();
    }

    @FXML
    private void handleExportDemandes() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("Étudiant");
        headers.add("Type");
        headers.add("Objet");
        headers.add("Date création");
        headers.add("Statut final");
        headers.add("Commentaire admin");

        List<List<String>> rows = new ArrayList<>();
        for (Demande d : demandes) {
            List<String> row = new ArrayList<>();
            row.add("D-" + d.getIdDemande());
            row.add(getUserName(d.getIdUtilisateur()));
            row.add(d.getTypeDemande() == null ? "" : d.getTypeDemande().getLibelle());
            row.add(d.getObjet() == null ? "" : d.getObjet());
            row.add(d.getDateCreation() == null ? "" : d.getDateCreation().format(dateFmt));
            row.add(d.getStatutDemande() == null ? "" : d.getStatutDemande().getLibelle());
            row.add(d.getCommentaireAdmin() == null ? "" : d.getCommentaireAdmin());
            rows.add(row);
        }

        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        ExportService.export(stage, "archives_demandes.csv", "Archives - Demandes", headers, rows);
    }

    @FXML
    private void handleExportReclamations() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("Étudiant");
        headers.add("Objet");
        headers.add("Priorité");
        headers.add("Date création");
        headers.add("Statut final");
        headers.add("Commentaire admin");

        List<List<String>> rows = new ArrayList<>();
        for (Reclamation r : reclamations) {
            List<String> row = new ArrayList<>();
            row.add("R-" + r.getIdReclamation());
            row.add(getUserName(r.getIdUtilisateur()));
            row.add(r.getObjet() == null ? "" : r.getObjet());
            row.add(r.getPriorite() == null ? "" : r.getPriorite().getLibelle());
            row.add(r.getDateCreation() == null ? "" : r.getDateCreation().format(dateFmt));
            row.add(r.getStatutReclamation() == null ? "" : r.getStatutReclamation().getLibelle());
            row.add(r.getCommentaireAdmin() == null ? "" : r.getCommentaireAdmin());
            rows.add(row);
        }

        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        ExportService.export(stage, "archives_reclamations.csv", "Archives - Réclamations", headers, rows);
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
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToProfilAdmin(stage);
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToLogin(stage);
    }
}
