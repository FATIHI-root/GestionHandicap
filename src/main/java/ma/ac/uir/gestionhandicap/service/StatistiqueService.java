package ma.ac.uir.gestionhandicap.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ma.ac.uir.gestionhandicap.dao.DemandeDAO;
import ma.ac.uir.gestionhandicap.dao.ReclamationDAO;
import ma.ac.uir.gestionhandicap.dao.UtilisateurDAO;
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.Reclamation;
import ma.ac.uir.gestionhandicap.model.enums.PrioriteReclamation;
import ma.ac.uir.gestionhandicap.model.enums.StatutCompte;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;
import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;
import ma.ac.uir.gestionhandicap.model.enums.TypeDemande;

public class StatistiqueService {

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private DemandeDAO demandeDAO = new DemandeDAO();
    private ReclamationDAO reclamationDAO = new ReclamationDAO();

    public int countUtilisateursValides() {
        return utilisateurDAO.countByStatut(StatutCompte.VALIDE);
    }

    public int countUtilisateursEnAttente() {
        return utilisateurDAO.countByStatut(StatutCompte.EN_ATTENTE);
    }

    public int countUtilisateursRefuses() {
        return utilisateurDAO.countByStatut(StatutCompte.REFUSE);
    }

    public int countTotalUtilisateurs() {
        return countUtilisateursValides() + countUtilisateursEnAttente() + countUtilisateursRefuses();
    }

    public int countDemandesTotal() {
        return demandeDAO.count();
    }

    public int countDemandesEnCours() {
        return demandeDAO.countByStatut(StatutDemande.EN_COURS);
    }

    public int countDemandesAcceptees() {
        return demandeDAO.countByStatut(StatutDemande.ACCEPTEE);
    }

    public int countDemandesRefusees() {
        return demandeDAO.countByStatut(StatutDemande.REFUSEE);
    }

    public Map<TypeDemande, Integer> repartitionDemandesParType() {
        Map<TypeDemande, Integer> map = new HashMap<>();
        for (TypeDemande t : TypeDemande.values()) {
            map.put(t, 0);
        }
        try {
            List<Demande> all = demandeDAO.findAll();
            for (Demande d : all) {
                TypeDemande t = d.getTypeDemande();
                if (t != null) {
                    map.put(t, map.get(t) + 1);
                }
            }
        } catch (Exception e) {
        }
        return map;
    }

    public int countReclamationsTotal() {
        try {
            return reclamationDAO.getAll().size();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int countReclamationsParStatut(StatutReclamation statut) {
        try {
            int total = 0;
            List<Reclamation> all = reclamationDAO.getAll();
            for (Reclamation r : all) {
                if (r.getStatutReclamation() == statut) {
                    total++;
                }
            }
            return total;
        } catch (SQLException e) {
            return 0;
        }
    }

    public Map<PrioriteReclamation, Integer> repartitionReclamationsParPriorite() {
        Map<PrioriteReclamation, Integer> map = new HashMap<>();
        for (PrioriteReclamation p : PrioriteReclamation.values()) {
            map.put(p, 0);
        }
        try {
            List<Reclamation> all = reclamationDAO.getAll();
            for (Reclamation r : all) {
                PrioriteReclamation p = r.getPriorite();
                if (p != null) {
                    map.put(p, map.get(p) + 1);
                }
            }
        } catch (SQLException e) {
        }
        return map;
    }
}
