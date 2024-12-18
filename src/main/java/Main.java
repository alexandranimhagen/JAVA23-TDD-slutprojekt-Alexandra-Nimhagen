import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        ATM atm = new ATM();
        atm.setBank(bank);

        Scanner scanner = new Scanner(System.in);

        // Visa välkomstmeddelande och bankens namn
        System.out.println("Välkommen till Bankomaten! Denna bankomat är kopplad till: " + Bank.getBankName());

        // Huvudloopen för programmet
        while (true) {
            // Be användaren att ange sitt ID eller avsluta programmet
            System.out.print("Ange användar-ID för att sätta in kortet. För att avsluta programmet skriv '0': ");
            String userId = scanner.nextLine().trim();

            // Avsluta programmet om användaren väljer '0'
            if (userId.equals("0")) {
                System.out.println("Programmet avslutas. Tack för att du använde tjänsten!");
                break;
            }

            // Försök sätta in kortet
            if (atm.insertCard(userId)) {
                // Hantera PIN-inmatning
                if (atm.handlePinEntry(scanner)) {
                    processUserChoices(scanner, atm); // Starta session
                } else {
                    System.out.println("Inloggningen misslyckades. Försök igen.\n"); // Lägg till radbrytning här
                }
            } else {
                System.out.println("Kortet kunde inte sättas in. Försök igen.\n"); // Lägg till radbrytning här
            }
        }
    }

    // Metod för att hantera användarens val i sessionen
    private static void processUserChoices(Scanner scanner, ATM atm) {
        boolean sessionRunning = true;

        while (sessionRunning) {
            // Visa meny för användaren
            System.out.println("\nVälj ett alternativ:");
            System.out.println("0. Avsluta programmet");
            System.out.println("1. Kontrollera saldo");
            System.out.println("2. Insättning");
            System.out.println("3. Uttag");
            System.out.println("4. Logga ut och avsluta session");

            int choice = -1;

            try {
                // Läs användarens val
                System.out.print("Ange ditt val: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Rensa bufferten
            } catch (InputMismatchException e) {
                System.out.println("Ogiltig inmatning. Ange ett nummer mellan 0 och 4.");
                scanner.nextLine(); // Rensa bufferten
                continue;
            }

            // Hantera användarens val
            switch (choice) {
                case 0 -> {
                    // Avsluta programmet
                    System.out.println("Programmet avslutas. Tack för att du använde tjänsten!");
                    System.exit(0);
                }
                case 1 -> atm.handleCheckBalance(); // Visa saldo
                case 2 -> atm.handleDeposit(scanner); // Hantera insättning
                case 3 -> atm.handleWithdraw(scanner); // Hantera uttag
                case 4 -> {
                    atm.handleEndSession();
                    System.out.println("Session avslutad. Du återgår nu till huvudmenyn.");
                    sessionRunning = false;
                }

                default -> System.out.println("Ogiltigt val. Försök igen."); // Hantera felaktig inmatning
            }
        }
    }
}
