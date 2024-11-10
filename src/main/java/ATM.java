import java.util.InputMismatchException;
import java.util.Scanner;

public class ATM {
    private Bank bank; // Referens till banken som ATM interagerar med
    private User currentUser; // Den nuvarande inloggade användaren

    // Metod för att sätta bankreferens
    public void setBank(Bank bank) {
        this.bank = bank;
    }

    // Metod för att sätta in ett kort (identifiera användaren)
    public boolean insertCard(String userId) {
        System.out.println("Försöker sätta in kort för ID: " + userId);
        if (bank.isCardLocked(userId)) {
            // Om kortet är låst visas ett meddelande och metoden returnerar false
            System.out.println("Kortet är låst.");
            return false;
        }
        currentUser = bank.getUserById(userId);
        if (currentUser != null) {
            // Om användaren finns, sätts kortet in framgångsrikt
            System.out.println("Kortet har satts in framgångsrikt.");
            return true;
        } else {
            // Om användaren inte hittas visas ett felmeddelande
            System.out.println("Kortinsättningen misslyckades. Ogiltigt kort-ID.");
            return false;
        }
    }

    // Metod för att ange PIN-kod
    public boolean enterPin(String pin) {
        if (currentUser == null) {
            // Om ingen användare är inloggad returnerar metoden false
            return false;
        }

        if (bank.verifyPin(currentUser.getId(), pin)) {
            // Om PIN är korrekt, återställs antalet misslyckade försök
            currentUser.resetFailedAttempts();
            bank.updateUser(currentUser);
            return true; // Inloggning lyckades
        } else {
            // Om PIN är felaktig, ökas antalet misslyckade försök
            currentUser.incrementFailedAttempts();
            bank.updateUser(currentUser);

            if (currentUser.getFailedAttempts() >= 3) {
                // Lås kortet om det har varit tre misslyckade försök
                currentUser.lockCard();
                bank.updateUser(currentUser);
                return false; // Kortet är nu låst
            }
            return false; // Fel PIN
        }
    }

    // Metod för att kontrollera saldo
    public double checkBalance() {
        if (currentUser != null) {
            // Om en användare är inloggad visas saldot
            System.out.println("Ditt saldo är: " + currentUser.getBalance() + " kr");
            return currentUser.getBalance();
        }
        // Meddelande om ingen användare är inloggad
        System.out.println("Ingen användare inloggad.");
        return 0.0;
    }

    // Metod för insättning av pengar
    public void deposit(double amount) {
        if (currentUser != null) {
            if (amount <= 0) {
                // Kontroll för att säkerställa att beloppet är positivt
                System.out.println("Insättningen misslyckades. Ange ett positivt belopp.");
                return;
            }
            currentUser.deposit(amount);
            bank.updateUser(currentUser);
            System.out.println("Insättning på " + amount + " kr lyckades.");
        } else {
            // Meddelande om ingen användare är inloggad
            System.out.println("Ingen användare inloggad.");
        }
    }

    // Metod för uttag av pengar
    public boolean withdraw(double amount) {
        if (currentUser != null) {
            if (amount <= 0) {
                // Kontroll för att säkerställa att beloppet är positivt
                System.out.println("Uttaget misslyckades. Ange ett positivt belopp.");
                return false;
            }
            if (currentUser.getBalance() >= amount) {
                // Kontroll för att se om saldot räcker för uttaget
                currentUser.withdraw(amount);
                bank.updateUser(currentUser);
                System.out.println("Uttag av " + amount + " kr lyckades.");
                return true;
            } else {
                // Meddelande om saldot är otillräckligt
                System.out.println("Otillräckligt saldo.");
                return false;
            }
        }
        // Meddelande om ingen användare är inloggad
        System.out.println("Ingen användare inloggad.");
        return false;
    }

    // Metod för att hantera uttag
    public void handleWithdrawal(double amount) {
        if (currentUser != null) {
            boolean success = currentUser.withdraw(amount);
            if (success) {
                // Om uttaget är framgångsrikt
                System.out.println("Uttaget genomfördes framgångsrikt.");
                bank.updateUser(currentUser);
            } else {
                // Om uttaget misslyckas
                System.out.println("Uttaget misslyckades. Kontrollera ditt saldo eller försök igen med ett lägre belopp.");
            }
        } else {
            // Meddelande om ingen användare är inloggad
            System.out.println("Ingen användare är inloggad.");
        }
    }

    // Metod för att avsluta sessionen
    public void endSession() {
        currentUser = null;
        System.out.println("Sessionen har avslutats.");
    }

    // Metod för att starta ATM-sessionen och hantera användarinteraktioner
    public void startATM() {
        // Visar bankens namn vid start av ATM-sessionen
        System.out.println("Välkommen till " + Bank.getBankName() + "!");

        Scanner scanner = new Scanner(System.in);
        boolean validInput;

        System.out.print("Ange användar-ID för att sätta in kortet: ");
        String userId = scanner.nextLine().trim();

        if (insertCard(userId)) {
            System.out.println("Fortsätt med att ange PIN...");
            System.out.print("Ange PIN: ");
            String pin = scanner.nextLine().trim();

            if (enterPin(pin)) {
                System.out.println("Inloggning lyckades.");
                boolean running = true;
                while (running) {
                    // Visa alternativ för användaren
                    System.out.println("\nVälj ett alternativ:");
                    System.out.println("1. Kontrollera saldo");
                    System.out.println("2. Sätt in pengar");
                    System.out.println("3. Ta ut pengar");
                    System.out.println("4. Avsluta session");

                    int choice = -1;
                    validInput = false;
                    while (!validInput) {
                        try {
                            choice = scanner.nextInt();
                            validInput = true;
                        } catch (InputMismatchException e) {
                            // Hanterar felaktig inmatning
                            System.out.println("Ogiltig inmatning. Ange ett giltigt nummer.");
                            scanner.nextLine(); // Rensar bufferten
                        }
                    }

                    // Hanterar användarens val
                    switch (choice) {
                        case 1:
                            checkBalance();
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
                                    } else {
                                        System.out.println("Beloppet måste vara positivt. Försök igen.");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("Ogiltig inmatning. Ange ett numeriskt belopp.");
                                    scanner.nextLine(); // Rensar bufferten
                                }
                            }
                            deposit(depositAmount);
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
                                    } else {
                                        System.out.println("Beloppet måste vara positivt. Försök igen.");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("Ogiltig inmatning. Ange ett numeriskt belopp.");
                                    scanner.nextLine(); // Rensar bufferten
                                }
                            }
                            handleWithdrawal(withdrawAmount);
                            break;
                        case 4:
                            // Avslutar sessionen
                            endSession();
                            running = false;
                            break;
                        default:
                            System.out.println("Ogiltigt val. Försök igen.");
                    }
                }
            } else {
                // Hanterar fel PIN eller låst kort
                if (currentUser != null && currentUser.isLocked()) {
                    System.out.println("Kortet är nu låst efter tre misslyckade försök.");
                } else {
                    System.out.println("Fel PIN. Försök igen.");
                }
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        ATM atm = new ATM();
        Bank mockBank = new Bank();
        atm.setBank(mockBank);

        // Startar ATM-sessionen
        atm.startATM();
    }
}
