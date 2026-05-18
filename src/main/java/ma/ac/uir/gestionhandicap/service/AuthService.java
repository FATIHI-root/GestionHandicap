package ma.ac.uir.gestionhandicap.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import ma.ac.uir.gestionhandicap.dao.UtilisateurDAO;
import ma.ac.uir.gestionhandicap.exception.AuthenticationException;
import ma.ac.uir.gestionhandicap.exception.ValidationException;
import ma.ac.uir.gestionhandicap.model.PersonneH;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.StatutCompte;
import ma.ac.uir.gestionhandicap.util.PasswordUtil;

public class AuthService {

    private UtilisateurDAO utilisateurDAO;
    private PieceJustificativeService pieceJustificativeService;

    public AuthService() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.pieceJustificativeService = new PieceJustificativeService();
    }

    public Utilisateur login(String email, String motDePasseEnClair) {
        if (email == null || email.isEmpty()) {
            throw new ValidationException("L'email est obligatoire");
        }
        if (motDePasseEnClair == null || motDePasseEnClair.isEmpty()) {
            throw new ValidationException("Le mot de passe est obligatoire");
        }

        Utilisateur u = utilisateurDAO.findByEmail(email);
        if (u == null) {
            throw new AuthenticationException("Email introuvable");
        }

        if (!PasswordUtil.verify(motDePasseEnClair, u.getMotDePasse())) {
            throw new AuthenticationException("Mot de passe incorrect");
        }

        if (u.getStatutCompte() == StatutCompte.EN_ATTENTE) {
            throw new AuthenticationException("Compte en attente de validation");
        }
        if (u.getStatutCompte() == StatutCompte.REFUSE) {
            String motif = u.getMotifRefus();
            if (motif != null && !motif.trim().isEmpty()) {
                throw new AuthenticationException("Votre compte a été refusé.\n\nMotif : " + motif);
            } else {
                throw new AuthenticationException("Votre compte a été refusé.");
            }
        }

        return u;
    }

    public PersonneH register(String nom, String prenom, String email, String telephone,
                              String motDePasse, String motDePasseConfirm) {
        return register(nom, prenom, email, telephone, motDePasse, motDePasseConfirm, (List<File>) null);
    }

    public PersonneH register(String nom, String prenom, String email, String telephone,
                              String motDePasse, String motDePasseConfirm,
                              File justificatif) {
        List<File> list = new ArrayList<>();
        if (justificatif != null) list.add(justificatif);
        return register(nom, prenom, email, telephone, motDePasse, motDePasseConfirm, list);
    }

    public PersonneH register(String nom, String prenom, String email, String telephone,
                              String motDePasse, String motDePasseConfirm,
                              List<File> justificatifs) {

        if (nom == null || nom.isEmpty()) {
            throw new ValidationException("Le nom est obligatoire");
        }
        if (prenom == null || prenom.isEmpty()) {
            throw new ValidationException("Le prénom est obligatoire");
        }
        if (email == null || email.isEmpty() || !email.contains("@")) {
            throw new ValidationException("Email invalide");
        }
        if (motDePasse == null || motDePasse.length() < 6) {
            throw new ValidationException("Le mot de passe doit contenir au moins 6 caractères");
        }
        if (!motDePasse.equals(motDePasseConfirm)) {
            throw new ValidationException("Les mots de passe ne correspondent pas");
        }

        if (utilisateurDAO.existsByEmail(email)) {
            throw new ValidationException("Email déjà utilisé");
        }

        String mdpHash = PasswordUtil.hash(motDePasse);
        PersonneH personne = new PersonneH(nom, prenom, email, mdpHash, telephone);
        personne.setStatutCompte(StatutCompte.EN_ATTENTE);

        utilisateurDAO.insert(personne);

        if (justificatifs != null) {
            for (int i = 0; i < justificatifs.size(); i++) {
                File f = justificatifs.get(i);
                if (f != null && f.exists()) {
                    try {
                        pieceJustificativeService.storeInscriptionFile(f, personne.getIdUtilisateur());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return personne;
    }
}
