package ma.ac.uir.gestionhandicap.model;

import java.time.LocalDateTime;
import ma.ac.uir.gestionhandicap.model.enums.CategoriePiece;

public class PieceJustificative {

    private int idPiece;
    private String nomFichier;
    private String cheminFichier;
    private String typeFichier;
    private Long tailleFichier;
    private CategoriePiece categoriePiece;
    private LocalDateTime dateUpload;
    private Integer idUtilisateur;
    private Integer idDemande;
    private Integer idReclamation;

    public PieceJustificative() {
    }

    public int getIdPiece() {
        return idPiece;
    }

    public void setIdPiece(int idPiece) {
        this.idPiece = idPiece;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public String getTypeFichier() {
        return typeFichier;
    }

    public void setTypeFichier(String typeFichier) {
        this.typeFichier = typeFichier;
    }

    public Long getTailleFichier() {
        return tailleFichier;
    }

    public void setTailleFichier(Long tailleFichier) {
        this.tailleFichier = tailleFichier;
    }

    public CategoriePiece getCategoriePiece() {
        return categoriePiece;
    }

    public void setCategoriePiece(CategoriePiece categoriePiece) {
        this.categoriePiece = categoriePiece;
    }

    public LocalDateTime getDateUpload() {
        return dateUpload;
    }

    public void setDateUpload(LocalDateTime dateUpload) {
        this.dateUpload = dateUpload;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Integer getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(Integer idDemande) {
        this.idDemande = idDemande;
    }

    public Integer getIdReclamation() {
        return idReclamation;
    }

    public void setIdReclamation(Integer idReclamation) {
        this.idReclamation = idReclamation;
    }
}
