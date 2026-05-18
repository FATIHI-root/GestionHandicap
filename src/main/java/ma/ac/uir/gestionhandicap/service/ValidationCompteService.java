package ma.ac.uir.gestionhandicap.service;

import java.util.List;
import ma.ac.uir.gestionhandicap.dao.UtilisateurDAO;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.StatutCompte;

public class ValidationCompteService {

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public List<Utilisateur> findEnAttente() {
        return utilisateurDAO.findByStatut(StatutCompte.EN_ATTENTE);
    }

    public List<Utilisateur> findValides() {
        return utilisateurDAO.findByStatut(StatutCompte.VALIDE);
    }

    public List<Utilisateur> findRefuses() {
        return utilisateurDAO.findByStatut(StatutCompte.REFUSE);
    }

    public void valider(int idUtilisateur) {
        utilisateurDAO.updateStatut(idUtilisateur, StatutCompte.VALIDE);
    }

    public void refuser(int idUtilisateur) {
        utilisateurDAO.updateStatut(idUtilisateur, StatutCompte.REFUSE);
    }

    public void refuser(int idUtilisateur, String motif) {
        utilisateurDAO.updateStatut(idUtilisateur, StatutCompte.REFUSE, motif);
    }

    public int countEnAttente() {
        return utilisateurDAO.countByStatut(StatutCompte.EN_ATTENTE);
    }

    public int countValides() {
        return utilisateurDAO.countByStatut(StatutCompte.VALIDE);
    }

    public int countRefuses() {
        return utilisateurDAO.countByStatut(StatutCompte.REFUSE);
    }
}
