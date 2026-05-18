package ma.ac.uir.gestionhandicap.controller.user;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;
import ma.ac.uir.gestionhandicap.service.DemandeService;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;

public class UserDashboardController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Label statDemandesLabel;

    @FXML
    private Label statEnCoursLabel;

    @FXML
    private Label statReclamationsLabel;

    @FXML
    private Label statAccepteesLabel;

    @FXML
    private Label pillAccepteesLabel;

    @FXML
    private Label pillEnCoursLabel;

    @FXML
    private Label pillReclamationsLabel;

    @FXML
    private TableView<Demande> demandesTable;

    @FXML
    private TableColumn<Demande, String> colDemandeId;

    @FXML
    private TableColumn<Demande, String> colDemandeType;

    @FXML
    private TableColumn<Demande, String> colDemandeDate;

    @FXML
    private TableColumn<Demande, String> colDemandeStatut;

    @FXML
    private TableView<Object> reclamationsTable;

    private DemandeService demandeService = new DemandeService();

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

        setupDemandeColumns();
        demandesTable.setPlaceholder(new Label("Aucune demande pour l'instant"));

        loadData();
    }

    private void setupDemandeColumns() {
        colDemandeId.setCellValueFactory(d -> new SimpleStringProperty("#D-" + d.getValue().getIdDemande()));
        colDemandeType.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTypeDemande() == null ? "—" : d.getValue().getTypeDemande().getLibelle()));
        colDemandeDate.setCellValueFactory(d -> {
            if (d.getValue().getDateCreation() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(d.getValue().getDateCreation().format(dateFmt));
        });
        colDemandeStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatutDemande().name()));

        colDemandeStatut.setCellFactory(col -> new TableCell<Demande, String>() {
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
    }

    private void loadData() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user == null) return;
        try {
            int total = demandeService.countByUtilisateur(user.getIdUtilisateur());
            int enCours = demandeService.countByUtilisateurAndStatut(user.getIdUtilisateur(), StatutDemande.EN_COURS);
            int acceptees = demandeService.countByUtilisateurAndStatut(user.getIdUtilisateur(), StatutDemande.ACCEPTEE);

            statDemandesLabel.setText(String.format("%02d", total));
            statEnCoursLabel.setText(String.format("%02d", enCours));
            statAccepteesLabel.setText(String.format("%02d", acceptees));
            statReclamationsLabel.setText("—");

            pillAccepteesLabel.setText(acceptees + " demandes acceptées");
            pillEnCoursLabel.setText(enCours + " demandes en cours");
            pillReclamationsLabel.setText("— réclamations enregistrées");

            List<Demande> all = demandeService.findByUtilisateur(user.getIdUtilisateur());
            List<Demande> recent = all.size() > 5 ? all.subList(0, 5) : all;
            ObservableList<Demande> items = FXCollections.observableArrayList(recent);
            demandesTable.setItems(items);
        } catch (Exception e) {
            statDemandesLabel.setText("—");
            statEnCoursLabel.setText("—");
            statAccepteesLabel.setText("—");
            statReclamationsLabel.setText("—");
        }
    }

    @FXML
    private void handleAccueil() {
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
