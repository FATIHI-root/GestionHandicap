package ma.ac.uir.gestionhandicap.dao;
import ma.ac.uir.gestionhandicap.config.DatabaseConnection;
import ma.ac.uir.gestionhandicap.model.Reclamation;
import ma.ac.uir.gestionhandicap.model.enums.PrioriteReclamation;
import ma.ac.uir.gestionhandicap.model.enums.StatutReclamation;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReclamationDAO {

    public boolean creer(Reclamation r) throws SQLException {
        String sql = "INSERT INTO reclamation (id_utilisateur, objet, description, priorite, statut_reclamation, date_creation, id_saison) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, r.getIdUtilisateur());
        ps.setString(2, r.getObjet());
        ps.setString(3, r.getDescription());
        ps.setString(4, r.getPriorite().name());
        ps.setString(5, r.getStatutReclamation().name());
        ps.setTimestamp(6, Timestamp.valueOf(r.getDateCreation()));
        if (r.getIdSaison() == null) {
            ps.setNull(7, Types.INTEGER);
        } else {
            ps.setInt(7, r.getIdSaison());
        }

        return ps.executeUpdate() > 0;
    }

    public boolean modifier(Reclamation r) throws SQLException {
        String sql = "UPDATE reclamation SET objet=?, description=?, priorite=?, date_modification=? " +
                     "WHERE id_reclamation=? AND id_utilisateur=?";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, r.getObjet());
        ps.setString(2, r.getDescription());
        ps.setString(3, r.getPriorite().name());
        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(5, r.getIdReclamation());
        ps.setInt(6, r.getIdUtilisateur());

        return ps.executeUpdate() > 0;
    }


    public boolean supprimer(int idReclamation, int idUtilisateur) throws SQLException {
        String sql = "DELETE FROM reclamation WHERE id_reclamation=? AND id_utilisateur=?";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, idReclamation);
        ps.setInt(2, idUtilisateur);

        return ps.executeUpdate() > 0;
    }


    public Reclamation getById(int idReclamation) throws SQLException {
        String sql = "SELECT * FROM reclamation WHERE id_reclamation=?";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idReclamation);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }


    public List<Reclamation> getByUtilisateur(int idUtilisateur) throws SQLException {
        String sql = "SELECT * FROM reclamation WHERE id_utilisateur=? ORDER BY date_creation DESC";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idUtilisateur);

        List<Reclamation> liste = new ArrayList<>();
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(mapRow(rs));
        }
        return liste;
    }


    public List<Reclamation> getAll() throws SQLException {
        String sql = "SELECT * FROM reclamation ORDER BY date_creation DESC";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        List<Reclamation> liste = new ArrayList<>();
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(mapRow(rs));
        }
        return liste;
    }

    public List<Reclamation> getBySaison(int idSaison) throws SQLException {
        String sql = "SELECT * FROM reclamation WHERE id_saison=? ORDER BY date_creation DESC";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idSaison);

        List<Reclamation> liste = new ArrayList<>();
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(mapRow(rs));
        }
        return liste;
    }


    private Reclamation mapRow(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setIdReclamation(rs.getInt("id_reclamation"));
        r.setIdUtilisateur(rs.getInt("id_utilisateur"));
        r.setObjet(rs.getString("objet"));
        r.setDescription(rs.getString("description"));
        r.setPriorite(PrioriteReclamation.valueOf(rs.getString("priorite")));
        r.setStatutReclamation(StatutReclamation.valueOf(rs.getString("statut_reclamation")));
        r.setCommentaireAdmin(rs.getString("commentaire_admin"));

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null)
            r.setDateCreation(dateCreation.toLocalDateTime());

        Timestamp dateModif = rs.getTimestamp("date_modification");
        if (dateModif != null)
            r.setDateModification(dateModif.toLocalDateTime());

        try {
            int idSaison = rs.getInt("id_saison");
            if (!rs.wasNull()) r.setIdSaison(idSaison);
        } catch (SQLException ignored) {
        }

        return r;
    }
}
