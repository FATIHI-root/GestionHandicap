package ma.ac.uir.gestionhandicap.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import ma.ac.uir.gestionhandicap.config.DatabaseConnection;
import ma.ac.uir.gestionhandicap.exception.DatabaseException;
import ma.ac.uir.gestionhandicap.model.PieceJustificative;
import ma.ac.uir.gestionhandicap.model.enums.CategoriePiece;

public class PieceJustificativeDAO {

    public int insert(PieceJustificative p) {
        String sql = "INSERT INTO piece_justificative (nom_fichier, chemin_fichier, type_fichier, taille_fichier, categorie_piece, id_utilisateur, id_demande, id_reclamation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getNomFichier());
            ps.setString(2, p.getCheminFichier());
            ps.setString(3, p.getTypeFichier());
            if (p.getTailleFichier() == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, p.getTailleFichier());
            }
            ps.setString(5, p.getCategoriePiece().name());
            if (p.getIdUtilisateur() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, p.getIdUtilisateur());
            }
            if (p.getIdDemande() == null) {
                ps.setNull(7, Types.INTEGER);
            } else {
                ps.setInt(7, p.getIdDemande());
            }
            if (p.getIdReclamation() == null) {
                ps.setNull(8, Types.INTEGER);
            } else {
                ps.setInt(8, p.getIdReclamation());
            }
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                p.setIdPiece(id);
                return id;
            }
            return -1;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de l'insertion de la pièce justificative", e);
        }
    }

    public PieceJustificative findByUtilisateurAndCategorie(int idUtilisateur, CategoriePiece categorie) {
        String sql = "SELECT * FROM piece_justificative WHERE id_utilisateur = ? AND categorie_piece = ? ORDER BY date_upload DESC LIMIT 1";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idUtilisateur);
            ps.setString(2, categorie.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildPiece(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche de pièce justificative", e);
        }
    }

    public List<PieceJustificative> findAllByUtilisateurAndCategorie(int idUtilisateur, CategoriePiece categorie) {
        List<PieceJustificative> result = new ArrayList<>();
        String sql = "SELECT * FROM piece_justificative WHERE id_utilisateur = ? AND categorie_piece = ? ORDER BY date_upload ASC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idUtilisateur);
            ps.setString(2, categorie.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(buildPiece(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche des pièces justificatives", e);
        }
    }

    public PieceJustificative findByDemande(int idDemande) {
        String sql = "SELECT * FROM piece_justificative WHERE id_demande = ? ORDER BY date_upload DESC LIMIT 1";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idDemande);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildPiece(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche de pièce par demande", e);
        }
    }

    public PieceJustificative findByReclamation(int idReclamation) {
        String sql = "SELECT * FROM piece_justificative WHERE id_reclamation = ? ORDER BY date_upload DESC LIMIT 1";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idReclamation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildPiece(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche de pièce par réclamation", e);
        }
    }

    public PieceJustificative findById(int id) {
        String sql = "SELECT * FROM piece_justificative WHERE id_piece = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildPiece(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche de pièce", e);
        }
    }

    private PieceJustificative buildPiece(ResultSet rs) throws SQLException {
        PieceJustificative p = new PieceJustificative();
        p.setIdPiece(rs.getInt("id_piece"));
        p.setNomFichier(rs.getString("nom_fichier"));
        p.setCheminFichier(rs.getString("chemin_fichier"));
        p.setTypeFichier(rs.getString("type_fichier"));

        long taille = rs.getLong("taille_fichier");
        if (rs.wasNull()) {
            p.setTailleFichier(null);
        } else {
            p.setTailleFichier(taille);
        }

        p.setCategoriePiece(CategoriePiece.valueOf(rs.getString("categorie_piece")));

        Timestamp ts = rs.getTimestamp("date_upload");
        if (ts != null) {
            p.setDateUpload(ts.toLocalDateTime());
        }

        int idUser = rs.getInt("id_utilisateur");
        if (!rs.wasNull()) p.setIdUtilisateur(idUser);
        int idDem = rs.getInt("id_demande");
        if (!rs.wasNull()) p.setIdDemande(idDem);
        int idRec = rs.getInt("id_reclamation");
        if (!rs.wasNull()) p.setIdReclamation(idRec);

        return p;
    }
}
