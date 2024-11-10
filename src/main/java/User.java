public class User {
    // Deklaration av fält för användarens ID, PIN, saldo, antal misslyckade försök och om kortet är låst
    private String id;
    private String pin;
    private double balance;
    private int failedAttempts;
    private boolean isLocked;

    // Konstruktor för att skapa en ny användare
    public User(String id, String pin, double balance) {
        // Kontroll för ogiltiga inmatningar vid skapande av användare
        if (id == null || id.isEmpty() || pin == null || pin.isEmpty() || balance < 0) {
            throw new IllegalArgumentException("Ogiltiga värden vid skapande av användare.");
        }
        // Tilldelar värden till fält
        this.id = id;
        this.pin = pin;
        this.balance = balance;
        this.failedAttempts = 0; // Sätter antalet misslyckade försök till 0
        this.isLocked = false;   // Kortet är initialt inte låst
    }

    // Getter för användarens ID
    public String getId() { return id; }

    // Getter för användarens PIN
    public String getPin() { return pin; }

    // Getter för användarens saldo
    public double getBalance() { return balance; }

    // Getter för antalet misslyckade försök
    public int getFailedAttempts() { return failedAttempts; }

    // Getter för att kontrollera om kortet är låst
    public boolean isLocked() { return isLocked; }

    // Metod för att låsa kortet
    public void lockCard() {
        this.isLocked = true; // Sätter kortet till låst
        System.out.println("Kortet är nu låst.");
    }

    // Metod för att öka antalet misslyckade försök
    public void incrementFailedAttempts() {
        this.failedAttempts++; // Ökar antalet misslyckade försök med 1
        // Låser kortet om det är minst tre misslyckade försök och kortet inte redan är låst
        if (this.failedAttempts >= 3 && !this.isLocked) {
            lockCard();
        }
    }

    // Metod för att återställa antalet misslyckade försök
    public void resetFailedAttempts() {
        this.failedAttempts = 0; // Sätter antalet misslyckade försök till 0
    }

    // Metod för att sätta in pengar på användarens konto
    public void deposit(double amount) {
        if (amount > 0) {
            // Ökar saldot med det insatta beloppet
            this.balance += amount;
            System.out.println("Insättning av " + amount + " kr lyckades. Ny balans: " + this.balance + " kr.");
        } else {
            // Meddelar att beloppet måste vara positivt
            System.out.println("Beloppet måste vara större än noll.");
        }
    }

    // Metod för att ta ut pengar från användarens konto
    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            // Minskar saldot med det uttagna beloppet om saldot är tillräckligt
            this.balance -= amount;
            System.out.println("Uttag av " + amount + " kr lyckades. Ny balans: " + this.balance + " kr.");
            return true; // Returnerar true om uttaget lyckades
        } else if (amount <= 0) {
            // Meddelar om beloppet är ogiltigt
            System.out.println("Beloppet måste vara större än noll.");
            return false; // Returnerar false om beloppet är noll eller negativt
        } else {
            // Meddelar om saldot är otillräckligt
            System.out.println("Otillräckligt saldo.");
            return false; // Returnerar false om saldot inte är tillräckligt
        }
    }
}
