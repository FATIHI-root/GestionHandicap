package ma.ac.uir.gestionhandicap.util;

public class ValidatorUtil {

    public static boolean isEmpty(String text) {
        if (text == null) {
            return true;
        }

        if (text.trim().equals("")) {
            return true;
        }

        return false;
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }

        if (email.contains("@") && email.contains(".")) {
            return true;
        }

        return false;
    }

    public static boolean isUirEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }

        if (email.endsWith("@uir.ac.ma")) {
            return true;
        }

        return false;
    }

    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) {
            return true;
        }

        if (phone.length() == 10 && phone.startsWith("0")) {
            return true;
        }

        return false;
    }

    public static boolean isValidPassword(String password) {
        if (isEmpty(password)) {
            return false;
        }

        if (password.length() >= 6) {
            return true;
        }

        return false;
    }

    public static boolean samePassword(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }

        if (password.equals(confirmPassword)) {
            return true;
        }

        return false;
    }

    public static boolean isValidName(String name) {
        if (isEmpty(name)) {
            return false;
        }

        if (name.trim().length() >= 2) {
            return true;
        }

        return false;
    }
}