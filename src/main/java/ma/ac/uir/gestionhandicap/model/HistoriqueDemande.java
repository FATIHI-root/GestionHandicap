package ma.ac.uir.gestionhandicap.model;

import java.time.LocalDateTime;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;

public class HistoriqueDemande {

    private int idHistoriqueDemande;
    private int idDemande;
    private int idAdmin;
    private StatutDemande ancienStatut;
    private StatutDemande nouveauStatut;
    private String commentaire;
    private LocalDateTime dateAction;

    public HistoriqueDemande() {
    }

    public HistoriqueDemande(int idDemande, int idAdmin, StatutDemande ancienStatut,
                             StatutDemande nouveauStatut, String commentaire) {
        this.idDemande = idDemande;
        this.idAdmin = idAdmin;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.commentaire = commentaire;
        this.dateAction = LocalDateTime.now();
    }

    public HistoriqueDemande(int idHistoriqueDemande, int idDemande, int idAdmin,
                             StatutDemande ancienStatut, StatutDemande nouveauStatut,
                             String commentaire, LocalDateTime dateAction) {
        this.idHistoriqueDemande = idHistoriqueDemande;
        this.idDemande = idDemande;
        this.idAdmin = idAdmin;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.commentaire = commentaire;
        this.dateAction = dateAction;
    }

    public int getIdHistoriqueDemande() {
        return idHistoriqueDemande;
    }

    public void setIdHistoriqueDemande(int idHistoriqueDemande) {
        this.idHistoriqueDemande = idHistoriqueDemande;
    }

    public int getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(int idDemande) {
        this.idDemande = idDemande;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public StatutDemande getAncienStatut() {
        return ancienStatut;
    }

    public void setAncienStatut(StatutDemande ancienStatut) {
        this.ancienStatut = ancienStatut;
    }

    public StatutDemande getNouveauStatut() {
        return nouveauStatut;
    }

    public void setNouveauStatut(StatutDemande nouveauStatut) {
        this.nouveauStatut = nouveauStatut;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }
}
