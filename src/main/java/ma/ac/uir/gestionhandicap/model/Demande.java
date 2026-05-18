package ma.ac.uir.gestionhandicap.model;

import java.time.LocalDateTime;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;
import ma.ac.uir.gestionhandicap.model.enums.TypeDemande;

public class Demande {

    private int idDemande;
    private int idUtilisateur;
    private TypeDemande typeDemande;
    private String objet;
    private String description;
    private StatutDemande statutDemande;
    private String commentaireAdmin;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Integer idSaison;

    public Demande() {
    }

    public Demande(int idUtilisateur, TypeDemande typeDemande, String objet, String description) {
        this.idUtilisateur = idUtilisateur;
        this.typeDemande = typeDemande;
        this.objet = objet;
        this.description = description;
        this.statutDemande = StatutDemande.EN_COURS;
        this.dateCreation = LocalDateTime.now();
    }

    public Demande(int idDemande, int idUtilisateur, TypeDemande typeDemande, String objet,
                   String description, StatutDemande statutDemande, String commentaireAdmin,
                   LocalDateTime dateCreation, LocalDateTime dateModification) {
        this.idDemande = idDemande;
        this.idUtilisateur = idUtilisateur;
        this.typeDemande = typeDemande;
        this.objet = objet;
        this.description = description;
        this.statutDemande = statutDemande;
        this.commentaireAdmin = commentaireAdmin;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
    }

    public int getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(int idDemande) {
        this.idDemande = idDemande;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public TypeDemande getTypeDemande() {
        return typeDemande;
    }

    public void setTypeDemande(TypeDemande typeDemande) {
        this.typeDemande = typeDemande;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StatutDemande getStatutDemande() {
        return statutDemande;
    }

    public void setStatutDemande(StatutDemande statutDemande) {
        this.statutDemande = statutDemande;
    }

    public String getCommentaireAdmin() {
        return commentaireAdmin;
    }

    public void setCommentaireAdmin(String commentaireAdmin) {
        this.commentaireAdmin = commentaireAdmin;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public Integer getIdSaison() {
        return idSaison;
    }

    public void setIdSaison(Integer idSaison) {
        this.idSaison = idSaison;
    }
}
