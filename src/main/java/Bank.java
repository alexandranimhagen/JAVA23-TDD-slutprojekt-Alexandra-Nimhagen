import java.util.HashMap;
import java.util.Map;

public class Bank implements BankInterface {
    // Karta för att lagra användare med deras ID som nyckel
    private final Map<String, User> users = new HashMap<>();

    // Metod för att hämta en användare baserat på ID
    public User getUserById(String id) {
        if (id == null || id.isEmpty()) {
            // Kontroll för ogiltigt ID och utskrift av felmeddelande
            System.out.println("Ogiltigt ID inmatat.");
            return null;
        }
        return users.get(id); // Returnerar användaren om den finns
    }

    // Metod för att kontrollera om ett kort är låst
    public boolean isCardLocked(String userId) {
        User user = users.get(userId);
        if (user == null) {
            // Utskrift om användaren inte hittas
            System.out.println("Användare med ID " + userId + " hittades inte.");
            return false;
        }
        return user.isLocked(); // Returnerar kortets låsstatus
    }

    // Statisk metod för att hämta bankens namn
    public static String getBankName() {
        return "MockBank";
    }

    // Metod för att verifiera en PIN-kod för en given användare
    public boolean verifyPin(String userId, String pin) {
        if (pin == null || pin.isEmpty()) {
            // Kontroll för ogiltig PIN och utskrift av felmeddelande
            System.out.println("PIN är ogiltig.");
            return false;
        }
        User user = users.get(userId);
        if (user == null) {
            // Utskrift om användaren inte hittas
            System.out.println("Användare med ID " + userId + " hittades inte.");
            return false;
        }
        return user.getPin().equals(pin); // Returnerar om PIN är korrekt
    }

    // Metod för att hämta antalet misslyckade försök för en användare
    public int getFailedAttempts(String userId) {
        User user = users.get(userId);
        if (user == null) {
            // Utskrift om användaren inte hittas och returnerar 0 som standardvärde
            System.out.println("Användare med ID " + userId + " hittades inte. Returnerar 0 misslyckade försök.");
            return 0;
        }
        return user.getFailedAttempts(); // Returnerar antalet misslyckade försök
    }

    // Metod för att lägga till en ny användare
    public void addUser(User user) {
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            // Kontroll för ogiltig användare och utskrift av felmeddelande
            System.out.println("Ogiltig användare. Kan inte läggas till.");
            return;
        }
        if (!users.containsKey(user.getId())) {
            // Lägger till användaren om den inte redan finns
            users.put(user.getId(), user);
            System.out.println("Användare med ID " + user.getId() + " har lagts till.");
        } else {
            // Utskrift om användaren redan finns
            System.out.println("Användare med ID " + user.getId() + " finns redan.");
        }
    }

    // Metod för att uppdatera en användare
    public void updateUser(User user) {
        if (user == null || user.getId() == null || !users.containsKey(user.getId())) {
            // Kontroll för ogiltig eller icke-existerande användare och utskrift av felmeddelande
            System.out.println("Användare med ID " + (user != null ? user.getId() : "null") + " kan inte uppdateras.");
            return;
        }
        // Uppdaterar användaren i kartan
        users.put(user.getId(), user);
        System.out.println("Användare med ID " + user.getId() + " har uppdaterats.");
    }
}
