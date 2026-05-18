package ma.ac.uir.gestionhandicap.model.enums;

public enum TypeDemande {
    AMENAGEMENT_EXAMEN("Aménagement d'examen"),
    ACCESSIBILITE("Accessibilité"),
    ACCOMPAGNEMENT("Accompagnement"),
    AUTRE("Autre");

    private final String libelle;

    TypeDemande(String libelle) {
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
