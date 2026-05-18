package ma.ac.uir.gestionhandicap.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ma.ac.uir.gestionhandicap.dao.DemandeDAO;
import ma.ac.uir.gestionhandicap.dao.HistoriqueDemandeDAO;
import ma.ac.uir.gestionhandicap.dao.HistoriqueReclamationDAO;
import ma.ac.uir.gestionhandicap.dao.ReclamationDAO;
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.HistoriqueDemande;
import ma.ac.uir.gestionhandicap.model.HistoriqueReclamation;
import ma.ac.uir.gestionhandicap.model.Reclamation;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;
import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;

public class ArchivageService {

    private DemandeDAO demandeDAO = new DemandeDAO();
    private ReclamationDAO reclamationDAO = new ReclamationDAO();
    private HistoriqueDemandeDAO historiqueDemandeDAO = new HistoriqueDemandeDAO();
    private HistoriqueReclamationDAO historiqueReclamationDAO = new HistoriqueReclamationDAO();

    public List<Demande> findDemandesArchivees() {
        List<Demande> result = new ArrayList<>();
        result.addAll(demandeDAO.findByStatut(StatutDemande.ACCEPTEE));
        result.addAll(demandeDAO.findByStatut(StatutDemande.REFUSEE));
        return result;
    }

    public List<Reclamation> findReclamationsArchivees() {
        List<Reclamation> result = new ArrayList<>();
        try {
            List<Reclamation> all = reclamationDAO.getAll();
            for (Reclamation r : all) {
                StatutReclamation s = r.getStatutReclamation();
                if (s == StatutReclamation.TRAITEE || s == StatutReclamation.REFUSEE) {
                    result.add(r);
                }
            }
        } catch (SQLException e) {
        }
        return result;
    }

    public List<HistoriqueDemande> historiqueDemande(int idDemande) {
        return historiqueDemandeDAO.findByDemande(idDemande);
    }

    public List<HistoriqueReclamation> historiqueReclamation(int idReclamation) {
        try {
            return historiqueReclamationDAO.getByReclamation(idReclamation);
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public int countDemandesArchivees() {
        return demandeDAO.countByStatut(StatutDemande.ACCEPTEE) + demandeDAO.countByStatut(StatutDemande.REFUSEE);
    }

    public int countReclamationsArchivees() {
        return findReclamationsArchivees().size();
    }
}
