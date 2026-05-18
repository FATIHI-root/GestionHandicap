package ma.ac.uir.gestionhandicap.config;

import ma.ac.uir.gestionhandicap.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final String CONFIG_FILE = "application.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new DatabaseException("Fichier " + CONFIG_FILE + " introuvable dans le classpath");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new DatabaseException("Erreur de lecture de " + CONFIG_FILE, e);
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
