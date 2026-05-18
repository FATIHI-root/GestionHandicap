package ma.ac.uir.gestionhandicap.config;

import ma.ac.uir.gestionhandicap.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static Connection connection;

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String driver = AppConfig.get("db.driver");
                String url = AppConfig.get("db.url");
                String user = AppConfig.get("db.user");
                String password = AppConfig.get("db.password", "");

                Class.forName(driver);
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Connexion MySQL établie : " + url);
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("Driver MySQL introuvable", e);
        } catch (SQLException e) {
            throw new DatabaseException("Échec de connexion à la base de données", e);
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Connexion MySQL fermée.");
                }
            } catch (SQLException e) {
                throw new DatabaseException("Erreur lors de la fermeture de la connexion", e);
            } finally {
                connection = null;
            }
        }
    }
}
