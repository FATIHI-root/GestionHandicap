package ma.ac.uir.gestionhandicap.util;

import ma.ac.uir.gestionhandicap.model.Utilisateur;

public class SessionManager {

    private static Utilisateur currentUser;

    public static void setCurrentUser(Utilisateur u) {
        currentUser = u;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }
}
