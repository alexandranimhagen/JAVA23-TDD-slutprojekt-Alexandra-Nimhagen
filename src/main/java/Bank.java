import java.util.HashMap;
import java.util.Map;

public class Bank implements BankInterface {
    private final Map<String, User> users = new HashMap<>(); // Lagrar användare med ID som nyckel

    public Bank() {
        // Skapar en standardanvändare vid initiering
        users.put("12345", new User("12345", "1234", 1000.0));
    }

    // Hämtar en användare baserat på ID
    public User getUserById(String id) {
        if (id == null || id.isEmpty()) {
            return null; // Returnerar null om ID är tomt eller null
        }
        return users.get(id); // Returnerar användaren om den finns
    }

    // Kollar om kortet är låst för en viss användare
    public boolean isCardLocked(String userId) {
        User user = users.get(userId);
        return user != null && user.isLocked(); // Returnerar true om användaren finns och kortet är låst
    }

    // Returnerar bankens namn
    public static String getBankName() {
        return "MockBank";
    }

    // Verifierar om PIN-koden är korrekt för en användare
    public boolean verifyPin(String userId, String pin) {
        User user = users.get(userId);
        return user != null && user.getPin().equals(pin); // Jämför PIN om användaren finns
    }

    // Lägger till en ny användare
    public void addUser(User user) {
        // Kollar om användaren eller dess ID är ogiltigt
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            System.out.println("Ogiltig användare. Kan inte läggas till.");
            return;
        }

        // Lägger till användaren om ID inte redan finns
        if (!users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            System.out.println("Användare med ID " + user.getId() + " har lagts till.");
        } else {
            System.out.println("Användare med ID " + user.getId() + " finns redan."); // Meddelar om användaren redan finns
        }
    }

    // Tar bort en användare baserat på ID
    public boolean deleteUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            System.out.println("Ogiltigt användar-ID.");
            return false;
        }
        User removedUser = users.remove(userId);
        if (removedUser != null) {
            System.out.println("Användare med ID " + userId + " har tagits bort.");
            return true;
        } else {
            System.out.println("Ingen användare hittades med ID " + userId + ".");
            return false;
        }
    }

    // Hanterar insättning av pengar för en användare
    public void deposit(String userId, double amount) {
        User user = users.get(userId);
        if (user != null && amount > 0) {
            user.setBalance(user.getBalance() + amount); // Uppdaterar saldot direkt i användarobjektet
        } else {
            throw new IllegalArgumentException("Ogiltigt användar-ID eller belopp."); // Hanterar felaktiga indata
        }
    }

    // Hanterar uttag av pengar för en användare
    public boolean withdraw(String userId, double amount) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Ogiltigt användar-ID."); // Hanterar ogiltigt användar-ID
        }
        if (amount > 0 && user.getBalance() >= amount) {
            user.setBalance(user.getBalance() - amount); // Minskar saldot vid ett lyckat uttag
            return true;
        } else if (amount <= 0) {
            throw new IllegalArgumentException("Beloppet måste vara större än 0."); // Hanterar negativa belopp
        } else {
            throw new IllegalArgumentException("Otillräckligt saldo."); // Meddelar om saldo inte räcker
        }
    }
}
