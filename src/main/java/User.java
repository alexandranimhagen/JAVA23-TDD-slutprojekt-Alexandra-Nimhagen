public class User {
    // Fält för användarens ID, PIN, saldo, antal misslyckade försök och låsstatus
    private String id;
    private String pin;
    private double balance;
    private int failedAttempts;
    private boolean isLocked;

    // Konstruktor för att skapa en ny användare
    public User(String id, String pin, double balance) {
        if (id == null || id.isEmpty() || pin == null || pin.isEmpty() || balance < 0) {
            throw new IllegalArgumentException("Ogiltiga värden vid skapande av användare.");
        }
        this.id = id;
        this.pin = pin;
        this.balance = balance;
        this.failedAttempts = 0; // Initierar antal misslyckade försök till 0
        this.isLocked = false;   // Sätter kortstatus till olåst
    }

    // Getter för användarens ID
    public String getId() {
        return id;
    }

    // Getter för användarens PIN
    public String getPin() {
        return pin;
    }

    // Getter för användarens saldo
    public double getBalance() {
        return balance;
    }

    // Setter för användarens saldo
    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Saldo kan inte vara negativt.");
        }
        this.balance = balance;
    }

    // Getter för antal misslyckade försök
    public int getFailedAttempts() {
        return failedAttempts;
    }

    // Kontrollerar om kortet är låst
    public boolean isLocked() {
        return isLocked;
    }

    // Låser kortet
    public void lockCard() {
        this.isLocked = true;
    }

    // Ökar antal misslyckade försök och låser kortet om det är tre eller fler försök
    public void incrementFailedAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 3 && !this.isLocked) {
            lockCard(); // Låser kortet vid tre misslyckade försök
        }
    }

    // Återställer misslyckade försök till 0
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }
}
