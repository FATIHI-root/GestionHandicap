package ma.ac.uir.gestionhandicap.service;

import java.io.File;
import java.io.IOException;
import ma.ac.uir.gestionhandicap.dao.PieceJustificativeDAO;
import ma.ac.uir.gestionhandicap.exception.DatabaseException;
import ma.ac.uir.gestionhandicap.model.PieceJustificative;
import ma.ac.uir.gestionhandicap.model.enums.CategoriePiece;
import ma.ac.uir.gestionhandicap.util.FileCompressor;

public class PieceJustificativeService {

    private static final String BASE_DIR =
            System.getProperty("user.dir") + File.separator + "documents" + File.separator + "justificatifs";

    private PieceJustificativeDAO dao = new PieceJustificativeDAO();

    public PieceJustificative storeInscriptionFile(File source, int idUtilisateur) {
        return store(source, CategoriePiece.INSCRIPTION, idUtilisateur, null, null);
    }

    public PieceJustificative storeDemandeFile(File source, int idDemande) {
        return store(source, CategoriePiece.DEMANDE, null, idDemande, null);
    }

    public PieceJustificative storeReclamationFile(File source, int idReclamation) {
        return store(source, CategoriePiece.RECLAMATION, null, null, idReclamation);
    }

    public PieceJustificative findDemandeDocument(int idDemande) {
        return dao.findByDemande(idDemande);
    }

    public PieceJustificative findReclamationDocument(int idReclamation) {
        return dao.findByReclamation(idReclamation);
    }

    public PieceJustificative findInscriptionDocument(int idUtilisateur) {
        return dao.findByUtilisateurAndCategorie(idUtilisateur, CategoriePiece.INSCRIPTION);
    }

    public java.util.List<PieceJustificative> findAllInscriptionDocuments(int idUtilisateur) {
        return dao.findAllByUtilisateurAndCategorie(idUtilisateur, CategoriePiece.INSCRIPTION);
    }

    private PieceJustificative store(File source, CategoriePiece categorie, Integer idUtilisateur,
                                     Integer idDemande, Integer idReclamation) {
        if (source == null || !source.exists()) {
            throw new DatabaseException("Le fichier source est invalide", null);
        }

        String subDir;
        if (categorie == CategoriePiece.INSCRIPTION) {
            subDir = "inscription";
        } else if (categorie == CategoriePiece.DEMANDE) {
            subDir = "demande";
        } else {
            subDir = "reclamation";
        }

        File destDir = new File(BASE_DIR + File.separator + subDir);

        String origName = source.getName();
        String prefix = idUtilisateur == null ? "anon" : String.valueOf(idUtilisateur);
        String baseName = prefix + "_" + System.currentTimeMillis() + "_" + FileCompressor.stripExtension(origName);

        File dest;
        try {
            dest = FileCompressor.compress(source, destDir, baseName);
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de la compression du fichier", e);
        }

        PieceJustificative p = new PieceJustificative();
        p.setNomFichier(origName);
        p.setCheminFichier(dest.getAbsolutePath());
        p.setTypeFichier(FileCompressor.getExtension(dest.getName()).toUpperCase());
        p.setTailleFichier(dest.length());
        p.setCategoriePiece(categorie);
        p.setIdUtilisateur(idUtilisateur);
        p.setIdDemande(idDemande);
        p.setIdReclamation(idReclamation);

        dao.insert(p);
        return p;
    }
}
