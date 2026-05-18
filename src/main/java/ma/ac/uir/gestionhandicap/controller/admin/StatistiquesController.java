package ma.ac.uir.gestionhandicap.controller.admin;

import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.PrioriteReclamation;
import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;
import ma.ac.uir.gestionhandicap.model.enums.TypeDemande;
import ma.ac.uir.gestionhandicap.service.StatistiqueService;
import ma.ac.uir.gestionhandicap.util.SceneNavigator;
import ma.ac.uir.gestionhandicap.util.SessionManager;

public class StatistiquesController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label avatarInitialLabel;

    @FXML
    private Label kpiUsersLabel;

    @FXML
    private Label kpiUsersSubLabel;

    @FXML
    private Label kpiDemandesLabel;

    @FXML
    private Label kpiDemandesSubLabel;

    @FXML
    private Label kpiReclamationsLabel;

    @FXML
    private Label kpiReclamationsSubLabel;

    @FXML
    private Label kpiAttenteLabel;

    @FXML
    private PieChart demandesPieChart;

    @FXML
    private PieChart reclamationsPieChart;

    @FXML
    private BarChart<String, Number> typesDemandeChart;

    @FXML
    private BarChart<String, Number> prioritesChart;

    private StatistiqueService statistiqueService = new StatistiqueService();

    @FXML
    private void initialize() {
        Utilisateur user = SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNomComplet());
            if (user.getPrenom() != null && user.getPrenom().length() > 0) {
                avatarInitialLabel.setText(user.getPrenom().substring(0, 1).toUpperCase());
            }
        }

        loadKpis();
        loadDemandesPieChart();
        loadReclamationsPieChart();
        loadDemandesParType();
        loadReclamationsParPriorite();
    }

    private void loadKpis() {
        try {
            int totalUsers = statistiqueService.countTotalUtilisateurs();
            int valides = statistiqueService.countUtilisateursValides();
            int attente = statistiqueService.countUtilisateursEnAttente();
            int totalDemandes = statistiqueService.countDemandesTotal();
            int demandesEnCours = statistiqueService.countDemandesEnCours();
            int totalReclamations = statistiqueService.countReclamationsTotal();
            int reclamationsOuvertes = statistiqueService.countReclamationsParStatut(StatutReclamation.ENREGISTREE)
                    + statistiqueService.countReclamationsParStatut(StatutReclamation.EN_COURS);

            kpiUsersLabel.setText(String.valueOf(totalUsers));
            kpiUsersSubLabel.setText(valides + " validés");

            kpiDemandesLabel.setText(String.valueOf(totalDemandes));
            kpiDemandesSubLabel.setText(demandesEnCours + " en cours");

            kpiReclamationsLabel.setText(String.valueOf(totalReclamations));
            kpiReclamationsSubLabel.setText(reclamationsOuvertes + " ouvertes");

            kpiAttenteLabel.setText(String.valueOf(attente));
        } catch (Exception e) {
            kpiUsersLabel.setText("—");
            kpiDemandesLabel.setText("—");
            kpiReclamationsLabel.setText("—");
            kpiAttenteLabel.setText("—");
        }
    }

    private void loadDemandesPieChart() {
        try {
            int enCours = statistiqueService.countDemandesEnCours();
            int acceptees = statistiqueService.countDemandesAcceptees();
            int refusees = statistiqueService.countDemandesRefusees();

            ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
            if (enCours > 0) data.add(new PieChart.Data("En cours (" + enCours + ")", enCours));
            if (acceptees > 0) data.add(new PieChart.Data("Acceptées (" + acceptees + ")", acceptees));
            if (refusees > 0) data.add(new PieChart.Data("Refusées (" + refusees + ")", refusees));
            if (data.isEmpty()) {
                data.add(new PieChart.Data("Aucune demande", 1));
            }
            demandesPieChart.setData(data);
            demandesPieChart.setTitle("");
        } catch (Exception e) {
        }
    }

    private void loadReclamationsPieChart() {
        try {
            int enregistrees = statistiqueService.countReclamationsParStatut(StatutReclamation.ENREGISTREE);
            int enCours = statistiqueService.countReclamationsParStatut(StatutReclamation.EN_COURS);
            int traitees = statistiqueService.countReclamationsParStatut(StatutReclamation.TRAITEE);
            int refusees = statistiqueService.countReclamationsParStatut(StatutReclamation.REFUSEE);

            ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
            if (enregistrees > 0) data.add(new PieChart.Data("Enregistrées (" + enregistrees + ")", enregistrees));
            if (enCours > 0) data.add(new PieChart.Data("En cours (" + enCours + ")", enCours));
            if (traitees > 0) data.add(new PieChart.Data("Traitées (" + traitees + ")", traitees));
            if (refusees > 0) data.add(new PieChart.Data("Refusées (" + refusees + ")", refusees));
            if (data.isEmpty()) {
                data.add(new PieChart.Data("Aucune réclamation", 1));
            }
            reclamationsPieChart.setData(data);
            reclamationsPieChart.setTitle("");
        } catch (Exception e) {
        }
    }

    private void loadDemandesParType() {
        typesDemandeChart.getData().clear();
        try {
            Map<TypeDemande, Integer> map = statistiqueService.repartitionDemandesParType();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Demandes");
            for (TypeDemande t : TypeDemande.values()) {
                int n = map.get(t) == null ? 0 : map.get(t);
                series.getData().add(new XYChart.Data<>(t.getLibelle(), n));
            }
            typesDemandeChart.getData().add(series);
        } catch (Exception e) {
        }
    }

    private void loadReclamationsParPriorite() {
        prioritesChart.getData().clear();
        try {
            Map<PrioriteReclamation, Integer> map = statistiqueService.repartitionReclamationsParPriorite();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Réclamations");
            for (PrioriteReclamation p : PrioriteReclamation.values()) {
                int n = map.get(p) == null ? 0 : map.get(p);
                series.getData().add(new XYChart.Data<>(p.getLibelle(), n));
            }
            prioritesChart.getData().add(series);
        } catch (Exception e) {
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
