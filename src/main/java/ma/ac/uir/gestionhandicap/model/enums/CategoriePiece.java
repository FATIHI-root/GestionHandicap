package ma.ac.uir.gestionhandicap.model.enums;

public enum CategoriePiece {
    INSCRIPTION("Inscription"),
    DEMANDE("Demande"),
    RECLAMATION("Réclamation");

    private final String libelle;

    CategoriePiece(String libelle) {
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
