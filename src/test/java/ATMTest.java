import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ATMTest {

    private ATM atm;
    private Bank mockBank;
    private Bank bank;
    private User user;

    // Initialiserar objekt före varje test
    @BeforeEach
    void setUp() {
        // Skapar en mock av Bank för enhetstester
        mockBank = Mockito.mock(Bank.class);
        atm = new ATM();
        atm.setBank(mockBank);

        // Skapar en faktisk Bank och User för integrationstester
        bank = new Bank();
        user = new User("12345", "1234", 1000);
        bank.addUser(user);
    }

    // Enhetstester för ATM-klassen
    @Nested
    @DisplayName("Enhetstester för ATM-klassen")
    class ATMUnitTests {

        // Testar att kort är låst vid insättning
        @Test
        @DisplayName("Verifierar att kort är låst vid insättning")
        void testInsertCardWhenCardIsLocked() {
            when(mockBank.isCardLocked("12345")).thenReturn(true);
            boolean result = atm.insertCard("12345");
            assertFalse(result, "Kortinsättningen bör misslyckas när kortet är låst");
            verify(mockBank).isCardLocked("12345");
        }

        // Testar saldo när ingen användare är inloggad
        @Test
        @DisplayName("Testar kontroll av saldo när ingen användare är inloggad")
        void testCheckBalanceWithoutUserLoggedIn() {
            double balance = atm.checkBalance();
            assertEquals(0.0, balance, "Saldot bör vara 0 när ingen användare är inloggad");
        }

        // Testar insättning av 0 eller negativt belopp
        @Test
        @DisplayName("Testar insättning av 0 eller negativt belopp")
        void testDepositZeroOrNegativeAmount() {
            User user = new User("12345", "1234", 1000);
            when(mockBank.getUserById("12345")).thenReturn(user);

            atm.insertCard("12345");
            atm.deposit(0);
            assertEquals(1000, user.getBalance(), "Saldot bör inte ändras vid insättning av 0");

            atm.deposit(-500);
            assertEquals(1000, user.getBalance(), "Saldot bör inte ändras vid insättning av negativt belopp");
        }

        // Testar uttag av belopp större än saldot
        @Test
        @DisplayName("Testar uttag av belopp större än saldot")
        void testWithdrawAmountGreaterThanBalance() {
            User user = new User("12345", "1234", 100);
            when(mockBank.getUserById("12345")).thenReturn(user);

            atm.insertCard("12345");
            boolean result = atm.withdraw(200);

            assertFalse(result, "Uttaget bör misslyckas när beloppet är större än saldot");
            assertEquals(100, user.getBalance(), "Saldot bör vara oförändrat efter misslyckat uttag");
        }

        // Testar PIN-inmatning utan kort
        @Test
        @DisplayName("Testar PIN-inmatning när ingen användare är inloggad")
        void testEnterPinWithoutCardInserted() {
            boolean loginResult = atm.enterPin("1234");
            assertFalse(loginResult, "Inloggning bör misslyckas när ingen användare är inloggad");
        }

        // Testar om getUserById returnerar null
        @Test
        @DisplayName("Testar null-returer från getUserById")
        void testGetUserByIdReturnsNull() {
            when(mockBank.getUserById("12345")).thenReturn(null);

            boolean cardInserted = atm.insertCard("12345");
            assertFalse(cardInserted, "Kortinsättningen bör misslyckas om getUserById returnerar null");
        }

        // Testar kortlåsning efter flera misslyckade PIN-försök
        @Test
        @DisplayName("Verifierar att kortet låses efter flera misslyckade PIN-försök")
        void testCardLockAfterMultipleFailedPinAttempts() {
            User user = new User("12345", "1234", 1000);
            when(mockBank.getUserById("12345")).thenReturn(user);
            when(mockBank.verifyPin("12345", "0000")).thenReturn(false);

            atm.insertCard("12345");
            atm.enterPin("0000");
            atm.enterPin("0000");
            atm.enterPin("0000");

            assertTrue(user.isLocked(), "Kortet bör vara låst efter tre misslyckade PIN-försök");
            verify(mockBank, times(3)).verifyPin("12345", "0000");
        }

        // Testar att avsluta session under interaktion
        @Test
        @DisplayName("Testar avslutning av session mitt i en interaktion")
        void testEndSessionDuringInteraction() {
            User user = new User("12345", "1234", 1000);
            when(mockBank.getUserById("12345")).thenReturn(user);

            atm.insertCard("12345");
            atm.endSession();

            double balance = atm.checkBalance();
            assertEquals(0.0, balance, "Saldot bör vara 0 efter avslutning av session");
        }

        // Testar flera transaktioner under samma session
        @Test
        @DisplayName("Testar flera transaktioner under samma session")
        void testMultipleTransactions() {
            User user = new User("12345", "1234", 1000);
            when(mockBank.getUserById("12345")).thenReturn(user);

            atm.insertCard("12345");
            atm.deposit(500);
            atm.withdraw(300);

            assertEquals(1200, user.getBalance(), "Saldot bör vara korrekt efter flera transaktioner");
            verify(mockBank, times(2)).updateUser(user);
        }

        // Testar inmatning av tom eller ogiltig PIN
        @Test
        @DisplayName("Testar inmatning av tom eller ogiltig PIN")
        void testEnterEmptyOrInvalidPin() {
            User user = new User("12345", "1234", 1000);
            when(mockBank.getUserById("12345")).thenReturn(user);

            atm.insertCard("12345");

            boolean emptyPinResult = atm.enterPin("");
            assertFalse(emptyPinResult, "Inmatning av tom PIN bör misslyckas");

            boolean invalidPinResult = atm.enterPin("!@#$");
            assertFalse(invalidPinResult, "Inmatning av ogiltiga tecken som PIN bör misslyckas");
        }

        // Testar att bankens namn returneras korrekt
        @Test
        @DisplayName("Verifierar att getBankName() returnerar korrekt namn")
        void testGetBankName() {
            String bankName = Bank.getBankName();
            assertEquals("MockBank", bankName, "Banknamnet bör vara 'MockBank'");
        }
    }

    // Integrationstester för ATM-flöde
    @Nested
    @DisplayName("Integrationstester för ATM-flöde")
    class ATMIntegrationTests {

        @Test
        @DisplayName("Integrationstest för ATM-flöde")
        void testATMFlow() {
            ATM atmIntegration = new ATM();
            atmIntegration.setBank(bank);

            assertTrue(atmIntegration.insertCard("12345"), "Kortinsättningen bör lyckas");
            assertTrue(atmIntegration.enterPin("1234"), "Inloggningen bör lyckas med korrekt PIN");
            assertEquals(1000.0, atmIntegration.checkBalance(), "Saldot bör vara korrekt");

            atmIntegration.deposit(500);
            assertEquals(1500.0, atmIntegration.checkBalance(), "Saldot bör uppdateras efter insättning");
            assertTrue(atmIntegration.withdraw(200), "Uttaget bör vara lyckat");
            assertEquals(1300.0, atmIntegration.checkBalance(), "Saldot bör uppdateras efter uttag");

            atmIntegration.endSession();
            assertEquals(0.0, atmIntegration.checkBalance(), "Saldot bör vara 0 efter avslutad session");
        }
    }

    // Tester för Bank-klassen
    @Nested
    @DisplayName("Tester för Bank-klassen")
    class BankTests {

        @Test
        @DisplayName("Testar att hämta en användare")
        void testGetUserById() {
            User foundUser = bank.getUserById("12345");
            assertNotNull(foundUser, "Användaren bör hittas");
            assertEquals("12345", foundUser.getId(), "Användar-ID bör stämma");

            assertNull(bank.getUserById("99999"), "Ej existerande användare bör returnera null");
        }

        @Test
        @DisplayName("Testar verifiering av PIN")
        void testVerifyPin() {
            assertTrue(bank.verifyPin("12345", "1234"), "PIN bör vara korrekt");
            assertFalse(bank.verifyPin("12345", "0000"), "Felaktig PIN bör inte accepteras");

            assertFalse(bank.verifyPin("99999", "1234"), "PIN-verifiering för ej existerande användare bör returnera false");
        }

        @Test
        @DisplayName("Testar misslyckade försök")
        void testGetFailedAttempts() {
            user.incrementFailedAttempts();
            assertEquals(1, bank.getFailedAttempts("12345"), "Antal misslyckade försök bör vara 1");

            assertEquals(0, bank.getFailedAttempts("99999"), "Misslyckade försök för ej existerande användare bör vara 0");
        }

        @Test
        @DisplayName("Testar uppdatering av användare")
        void testUpdateUser() {
            user.deposit(500);
            bank.updateUser(user);
            assertEquals(1500, bank.getUserById("12345").getBalance(), "Balansen bör uppdateras korrekt");

            User nonExistentUser = new User("99999", "0000", 500);
            bank.updateUser(nonExistentUser);
            assertNull(bank.getUserById("99999"), "Ej existerande användare bör inte läggas till vid uppdatering");
        }

        @Test
        @DisplayName("Testar låsning av kort efter tre misslyckade försök")
        void testLockCardAfterThreeFailedAttempts() {
            user.incrementFailedAttempts();
            user.incrementFailedAttempts();
            user.incrementFailedAttempts();
            bank.updateUser(user);
            assertTrue(bank.getUserById("12345").isLocked(), "Kortet bör vara låst efter tre misslyckade försök");
        }

        @Test
        @DisplayName("Testar om kortet är låst")
        void testIsCardLocked() {
            assertFalse(bank.isCardLocked("12345"), "Kortet bör inte vara låst initialt");

            user.lockCard();
            bank.updateUser(user);
            assertTrue(bank.isCardLocked("12345"), "Kortet bör vara låst efter att användaren låsts");

            assertFalse(bank.isCardLocked("99999"), "Kortstatus för ej existerande användare bör returnera false");
        }
    }
}
