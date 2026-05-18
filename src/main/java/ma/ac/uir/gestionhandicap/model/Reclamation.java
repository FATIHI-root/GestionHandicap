package ma.ac.uir.gestionhandicap.model;
import ma.ac.uir.gestionhandicap.model.enums.PrioriteReclamation;
import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;
import java.time.LocalDateTime;

public class Reclamation {
    private int idReclamation;
    private int idUtilisateur;
    private String objet;
    private String description;
    private PrioriteReclamation priorite;
    private StatutReclamation statutReclamation;
    private String commentaireAdmin;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Integer idSaison;

    public Reclamation() {
    }

   public Reclamation(int idUtilisateur, String objet, String description,PrioriteReclamation priorite) {
        this.idUtilisateur = idUtilisateur;
        this.objet = objet;
        this.description = description;
        this.priorite = priorite;
        this.statutReclamation = StatutReclamation.ENREGISTREE;
        this.dateCreation = LocalDateTime.now();
    }


    public int getIdReclamation() {
        return idReclamation; }

    public int getIdUtilisateur() {
        return idUtilisateur; }

    public String getObjet() {
        return objet; }

    public String getDescription() {
        return description; }

    public PrioriteReclamation getPriorite() {
        return priorite; }

    public StatutReclamation getStatutReclamation() {
        return statutReclamation; }

    public String getCommentaireAdmin() {
        return commentaireAdmin; }

    public LocalDateTime getDateCreation() {
        return dateCreation; }

    public LocalDateTime getDateModification() {
        return dateModification; }

    public Integer getIdSaison() {
        return idSaison; }


    public void setIdReclamation(int idReclamation) {
        this.idReclamation = idReclamation; }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur; }

    public void setObjet(String objet) {
        this.objet = objet; }

    public void setDescription(String description) {
        this.description = description; }

    public void setPriorite(PrioriteReclamation priorite) {
        this.priorite = priorite; }

    public void setStatutReclamation(StatutReclamation statutReclamation) {
        this.statutReclamation = statutReclamation; }

    public void setCommentaireAdmin(String commentaireAdmin) {
        this.commentaireAdmin = commentaireAdmin; }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation; }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification; }

    public void setIdSaison(Integer idSaison) {
        this.idSaison = idSaison; }

    @Override
    public String toString() {
        return "Reclamation{" +"id=" + idReclamation +", objet='" + objet + '\'' +", priorite=" + priorite +", statut=" + statutReclamation +
                ", dateCreation=" + dateCreation +'}';
    }
}
