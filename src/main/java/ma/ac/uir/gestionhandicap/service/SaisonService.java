package ma.ac.uir.gestionhandicap.service;

import java.time.LocalDate;
import java.util.List;
import ma.ac.uir.gestionhandicap.dao.SaisonDAO;
import ma.ac.uir.gestionhandicap.exception.NotFoundException;
import ma.ac.uir.gestionhandicap.exception.ValidationException;
import ma.ac.uir.gestionhandicap.model.Saison;

public class SaisonService {

    private SaisonDAO saisonDAO = new SaisonDAO();

    public Saison findActive() {
        return saisonDAO.findActive();
    }

    public Saison findById(int id) {
        Saison s = saisonDAO.findById(id);
        if (s == null) throw new NotFoundException("Saison introuvable");
        return s;
    }

    public List<Saison> findAll() {
        return saisonDAO.findAll();
    }

    public Saison creerSaison(String libelle, LocalDate debut, LocalDate fin) {
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new ValidationException("Le libellé est obligatoire");
        }
        if (debut == null || fin == null) {
            throw new ValidationException("Les dates de début et de fin sont obligatoires");
        }
        if (!debut.isBefore(fin)) {
            throw new ValidationException("La date de fin doit être postérieure à la date de début");
        }
        Saison s = new Saison(libelle.trim(), debut, fin, false);
        saisonDAO.insert(s);
        return s;
    }

    public void activerSaison(int idSaison) {
        Saison s = findById(idSaison);
        saisonDAO.desactiverToutes();
        saisonDAO.activer(idSaison);
        s.setEstActive(true);
    }

    public void cloturerSaison(int idSaison) {
        findById(idSaison);
        saisonDAO.cloturer(idSaison);
    }

    public boolean isSaisonActive(int idSaison) {
        Saison s = saisonDAO.findById(idSaison);
        return s != null && s.isEstActive();
    }
}
