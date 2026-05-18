package ma.ac.uir.gestionhandicap.dao;

import ma.ac.uir.gestionhandicap.config.DatabaseConnection;
import ma.ac.uir.gestionhandicap.model.HistoriqueReclamation;
import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueReclamationDAO {

 
    public boolean ajouter(HistoriqueReclamation h) throws SQLException {
        String sql = "INSERT INTO historique_reclamation (id_reclamation, id_admin, ancien_statut, nouveau_statut, commentaire, date_action) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, h.getIdReclamation());
        ps.setInt(2, h.getIdAdmin());

      
        if (h.getAncienStatut() != null) {
            ps.setString(3, h.getAncienStatut().name());
        } else {
            ps.setNull(3, Types.VARCHAR);
        }

        ps.setString(4, h.getNouveauStatut().name());
        ps.setString(5, h.getCommentaire());
        ps.setTimestamp(6, Timestamp.valueOf(h.getDateAction()));

        return ps.executeUpdate() > 0;
    }

    public List<HistoriqueReclamation> getByReclamation(int idReclamation) throws SQLException {
        String sql = "SELECT * FROM historique_reclamation WHERE id_reclamation=? ORDER BY date_action ASC";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idReclamation);

        List<HistoriqueReclamation> liste = new ArrayList<>();
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(mapRow(rs));
        }
        return liste;
    }


    private HistoriqueReclamation mapRow(ResultSet rs) throws SQLException {
        HistoriqueReclamation h = new HistoriqueReclamation();

        h.setIdHistoriqueReclamation(rs.getInt("id_historique_reclamation"));
        h.setIdReclamation(rs.getInt("id_reclamation"));
        h.setIdAdmin(rs.getInt("id_admin"));

      
        String ancienStatut = rs.getString("ancien_statut");
        if (ancienStatut != null) {
            h.setAncienStatut(StatutReclamation.valueOf(ancienStatut));
        }

        h.setNouveauStatut(StatutReclamation.valueOf(rs.getString("nouveau_statut")));
        h.setCommentaire(rs.getString("commentaire"));
        h.setDateAction(rs.getTimestamp("date_action").toLocalDateTime());

        return h;
    }
}