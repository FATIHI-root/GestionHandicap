package ma.ac.uir.gestionhandicap.service;

import ma.ac.uir.gestionhandicap.dao.HistoriqueReclamationDAO;
import ma.ac.uir.gestionhandicap.dao.ReclamationDAO;
import ma.ac.uir.gestionhandicap.model.HistoriqueReclamation;
import ma.ac.uir.gestionhandicap.model.Reclamation;
import ma.ac.uir.gestionhandicap.model.Saison;
import ma.ac.uir.gestionhandicap.model.enums.PrioriteReclamation;
import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;

import java.sql.SQLException;
import java.util.List;

public class ReclamationService {
    private final ReclamationDAO reclamationDAO = new ReclamationDAO();
    private final HistoriqueReclamationDAO historiqueDAO = new HistoriqueReclamationDAO();
    private final SaisonService saisonService = new SaisonService();


    public boolean creerReclamation(int idUtilisateur, String objet,
                                     String description, PrioriteReclamation priorite)
                                     throws SQLException {


        if (objet == null || objet.trim().isEmpty()) {
            throw new IllegalArgumentException("L'objet ne peut pas être vide.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La description ne peut pas être vide.");
        }

        Saison active = saisonService.findActive();
        if (active == null) {
            throw new IllegalArgumentException("Aucune saison n'est active. Impossible de créer une réclamation.");
        }

        Reclamation r = new Reclamation(idUtilisateur, objet.trim(),description.trim(), priorite);
        r.setIdSaison(active.getIdSaison());

        return reclamationDAO.creer(r);
    }

    public List<Reclamation> getBySaison(int idSaison) throws SQLException {
        return reclamationDAO.getBySaison(idSaison);
    }


    public boolean modifierReclamation(int idReclamation, int idUtilisateur,String nouvelObjet, String nouvelleDescription,PrioriteReclamation nouvellePriorite)throws SQLException {

        Reclamation r = reclamationDAO.getById(idReclamation);
        if (r == null) {
            throw new IllegalArgumentException("Réclamation introuvable.");
        }


        if (r.getIdUtilisateur() != idUtilisateur) {
            throw new IllegalArgumentException("Vous ne pouvez pas modifier cette réclamation.");
        }


        if (r.getStatutReclamation() == StatutReclamation.TRAITEE ||
            r.getStatutReclamation() == StatutReclamation.REFUSEE) {
            throw new IllegalArgumentException("Cette réclamation ne peut plus être modifiée.");
        }

        if (r.getIdSaison() != null && !saisonService.isSaisonActive(r.getIdSaison())) {
            throw new IllegalArgumentException("Impossible de modifier une réclamation d'une saison clôturée.");
        }


        r.setObjet(nouvelObjet.trim());
        r.setDescription(nouvelleDescription.trim());
        r.setPriorite(nouvellePriorite);

        return reclamationDAO.modifier(r);
    }


    public boolean supprimerReclamation(int idReclamation, int idUtilisateur)throws SQLException {
        Reclamation r = reclamationDAO.getById(idReclamation);
        if (r == null) {
            throw new IllegalArgumentException("Réclamation introuvable.");
        }


        if (r.getIdUtilisateur() != idUtilisateur) {
            throw new IllegalArgumentException("Vous ne pouvez pas supprimer cette réclamation.");
        }

        if (r.getIdSaison() != null && !saisonService.isSaisonActive(r.getIdSaison())) {
            throw new IllegalArgumentException("Impossible de supprimer une réclamation d'une saison clôturée.");
        }

        return reclamationDAO.supprimer(idReclamation, idUtilisateur);
    }

    public Reclamation getReclamationById(int idReclamation) throws SQLException {
        Reclamation r = reclamationDAO.getById(idReclamation);
        if (r == null) {
            throw new IllegalArgumentException("Réclamation introuvable.");
        }
        return r;
    }


    public List<Reclamation> getMesReclamations(int idUtilisateur) throws SQLException {
        return reclamationDAO.getByUtilisateur(idUtilisateur);
    }

    public List<Reclamation> getAllReclamations() throws SQLException {
        return reclamationDAO.getAll();
    }


    public boolean changerStatut(int idReclamation, int idAdmin,StatutReclamation nouveauStatut,String commentaire) throws SQLException {

        Reclamation r = reclamationDAO.getById(idReclamation);
        if (r == null) {
            throw new IllegalArgumentException("Réclamation introuvable.");
        }

        StatutReclamation ancienStatut = r.getStatutReclamation();

        r.setStatutReclamation(nouveauStatut);
        reclamationDAO.modifier(r);

        HistoriqueReclamation h = new HistoriqueReclamation(
                idReclamation, idAdmin,
                ancienStatut, nouveauStatut,
                commentaire
        );
        return historiqueDAO.ajouter(h);
    }

    public List<HistoriqueReclamation> getHistorique(int idReclamation) throws SQLException {
        return historiqueDAO.getByReclamation(idReclamation);
    }
}
