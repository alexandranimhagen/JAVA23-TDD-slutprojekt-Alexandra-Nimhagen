import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();

        // Skapa och lägg till användare
        User user1 = new User("1234", "1234", 1000.0);
        User user2 = new User("5678", "5678", 500.0);
        bank.addUser(user1);
        bank.addUser(user2);

        // Skapa ATM-instans och associera med banken
        ATM atm = new ATM();
        atm.setBank(bank);

        Scanner scanner = new Scanner(System.in);
        boolean validInput;

        System.out.print("Ange användar-ID för att sätta in kortet: ");
        String userId = scanner.nextLine().trim();

        if (atm.insertCard(userId)) {
            System.out.println("Fortsätt med att ange PIN...");
            System.out.print("Ange PIN: ");
            String pin = scanner.nextLine().trim();

            if (atm.enterPin(pin)) {
                System.out.println("Inloggning lyckades!");

                boolean running = true;
                while (running) {
                    System.out.println("\nVälj ett alternativ:");
                    System.out.println("1. Kontrollera saldo");
                    System.out.println("2. Insättning");
                    System.out.println("3. Uttag");
                    System.out.println("4. Avsluta session");

                    int choice = -1;
                    validInput = false;
                    while (!validInput) {
                        try {
                            System.out.print("Ange ditt val: ");
                            choice = scanner.nextInt();
                            validInput = true;
                        } catch (InputMismatchException e) {
                            System.out.println("Ogiltig inmatning. Ange ett nummer mellan 1 och 4.");
                            scanner.nextLine(); // Rensar bufferten för ogiltig inmatning
                        }
                    }

                    switch (choice) {
                        case 1:
                            atm.checkBalance();
                            break;
                        case 2:
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
                                    System.out.println("Ogiltig inmatning. Ange ett numeriskt belopp.");
                                    scanner.nextLine(); // Rensar bufferten
                                }
                            }
                            break;
                        case 3:
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
                                    System.out.println("Ogiltig inmatning. Ange ett numeriskt belopp.");
                                    scanner.nextLine(); // Rensar bufferten
                                }
                            }
                            break;
                        case 4:
                            atm.endSession();
                            running = false;
                            break;
                        default:
                            System.out.println("Ogiltigt val, försök igen.");
                            break;
                    }
                }
            } else {
                System.out.println("Fel PIN. Kortet kan vara låst efter flera misslyckade försök.");
            }
        } else {
            System.out.println("Kortinsättningen misslyckades. Kortet är kanske låst eller ogiltigt.");
        }

        scanner.close();
    }
}
