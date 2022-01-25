import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import javax.mail.MessagingException;
import javax.swing.JOptionPane;

public class DataBase {
    public static ArrayList<User> usersDataBase = new ArrayList<>();
    public static ArrayList<Group> groupsDataBase = new ArrayList<>();

    public static boolean twoFactorAuthentication(String email) throws MessagingException {
        int random8DigitNumber = new Random().nextInt(99999999) + 100000000;

        new JavaMail().send("Two-Factor Authentication (2FA)", email, "Number: " + random8DigitNumber);

        int inputNumber = Integer.parseInt(JOptionPane.showInputDialog(null,
                "An 8-Digit Number Has Been Sent to Your Email Address (" + email
                        + ")\nPlease Enter The Number To Check Your Identity:",
                "Two-Factor Authentication (2FA)", JOptionPane.QUESTION_MESSAGE));

        if (inputNumber == random8DigitNumber) {
            JOptionPane.showMessageDialog(null, "Two-Factor Authentication Passed Successfully.",
                    "Two-Factor Authentication (2FA)", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        JOptionPane.showMessageDialog(null, "Incorrect Number\nTwo-Factor Authentication Failed.",
                "Two-Factor Authentication (2FA)", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public static User getUser(String username) {
        for (User searchUser : getUsersDataBase())
            if (username.equals(searchUser.getUsername()))
                return searchUser;
        return null;
    }

    public static boolean isTakenUsername(String username) {
        for (User searchUser : getUsersDataBase())
            if (username.equals(searchUser.getUsername()))
                return true;
        return false;
    }

    public static Group getGroup(int groupID) {
        for (Group group : getGroupsDataBase())
            if (group.getID() == groupID)
                return group;
        return null;

    }

    public static boolean isValidGroupID(int groupID) {
        return groupID > 0 && groupID < Group.creationID ? true : false;
    }

    public static ArrayList<User> getUsersDataBase() {
        return usersDataBase;
    }

    public static ArrayList<Group> getGroupsDataBase() {
        return groupsDataBase;
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder(new BigInteger(1, hash).toString(16));

        while (hexString.length() < 32)
            hexString.insert(0, '0');

        return hexString.toString();
    }
}
