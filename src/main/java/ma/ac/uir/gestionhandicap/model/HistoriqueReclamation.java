package ma.ac.uir.gestionhandicap.model;

import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;
import java.time.LocalDateTime;

public class HistoriqueReclamation {

    private int idHistoriqueReclamation;
    private int idReclamation;
    private int idAdmin;
    private StatutReclamation ancienStatut;   
    private StatutReclamation nouveauStatut;
    private String commentaire;
    private LocalDateTime dateAction;

    
    public HistoriqueReclamation() {
    }

    public HistoriqueReclamation(int idReclamation, int idAdmin,StatutReclamation ancienStatut,StatutReclamation nouveauStatut,String commentaire) {
        this.idReclamation = idReclamation;
        this.idAdmin = idAdmin;
        this.ancienStatut = ancienStatut;     
        this.nouveauStatut = nouveauStatut;
        this.commentaire = commentaire;
        this.dateAction = LocalDateTime.now(); 
    }

    
    public int getIdHistoriqueReclamation() {
        return idHistoriqueReclamation; }
    
    public int getIdReclamation() {
        return idReclamation; }
    
    public int getIdAdmin() {
        return idAdmin; }
    
    public StatutReclamation getAncienStatut() {
        return ancienStatut; }
    
    public StatutReclamation getNouveauStatut() {
        return nouveauStatut; }
    
    public String getCommentaire() {
        return commentaire; }
    
    public LocalDateTime getDateAction() {
        return dateAction; }

    
    public void setIdHistoriqueReclamation(int id) {
        this.idHistoriqueReclamation = id; }
    
    public void setIdReclamation(int idReclamation) {
        this.idReclamation = idReclamation; }
    
    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin; }
    
    public void setAncienStatut(StatutReclamation ancienStatut) {
        this.ancienStatut = ancienStatut; }
    
    public void setNouveauStatut(StatutReclamation nouveauStatut) {
        this.nouveauStatut = nouveauStatut; }
    
    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire; }
    
    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction; }
    
   
    @Override
    public String toString() {
        return "HistoriqueReclamation{" +"id=" + idHistoriqueReclamation +", idReclamation=" + idReclamation +", idAdmin=" + idAdmin +", ancienStatut=" + ancienStatut +", nouveauStatut=" + nouveauStatut +", dateAction=" + dateAction +'}';
    }
}