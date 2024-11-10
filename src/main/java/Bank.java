import java.util.HashMap;
import java.util.Map;

public class Bank implements BankInterface {
    private final Map<String, User> users = new HashMap<>();

    public User getUserById(String id) {
        if (id == null || id.isEmpty()) {
            System.out.println("Ogiltigt ID inmatat.");
            return null;
        }
        return users.get(id);
    }

    public boolean isCardLocked(String userId) {
        User user = users.get(userId);
        if (user == null) {
            System.out.println("Användare med ID " + userId + " hittades inte.");
            return false;
        }
        return user.isLocked();
    }

    public static String getBankName() {
        return "MockBank";
    }

    public boolean verifyPin(String userId, String pin) {
        if (pin == null || pin.isEmpty()) {
            System.out.println("PIN är ogiltig.");
            return false;
        }
        User user = users.get(userId);
        if (user == null) {
            System.out.println("Användare med ID " + userId + " hittades inte.");
            return false;
        }
        return user.getPin().equals(pin);
    }

    public int getFailedAttempts(String userId) {
        User user = users.get(userId);
        if (user == null) {
            System.out.println("Användare med ID " + userId + " hittades inte. Returnerar 0 misslyckade försök.");
            return 0;
        }
        return user.getFailedAttempts();
    }

    public void addUser(User user) {
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            System.out.println("Ogiltig användare. Kan inte läggas till.");
            return;
        }
        if (!users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            System.out.println("Användare med ID " + user.getId() + " har lagts till.");
        } else {
            System.out.println("Användare med ID " + user.getId() + " finns redan.");
        }
    }

    public void updateUser(User user) {
        if (user == null || user.getId() == null || !users.containsKey(user.getId())) {
            System.out.println("Användare med ID " + (user != null ? user.getId() : "null") + " kan inte uppdateras.");
            return;
        }
        users.put(user.getId(), user);
        System.out.println("Användare med ID " + user.getId() + " har uppdaterats.");
    }
}
