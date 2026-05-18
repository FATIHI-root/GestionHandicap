package ma.ac.uir.gestionhandicap.model;

import ma.ac.uir.gestionhandicap.model.enums.Role;
import ma.ac.uir.gestionhandicap.model.enums.StatutCompte;

import java.time.LocalDateTime;

public class PersonneH extends Utilisateur {

    public PersonneH() {
        super();
        this.role = Role.PERSONNE;
    }

    public PersonneH(String nom, String prenom, String email, String motDePasse, String telephone) {
        super(nom, prenom, email, motDePasse, telephone, Role.PERSONNE, StatutCompte.EN_ATTENTE);
    }

    public PersonneH(int idUtilisateur, String nom, String prenom, String email, String motDePasse, String telephone, StatutCompte statutCompte, 
                    LocalDateTime dateInscription) {
        super(idUtilisateur, nom, prenom, email, motDePasse, telephone, Role.PERSONNE, statutCompte, dateInscription);
    }
}
