package ma.ac.uir.gestionhandicap.controller.admin;

import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.dao.UtilisateurDAO;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.Reclamation;
import ma.ac.uir.gestionhandicap.model.Saison;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;
import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;
import ma.ac.uir.gestionhandicap.service.DemandeService;
import ma.ac.uir.gestionhandicap.service.ReclamationService;
import ma.ac.uir.gestionhandicap.service.StatistiqueService;
import ma.ac.uir.gestionhandicap.service.ValidationCompteService;
import ma.ac.uir.gestionhandicap.util.ExportService;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;

public class AdminDashboardController {

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
    private Label statEnAttenteLabel;

    @FXML
    private PieChart demandesPieChart;

    @FXML
    private PieChart reclamationsPieChart;

    @FXML
    private ComboBox<String> filtrePeriodeCombo;

    @FXML
    private ComboBox<String> filtreSaisonCombo;

    @FXML
    private VBox comptesValiderBox;

    @FXML
    private VBox demandesRecentesBox;

    @FXML
    private VBox reclamationsRecentesBox;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ValidationCompteService validationCompteService = new ValidationCompteService();
    private DemandeService demandeService = new DemandeService();
    private ReclamationService reclamationService = new ReclamationService();
    private StatistiqueService statistiqueService = new StatistiqueService();
    private ma.ac.uir.gestionhandicap.service.SaisonService saisonService = new ma.ac.uir.gestionhandicap.service.SaisonService();

    private Map<String, Integer> saisonsMap = new HashMap<>();

    @FXML
    private void initialize() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNomComplet());
            String prenom = user.getPrenom();
            if (prenom != null && prenom.length() > 0) {
                avatarInitialLabel.setText(prenom.substring(0, 1).toUpperCase());
            }
        }
        try {
            int enAttente = validationCompteService.countEnAttente();
            statEnAttenteLabel.setText(String.format("%02d", enAttente));
        } catch (Exception e) {
            statEnAttenteLabel.setText("—");
        }
        try {
            int total = demandeService.countTotal();
            int enCours = demandeService.countByStatut(StatutDemande.EN_COURS);
            statDemandesLabel.setText(String.format("%02d", total));
            statEnCoursLabel.setText(String.format("%02d", enCours));
        } catch (Exception e) {
            statDemandesLabel.setText("—");
            statEnCoursLabel.setText("—");
        }
        try {
            int totalRec = statistiqueService.countReclamationsTotal();
            statReclamationsLabel.setText(String.format("%02d", totalRec));
        } catch (Exception e) {
            statReclamationsLabel.setText("—");
        }

        filtrePeriodeCombo.getItems().add("Tout");
        filtrePeriodeCombo.getItems().add("Ce mois");
        filtrePeriodeCombo.getItems().add("Cette année");
        filtrePeriodeCombo.setValue("Tout");
        filtrePeriodeCombo.setOnAction(e -> reloadStats());

        filtreSaisonCombo.getItems().add("Toutes");
        try {
            for (Saison s : saisonService.findAll()) {
                filtreSaisonCombo.getItems().add(s.getLibelle());
                saisonsMap.put(s.getLibelle(), s.getIdSaison());
            }
        } catch (Exception e) {
        }
        filtreSaisonCombo.setValue("Toutes");
        filtreSaisonCombo.setOnAction(e -> reloadStats());

        loadCharts();
        loadComptesValider();
        loadDemandesRecentes();
        loadReclamationsRecentes();
    }

    private void loadComptesValider() {
        if (comptesValiderBox == null) return;
        comptesValiderBox.getChildren().clear();
        try {
            List<Utilisateur> enAttente = validationCompteService.findEnAttente();
            int max = Math.min(3, enAttente.size());
            for (int i = 0; i < max; i++) {
                Utilisateur u = enAttente.get(i);
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);

                Label nom = new Label(u.getNomComplet());
                nom.setPrefWidth(180);
                nom.getStyleClass().add("profil-value");

                Label email = new Label(u.getEmail() == null ? "" : u.getEmail());
                email.setPrefWidth(170);
                email.getStyleClass().add("section-subtitle");

                Label date = new Label(u.getDateInscription() == null ? "" : u.getDateInscription().format(dateFmt));
                date.setPrefWidth(100);
                date.getStyleClass().add("section-subtitle");

                Button voir = new Button("Voir");
                voir.setPrefWidth(90);
                voir.setPrefHeight(30);
                voir.getStyleClass().add("primary-button");
                voir.setOnAction(e -> handleValidationComptes());

                row.getChildren().addAll(nom, email, date, voir);
                comptesValiderBox.getChildren().add(row);
            }
            if (comptesValiderBox.getChildren().isEmpty()) {
                Label vide = new Label("Aucun compte en attente.");
                vide.getStyleClass().add("section-subtitle");
                comptesValiderBox.getChildren().add(vide);
            }
        } catch (Exception e) {
        }
    }

    private void loadDemandesRecentes() {
        if (demandesRecentesBox == null) return;
        demandesRecentesBox.getChildren().clear();
        try {
            List<Demande> all = demandeService.findAll();
            int max = Math.min(3, all.size());
            for (int i = 0; i < max; i++) {
                Demande d = all.get(i);
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);

                Label id = new Label("#D-" + d.getIdDemande());
                id.setPrefWidth(80);
                id.getStyleClass().add("profil-value");

                Label type = new Label(d.getTypeDemande() == null ? "—" : d.getTypeDemande().getLibelle());
                type.setPrefWidth(200);
                type.getStyleClass().add("profil-value");

                Label etu = new Label(getUserName(d.getIdUtilisateur()));
                etu.setPrefWidth(140);
                etu.getStyleClass().add("profil-value");

                Label statut = new Label(d.getStatutDemande() == null ? "—" : d.getStatutDemande().getLibelle());
                statut.setPrefWidth(110);
                if (d.getStatutDemande() == StatutDemande.ACCEPTEE) {
                    statut.getStyleClass().add("badge-success-text");
                } else if (d.getStatutDemande() == StatutDemande.EN_COURS) {
                    statut.getStyleClass().add("badge-warning-text");
                } else {
                    statut.getStyleClass().add("section-subtitle");
                }

                Button voir = new Button("Voir");
                voir.setPrefWidth(80);
                voir.setPrefHeight(30);
                voir.getStyleClass().add("secondary-button");
                voir.setOnAction(e -> handleDemandes());

                row.getChildren().addAll(id, type, etu, statut, voir);
                demandesRecentesBox.getChildren().add(row);
            }
            if (demandesRecentesBox.getChildren().isEmpty()) {
                Label vide = new Label("Aucune demande enregistrée.");
                vide.getStyleClass().add("section-subtitle");
                demandesRecentesBox.getChildren().add(vide);
            }
        } catch (Exception e) {
        }
    }

    private void loadReclamationsRecentes() {
        if (reclamationsRecentesBox == null) return;
        reclamationsRecentesBox.getChildren().clear();
        try {
            List<Reclamation> all = reclamationService.getAllReclamations();
            int max = Math.min(3, all.size());
            for (int i = 0; i < max; i++) {
                Reclamation r = all.get(i);
                String objet = r.getObjet() == null ? "" : r.getObjet();
                if (objet.length() > 28) objet = objet.substring(0, 28) + "…";
                String texte = "#R-" + r.getIdReclamation() + " — " + objet;

                Label pill = new Label(texte);
                pill.setMaxWidth(Double.MAX_VALUE);
                pill.setPrefHeight(30);
                pill.setPadding(new javafx.geometry.Insets(6, 14, 6, 14));

                if (r.getStatutReclamation() == StatutReclamation.TRAITEE) {
                    pill.getStyleClass().addAll("pill-success", "pill-success-text");
                } else if (r.getStatutReclamation() == StatutReclamation.REFUSEE) {
                    pill.getStyleClass().addAll("badge-danger", "badge-warning-text");
                } else {
                    pill.getStyleClass().addAll("pill-warning", "pill-warning-text");
                }

                reclamationsRecentesBox.getChildren().add(pill);
            }
            if (reclamationsRecentesBox.getChildren().isEmpty()) {
                Label vide = new Label("Aucune réclamation.");
                vide.getStyleClass().add("section-subtitle");
                reclamationsRecentesBox.getChildren().add(vide);
            }
        } catch (Exception e) {
        }
    }

    private String getUserName(Integer idUtilisateur) {
        if (idUtilisateur == null) return "—";
        try {
            Utilisateur u = utilisateurDAO.findById(idUtilisateur);
            if (u != null) return u.getNomComplet();
        } catch (Exception e) {
        }
        return "—";
    }

    private void reloadStats() {
        try {
            List<Demande> demandes = filterDemandes(demandeService.findAll());
            int totalD = demandes.size();
            int enCoursD = 0;
            for (Demande d : demandes) {
                if (d.getStatutDemande() == StatutDemande.EN_COURS) enCoursD++;
            }
            statDemandesLabel.setText(String.format("%02d", totalD));
            statEnCoursLabel.setText(String.format("%02d", enCoursD));

            List<Reclamation> recs = filterReclamations(reclamationService.getAllReclamations());
            statReclamationsLabel.setText(String.format("%02d", recs.size()));
        } catch (Exception e) {
        }
        loadCharts();
        loadComptesValider();
        loadDemandesRecentes();
        loadReclamationsRecentes();
    }

    private List<Demande> filterDemandes(List<Demande> source) {
        List<Demande> result = new ArrayList<>();
        String periode = filtrePeriodeCombo == null ? "Tout" : filtrePeriodeCombo.getValue();
        String saisonF = filtreSaisonCombo == null ? "Toutes" : filtreSaisonCombo.getValue();
        Integer idS = saisonF == null || saisonF.equals("Toutes") ? null : saisonsMap.get(saisonF);
        LocalDateTime now = LocalDateTime.now();

        for (Demande d : source) {
            if (idS != null && (d.getIdSaison() == null || !d.getIdSaison().equals(idS))) continue;
            if (!matchPeriode(d.getDateCreation(), periode, now)) continue;
            result.add(d);
        }
        return result;
    }

    private List<Reclamation> filterReclamations(List<Reclamation> source) {
        List<Reclamation> result = new ArrayList<>();
        String periode = filtrePeriodeCombo == null ? "Tout" : filtrePeriodeCombo.getValue();
        String saisonF = filtreSaisonCombo == null ? "Toutes" : filtreSaisonCombo.getValue();
        Integer idS = saisonF == null || saisonF.equals("Toutes") ? null : saisonsMap.get(saisonF);
        LocalDateTime now = LocalDateTime.now();

        for (Reclamation r : source) {
            if (idS != null && (r.getIdSaison() == null || !r.getIdSaison().equals(idS))) continue;
            if (!matchPeriode(r.getDateCreation(), periode, now)) continue;
            result.add(r);
        }
        return result;
    }

    private boolean matchPeriode(LocalDateTime date, String periode, LocalDateTime now) {
        if (date == null || periode == null || periode.equals("Tout")) return true;
        if (periode.equals("Ce mois")) {
            return date.getYear() == now.getYear() && date.getMonthValue() == now.getMonthValue();
        }
        if (periode.equals("Cette année")) {
            return date.getYear() == now.getYear();
        }
        return true;
    }

    @FXML
    private void handleExport() {
        try {
            List<String> headers = new ArrayList<>();
            headers.add("Indicateur");
            headers.add("Valeur");

            List<List<String>> rows = new ArrayList<>();

            String periode = filtrePeriodeCombo == null ? "Tout" : filtrePeriodeCombo.getValue();
            String saison = filtreSaisonCombo == null ? "Toutes" : filtreSaisonCombo.getValue();

            rows.add(buildRow("Période", periode));
            rows.add(buildRow("Saison", saison));
            rows.add(buildRow("", ""));

            rows.add(buildRow("--- Utilisateurs ---", ""));
            rows.add(buildRow("Total utilisateurs", String.valueOf(statistiqueService.countTotalUtilisateurs())));
            rows.add(buildRow("Validés", String.valueOf(statistiqueService.countUtilisateursValides())));
            rows.add(buildRow("En attente", String.valueOf(statistiqueService.countUtilisateursEnAttente())));
            rows.add(buildRow("Refusés", String.valueOf(statistiqueService.countUtilisateursRefuses())));
            rows.add(buildRow("", ""));

            List<Demande> demandes = filterDemandes(demandeService.findAll());
            int dEnCours = 0, dAcceptees = 0, dRefusees = 0;
            for (Demande d : demandes) {
                if (d.getStatutDemande() == StatutDemande.EN_COURS) dEnCours++;
                else if (d.getStatutDemande() == StatutDemande.ACCEPTEE) dAcceptees++;
                else if (d.getStatutDemande() == StatutDemande.REFUSEE) dRefusees++;
            }
            rows.add(buildRow("--- Demandes (filtrées) ---", ""));
            rows.add(buildRow("Total", String.valueOf(demandes.size())));
            rows.add(buildRow("En cours", String.valueOf(dEnCours)));
            rows.add(buildRow("Acceptées", String.valueOf(dAcceptees)));
            rows.add(buildRow("Refusées", String.valueOf(dRefusees)));
            rows.add(buildRow("", ""));

            List<Reclamation> recs = filterReclamations(reclamationService.getAllReclamations());
            int rEnr = 0, rEnCours = 0, rTraitees = 0, rRefusees = 0;
            for (Reclamation r : recs) {
                if (r.getStatutReclamation() == StatutReclamation.ENREGISTREE) rEnr++;
                else if (r.getStatutReclamation() == StatutReclamation.EN_COURS) rEnCours++;
                else if (r.getStatutReclamation() == StatutReclamation.TRAITEE) rTraitees++;
                else if (r.getStatutReclamation() == StatutReclamation.REFUSEE) rRefusees++;
            }
            rows.add(buildRow("--- Réclamations (filtrées) ---", ""));
            rows.add(buildRow("Total", String.valueOf(recs.size())));
            rows.add(buildRow("Enregistrées", String.valueOf(rEnr)));
            rows.add(buildRow("En cours", String.valueOf(rEnCours)));
            rows.add(buildRow("Traitées", String.valueOf(rTraitees)));
            rows.add(buildRow("Refusées", String.valueOf(rRefusees)));

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            ExportService.export(stage, "rapport_dashboard.csv", "Rapport global", headers, rows);
        } catch (Exception e) {
            ma.ac.uir.gestionhandicap.util.AlertUtil.showError("Erreur", "Impossible d'exporter : " + e.getMessage());
        }
    }

    private int pct(int value, int total) {
        if (total == 0) return 0;
        return (int) Math.round(value * 100.0 / total);
    }

    private List<String> buildRow(String a, String b) {
        List<String> r = new ArrayList<>();
        r.add(a);
        r.add(b);
        return r;
    }

    private void loadCharts() {
        try {
            List<Demande> demandes = filterDemandes(demandeService.findAll());
            int enCours = 0, acceptees = 0, refusees = 0;
            for (Demande d : demandes) {
                if (d.getStatutDemande() == StatutDemande.EN_COURS) enCours++;
                else if (d.getStatutDemande() == StatutDemande.ACCEPTEE) acceptees++;
                else if (d.getStatutDemande() == StatutDemande.REFUSEE) refusees++;
            }

            int totalD = enCours + acceptees + refusees;
            ObservableList<PieChart.Data> dData = FXCollections.observableArrayList();
            if (enCours > 0) dData.add(new PieChart.Data("En cours : " + enCours + " (" + pct(enCours, totalD) + "%)", enCours));
            if (acceptees > 0) dData.add(new PieChart.Data("Acceptées : " + acceptees + " (" + pct(acceptees, totalD) + "%)", acceptees));
            if (refusees > 0) dData.add(new PieChart.Data("Refusées : " + refusees + " (" + pct(refusees, totalD) + "%)", refusees));
            if (dData.isEmpty()) dData.add(new PieChart.Data("Aucune demande", 1));
            demandesPieChart.setData(dData);
            demandesPieChart.setTitle("");
        } catch (Exception e) {
        }

        try {
            List<Reclamation> recs = filterReclamations(reclamationService.getAllReclamations());
            int enr = 0, rec = 0, tra = 0, ref = 0;
            for (Reclamation r : recs) {
                if (r.getStatutReclamation() == StatutReclamation.ENREGISTREE) enr++;
                else if (r.getStatutReclamation() == StatutReclamation.EN_COURS) rec++;
                else if (r.getStatutReclamation() == StatutReclamation.TRAITEE) tra++;
                else if (r.getStatutReclamation() == StatutReclamation.REFUSEE) ref++;
            }

            int totalR = enr + rec + tra + ref;
            ObservableList<PieChart.Data> rData = FXCollections.observableArrayList();
            if (enr > 0) rData.add(new PieChart.Data("Enregistrées : " + enr + " (" + pct(enr, totalR) + "%)", enr));
            if (rec > 0) rData.add(new PieChart.Data("En cours : " + rec + " (" + pct(rec, totalR) + "%)", rec));
            if (tra > 0) rData.add(new PieChart.Data("Traitées : " + tra + " (" + pct(tra, totalR) + "%)", tra));
            if (ref > 0) rData.add(new PieChart.Data("Refusées : " + ref + " (" + pct(ref, totalR) + "%)", ref));
            if (rData.isEmpty()) rData.add(new PieChart.Data("Aucune réclamation", 1));
            reclamationsPieChart.setData(rData);
            reclamationsPieChart.setTitle("");
        } catch (Exception e) {
        }
    }

    @FXML
    private void handleAccueil() {
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
