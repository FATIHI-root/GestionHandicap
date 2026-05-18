package ma.ac.uir.gestionhandicap.model;

import ma.ac.uir.gestionhandicap.model.enums.Role;
import ma.ac.uir.gestionhandicap.model.enums.StatutCompte;

import java.time.LocalDateTime;

public class Administrateur extends Utilisateur {

    public Administrateur() {
        super();
        this.role = Role.ADMIN;
    }

    public Administrateur(String nom, String prenom, String email, String motDePasse, String telephone) {
        super(nom, prenom, email, motDePasse, telephone, Role.ADMIN, StatutCompte.VALIDE);
    }

    public Administrateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse,String telephone, 
            StatutCompte statutCompte, LocalDateTime dateInscription) {
        super(idUtilisateur, nom, prenom, email, motDePasse, telephone, Role.ADMIN, statutCompte, dateInscription);
    }
}
