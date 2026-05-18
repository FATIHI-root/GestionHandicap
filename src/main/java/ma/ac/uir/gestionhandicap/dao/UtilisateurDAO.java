package ma.ac.uir.gestionhandicap.dao;

import ma.ac.uir.gestionhandicap.config.DatabaseConnection;
import ma.ac.uir.gestionhandicap.exception.DatabaseException;
import ma.ac.uir.gestionhandicap.model.Administrateur;
import ma.ac.uir.gestionhandicap.model.PersonneH;
import ma.ac.uir.gestionhandicap.model.Utilisateur;
import ma.ac.uir.gestionhandicap.model.enums.Role;
import ma.ac.uir.gestionhandicap.model.enums.StatutCompte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    public Utilisateur findByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildUtilisateur(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche par email", e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la vérification de l'email", e);
        }
    }

    public int insert(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, role, statut_compte) "  + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getTelephone());
            ps.setString(6, u.getRole().name());
            ps.setString(7, u.getStatutCompte().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                u.setIdUtilisateur(id);
                return id;
            }
            return -1;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de l'insertion de l'utilisateur", e);
        }
    }

    public List<Utilisateur> findByStatut(StatutCompte statut) {
        List<Utilisateur> result = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE statut_compte = ? ORDER BY date_inscription DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, statut.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(buildUtilisateur(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche par statut", e);
        }
    }

    public void updateStatut(int id, StatutCompte statut) {
        updateStatut(id, statut, null);
    }

    public void updateStatut(int id, StatutCompte statut, String motifRefus) {
        String sql = "UPDATE utilisateur SET statut_compte = ? WHERE id_utilisateur = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, statut.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la mise à jour du statut", e);
        }

        if (motifRefus != null) {
            String sql2 = "UPDATE utilisateur SET motif_refus = ? WHERE id_utilisateur = ?";
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql2);
                ps.setString(1, motifRefus);
                ps.setInt(2, id);
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
    }

    public Utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildUtilisateur(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la recherche par id", e);
        }
    }

    public void updateProfile(int id, String nom, String prenom, String telephone) {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, telephone = ? WHERE id_utilisateur = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, telephone);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la mise à jour du profil", e);
        }
    }

    public void updatePassword(int id, String hash) {
        String sql = "UPDATE utilisateur SET mot_de_passe = ? WHERE id_utilisateur = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, hash);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la mise à jour du mot de passe", e);
        }
    }

    public int countByStatut(StatutCompte statut) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE statut_compte = ?";
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
            throw new DatabaseException("Erreur lors du comptage", e);
        }
    }

    private Utilisateur buildUtilisateur(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_utilisateur");
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        String email = rs.getString("email");
        String mdp = rs.getString("mot_de_passe");
        String tel = rs.getString("telephone");
        Role role = Role.valueOf(rs.getString("role"));
        StatutCompte statut = StatutCompte.valueOf(rs.getString("statut_compte"));

        java.sql.Timestamp ts = rs.getTimestamp("date_inscription");
        java.time.LocalDateTime date = ts.toLocalDateTime();

        String motif = null;
        try {
            motif = rs.getString("motif_refus");
        } catch (SQLException e) {
            motif = null;
        }

        Utilisateur u;
        if (role == Role.ADMIN) {
            u = new Administrateur(id, nom, prenom, email, mdp, tel, statut, date);
        } else {
            u = new PersonneH(id, nom, prenom, email, mdp, tel, statut, date);
        }
        u.setMotifRefus(motif);
        return u;
    }
}
