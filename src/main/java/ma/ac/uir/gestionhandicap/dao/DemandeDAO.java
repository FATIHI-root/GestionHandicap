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
import ma.ac.uir.gestionhandicap.model.Demande;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;
import ma.ac.uir.gestionhandicap.model.enums.TypeDemande;

public class DemandeDAO {

    public int insert(Demande d) {
        String sql = "INSERT INTO demande (id_utilisateur, type_demande, objet, description, statut_demande, id_saison) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, d.getIdUtilisateur());
            ps.setString(2, d.getTypeDemande().name());
            ps.setString(3, d.getObjet());
            ps.setString(4, d.getDescription());
            ps.setString(5, d.getStatutDemande().name());
            if (d.getIdSaison() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, d.getIdSaison());
            }
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                d.setIdDemande(id);
                return id;
            }
            return -1;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de l'insertion de la demande", e);
        }
    }

    public void update(Demande d) {
        String sql = "UPDATE demande SET type_demande = ?, objet = ?, description = ?, statut_demande = ?, commentaire_admin = ?, date_modification = CURRENT_TIMESTAMP WHERE id_demande = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, d.getTypeDemande().name());
            ps.setString(2, d.getObjet());
            ps.setString(3, d.getDescription());
            ps.setString(4, d.getStatutDemande().name());
            if (d.getCommentaireAdmin() == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, d.getCommentaireAdmin());
            }
            ps.setInt(6, d.getIdDemande());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la mise à jour de la demande", e);
        }
    }

    public void delete(int idDemande) {
        String sql = "DELETE FROM demande WHERE id_demande = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idDemande);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la suppression de la demande", e);
        }
    }

    public Demande findById(int id) {
        String sql = "SELECT * FROM demande WHERE id_demande = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildDemande(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche de la demande", e);
        }
    }

    public List<Demande> findAll() {
        List<Demande> result = new ArrayList<>();
        String sql = "SELECT * FROM demande ORDER BY date_creation DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(buildDemande(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la récupération des demandes", e);
        }
    }

    public List<Demande> findByUtilisateur(int idUtilisateur) {
        List<Demande> result = new ArrayList<>();
        String sql = "SELECT * FROM demande WHERE id_utilisateur = ? ORDER BY date_creation DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(buildDemande(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche des demandes de l'utilisateur", e);
        }
    }

    public List<Demande> findByStatut(StatutDemande statut) {
        List<Demande> result = new ArrayList<>();
        String sql = "SELECT * FROM demande WHERE statut_demande = ? ORDER BY date_creation DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, statut.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(buildDemande(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche par statut", e);
        }
    }

    public int countByStatut(StatutDemande statut) {
        String sql = "SELECT COUNT(*) FROM demande WHERE statut_demande = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, statut.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors du comptage des demandes", e);
        }
    }

    public int countByUtilisateur(int idUtilisateur) {
        String sql = "SELECT COUNT(*) FROM demande WHERE id_utilisateur = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors du comptage", e);
        }
    }

    public int countByUtilisateurAndStatut(int idUtilisateur, StatutDemande statut) {
        String sql = "SELECT COUNT(*) FROM demande WHERE id_utilisateur = ? AND statut_demande = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idUtilisateur);
            ps.setString(2, statut.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors du comptage", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM demande";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors du comptage", e);
        }
    }

    public List<Demande> findBySaison(int idSaison) {
        List<Demande> result = new ArrayList<>();
        String sql = "SELECT * FROM demande WHERE id_saison = ? ORDER BY date_creation DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idSaison);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(buildDemande(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche par saison", e);
        }
    }

    private Demande buildDemande(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_demande");
        int idUser = rs.getInt("id_utilisateur");
        TypeDemande type = TypeDemande.valueOf(rs.getString("type_demande"));
        String objet = rs.getString("objet");
        String description = rs.getString("description");
        StatutDemande statut = StatutDemande.valueOf(rs.getString("statut_demande"));
        String comAdmin = rs.getString("commentaire_admin");

        Timestamp tsCreation = rs.getTimestamp("date_creation");
        java.time.LocalDateTime dateCreation = tsCreation == null ? null : tsCreation.toLocalDateTime();

        Timestamp tsModif = rs.getTimestamp("date_modification");
        java.time.LocalDateTime dateModif = tsModif == null ? null : tsModif.toLocalDateTime();

        Demande d = new Demande(id, idUser, type, objet, description, statut, comAdmin, dateCreation, dateModif);
        try {
            int idSaison = rs.getInt("id_saison");
            if (!rs.wasNull()) d.setIdSaison(idSaison);
        } catch (SQLException ignored) {
        }
        return d;
    }
}
