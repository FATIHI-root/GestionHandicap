package ma.ac.uir.gestionhandicap.controller.admin;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.dao.UtilisateurDAO;
import ma.ac.uir.gestionhandicap.model.Reclamation;
import ma.ac.uir.gestionhandicap.model.Saison;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.PrioriteReclamation;
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

public class AdminReclamationsController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private Label statTotalLabel;

    @FXML
    private Label statOuvertesLabel;

    @FXML
    private Label statTraiteesLabel;

    @FXML
    private Label statRefuseesLabel;

    @FXML
    private ComboBox<String> filtreStatutCombo;

    @FXML
    private ComboBox<String> filtrePrioriteCombo;

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
    private TableColumn<Reclamation, String> colEtudiant;

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

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private List<Reclamation> allReclamations = new ArrayList<>();
    private HashMap<String, Integer> saisonsMap = new HashMap<>();

    private Map<Integer, String> userNameCache = new HashMap<>();

    private ObservableList<Reclamation> reclamations = FXCollections.observableArrayList();

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

        filtrePrioriteCombo.getItems().add("Toutes");
        for (PrioriteReclamation p : PrioriteReclamation.values()) {
            filtrePrioriteCombo.getItems().add(p.getLibelle());
        }
        filtrePrioriteCombo.setValue("Toutes");
        filtrePrioriteCombo.setOnAction(e -> applyFilters());

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

    private void setupColumns() {
        colId.setCellValueFactory(r -> new SimpleStringProperty("#R-" + r.getValue().getIdReclamation()));
        colObjet.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getObjet()));
        colEtudiant.setCellValueFactory(r -> new SimpleStringProperty(getUserName(r.getValue().getIdUtilisateur())));
        colPriorite.setCellValueFactory(r ->
                new SimpleStringProperty(r.getValue().getPriorite() == null ? "—" : r.getValue().getPriorite().getLibelle()));
        colDate.setCellValueFactory(r -> {
            if (r.getValue().getDateCreation() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(r.getValue().getDateCreation().format(dateFmt));
        });
        colStatut.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getStatutReclamation().name()));

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
            private final Button prendreBtn = new Button("Prendre");
            private final Button traiterBtn = new Button("Traiter");
            private final Button refuserBtn = new Button("Refuser");
            private final HBox box = new HBox(6);
            {
                voirBtn.getStyleClass().add("secondary-button");
                voirBtn.setPrefHeight(28);
                voirBtn.setPrefWidth(56);

                prendreBtn.getStyleClass().add("action-button-dark");
                prendreBtn.setPrefHeight(28);
                prendreBtn.setPrefWidth(74);

                traiterBtn.getStyleClass().add("primary-button");
                traiterBtn.setPrefHeight(28);
                traiterBtn.setPrefWidth(64);

                refuserBtn.getStyleClass().add("danger-button");
                refuserBtn.setPrefHeight(28);
                refuserBtn.setPrefWidth(70);

                box.setAlignment(Pos.CENTER_LEFT);

                voirBtn.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    onVoir(r);
                });
                prendreBtn.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    onChangerStatut(r, StatutReclamation.EN_COURS);
                });
                traiterBtn.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    onChangerStatut(r, StatutReclamation.TRAITEE);
                });
                refuserBtn.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    onChangerStatut(r, StatutReclamation.REFUSEE);
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
                if (s == StatutReclamation.ENREGISTREE) {
                    box.getChildren().add(prendreBtn);
                    box.getChildren().add(refuserBtn);
                } else if (s == StatutReclamation.EN_COURS) {
                    box.getChildren().add(traiterBtn);
                    box.getChildren().add(refuserBtn);
                }
                setGraphic(box);
            }
        });
    }

    private void refresh() {
        try {
            allReclamations = reclamationService.getAllReclamations();
            applyFilters();

            int total = allReclamations.size();
            int ouvertes = 0, traitees = 0, refusees = 0;
            for (Reclamation r : allReclamations) {
                StatutReclamation s = r.getStatutReclamation();
                if (s == StatutReclamation.ENREGISTREE || s == StatutReclamation.EN_COURS) ouvertes++;
                else if (s == StatutReclamation.TRAITEE) traitees++;
                else if (s == StatutReclamation.REFUSEE) refusees++;
            }

            statTotalLabel.setText(String.valueOf(total));
            statOuvertesLabel.setText(String.valueOf(ouvertes));
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
        String prioF = filtrePrioriteCombo == null ? null : filtrePrioriteCombo.getValue();
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
            if (prioF != null && !prioF.equals("Toutes")) {
                if (r.getPriorite() == null || !r.getPriorite().getLibelle().equals(prioF)) continue;
            }
            if (saisonF != null && !saisonF.equals("Toutes")) {
                Integer idS = saisonsMap.get(saisonF);
                if (idS == null) continue;
                if (r.getIdSaison() == null || !r.getIdSaison().equals(idS)) continue;
            }
            if (!search.isEmpty()) {
                String objet = r.getObjet() == null ? "" : r.getObjet().toLowerCase();
                String desc = r.getDescription() == null ? "" : r.getDescription().toLowerCase();
                String etu = getUserName(r.getIdUtilisateur()).toLowerCase();
                String id = String.valueOf(r.getIdReclamation());
                if (!objet.contains(search) && !desc.contains(search) && !etu.contains(search) && !id.contains(search)) continue;
            }
            filtered.add(r);
        }
        reclamations.setAll(filtered);
    }

    private void onVoir(Reclamation r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Étudiant : ").append(getUserName(r.getIdUtilisateur())).append("\n");
        sb.append("Objet : ").append(r.getObjet()).append("\n");
        sb.append("Priorité : ").append(r.getPriorite() == null ? "—" : r.getPriorite().getLibelle()).append("\n");
        sb.append("Statut : ").append(r.getStatutReclamation() == null ? "—" : r.getStatutReclamation().getLibelle()).append("\n");
        if (r.getDateCreation() != null) {
            sb.append("Créée le : ").append(r.getDateCreation().format(dateFmt)).append("\n");
        }
        sb.append("\nDescription :\n").append(r.getDescription());
        if (r.getCommentaireAdmin() != null && !r.getCommentaireAdmin().isEmpty()) {
            sb.append("\n\nCommentaire admin : ").append(r.getCommentaireAdmin());
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

    private void onChangerStatut(Reclamation r, StatutReclamation nouveau) {
        Utilisateur admin = SessionManager.getCurrentUser();
        if (admin == null) return;

        String action;
        if (nouveau == StatutReclamation.EN_COURS) action = "Prendre en charge";
        else if (nouveau == StatutReclamation.TRAITEE) action = "Marquer comme traitée";
        else action = "Refuser";

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(action);
        dialog.setHeaderText(action + " la réclamation #R-" + r.getIdReclamation());

        TextArea commentaireArea = new TextArea();
        commentaireArea.setPromptText("Commentaire (recommandé pour traitement et refus)...");
        commentaireArea.setPrefRowCount(4);
        commentaireArea.setWrapText(true);

        VBox content = new VBox(10);
        content.getChildren().add(new Label("Commentaire :"));
        content.getChildren().add(commentaireArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(420);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            String commentaire = commentaireArea.getText();
            if (nouveau == StatutReclamation.REFUSEE && (commentaire == null || commentaire.trim().isEmpty())) {
                AlertUtil.showWarning("Commentaire requis", "Veuillez justifier le refus.");
                return;
            }
            try {
                reclamationService.changerStatut(r.getIdReclamation(), admin.getIdUtilisateur(), nouveau,
                        commentaire == null ? "" : commentaire.trim());
                AlertUtil.showInfo("Statut modifié",
                        "La réclamation #R-" + r.getIdReclamation() + " est maintenant : " + nouveau.getLibelle());
                refresh();
            } catch (IllegalArgumentException e) {
                AlertUtil.showWarning("Refusé", e.getMessage());
            } catch (SQLException e) {
                AlertUtil.showError("Erreur", "Impossible de changer le statut : " + e.getMessage());
            } catch (Exception e) {
                AlertUtil.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleExport() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("Étudiant");
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
            row.add(getUserName(r.getIdUtilisateur()));
            row.add(r.getObjet() == null ? "" : r.getObjet());
            row.add(r.getDescription() == null ? "" : r.getDescription());
            row.add(r.getPriorite() == null ? "" : r.getPriorite().getLibelle());
            row.add(r.getDateCreation() == null ? "" : r.getDateCreation().format(dateFmt));
            row.add(r.getStatutReclamation() == null ? "" : r.getStatutReclamation().getLibelle());
            row.add(r.getCommentaireAdmin() == null ? "" : r.getCommentaireAdmin());
            rows.add(row);
        }

        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        ExportService.export(stage, "reclamations_admin.csv", "Réclamations", headers, rows);
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
