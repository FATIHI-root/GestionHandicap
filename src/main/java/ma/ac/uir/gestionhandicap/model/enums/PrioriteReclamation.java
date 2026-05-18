package ma.ac.uir.gestionhandicap.model.enums;

public enum PrioriteReclamation {
    BASSE("Basse"),
    NORMALE("Normale"),
    HAUTE("Haute");

    private final String libelle;

    PrioriteReclamation(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
