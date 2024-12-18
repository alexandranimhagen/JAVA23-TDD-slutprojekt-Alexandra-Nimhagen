import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Scanner;

public class ATMTest {

    private ATM atm; // Objektet av ATM-klassen som vi testar
    private Bank mockBank; // En mockad version av Bank för att simulera bankens funktionalitet
    private User user; // En dummy-användare för tester

    @BeforeEach
    void setUp() {
        // Skapa mock-objekt för Bank och initialisera ATM
        mockBank = Mockito.mock(Bank.class);
        atm = new ATM();
        atm.setBank(mockBank); // Koppla mock-banken till ATM

        // Skapa en dummy-användare
        user = new User("12345", "1234", 1000);
    }

    @Nested
    @DisplayName("Enhetstester för ATM-klassen")
    class ATMUnitTests {

        @Test
        @DisplayName("Testar PIN-flöde med korrekt PIN")
        void testHandlePinEntryCorrectPin() {
            // Mocka beteendet för att returnera användaren och verifiera PIN
            when(mockBank.getUserById("12345")).thenReturn(user);
            when(mockBank.verifyPin("12345", "1234")).thenReturn(true);

            atm.insertCard("12345"); // Sätt in kortet

            // Simulera att användaren anger rätt PIN
            Scanner scanner = new Scanner("1234\n");
            boolean result = atm.handlePinEntry(scanner);

            // Kontrollera att PIN-flödet lyckas och att misslyckade försök återställs
            assertTrue(result, "PIN-flödet bör lyckas med korrekt PIN.");
            assertEquals(0, user.getFailedAttempts(), "Antalet misslyckade försök bör återställas.");
            verify(mockBank).verifyPin("12345", "1234"); // Verifiera att PIN-kontroll anropades
        }

        @Test
        @DisplayName("Testar PIN-flöde med tre misslyckade försök")
        void testHandlePinEntryLockCardAfterThreeAttempts() {
            // Mocka för att returnera användaren
            when(mockBank.getUserById("12345")).thenReturn(user);

            atm.insertCard("12345"); // Sätt in kortet

            // Simulera att användaren anger fel PIN tre gånger
            Scanner scanner = new Scanner("1111\n1111\n1111\n");
            boolean result = atm.handlePinEntry(scanner);

            // Kontrollera att flödet misslyckas och kortet låses efter tre försök
            assertFalse(result, "PIN-flödet bör misslyckas efter tre försök.");
            assertTrue(user.isLocked(), "Kortet bör låsas efter tre misslyckade försök.");
            assertNull(atm.currentUser, "Användarsessionen bör avslutas efter kortlåsning.");
            assertEquals(3, user.getFailedAttempts(), "Antalet misslyckade försök bör vara tre.");
        }

        @Test
        @DisplayName("Verifierar att insättningsmetoden i banken anropas korrekt")
        void testDepositCallsBankMethod() {
            // Mocka för att returnera användaren
            when(mockBank.getUserById("12345")).thenReturn(user);
            atm.insertCard("12345"); // Sätt in kortet

            // Simulera en insättning
            Scanner scanner = new Scanner("500\n");
            atm.handleDeposit(scanner);

            // Kontrollera att bankens insättningsmetod anropas
            verify(mockBank).deposit(eq("12345"), eq(500.0));
        }

        @Test
        @DisplayName("Verifierar att uttagsmetoden i banken anropas korrekt")
        void testWithdrawCallsBankMethod() {
            // Mocka för att returnera användaren
            when(mockBank.getUserById("12345")).thenReturn(user);
            atm.insertCard("12345"); // Sätt in kortet

            // Mocka bankens beteende vid uttag
            when(mockBank.withdraw(eq("12345"), eq(400.0))).thenReturn(true);
            Scanner scanner = new Scanner("400\n");
            atm.handleWithdraw(scanner);

            // Kontrollera att bankens uttagsmetod anropas
            verify(mockBank).withdraw(eq("12345"), eq(400.0));
        }

        @Test
        @DisplayName("Mocka och verifiera statisk metod getBankName")
        void testGetBankName() {
            // Mocka den statiska metoden för att returnera bankens namn
            try (MockedStatic<Bank> mockedStatic = mockStatic(Bank.class)) {
                mockedStatic.when(Bank::getBankName).thenReturn("Mocked Bank");

                atm.displayBankName(); // Visa bankens namn

                // Verifiera att den statiska metoden anropades
                mockedStatic.verify(Bank::getBankName);
            }
        }
    }

    @Nested
    @DisplayName("Integrationstester för ATM-flöde")
    class ATMIntegrationTests {

        @Test
        @DisplayName("Integrationstest för PIN-flöde")
        void testHandlePinEntryIntegration() {
            // Skapa en riktig bank för integrationstest
            Bank actualBank = new Bank();
            ATM atmIntegration = new ATM();
            atmIntegration.setBank(actualBank);

            // Lägg till en användare i den riktiga banken
            User user = new User("12345", "1234", 1000);
            actualBank.addUser(user);

            atmIntegration.insertCard("12345"); // Sätt in kortet
            Scanner scanner = new Scanner("1234\n"); // Ange korrekt PIN
            boolean result = atmIntegration.handlePinEntry(scanner);

            // Kontrollera att PIN-flödet lyckas
            assertTrue(result, "PIN-flödet bör lyckas med korrekt PIN i integrationstest.");
        }

        @Test
        @DisplayName("Testar PIN-flöde med tre misslyckade försök")
        void testHandlePinEntryLockCardAfterThreeAttempts() {
            // Mocka för att returnera användaren
            when(mockBank.getUserById("12345")).thenReturn(user);

            atm.insertCard("12345"); // Sätt in kortet

            // Simulera tre felaktiga PIN-försök
            Scanner scanner = new Scanner("1111\n1111\n1111\n");
            boolean result = atm.handlePinEntry(scanner);

            // Kontrollera att kortet låses och sessionen avslutas
            assertFalse(result, "PIN-flödet bör misslyckas efter tre försök.");
            assertTrue(user.isLocked(), "Kortet bör låsas efter tre misslyckade försök.");
            assertNull(atm.currentUser, "Användarsessionen bör avslutas efter kortlåsning.");
            assertEquals(3, user.getFailedAttempts(), "Antalet misslyckade försök bör vara tre.");
        }
    }
}
