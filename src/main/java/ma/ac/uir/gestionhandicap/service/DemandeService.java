package ma.ac.uir.gestionhandicap.service;

import java.util.List;
import ma.ac.uir.gestionhandicap.dao.DemandeDAO;
import ma.ac.uir.gestionhandicap.dao.HistoriqueDemandeDAO;
import ma.ac.uir.gestionhandicap.exception.NotFoundException;
import ma.ac.uir.gestionhandicap.exception.ValidationException;
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.HistoriqueDemande;
import ma.ac.uir.gestionhandicap.model.Saison;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;
import ma.ac.uir.gestionhandicap.model.enums.TypeDemande;

public class DemandeService {

    private DemandeDAO demandeDAO = new DemandeDAO();
    private HistoriqueDemandeDAO historiqueDAO = new HistoriqueDemandeDAO();
    private SaisonService saisonService = new SaisonService();

    public Demande creerDemande(int idUtilisateur, TypeDemande type, String objet, String description) {
        if (type == null) {
            throw new ValidationException("Le type de demande est obligatoire");
        }
        if (objet == null || objet.trim().isEmpty()) {
            throw new ValidationException("L'objet est obligatoire");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("La description est obligatoire");
        }

        Saison active = saisonService.findActive();
        if (active == null) {
            throw new ValidationException("Aucune saison n'est active. Impossible de créer une demande.");
        }

        Demande d = new Demande(idUtilisateur, type, objet.trim(), description.trim());
        d.setIdSaison(active.getIdSaison());
        demandeDAO.insert(d);
        return d;
    }

    public void modifierDemande(Demande d) {
        if (d.getIdDemande() <= 0) {
            throw new ValidationException("La demande doit avoir un identifiant");
        }
        verifierSaisonActive(d.getIdSaison(), "Impossible de modifier une demande d'une saison clôturée");
        demandeDAO.update(d);
    }

    public void supprimerDemande(int idDemande) {
        Demande d = demandeDAO.findById(idDemande);
        if (d != null) {
            verifierSaisonActive(d.getIdSaison(), "Impossible de supprimer une demande d'une saison clôturée");
        }
        demandeDAO.delete(idDemande);
    }

    private void verifierSaisonActive(Integer idSaison, String message) {
        if (idSaison == null) return;
        if (!saisonService.isSaisonActive(idSaison)) {
            throw new ValidationException(message);
        }
    }

    public List<Demande> findBySaison(int idSaison) {
        return demandeDAO.findBySaison(idSaison);
    }

    public Demande findById(int id) {
        Demande d = demandeDAO.findById(id);
        if (d == null) {
            throw new NotFoundException("Demande introuvable");
        }
        return d;
    }

    public List<Demande> findAll() {
        return demandeDAO.findAll();
    }

    public List<Demande> findByUtilisateur(int idUtilisateur) {
        return demandeDAO.findByUtilisateur(idUtilisateur);
    }

    public List<Demande> findByStatut(StatutDemande statut) {
        return demandeDAO.findByStatut(statut);
    }

    public void changerStatut(int idDemande, StatutDemande nouveauStatut, int idAdmin, String commentaire) {
        Demande d = demandeDAO.findById(idDemande);
        if (d == null) {
            throw new NotFoundException("Demande introuvable");
        }
        StatutDemande ancien = d.getStatutDemande();
        d.setStatutDemande(nouveauStatut);
        if (commentaire != null && !commentaire.trim().isEmpty()) {
            d.setCommentaireAdmin(commentaire.trim());
        }
        demandeDAO.update(d);

        HistoriqueDemande log = new HistoriqueDemande(idDemande, idAdmin, ancien, nouveauStatut, commentaire);
        historiqueDAO.insert(log);
    }

    public List<HistoriqueDemande> consulterHistorique(int idDemande) {
        return historiqueDAO.findByDemande(idDemande);
    }

    public int countByStatut(StatutDemande statut) {
        return demandeDAO.countByStatut(statut);
    }

    public int countByUtilisateur(int idUtilisateur) {
        return demandeDAO.countByUtilisateur(idUtilisateur);
    }

    public int countByUtilisateurAndStatut(int idUtilisateur, StatutDemande statut) {
        return demandeDAO.countByUtilisateurAndStatut(idUtilisateur, statut);
    }

    public int countTotal() {
        return demandeDAO.count();
    }
}
