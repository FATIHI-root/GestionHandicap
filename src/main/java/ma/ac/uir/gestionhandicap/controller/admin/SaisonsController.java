package ma.ac.uir.gestionhandicap.controller.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.exception.ValidationException;
import ma.ac.uir.gestionhandicap.model.Saison;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.service.SaisonService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;

public class SaisonsController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private Label bannerActiveLabel;

    @FXML
    private Label statTotalLabel;

    @FXML
    private Label statActiveLabel;

    @FXML
    private Label statClotureesLabel;

    @FXML
    private TableView<Saison> saisonsTable;

    @FXML
    private TableColumn<Saison, String> colId;

    @FXML
    private TableColumn<Saison, String> colLibelle;

    @FXML
    private TableColumn<Saison, String> colDebut;

    @FXML
    private TableColumn<Saison, String> colFin;

    @FXML
    private TableColumn<Saison, String> colStatut;

    @FXML
    private TableColumn<Saison, Void> colActions;

    private SaisonService saisonService = new SaisonService();

    private ObservableList<Saison> saisons = FXCollections.observableArrayList();

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

        setupColumns();
        saisonsTable.setItems(saisons);
        saisonsTable.setPlaceholder(new Label("Aucune saison"));

        refresh();
    }

    private void setupColumns() {
        colId.setCellValueFactory(s -> new SimpleStringProperty("#S-" + s.getValue().getIdSaison()));
        colLibelle.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getLibelle()));
        colDebut.setCellValueFactory(s -> {
            if (s.getValue().getDateDebut() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(s.getValue().getDateDebut().format(dateFmt));
        });
        colFin.setCellValueFactory(s -> {
            if (s.getValue().getDateFin() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(s.getValue().getDateFin().format(dateFmt));
        });
        colStatut.setCellValueFactory(s ->
                new SimpleStringProperty(s.getValue().isEstActive() ? "ACTIVE" : "CLOTUREE"));

        colStatut.setCellFactory(col -> new TableCell<Saison, String>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.getStyleClass().clear();
                    if (item.equals("ACTIVE")) {
                        badge.setText("Active");
                        badge.getStyleClass().add("statut-badge-success");
                    } else {
                        badge.setText("Clôturée");
                        badge.getStyleClass().add("statut-badge-warning");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<Saison, Void>() {
            private final Button activerBtn = new Button("Activer");
            private final Button cloturerBtn = new Button("Clôturer");
            private final HBox box = new HBox(8);
            {
                activerBtn.getStyleClass().add("primary-button");
                activerBtn.setPrefHeight(30);
                activerBtn.setPrefWidth(90);

                cloturerBtn.getStyleClass().add("danger-button");
                cloturerBtn.setPrefHeight(30);
                cloturerBtn.setPrefWidth(100);

                box.setAlignment(Pos.CENTER_LEFT);

                activerBtn.setOnAction(e -> {
                    Saison s = getTableView().getItems().get(getIndex());
                    onActiver(s);
                });
                cloturerBtn.setOnAction(e -> {
                    Saison s = getTableView().getItems().get(getIndex());
                    onCloturer(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Saison s = getTableView().getItems().get(getIndex());
                box.getChildren().clear();
                if (s.isEstActive()) {
                    box.getChildren().add(cloturerBtn);
                } else {
                    box.getChildren().add(activerBtn);
                }
                setGraphic(box);
            }
        });
    }

    private void refresh() {
        try {
            List<Saison> list = saisonService.findAll();
            saisons.setAll(list);

            Saison active = saisonService.findActive();
            int total = list.size();
            int closes = 0;
            for (Saison s : list) {
                if (!s.isEstActive()) closes++;
            }

            statTotalLabel.setText(String.valueOf(total));
            statActiveLabel.setText(active == null ? "—" : active.getLibelle());
            statClotureesLabel.setText(String.valueOf(closes));
            bannerActiveLabel.setText(active == null
                    ? "Aucune saison active — créez ou activez-en une pour permettre les demandes/réclamations."
                    : "Saison active : " + active.getLibelle());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger les saisons : " + e.getMessage());
        }
    }

    @FXML
    private void handleNouvelleSaison() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Nouvelle saison");
        dialog.setHeaderText("Créer une nouvelle saison académique");

        TextField libelleField = new TextField();
        libelleField.setPromptText("Exemple : 2026-2027");

        DatePicker debutPicker = new DatePicker(LocalDate.now().withMonth(9).withDayOfMonth(1));
        DatePicker finPicker = new DatePicker(LocalDate.now().plusYears(1).withMonth(8).withDayOfMonth(31));

        VBox content = new VBox(10);
        content.getChildren().add(new Label("Libellé :"));
        content.getChildren().add(libelleField);
        content.getChildren().add(new Label("Date de début :"));
        content.getChildren().add(debutPicker);
        content.getChildren().add(new Label("Date de fin :"));
        content.getChildren().add(finPicker);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(400);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                saisonService.creerSaison(libelleField.getText(), debutPicker.getValue(), finPicker.getValue());
                AlertUtil.showInfo("Saison créée", "La saison a été créée. Elle n'est pas active par défaut.");
                refresh();
            } catch (ValidationException e) {
                AlertUtil.showWarning("Validation", e.getMessage());
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de créer la saison : " + e.getMessage());
            }
        }
    }

    private void onActiver(Saison s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Activer");
        confirm.setHeaderText("Activer la saison " + s.getLibelle() + " ?");
        confirm.setContentText("La saison actuellement active sera automatiquement clôturée.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                saisonService.activerSaison(s.getIdSaison());
                AlertUtil.showInfo("Saison activée", "La saison " + s.getLibelle() + " est maintenant active.");
                refresh();
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible d'activer : " + e.getMessage());
            }
        }
    }

    private void onCloturer(Saison s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clôturer");
        confirm.setHeaderText("Clôturer la saison " + s.getLibelle() + " ?");
        confirm.setContentText("Plus aucune demande ou réclamation ne pourra être créée tant qu'une autre saison n'est pas activée.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                saisonService.cloturerSaison(s.getIdSaison());
                AlertUtil.showInfo("Saison clôturée", "La saison " + s.getLibelle() + " a été clôturée.");
                refresh();
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de clôturer : " + e.getMessage());
            }
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
