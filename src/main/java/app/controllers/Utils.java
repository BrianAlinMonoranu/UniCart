package app.controllers;

import java.time.LocalDate;

import app.db.User;

public class Utils {
    public static class Session {
        User currentUser;
    }

    public static User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    public static boolean passwordValidation(String password) {
        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}";
        return password.matches(pattern);
    }

    public static boolean checkCardDetails(String name, Long number, int CVV, int expiry_m, int expiry_y) {
        // Check Card expiry date
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = LocalDate.of(expiry_y, expiry_m + 1, 1);

        // Check size of input values
        return (number.toString().length() == 16) && (Integer.toString(CVV).length() == 3)
                && today.isBefore(expiryDate);
    }

    public static User logoutUser() {
        return new User();
    }

}
