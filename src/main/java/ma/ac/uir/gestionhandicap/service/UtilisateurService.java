package ma.ac.uir.gestionhandicap.service;

import ma.ac.uir.gestionhandicap.dao.UtilisateurDAO;
import ma.ac.uir.gestionhandicap.exception.AuthenticationException;
import ma.ac.uir.gestionhandicap.exception.NotFoundException;
import ma.ac.uir.gestionhandicap.exception.ValidationException;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.util.PasswordUtil;
import ma.ac.uir.gestionhandicap.util.ValidatorUtil;

public class UtilisateurService {

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public Utilisateur findById(int id) {
        Utilisateur u = utilisateurDAO.findById(id);
        if (u == null) {
            throw new NotFoundException("Utilisateur introuvable");
        }
        return u;
    }

    public void updateProfile(int id, String nom, String prenom, String telephone) {
        if (ValidatorUtil.isEmpty(nom) || ValidatorUtil.isEmpty(prenom)) {
            throw new ValidationException("Le nom et le prénom sont obligatoires");
        }
        if (!ValidatorUtil.isValidName(nom)) {
            throw new ValidationException("Le nom doit contenir au moins 2 caractères");
        }
        if (!ValidatorUtil.isValidName(prenom)) {
            throw new ValidationException("Le prénom doit contenir au moins 2 caractères");
        }
        if (telephone != null && !telephone.isEmpty() && !ValidatorUtil.isValidPhone(telephone)) {
            throw new ValidationException("Numéro de téléphone invalide");
        }
        utilisateurDAO.updateProfile(id, nom, prenom, telephone);
    }

    public void changePassword(int id, String ancien, String nouveau, String confirmation) {
        if (ValidatorUtil.isEmpty(ancien) || ValidatorUtil.isEmpty(nouveau) || ValidatorUtil.isEmpty(confirmation)) {
            throw new ValidationException("Tous les champs sont obligatoires");
        }
        if (nouveau.length() < 6) {
            throw new ValidationException("Le nouveau mot de passe doit contenir au moins 6 caractères");
        }
        if (!nouveau.equals(confirmation)) {
            throw new ValidationException("La confirmation ne correspond pas");
        }

        Utilisateur u = utilisateurDAO.findById(id);
        if (u == null) {
            throw new NotFoundException("Utilisateur introuvable");
        }
        if (!PasswordUtil.verify(ancien, u.getMotDePasse())) {
            throw new AuthenticationException("Mot de passe actuel incorrect");
        }

        String hash = PasswordUtil.hash(nouveau);
        utilisateurDAO.updatePassword(id, hash);
    }
}
