package ma.ac.uir.gestionhandicap.model.enums;

public enum StatutDemande {
    EN_COURS("En cours"),
    ACCEPTEE("Acceptée"),
    REFUSEE("Refusée");

    private final String libelle;

    StatutDemande(String libelle) {
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
