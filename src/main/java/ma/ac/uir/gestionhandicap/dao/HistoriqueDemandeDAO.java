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
import ma.ac.uir.gestionhandicap.model.HistoriqueDemande;
import ma.ac.uir.gestionhandicap.model.enums.StatutDemande;

public class HistoriqueDemandeDAO {

    public int insert(HistoriqueDemande h) {
        String sql = "INSERT INTO historique_demande (id_demande, id_admin, ancien_statut, nouveau_statut, commentaire) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, h.getIdDemande());
            ps.setInt(2, h.getIdAdmin());
            if (h.getAncienStatut() == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, h.getAncienStatut().name());
            }
            ps.setString(4, h.getNouveauStatut().name());
            if (h.getCommentaire() == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, h.getCommentaire());
            }
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                h.setIdHistoriqueDemande(id);
                return id;
            }
            return -1;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de l'insertion de l'historique", e);
        }
    }

    public List<HistoriqueDemande> findByDemande(int idDemande) {
        List<HistoriqueDemande> result = new ArrayList<>();
        String sql = "SELECT * FROM historique_demande WHERE id_demande = ? ORDER BY date_action DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idDemande);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(buildHistorique(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche d'historique", e);
        }
    }

    private HistoriqueDemande buildHistorique(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_historique_demande");
        int idDem = rs.getInt("id_demande");
        int idAdm = rs.getInt("id_admin");

        String ancien = rs.getString("ancien_statut");
        StatutDemande ancienStatut = ancien == null ? null : StatutDemande.valueOf(ancien);
        StatutDemande nouveauStatut = StatutDemande.valueOf(rs.getString("nouveau_statut"));

        String commentaire = rs.getString("commentaire");

        Timestamp ts = rs.getTimestamp("date_action");
        java.time.LocalDateTime date = ts == null ? null : ts.toLocalDateTime();

        return new HistoriqueDemande(id, idDem, idAdm, ancienStatut, nouveauStatut, commentaire, date);
    }
}
