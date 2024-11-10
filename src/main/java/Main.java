import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Skapar en instans av Bank
        Bank bank = new Bank();

        // Skapar och lägger till användare i banken
        User user1 = new User("1234", "1234", 1000.0);
        User user2 = new User("5678", "5678", 500.0);
        bank.addUser(user1);
        bank.addUser(user2);

        // Skapar en instans av ATM och associerar den med banken
        ATM atm = new ATM();
        atm.setBank(bank);

        // Skapar en scanner för att läsa användarinmatning
        Scanner scanner = new Scanner(System.in);
        boolean validInput;

        // Ber användaren att ange sitt ID för att sätta in kortet
        System.out.print("Ange användar-ID för att sätta in kortet: ");
        String userId = scanner.nextLine().trim();

        // Kontrollerar om kortet sätts in korrekt
        if (atm.insertCard(userId)) {
            System.out.println("Fortsätt med att ange PIN...");
            System.out.print("Ange PIN: ");
            String pin = scanner.nextLine().trim();

            // Kontrollerar om PIN är korrekt
            if (atm.enterPin(pin)) {
                System.out.println("Inloggning lyckades!");

                boolean running = true;
                while (running) {
                    // Visar meny för användaren
                    System.out.println("\nVälj ett alternativ:");
                    System.out.println("1. Kontrollera saldo");
                    System.out.println("2. Insättning");
                    System.out.println("3. Uttag");
                    System.out.println("4. Avsluta session");

                    int choice = -1;
                    validInput = false;
                    // Kontrollerar att inmatningen är ett giltigt nummer
                    while (!validInput) {
                        try {
                            System.out.print("Ange ditt val: ");
                            choice = scanner.nextInt();
                            validInput = true;
                        } catch (InputMismatchException e) {
                            // Hanterar ogiltig inmatning
                            System.out.println("Ogiltig inmatning. Ange ett nummer mellan 1 och 4.");
                            scanner.nextLine(); // Rensar bufferten för att undvika loop
                        }
                    }

                    // Hanterar användarens val
                    switch (choice) {
                        case 1:
                            // Kontrollera saldo
                            atm.checkBalance();
                            break;
                        case 2:
                            // Insättning av pengar
                            System.out.print("Ange belopp att sätta in: ");
                            double depositAmount = -1;
                            validInput = false;
                            while (!validInput) {
                                try {
                                    depositAmount = scanner.nextDouble();
                                    if (depositAmount > 0) {
                                        validInput = true;
                                        atm.deposit(depositAmount);
                                    } else {
                                        System.out.println("Beloppet måste vara positivt. Försök igen.");
                                    }
                                } catch (InputMismatchException e) {
                                    // Hanterar ogiltig inmatning
                                    System.out.println("Ogiltig inmatning. Ange ett numeriskt belopp.");
                                    scanner.nextLine(); // Rensar bufferten
                                }
                            }
                            break;
                        case 3:
                            // Uttag av pengar
                            System.out.print("Ange belopp att ta ut: ");
                            double withdrawAmount = -1;
                            validInput = false;
                            while (!validInput) {
                                try {
                                    withdrawAmount = scanner.nextDouble();
                                    if (withdrawAmount > 0) {
                                        validInput = true;
                                        boolean success = atm.withdraw(withdrawAmount);
                                        if (success) {
                                            System.out.println("Uttaget genomfördes.");
                                        } else {
                                            System.out.println("Uttaget misslyckades.");
                                        }
                                    } else {
                                        System.out.println("Beloppet måste vara positivt. Försök igen.");
                                    }
                                } catch (InputMismatchException e) {
                                    // Hanterar ogiltig inmatning
                                    System.out.println("Ogiltig inmatning. Ange ett numeriskt belopp.");
                                    scanner.nextLine(); // Rensar bufferten
                                }
                            }
                            break;
                        case 4:
                            // Avsluta session
                            atm.endSession();
                            running = false;
                            break;
                        default:
                            // Hanterar ogiltiga val
                            System.out.println("Ogiltigt val, försök igen.");
                            break;
                    }
                }
            } else {
                // Meddelar om PIN är felaktig
                System.out.println("Fel PIN. Kortet kan vara låst efter flera misslyckade försök.");
            }
        } else {
            // Meddelar om kortinsättningen misslyckas
            System.out.println("Kortinsättningen misslyckades. Kortet är kanske låst eller ogiltigt.");
        }

        // Stänger scannern
        scanner.close();
    }
}
