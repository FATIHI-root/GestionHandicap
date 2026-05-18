package ma.ac.uir.gestionhandicap.model.enums;

public enum StatutReclamation {
    ENREGISTREE("Enregistrée"),
    EN_COURS("En cours"),
    TRAITEE("Traitée"),
    REFUSEE("Refusée");

    private final String libelle;

    StatutReclamation(String libelle) {
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
