public class User {
    private String id;
    private String pin;
    private double balance;
    private int failedAttempts;
    private boolean isLocked;

    public User(String id, String pin, double balance) {
        if (id == null || id.isEmpty() || pin == null || pin.isEmpty() || balance < 0) {
            throw new IllegalArgumentException("Ogiltiga värden vid skapande av användare.");
        }
        this.id = id;
        this.pin = pin;
        this.balance = balance;
        this.failedAttempts = 0;
        this.isLocked = false;
    }

    public String getId() { return id; }
    public String getPin() { return pin; }
    public double getBalance() { return balance; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return isLocked; }

    public void lockCard() {
        this.isLocked = true;
        System.out.println("Kortet är nu låst.");
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 3 && !this.isLocked) {
            lockCard();
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            System.out.println("Insättning av " + amount + " kr lyckades. Ny balans: " + this.balance + " kr.");
        } else {
            System.out.println("Beloppet måste vara större än noll.");
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            System.out.println("Uttag av " + amount + " kr lyckades. Ny balans: " + this.balance + " kr.");
            return true;
        } else if (amount <= 0) {
            System.out.println("Beloppet måste vara större än noll.");
            return false;
        } else {
            System.out.println("Otillräckligt saldo.");
            return false;
        }
    }
}
