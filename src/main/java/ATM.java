import java.util.InputMismatchException;
import java.util.Scanner;

public class ATM {
    protected Bank bank; // Referens till banken som används
    protected User currentUser; // Håller reda på den inloggade användaren

    // Sätter vilken bank som denna ATM är kopplad till
    public void setBank(Bank bank) {
        this.bank = bank;
    }

    // Metod för att sätta in kort (logga in användare)
    public boolean insertCard(String userId) {
        currentUser = null; // Sätt alltid currentUser till null innan nytt kortförsök
        if (userId == null || userId.trim().isEmpty()) { // Kontrollera om ID är tomt
            System.out.println("Ogiltigt ID inmatat. Försök igen.");
            return false;
        }

        System.out.println("Försöker sätta in kort för ID: " + userId);
        if (bank.isCardLocked(userId)) { // Kontrollera om kortet är låst
            System.out.println("Kortet är låst. Kontakta banken för att låsa upp kortet.");
            return false;
        }

        currentUser = bank.getUserById(userId); // Hämta användaren från banken
        if (currentUser != null) {
            System.out.println("Kortet har satts in framgångsrikt.");
            return true;
        } else {
            System.out.println("Användare med ID " + userId + " hittades inte."); // Ingen användare hittades
            return false;
        }
    }

    // Visa vilken bank ATM är kopplad till
    public void displayBankName() {
        String bankName = Bank.getBankName();
        System.out.println("Denna bankomat är kopplad till: " + bankName);
    }

    // Metod för att hantera inmatning av PIN-kod
    public boolean handlePinEntry(Scanner scanner) {
        System.out.print("Ange PIN: ");
        int attemptsLeft = 3; // Max antal försök är 3

        while (attemptsLeft > 0) {
            if (!scanner.hasNextLine()) { // Kontrollera om det finns input
                System.out.println("Ingen inmatning upptäckt. Avbryter.");
                return false;
            }

            String pin = scanner.nextLine().trim(); // Läs in PIN

            if (pin.equals("0")) { // Användaren vill avbryta
                System.out.println("Avslutar PIN-inmatning.");
                currentUser = null;
                return false;
            }

            if (bank.verifyPin(currentUser.getId(), pin)) { // Kontrollera PIN
                System.out.println("PIN korrekt, inloggning lyckades.");
                currentUser.resetFailedAttempts(); // Återställ misslyckade försök
                return true; // PIN korrekt
            } else {
                // Hantera felaktig PIN
                attemptsLeft = handleFailedPinAttempt(attemptsLeft);
            }
        }

        // Returnera false istället för att stänga av programmet
        return false;
    }

    // Metod för att hantera misslyckade PIN-försök
    private int handleFailedPinAttempt(int attemptsLeft) {
        attemptsLeft--; // Minska antalet försök
        currentUser.incrementFailedAttempts(); // Öka misslyckade försök i User

        if (attemptsLeft > 0) {
            // Meddela användaren hur många försök som återstår
            System.out.printf("Fel PIN. Du har %d försök kvar.%n", attemptsLeft);
            System.out.print("Ange PIN: ");
        } else {
            // När försök är slut
            currentUser.lockCard(); // Lås kortet
            currentUser = null; // Avsluta sessionen genom att sätta currentUser till null
            System.out.println("Antal misslyckade försök: 3");
            System.out.println("Fel PIN, inloggningen misslyckades. Kortet är nu låst efter tre misslyckade försök.");
            System.out.println("Kontakta din bank för att låsa upp kortet.");
        }
        return attemptsLeft;
    }

    // Visa användarens saldo
    public void handleCheckBalance() {
        if (currentUser != null) {
            System.out.printf("Ditt saldo är: %s kr%n", formatAmount(currentUser.getBalance()));
        } else {
            System.out.println("Ingen användare inloggad.");
        }
    }

    // Hantera insättning av pengar
    protected boolean handleDeposit(Scanner scanner) {
        double amount = getValidAmount(scanner, "Ange belopp att sätta in: ", 20, 10);
        if (currentUser != null) {
            if (amount <= 0) {
                System.out.println("Insättningsbeloppet måste vara större än 0.");
                return false;
            }
            bank.deposit(currentUser.getId(), amount); // Gör insättning
            System.out.printf("Insättning av %s kr lyckades. Ny balans: %s kr.%n",
                    formatAmount(amount),
                    formatAmount(bank.getUserById(currentUser.getId()).getBalance()));
            return true;
        } else {
            System.out.println("Ingen användare inloggad.");
            return false;
        }
    }

    // Hantera uttag av pengar
    protected boolean handleWithdraw(Scanner scanner) {
        double amount = getValidAmount(scanner, "Ange belopp att ta ut: ", 100, 100);
        if (currentUser != null) {
            try {
                if (bank.withdraw(currentUser.getId(), amount)) { // Gör uttag
                    System.out.printf("Uttag av %s kr lyckades. Ny balans: %s kr.%n",
                            formatAmount(amount),
                            formatAmount(bank.getUserById(currentUser.getId()).getBalance()));
                    return true;
                } else {
                    System.out.println("Uttag misslyckades. Kontrollera ditt saldo.");
                    return false;
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage()); // Visa felmeddelande
                return false;
            }
        } else {
            System.out.println("Ingen användare inloggad.");
            return false;
        }
    }

    // Avsluta användarsessionen
    public void handleEndSession() {
        currentUser = null; // Nollställ inloggad användare
        System.out.println("Sessionen har avslutats.");
    }

    // Metod för att ta bort ett konto
    public boolean deleteAccount(String userId) {
        if (bank == null) {
            System.out.println("Ingen bank ansluten.");
            return false;
        }
        return bank.deleteUser(userId);
    }

    // Validera att belopp är korrekt inmatat
    protected double getValidAmount(Scanner scanner, String prompt, int minAmount, int step) {
        double amount = -1;
        while (true) {
            try {
                System.out.print(prompt);
                if (!scanner.hasNextDouble()) { // Kontrollera om nästa värde är en double
                    if (!scanner.hasNext()) { // Kontrollera om det finns mer inmatning alls
                        System.out.println("Ingen inmatning upptäckt. Avbryter.");
                        break; // Avsluta loopen om ingen inmatning finns
                    }
                    System.out.println("Ogiltig inmatning. Ange ett numeriskt belopp.");
                    scanner.nextLine(); // Rensa ogiltig inmatning
                    continue;
                }
                amount = scanner.nextDouble(); // Läs in beloppet
                if (amount < minAmount) {
                    System.out.printf("Ogiltigt belopp, Minsta möjliga belopp: %s kr. Försök igen.%n", formatAmount(minAmount));
                } else if (amount % step != 0) {
                    System.out.println("Beloppet måste vara en multipel av " + step + ". Försök igen.");
                } else {
                    break; // Avsluta loopen när beloppet är giltigt
                }
            } catch (InputMismatchException e) {
                System.out.println("Ogiltig inmatning. Ange ett numeriskt belopp.");
                scanner.nextLine(); // Rensa ogiltig inmatning
            }
        }
        return amount;
    }

    // Formatera belopp för snygg utskrift
    private String formatAmount(double amount) {
        if (amount == (int) amount) {
            return String.format("%d", (int) amount);
        } else {
            return String.format("%.2f", amount);
        }
    }
}
