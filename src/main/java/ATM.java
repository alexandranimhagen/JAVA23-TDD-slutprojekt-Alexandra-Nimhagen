import java.util.InputMismatchException;
import java.util.Scanner;

public class ATM {
    private Bank bank;
    private User currentUser;

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public boolean insertCard(String userId) {
        System.out.println("Försöker sätta in kort för ID: " + userId);
        if (bank.isCardLocked(userId)) {
            System.out.println("Kortet är låst.");
            return false;
        }
        currentUser = bank.getUserById(userId);
        if (currentUser != null) {
            System.out.println("Kortet har satts in framgångsrikt.");
            return true;
        } else {
            System.out.println("Kortinsättningen misslyckades. Ogiltigt kort-ID.");
            return false;
        }
    }

    public boolean enterPin(String pin) {
        if (currentUser == null) {
            System.out.println("Ingen användare identifierad.");
            return false;
        }

        if (bank.verifyPin(currentUser.getId(), pin)) {
            currentUser.resetFailedAttempts();
            bank.updateUser(currentUser);
            System.out.println("Inloggning lyckades.");
            return true;
        } else {
            currentUser.incrementFailedAttempts();
            bank.updateUser(currentUser);

            if (currentUser.getFailedAttempts() >= 3) {
                currentUser.lockCard();
                bank.updateUser(currentUser);
                System.out.println("Kortet är nu låst efter tre misslyckade försök.");
            } else {
                System.out.println("Fel PIN. Försök igen.");
            }
            return false;
        }
    }

    public double checkBalance() {
        if (currentUser != null) {
            System.out.println("Ditt saldo är: " + currentUser.getBalance() + " kr");
            return currentUser.getBalance();
        }
        System.out.println("Ingen användare inloggad.");
        return 0.0;
    }

    public void deposit(double amount) {
        if (currentUser != null) {
            if (amount <= 0) {
                System.out.println("Insättningen misslyckades. Ange ett positivt belopp.");
                return;
            }
            currentUser.deposit(amount);
            bank.updateUser(currentUser);
            System.out.println("Insättning på " + amount + " kr lyckades.");
        } else {
            System.out.println("Ingen användare inloggad.");
        }
    }

    public boolean withdraw(double amount) {
        if (currentUser != null) {
            if (amount <= 0) {
                System.out.println("Uttaget misslyckades. Ange ett positivt belopp.");
                return false;
            }
            if (currentUser.getBalance() >= amount) {
                currentUser.withdraw(amount);
                bank.updateUser(currentUser);
                System.out.println("Uttag av " + amount + " kr lyckades.");
                return true;
            } else {
                System.out.println("Otillräckligt saldo.");
                return false;
            }
        }
        System.out.println("Ingen användare inloggad.");
        return false;
    }

    public void handleWithdrawal(double amount) {
        if (currentUser != null) {
            boolean success = currentUser.withdraw(amount);
            if (success) {
                System.out.println("Uttaget genomfördes framgångsrikt.");
                bank.updateUser(currentUser);
            } else {
                System.out.println("Uttaget misslyckades. Kontrollera ditt saldo eller försök igen med ett lägre belopp.");
            }
        } else {
            System.out.println("Ingen användare är inloggad.");
        }
    }

    public void endSession() {
        currentUser = null;
        System.out.println("Sessionen har avslutats.");
    }

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
                boolean running = true;
                while (running) {
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
                            System.out.println("Ogiltig inmatning. Ange ett giltigt nummer.");
                            scanner.nextLine(); // Rensar bufferten
                        }
                    }

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
                            endSession();
                            running = false;
                            break;
                        default:
                            System.out.println("Ogiltigt val. Försök igen.");
                    }
                }
            } else {
                System.out.println("Felaktig PIN. Försök igen senare.");
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        ATM atm = new ATM();
        Bank mockBank = new Bank();
        atm.setBank(mockBank);

        atm.startATM();
    }
}
