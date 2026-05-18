package ma.ac.uir.gestionhandicap.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ma.ac.uir.gestionhandicap.config.DatabaseConnection;
import ma.ac.uir.gestionhandicap.exception.DatabaseException;
import ma.ac.uir.gestionhandicap.model.Saison;

public class SaisonDAO {

    public int insert(Saison s) {
        String sql = "INSERT INTO saison (libelle, date_debut, date_fin, est_active) VALUES (?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.getLibelle());
            ps.setDate(2, Date.valueOf(s.getDateDebut()));
            ps.setDate(3, Date.valueOf(s.getDateFin()));
            ps.setBoolean(4, s.isEstActive());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                s.setIdSaison(id);
                return id;
            }
            return -1;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de l'insertion de la saison", e);
        }
    }

    public Saison findById(int id) {
        String sql = "SELECT * FROM saison WHERE id_saison = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildSaison(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche de saison", e);
        }
    }

    public Saison findActive() {
        String sql = "SELECT * FROM saison WHERE est_active = 1 LIMIT 1";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildSaison(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche de la saison active", e);
        }
    }

    public List<Saison> findAll() {
        List<Saison> result = new ArrayList<>();
        String sql = "SELECT * FROM saison ORDER BY date_debut DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(buildSaison(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la récupération des saisons", e);
        }
    }

    public void desactiverToutes() {
        String sql = "UPDATE saison SET est_active = 0";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la désactivation des saisons", e);
        }
    }

    public void activer(int idSaison) {
        String sql = "UPDATE saison SET est_active = 1 WHERE id_saison = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idSaison);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de l'activation de la saison", e);
        }
    }

    public void cloturer(int idSaison) {
        String sql = "UPDATE saison SET est_active = 0 WHERE id_saison = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idSaison);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la clôture de la saison", e);
        }
    }

    private Saison buildSaison(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_saison");
        String libelle = rs.getString("libelle");
        LocalDateConverter c = new LocalDateConverter();
        java.time.LocalDate dDebut = c.toLocal(rs.getDate("date_debut"));
        java.time.LocalDate dFin = c.toLocal(rs.getDate("date_fin"));
        boolean active = rs.getBoolean("est_active");
        return new Saison(id, libelle, dDebut, dFin, active);
    }

    private static class LocalDateConverter {
        java.time.LocalDate toLocal(Date d) {
            return d == null ? null : d.toLocalDate();
        }
    }
}
