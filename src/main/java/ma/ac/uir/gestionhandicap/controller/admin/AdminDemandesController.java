package ma.ac.uir.gestionhandicap.controller.admin;

import java.time.format.DateTimeFormatter;
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
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.Saison;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;
import ma.ac.uir.gestionhandicap.model.enums.TypeDemande;
import java.awt.Desktop;
import java.io.File;
import ma.ac.uir.gestionhandicap.model.PieceJustificative;
import ma.ac.uir.gestionhandicap.service.DemandeService;
import ma.ac.uir.gestionhandicap.service.PieceJustificativeService;
import ma.ac.uir.gestionhandicap.service.SaisonService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.ExportService;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;
import java.util.ArrayList;

public class AdminDemandesController {

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
    private ComboBox<String> filtreStatutCombo;

    @FXML
    private ComboBox<String> filtreTypeCombo;

    @FXML
    private ComboBox<String> filtreSaisonCombo;

    @FXML
    private javafx.scene.control.TextField searchField;

    @FXML
    private TableView<Demande> demandesTable;

    @FXML
    private TableColumn<Demande, String> colId;

    @FXML
    private TableColumn<Demande, String> colType;

    @FXML
    private TableColumn<Demande, String> colObjet;

    @FXML
    private TableColumn<Demande, String> colEtudiant;

    @FXML
    private TableColumn<Demande, String> colDate;

    @FXML
    private TableColumn<Demande, String> colStatut;

    @FXML
    private TableColumn<Demande, Void> colActions;

    private DemandeService demandeService = new DemandeService();
    private SaisonService saisonService = new SaisonService();
    private PieceJustificativeService pieceService = new PieceJustificativeService();

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private List<Demande> allDemandes = new ArrayList<>();
    private Map<String, Integer> saisonsMap = new HashMap<>();

    private Map<Integer, String> userNameCache = new HashMap<>();

    private ObservableList<Demande> demandes = FXCollections.observableArrayList();

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
        filtreStatutCombo.getItems().add("En cours");
        filtreStatutCombo.getItems().add("Acceptées");
        filtreStatutCombo.getItems().add("Refusées");
        filtreStatutCombo.setValue("En cours");
        filtreStatutCombo.setOnAction(e -> applyFilters());

        filtreTypeCombo.getItems().add("Tous");
        for (TypeDemande t : TypeDemande.values()) {
            filtreTypeCombo.getItems().add(t.getLibelle());
        }
        filtreTypeCombo.setValue("Tous");
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
        colId.setCellValueFactory(d -> new SimpleStringProperty("#D-" + d.getValue().getIdDemande()));
        colType.setCellValueFactory(d -> {
            return new SimpleStringProperty(d.getValue().getTypeDemande() == null ? "—" : d.getValue().getTypeDemande().getLibelle());
        });
        colObjet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObjet()));
        colEtudiant.setCellValueFactory(d -> new SimpleStringProperty(getUserName(d.getValue().getIdUtilisateur())));
        colDate.setCellValueFactory(d -> {
            if (d.getValue().getDateCreation() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(d.getValue().getDateCreation().format(dateFmt));
        });
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatutDemande().name()));

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
            private final Button accepterBtn = new Button("Accepter");
            private final Button refuserBtn = new Button("Refuser");
            private final HBox box = new HBox(6);
            {
                voirBtn.getStyleClass().add("secondary-button");
                voirBtn.setPrefHeight(28);
                voirBtn.setPrefWidth(60);

                accepterBtn.getStyleClass().add("primary-button");
                accepterBtn.setPrefHeight(28);
                accepterBtn.setPrefWidth(82);

                refuserBtn.getStyleClass().add("danger-button");
                refuserBtn.setPrefHeight(28);
                refuserBtn.setPrefWidth(80);

                box.setAlignment(Pos.CENTER_LEFT);

                voirBtn.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());
                    onVoir(d);
                });
                accepterBtn.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());
                    onChangerStatut(d, StatutDemande.ACCEPTEE);
                });
                refuserBtn.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());
                    onChangerStatut(d, StatutDemande.REFUSEE);
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
                    box.getChildren().add(accepterBtn);
                    box.getChildren().add(refuserBtn);
                }
                setGraphic(box);
            }
        });
    }

    private void refresh() {
        try {
            allDemandes = demandeService.findAll();
            applyFilters();

            int total = demandeService.countTotal();
            int enCours = demandeService.countByStatut(StatutDemande.EN_COURS);
            int acceptees = demandeService.countByStatut(StatutDemande.ACCEPTEE);
            int refusees = demandeService.countByStatut(StatutDemande.REFUSEE);

            statTotalLabel.setText(String.valueOf(total));
            statEnCoursLabel.setText(String.valueOf(enCours));
            statAccepteesLabel.setText(String.valueOf(acceptees));
            statRefuseesLabel.setText(String.valueOf(refusees));
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger les demandes : " + e.getMessage());
        }
    }

    private void applyFilters() {
        String statF = filtreStatutCombo.getValue();
        String typeF = filtreTypeCombo == null ? null : filtreTypeCombo.getValue();
        String saisonF = filtreSaisonCombo == null ? null : filtreSaisonCombo.getValue();
        String search = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Demande> filtered = new ArrayList<>();
        for (Demande d : allDemandes) {
            if (statF != null && !statF.equals("Tous")) {
                String s = d.getStatutDemande().name();
                if (statF.equals("En cours") && !s.equals("EN_COURS")) continue;
                if (statF.equals("Acceptées") && !s.equals("ACCEPTEE")) continue;
                if (statF.equals("Refusées") && !s.equals("REFUSEE")) continue;
            }
            if (typeF != null && !typeF.equals("Tous")) {
                if (d.getTypeDemande() == null || !d.getTypeDemande().getLibelle().equals(typeF)) continue;
            }
            if (saisonF != null && !saisonF.equals("Toutes")) {
                Integer idS = saisonsMap.get(saisonF);
                if (idS == null) continue;
                if (d.getIdSaison() == null || !d.getIdSaison().equals(idS)) continue;
            }
            if (!search.isEmpty()) {
                String objet = d.getObjet() == null ? "" : d.getObjet().toLowerCase();
                String desc = d.getDescription() == null ? "" : d.getDescription().toLowerCase();
                String etu = getUserName(d.getIdUtilisateur()).toLowerCase();
                String id = String.valueOf(d.getIdDemande());
                if (!objet.contains(search) && !desc.contains(search) && !etu.contains(search) && !id.contains(search)) continue;
            }
            filtered.add(d);
        }
        demandes.setAll(filtered);
    }

    private void onVoir(Demande d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Étudiant : ").append(getUserName(d.getIdUtilisateur())).append("\n");
        sb.append("Type : ").append(d.getTypeDemande() == null ? "—" : d.getTypeDemande().getLibelle()).append("\n");
        sb.append("Objet : ").append(d.getObjet()).append("\n");
        sb.append("Description :\n").append(d.getDescription()).append("\n\n");
        sb.append("Statut : ").append(d.getStatutDemande() == null ? "—" : d.getStatutDemande().getLibelle()).append("\n");
        if (d.getDateCreation() != null) {
            sb.append("Créée le : ").append(d.getDateCreation().format(dateFmt)).append("\n");
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

    private void onChangerStatut(Demande d, StatutDemande nouveau) {
        Utilisateur admin = SessionManager.getCurrentUser();
        if (admin == null) return;

        String action = nouveau == StatutDemande.ACCEPTEE ? "Accepter" : "Refuser";

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(action + " la demande");
        dialog.setHeaderText(action + " la demande #D-" + d.getIdDemande());

        TextArea commentaireArea = new TextArea();
        commentaireArea.setPromptText("Commentaire (optionnel pour acceptation, recommandé pour refus)...");
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
            if (nouveau == StatutDemande.REFUSEE && (commentaire == null || commentaire.trim().isEmpty())) {
                AlertUtil.showWarning("Commentaire requis", "Veuillez justifier le refus.");
                return;
            }
            try {
                demandeService.changerStatut(d.getIdDemande(), nouveau, admin.getIdUtilisateur(), commentaire);
                AlertUtil.showInfo("Statut modifié",
                        "La demande #D-" + d.getIdDemande() + " a été " + (nouveau == StatutDemande.ACCEPTEE ? "acceptée" : "refusée") + ".");
                refresh();
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de changer le statut : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExport() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("Étudiant");
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
            row.add(getUserName(d.getIdUtilisateur()));
            row.add(d.getTypeDemande() == null ? "" : d.getTypeDemande().getLibelle());
            row.add(d.getObjet() == null ? "" : d.getObjet());
            row.add(d.getDescription() == null ? "" : d.getDescription());
            row.add(d.getDateCreation() == null ? "" : d.getDateCreation().format(dateFmt));
            row.add(d.getStatutDemande() == null ? "" : d.getStatutDemande().getLibelle());
            row.add(d.getCommentaireAdmin() == null ? "" : d.getCommentaireAdmin());
            rows.add(row);
        }

        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        ExportService.export(stage, "demandes_admin.csv", "Demandes", headers, rows);
    }

    @FXML
    private void handleAccueil() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        SceneNavigator.goToAdminDashboard(stage);
    }

    @FXML
    private void handleDemandes() {
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
