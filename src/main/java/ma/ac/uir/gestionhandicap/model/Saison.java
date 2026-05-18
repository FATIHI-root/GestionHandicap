package ma.ac.uir.gestionhandicap.model;

import java.time.LocalDate;

public class Saison {

    private int idSaison;
    private String libelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean estActive;

    public Saison() {
    }

    public Saison(String libelle, LocalDate dateDebut, LocalDate dateFin, boolean estActive) {
        this.libelle = libelle;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.estActive = estActive;
    }

    public Saison(int idSaison, String libelle, LocalDate dateDebut, LocalDate dateFin, boolean estActive) {
        this.idSaison = idSaison;
        this.libelle = libelle;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.estActive = estActive;
    }

    public int getIdSaison() {
        return idSaison;
    }

    public void setIdSaison(int idSaison) {
        this.idSaison = idSaison;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public boolean isEstActive() {
        return estActive;
    }

    public void setEstActive(boolean estActive) {
        this.estActive = estActive;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
