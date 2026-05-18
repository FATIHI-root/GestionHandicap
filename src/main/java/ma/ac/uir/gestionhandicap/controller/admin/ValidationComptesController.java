package ma.ac.uir.gestionhandicap.controller.admin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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
import javafx.scene.control.TextField;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.model.PieceJustificative;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.StatutCompte;
import ma.ac.uir.gestionhandicap.service.PieceJustificativeService;
import ma.ac.uir.gestionhandicap.service.ValidationCompteService;
import ma.ac.uir.gestionhandicap.util.AlertUtil;
import ma.ac.uir.gestionhandicap.util.ExportService;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;
import java.util.ArrayList;

public class ValidationComptesController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filtreCombo;

    private List<Utilisateur> allComptes = new ArrayList<>();

    @FXML
    private Label statEnAttenteLabel;

    @FXML
    private Label statValidesLabel;

    @FXML
    private Label statRefusesLabel;

    @FXML
    private Label statTotalLabel;

    @FXML
    private TableView<Utilisateur> comptesTable;

    @FXML
    private TableColumn<Utilisateur, String> colNomComplet;

    @FXML
    private TableColumn<Utilisateur, String> colEmail;

    @FXML
    private TableColumn<Utilisateur, String> colTelephone;

    @FXML
    private TableColumn<Utilisateur, String> colDate;

    @FXML
    private TableColumn<Utilisateur, String> colStatut;

    @FXML
    private TableColumn<Utilisateur, Void> colDocument;

    @FXML
    private TableColumn<Utilisateur, Void> colActions;

    private ValidationCompteService service = new ValidationCompteService();

    private PieceJustificativeService pieceService = new PieceJustificativeService();

    private ObservableList<Utilisateur> comptes = FXCollections.observableArrayList();

    private DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        Utilisateur current = SessionManager.getCurrentUser();
        if (current != null) {
            userNameLabel.setText(current.getNomComplet());
            String prenom = current.getPrenom();
            if (prenom != null && prenom.length() > 0) {
                avatarInitialLabel.setText(prenom.substring(0, 1).toUpperCase());
            }
        }

        filtreCombo.getItems().add("En attente");
        filtreCombo.getItems().add("Validés");
        filtreCombo.getItems().add("Refusés");
        filtreCombo.getItems().add("Tous");
        filtreCombo.setValue("En attente");
        filtreCombo.setOnAction(e -> refresh());

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applySearch());
        }

        setupColumns();
        comptesTable.setItems(comptes);
        comptesTable.setPlaceholder(new Label("Aucun compte à afficher"));

        refresh();
    }

    private void setupColumns() {
        colNomComplet.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomComplet()));
        colEmail.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEmail()));
        colTelephone.setCellValueFactory(data -> {
            String tel = data.getValue().getTelephone();
            return new SimpleStringProperty(tel == null ? "—" : tel);
        });
        colDate.setCellValueFactory(data -> {
            if (data.getValue().getDateInscription() == null) {
                return new SimpleStringProperty("—");
            }
            return new SimpleStringProperty(data.getValue().getDateInscription().format(dateFmt));
        });
        colStatut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatutCompte().name()));

        colStatut.setCellFactory(col -> new TableCell<Utilisateur, String>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.getStyleClass().clear();
                    if (item.equals("EN_ATTENTE")) {
                        badge.setText("En attente");
                        badge.getStyleClass().add("statut-badge-warning");
                    } else if (item.equals("VALIDE")) {
                        badge.setText("Validé");
                        badge.getStyleClass().add("statut-badge-success");
                    } else {
                        badge.setText("Refusé");
                        badge.getStyleClass().add("statut-badge-danger");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colDocument.setCellFactory(col -> new TableCell<Utilisateur, Void>() {
            private final Button voirBtn = new Button("Voir");
            {
                voirBtn.getStyleClass().add("secondary-button");
                voirBtn.setPrefWidth(80);
                voirBtn.setPrefHeight(28);
                voirBtn.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    onVoirDocument(u);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : voirBtn);
            }
        });

        colActions.setCellFactory(col -> new TableCell<Utilisateur, Void>() {
            private final Button validerBtn = new Button("Valider");
            private final Button refuserBtn = new Button("Refuser");
            private final HBox box = new HBox(8);
            {
                validerBtn.getStyleClass().add("primary-button");
                validerBtn.setPrefWidth(86);
                validerBtn.setPrefHeight(30);

                refuserBtn.getStyleClass().add("danger-button");
                refuserBtn.setPrefWidth(86);
                refuserBtn.setPrefHeight(30);

                box.setAlignment(Pos.CENTER_LEFT);

                validerBtn.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    onValider(u);
                });
                refuserBtn.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    onRefuser(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Utilisateur u = getTableView().getItems().get(getIndex());
                box.getChildren().clear();
                StatutCompte s = u.getStatutCompte();
                if (s == StatutCompte.EN_ATTENTE) {
                    box.getChildren().add(validerBtn);
                    box.getChildren().add(refuserBtn);
                } else if (s == StatutCompte.VALIDE) {
                    box.getChildren().add(refuserBtn);
                } else {
                    box.getChildren().add(validerBtn);
                }
                setGraphic(box);
            }
        });
    }

    private void refresh() {
        try {
            String filtre = filtreCombo.getValue();
            List<Utilisateur> list;
            if (filtre == null || filtre.equals("En attente")) {
                list = service.findEnAttente();
            } else if (filtre.equals("Validés")) {
                list = service.findValides();
            } else if (filtre.equals("Refusés")) {
                list = service.findRefuses();
            } else {
                list = new java.util.ArrayList<>();
                list.addAll(service.findEnAttente());
                list.addAll(service.findValides());
                list.addAll(service.findRefuses());
            }
            allComptes = list;
            applySearch();

            int enAttente = service.countEnAttente();
            int valides = service.countValides();
            int refuses = service.countRefuses();

            statEnAttenteLabel.setText(String.valueOf(enAttente));
            statValidesLabel.setText(String.valueOf(valides));
            statRefusesLabel.setText(String.valueOf(refuses));
            statTotalLabel.setText(String.valueOf(enAttente + valides + refuses));
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger les comptes.");
        }
    }

    private void onVoirDocument(Utilisateur u) {
        try {
            List<PieceJustificative> docs = pieceService.findAllInscriptionDocuments(u.getIdUtilisateur());
            if (docs == null || docs.isEmpty()) {
                AlertUtil.showWarning("Document indisponible",
                        "Aucun document justificatif n'a été fourni pour ce compte.");
                return;
            }
            showInAppViewer(docs, u.getNomComplet());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'afficher le document : " + e.getMessage());
        }
    }

    private void showInAppViewer(List<PieceJustificative> docs, String userName) {
        try {
            ImageView iv = new ImageView();
            iv.setPreserveRatio(true);
            iv.setFitWidth(900);

            ScrollPane scroll = new ScrollPane(iv);
            scroll.setFitToWidth(false);
            scroll.setPannable(true);

            Label info = new Label();
            info.getStyleClass().add("section-subtitle");

            Button prevDocBtn = new Button("◀ Doc préc.");
            prevDocBtn.getStyleClass().add("outline-button");
            prevDocBtn.setPrefHeight(34);
            prevDocBtn.setPrefWidth(120);

            Label docLabel = new Label();
            docLabel.getStyleClass().add("field-label");

            Button nextDocBtn = new Button("Doc suiv. ▶");
            nextDocBtn.getStyleClass().add("outline-button");
            nextDocBtn.setPrefHeight(34);
            nextDocBtn.setPrefWidth(120);

            Button downloadBtn = new Button("Télécharger");
            downloadBtn.getStyleClass().add("primary-button");
            downloadBtn.setPrefWidth(140);
            downloadBtn.setPrefHeight(38);

            Button closeBtn = new Button("Fermer");
            closeBtn.getStyleClass().add("outline-button");
            closeBtn.setPrefWidth(100);
            closeBtn.setPrefHeight(38);

            HBox topBar = new HBox(12);
            topBar.setPadding(new javafx.geometry.Insets(12, 18, 12, 18));
            topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            topBar.getStyleClass().add("topbar");

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            topBar.getChildren().addAll(info, spacer, prevDocBtn, docLabel, nextDocBtn, downloadBtn, closeBtn);

            Button prevPageBtn = new Button("◀ Précédente");
            prevPageBtn.getStyleClass().add("outline-button");
            prevPageBtn.setPrefHeight(34);
            prevPageBtn.setPrefWidth(130);

            Label pageLabel = new Label();
            pageLabel.getStyleClass().add("field-label");

            Button nextPageBtn = new Button("Suivante ▶");
            nextPageBtn.getStyleClass().add("outline-button");
            nextPageBtn.setPrefHeight(34);
            nextPageBtn.setPrefWidth(130);

            HBox pageBar = new HBox(14, prevPageBtn, pageLabel, nextPageBtn);
            pageBar.setPadding(new javafx.geometry.Insets(12, 18, 12, 18));
            pageBar.setAlignment(javafx.geometry.Pos.CENTER);
            pageBar.getStyleClass().add("topbar");

            BorderPane root = new BorderPane();
            root.setTop(topBar);
            root.setCenter(scroll);
            root.getStyleClass().add("dashboard-root");

            Stage stage = new Stage();
            stage.setTitle("Justificatifs — " + userName);
            Scene scene = new Scene(root, 1000, 800);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);

            int[] currentDoc = { 0 };
            int[] currentPage = { 0 };
            PDDocument[] currentPdf = { null };
            PDFRenderer[] currentRenderer = { null };
            int[] totalPages = { 0 };
            File[] currentFile = { null };

            Runnable updatePage = () -> {
                try {
                    if (currentPdf[0] == null) return;
                    BufferedImage bi = currentRenderer[0].renderImageWithDPI(currentPage[0], 150);
                    File tmp = File.createTempFile("pdfpage_", ".png");
                    tmp.deleteOnExit();
                    ImageIO.write(bi, "png", tmp);
                    iv.setImage(new Image(tmp.toURI().toString()));
                    pageLabel.setText("Page " + (currentPage[0] + 1) + " / " + totalPages[0]);
                    prevPageBtn.setDisable(currentPage[0] == 0);
                    nextPageBtn.setDisable(currentPage[0] >= totalPages[0] - 1);
                } catch (Exception ex) {
                    AlertUtil.showError("Erreur", "Impossible d'afficher la page : " + ex.getMessage());
                }
            };

            Runnable loadDoc = () -> {
                try {
                    if (currentPdf[0] != null) {
                        try { currentPdf[0].close(); } catch (Exception ex) { }
                        currentPdf[0] = null;
                        currentRenderer[0] = null;
                    }
                    PieceJustificative piece = docs.get(currentDoc[0]);
                    File f = new File(piece.getCheminFichier());
                    currentFile[0] = f;

                    info.setText("Document " + (currentDoc[0] + 1) + " de " + userName + "  •  " + f.getName());

                    if (!f.exists()) {
                        iv.setImage(null);
                        AlertUtil.showError("Fichier introuvable",
                                "Le fichier n'existe plus :\n" + f.getAbsolutePath());
                    } else if (f.getName().toLowerCase().endsWith(".pdf")) {
                        PDDocument doc = Loader.loadPDF(f);
                        currentPdf[0] = doc;
                        currentRenderer[0] = new PDFRenderer(doc);
                        totalPages[0] = doc.getNumberOfPages();
                        currentPage[0] = 0;
                        root.setBottom(pageBar);
                        updatePage.run();
                    } else {
                        iv.setImage(new Image(f.toURI().toString()));
                        root.setBottom(null);
                    }

                    boolean multi = docs.size() > 1;
                    prevDocBtn.setVisible(multi);
                    prevDocBtn.setManaged(multi);
                    nextDocBtn.setVisible(multi);
                    nextDocBtn.setManaged(multi);
                    docLabel.setVisible(multi);
                    docLabel.setManaged(multi);
                    if (multi) {
                        docLabel.setText("Doc " + (currentDoc[0] + 1) + " / " + docs.size());
                        prevDocBtn.setDisable(currentDoc[0] == 0);
                        nextDocBtn.setDisable(currentDoc[0] >= docs.size() - 1);
                    }
                } catch (Exception ex) {
                    AlertUtil.showError("Erreur", "Impossible de charger le document : " + ex.getMessage());
                }
            };

            prevDocBtn.setOnAction(e -> {
                if (currentDoc[0] > 0) {
                    currentDoc[0]--;
                    loadDoc.run();
                }
            });
            nextDocBtn.setOnAction(e -> {
                if (currentDoc[0] < docs.size() - 1) {
                    currentDoc[0]++;
                    loadDoc.run();
                }
            });

            prevPageBtn.setOnAction(e -> {
                if (currentPage[0] > 0) {
                    currentPage[0]--;
                    updatePage.run();
                }
            });
            nextPageBtn.setOnAction(e -> {
                if (currentPage[0] < totalPages[0] - 1) {
                    currentPage[0]++;
                    updatePage.run();
                }
            });

            downloadBtn.setOnAction(e -> {
                if (currentFile[0] != null) {
                    handleDownload(stage, currentFile[0], userName);
                }
            });
            closeBtn.setOnAction(e -> stage.close());

            stage.setOnCloseRequest(e -> {
                if (currentPdf[0] != null) {
                    try { currentPdf[0].close(); } catch (Exception ex) { }
                }
            });

            loadDoc.run();
            stage.show();
        } catch (Exception e) {
            AlertUtil.showError("Erreur d'affichage",
                    "Impossible d'afficher le document : " + e.getMessage());
        }
    }

    private void handleDownload(Stage owner, File source, String userName) {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Enregistrer le justificatif");
            fc.setInitialFileName(buildDownloadName(source, userName));

            String ext = "";
            int dot = source.getName().lastIndexOf('.');
            if (dot > 0) {
                ext = source.getName().substring(dot + 1).toLowerCase();
            }
            if (ext.equals("pdf")) {
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            } else if (ext.equals("jpg") || ext.equals("jpeg")) {
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image JPG", "*.jpg"));
            } else if (ext.equals("png")) {
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
            }
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tous", "*.*"));

            File dest = fc.showSaveDialog(owner);
            if (dest == null) return;

            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            AlertUtil.showInfo("Téléchargement réussi",
                    "Le fichier a été enregistré :\n" + dest.getAbsolutePath());
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Échec du téléchargement : " + e.getMessage());
        }
    }

    private String buildDownloadName(File source, String userName) {
        String clean = userName == null ? "utilisateur" : userName.replaceAll("[^A-Za-z0-9_-]+", "_");
        String ext = "";
        int dot = source.getName().lastIndexOf('.');
        if (dot > 0) ext = source.getName().substring(dot);
        return "justificatif_" + clean + ext;
    }

    private void applySearch() {
        String search = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (search.isEmpty()) {
            comptes.setAll(allComptes);
            return;
        }
        List<Utilisateur> filtered = new ArrayList<>();
        for (Utilisateur u : allComptes) {
            String nom = u.getNomComplet() == null ? "" : u.getNomComplet().toLowerCase();
            String email = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
            String tel = u.getTelephone() == null ? "" : u.getTelephone().toLowerCase();
            if (nom.contains(search) || email.contains(search) || tel.contains(search)) {
                filtered.add(u);
            }
        }
        comptes.setAll(filtered);
    }

    private void onValider(Utilisateur u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Valider le compte");
        confirm.setHeaderText("Valider le compte de " + u.getNomComplet() + " ?");
        confirm.setContentText("L'utilisateur pourra se connecter à la plateforme.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.valider(u.getIdUtilisateur());
                AlertUtil.showInfo("Compte validé", "Le compte de " + u.getNomComplet() + " a été validé.");
                refresh();
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de valider le compte.");
            }
        }
    }

    private void onRefuser(Utilisateur u) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Refuser le compte");
        dialog.setHeaderText("Refuser le compte de " + u.getNomComplet());

        TextArea motifArea = new TextArea();
        motifArea.setPromptText("Veuillez indiquer le motif du refus...");
        motifArea.setPrefRowCount(4);
        motifArea.setWrapText(true);

        VBox content = new VBox(10);
        content.getChildren().add(new Label("Motif du refus :"));
        content.getChildren().add(motifArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(420);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String motif = motifArea.getText();
            if (motif == null || motif.trim().isEmpty()) {
                AlertUtil.showWarning("Motif requis", "Veuillez saisir un motif de refus.");
                return;
            }
            try {
                service.refuser(u.getIdUtilisateur(), motif.trim());
                AlertUtil.showInfo("Compte refusé",
                        "Le compte de " + u.getNomComplet() + " a été refusé.\n\nMotif : " + motif.trim());
                refresh();
            } catch (Exception e) {
                String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                AlertUtil.showError("Erreur", "Impossible de refuser le compte : " + msg);
            }
        }
    }

    @FXML
    private void handleExport() {
        List<String> headers = new ArrayList<>();
        headers.add("Nom complet");
        headers.add("Email");
        headers.add("Téléphone");
        headers.add("Date inscription");
        headers.add("Statut");
        headers.add("Motif de refus");

        List<List<String>> rows = new ArrayList<>();
        for (Utilisateur u : comptes) {
            List<String> row = new ArrayList<>();
            row.add(u.getNomComplet());
            row.add(u.getEmail() == null ? "" : u.getEmail());
            row.add(u.getTelephone() == null ? "" : u.getTelephone());
            row.add(u.getDateInscription() == null ? "" : u.getDateInscription().format(dateFmt));
            row.add(u.getStatutCompte() == null ? "" : u.getStatutCompte().name());
            row.add(u.getMotifRefus() == null ? "" : u.getMotifRefus());
            rows.add(row);
        }

        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        ExportService.export(stage, "comptes.csv", "Comptes utilisateurs", headers, rows);
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
