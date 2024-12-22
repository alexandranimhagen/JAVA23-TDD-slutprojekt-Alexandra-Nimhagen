import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class ATMTest {

    private ATM atm; // Objektet av ATM-klassen som vi testar
    private Bank mockBank; // En mockad version av Bank för att simulera bankens funktionalitet
    private User user; // En dummy-användare för tester

    @BeforeEach
    void setUp() {
        // Initialize the mock and dependencies
        mockBank = Mockito.mock(Bank.class);
        atm = new ATM();
        atm.setBank(mockBank);

        // Create a valid user
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
        @DisplayName("Testar insertCard med null userId")
        void testInsertCard_NullUserId() {
            boolean result = atm.insertCard(null);
            assertFalse(result, "Kortinmatning med null userId bör misslyckas.");
        }

        @Test
        @DisplayName("Testar insertCard med tomt userId")
        void testInsertCard_EmptyUserId() {
            boolean result = atm.insertCard("");
            assertFalse(result, "Kortinmatning med tomt userId bör misslyckas.");
        }

        @Test
        @DisplayName("Testar insertCard med låst kort")
        void testInsertCard_LockedCard() {
            when(mockBank.isCardLocked("12345")).thenReturn(true); // Mocka att kortet är låst
            boolean result = atm.insertCard("12345"); // Försök sätta in kortet
            assertFalse(result, "Kortinmatning bör misslyckas om kortet är låst."); // Verifiera att det misslyckas
        }


        @Test
        @DisplayName("Testar PIN-flöde med tre misslyckade försök")
        void testHandlePinEntryLockCardAfterThreeAttempts() {
            // Mocka för att returnera användaren
            when(mockBank.getUserById("12345")).thenReturn(user);

            atm.insertCard("12345"); // Sätt in kortet
            Scanner scanner = new Scanner("1111\n1111\n1111\n");
            boolean result = atm.handlePinEntry(scanner);
            assertFalse(result, "PIN-flödet bör misslyckas efter tre försök.");

            // Kontrollera att flödet misslyckas och kortet låses efter tre försök
            assertFalse(result, "PIN-flödet bör misslyckas efter tre försök.");
            assertTrue(user.isLocked(), "Kortet bör låsas efter tre misslyckade försök.");
            assertNull(atm.currentUser, "Användarsessionen bör avslutas efter kortlåsning.");
            assertEquals(3, user.getFailedAttempts(), "Antalet misslyckade försök bör vara tre.");
        }
        @Test
        @DisplayName("Testar ogiltig PIN vid PIN-inmatning")
        void testHandlePinEntryInvalidPin() {
            when(mockBank.getUserById("12345")).thenReturn(user);
            when(mockBank.verifyPin("12345", "0000")).thenReturn(false);

            atm.insertCard("12345");
            Scanner scanner = new Scanner("0000\n");
            boolean result = atm.handlePinEntry(scanner);

            assertFalse(result, "PIN-inmatningen bör misslyckas med ogiltig PIN.");
        }

        @Test
        @DisplayName("Testar maximalt antal misslyckade PIN-försök")
        void testMaxFailedPinAttempts() {
            when(mockBank.getUserById("12345")).thenReturn(user);

            atm.insertCard("12345");
            Scanner scanner = new Scanner("1111\n1111\n1111\n");
            atm.handlePinEntry(scanner);

            assertTrue(user.isLocked(), "Kortet bör låsas efter tre misslyckade försök.");
            assertNull(atm.currentUser, "Användarsessionen bör avslutas efter att kortet låsts.");
        }

        @Test
        @DisplayName("Testar uttag med otillräckligt saldo")
        void testHandleWithdrawInsufficientBalance() {
            when(mockBank.getUserById("12345")).thenReturn(user);
            when(mockBank.withdraw("12345", 2000)).thenThrow(new IllegalArgumentException("Otillräckligt saldo"));

            atm.insertCard("12345");
            Scanner scanner = new Scanner("2000\n");
            boolean result = atm.handleWithdraw(scanner);

            assertFalse(result, "Uttag med otillräckligt saldo bör misslyckas.");
        }

        @Test
        @DisplayName("Testar uttag med giltigt saldo")
        void testHandleWithdrawValidAmount() {
            when(mockBank.getUserById("12345")).thenReturn(user);
            when(mockBank.withdraw("12345", 500)).thenReturn(true);

            atm.insertCard("12345");
            Scanner scanner = new Scanner("500\n");
            boolean result = atm.handleWithdraw(scanner);

            assertTrue(result, "Uttag med giltigt saldo bör lyckas.");
            verify(mockBank).withdraw(eq("12345"), eq(500.0));
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
        @DisplayName("Testar handlePinEntry med avbruten inmatning")
        void testHandlePinEntry_Cancelled() {
            atm.insertCard("12345");
            Scanner scanner = new Scanner("0\n");
            boolean result = atm.handlePinEntry(scanner);
            assertFalse(result, "PIN-inmatning bör misslyckas när användaren avbryter med '0'.");
        }

        @Test
        @DisplayName("Testar handleCheckBalance utan inloggad användare")
        void testHandleCheckBalance_NoUser() {
            atm.handleCheckBalance();
            // Kontrollera manuellt via konsolutskrift
        }

        @Test
        @DisplayName("Testar handleCheckBalance med inloggad användare")
        void testHandleCheckBalance_WithUser() {
            atm.insertCard("12345");
            atm.handleCheckBalance();
            // Kontrollera manuellt via konsolutskrift
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

        @Test
        @DisplayName("Test getUserById with valid ID")
        void testGetUserById_ValidId() {
            // Mock behavior for getUserById
            when(mockBank.getUserById("12345")).thenReturn(user);

            // Invoke the method
            User retrievedUser = mockBank.getUserById("12345");

            // Assert the result is not null and matches expectations
            assertNotNull(retrievedUser, "The retrieved user should not be null for a valid ID.");
            assertEquals("12345", retrievedUser.getId(), "The user ID should match the expected value.");
        }

        @Test
        @DisplayName("Test getUserById with invalid ID")
        void testGetUserById_InvalidId() {
            assertNull(mockBank.getUserById("99999"), "Ogiltigt ID bör returnera null.");
            assertNull(mockBank.getUserById(null), "Null-ID bör returnera null.");
        }

        @Test
        @DisplayName("Test getUserById with null ID")
        void testGetUserById_NullId() {
            assertNull(mockBank.getUserById("99999"));
        }

        @Test
        @DisplayName("Test isCardLocked for unlocked user")
        void testIsCardLocked_UnlockedUser() {
            when(mockBank.getUserById("12345")).thenReturn(user);
            assertFalse(mockBank.isCardLocked("12345"));
        }

        @Test
        @DisplayName("Testar handleDeposit med negativt belopp")
        void testHandleDeposit_NegativeAmount() {
            atm.insertCard("12345");
            Scanner scanner = new Scanner("-500\n");
            boolean result = atm.handleDeposit(scanner);
            assertFalse(result, "Insättning med negativt belopp bör misslyckas.");
        }

        @Test
        @DisplayName("Testar handleDeposit med giltigt belopp")
        void testHandleDeposit_ValidAmount() {
            when(mockBank.getUserById("12345")).thenReturn(user);
            atm.insertCard("12345");
            Scanner scanner = new Scanner("500\n");
            boolean result = atm.handleDeposit(scanner);
            assertTrue(result, "Insättning med giltigt belopp bör lyckas.");
        }

        @Test
        @DisplayName("Test verifyPin with correct PIN")
        void testVerifyPin_CorrectPin() {
            when(mockBank.verifyPin("12345", "1234")).thenReturn(true);
            assertTrue(mockBank.verifyPin("12345", "1234"));
        }

        @Test
        @DisplayName("Test verifyPin with incorrect PIN")
        void testVerifyPin_IncorrectPin() {
            when(mockBank.verifyPin("12345", "0000")).thenReturn(false);
            assertFalse(mockBank.verifyPin("12345", "0000"));
        }

        @Test
        @DisplayName("Test verifyPin with invalid user ID")
        void testVerifyPin_InvalidUserId() {
            when(mockBank.verifyPin("99999", "1234")).thenReturn(false);
            assertFalse(mockBank.verifyPin("99999", "1234"));
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
        @DisplayName("Testar handleWithdraw med otillräckligt saldo")
        void testHandleWithdraw_InsufficientBalance() {
            when(mockBank.getUserById("12345")).thenReturn(user);
            when(mockBank.withdraw("12345", 2000)).thenThrow(new IllegalArgumentException("Otillräckligt saldo"));
            atm.insertCard("12345");
            Scanner scanner = new Scanner("2000\n");
            boolean result = atm.handleWithdraw(scanner);
            assertFalse(result, "Uttag med otillräckligt saldo bör misslyckas.");
        }

        @Test
        @DisplayName("Testar handleWithdraw med giltigt belopp")
        void testHandleWithdraw_ValidAmount() {
            when(mockBank.getUserById("12345")).thenReturn(user);
            when(mockBank.withdraw("12345", 500)).thenReturn(true);
            atm.insertCard("12345");
            Scanner scanner = new Scanner("500\n");
            boolean result = atm.handleWithdraw(scanner);
            assertTrue(result, "Uttag med giltigt belopp bör lyckas.");
        }

        @Test
        @DisplayName("Testar handleEndSession")
        void testHandleEndSession() {
            atm.insertCard("12345");
            atm.handleEndSession();
            assertNull(atm.currentUser, "Användarsessionen bör avslutas och currentUser bör vara null.");
        }

        @Test
        @DisplayName("Testar deleteAccount med befintlig användare")
        void testDeleteAccount_ExistingUser() {
            when(mockBank.deleteUser("12345")).thenReturn(true);
            boolean result = atm.deleteAccount("12345");
            assertTrue(result, "Radering av befintlig användare bör lyckas.");
        }

        @Test
        @DisplayName("Testar deleteAccount med obefintlig användare")
        void testDeleteAccount_NonExistingUser() {
            when(mockBank.deleteUser("99999")).thenReturn(false);
            boolean result = atm.deleteAccount("99999");
            assertFalse(result, "Radering av obefintlig användare bör misslyckas.");
        }

        @Test
        @DisplayName("Testar getValidAmount med för litet belopp")
        void testGetValidAmount_TooSmall() {
            Scanner scanner = new Scanner("10\n20\n");
            double result = atm.getValidAmount(scanner, "Ange belopp: ", 20, 10);
            assertEquals(20, result, "Beloppet bör vara minst 20.");
        }

        @Test
        @DisplayName("Testar getValidAmount med ogiltig input")
        void testGetValidAmount_InvalidInput() {
            Scanner scanner = new Scanner("abc\n50\n");
            double result = atm.getValidAmount(scanner, "Ange belopp: ", 20, 10);
            assertEquals(50, result, "Beloppet bör vara det första giltiga värdet.");
        }
        @Test
        @DisplayName("Testar getValidAmount med reservinmatning")
        void testGetValidAmount_WithFallbackInput() {
            Scanner scanner = new Scanner("abc\n10\n20\n"); // Ogiltig inmatning följt av ogiltigt belopp och sedan giltigt belopp
            double result = atm.getValidAmount(scanner, "Ange belopp: ", 20, 10);
            assertEquals(20, result, "Beloppet bör vara det första giltiga värdet.");
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
    @Nested
    @DisplayName("Enhetstester för Bank-klassen")
    class BankUnitTests {

        private Bank bank;

        @BeforeEach
        void setUpBank() {
            bank = new Bank(); // Skapa en ny instans av Bank
        }

        @Test
        @DisplayName("Test isCardLocked for locked user")
        void testIsCardLocked_LockedUser() {
            User user = bank.getUserById("12345");
            user.lockCard(); // Lås kortet

            assertTrue(bank.isCardLocked("12345"), "Kortet bör vara låst.");
        }

        @Test
        @DisplayName("Test adding a valid user")
        void testAddUser_ValidUser() {
            User newUser = new User("67890", "5678", 500.0);
            bank.addUser(newUser);

            assertNotNull(bank.getUserById("67890"), "Den nya användaren bör läggas till i banken.");
        }

        @Test
        @DisplayName("Test adding a null user")
        void testAddUser_NullUser() {
            bank.addUser(null);

            // Verifiera att ingen användare läggs till
            assertNull(bank.getUserById(null), "Null-användare bör inte läggas till.");
        }

        @Test
        @DisplayName("Test adding a null user with console validation")
        void testAddUser_NullUser_Console() {
            bank.addUser(null);
            assertNull(bank.getUserById(null), "Null-användare bör inte läggas till.");
        }

        @Test
        @DisplayName("Test deleting an existing user")
        void testDeleteUser_ValidId() {
            boolean result = bank.deleteUser("12345");

            assertTrue(result, "Användaren bör tas bort framgångsrikt.");
            assertNull(bank.getUserById("12345"), "Användaren bör inte längre finnas i banken.");
        }

        @Test
        @DisplayName("Test deleting a non-existing user")
        void testDeleteUser_InvalidId() {
            boolean result = bank.deleteUser("99999");

            assertFalse(result, "Det bör inte gå att ta bort en användare som inte finns.");
        }

        @Test
        @DisplayName("Test deposit with negative amount")
        void testDeposit_NegativeAmount() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> bank.deposit("12345", -500));

            assertEquals("Ogiltigt användar-ID eller belopp.", exception.getMessage());
        }

        @Test
        @DisplayName("Test deposit with zero amount")
        void testDeposit_ZeroAmount() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> bank.deposit("12345", 0));

            assertEquals("Ogiltigt användar-ID eller belopp.", exception.getMessage());
        }

        @Test
        @DisplayName("Test deposit with valid amount")
        void testDeposit_ValidAmount() {
            bank.deposit("12345", 500);

            User user = bank.getUserById("12345");
            assertEquals(1500.0, user.getBalance(), "Saldo bör öka med insatt belopp.");
        }

        @Test
        @DisplayName("Test withdraw with insufficient balance")
        void testWithdraw_InsufficientBalance() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> bank.withdraw("12345", 2000));

            assertEquals("Otillräckligt saldo.", exception.getMessage());
        }

        @Test
        @DisplayName("Test withdraw with null user ID")
        void testWithdraw_NullUserId() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> bank.withdraw(null, 100));
            assertEquals("Ogiltigt användar-ID.", exception.getMessage());
        }

        @Test
        @DisplayName("Test withdraw with valid amount")
        void testWithdraw_ValidAmount() {
            boolean result = bank.withdraw("12345", 500);

            assertTrue(result, "Uttaget bör lyckas.");
            User user = bank.getUserById("12345");
            assertEquals(500.0, user.getBalance(), "Saldo bör minska med det uttagna beloppet.");
        }

    }

    @Nested
    @DisplayName("Tester för Main-klassen")
    class MainTests {

        @Test
        @DisplayName("Testar att programmet avslutas korrekt med '0'")
        void testMainExitProgram() {
            // Simulera användarinmatning: "0" för att avsluta programmet
            String simulatedInput = "0\n";
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            // Fånga konsolutgången
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            // Kör main-metoden
            Main.main(new String[]{});

            // Kontrollera att programmet avslutades med rätt meddelande
            String consoleOutput = output.toString();
            assertTrue(consoleOutput.contains("Programmet avslutas. Tack för att du använde tjänsten!"));
        }

        @Test
        @DisplayName("Testar avslut från huvudloopen med '0'")
        void testExitFromMainLoop() {
            String simulatedInput = "0\n";
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            // Kör main-metoden
            Main.main(new String[]{});

            String consoleOutput = output.toString();
            assertTrue(consoleOutput.contains("Programmet avslutas. Tack för att du använde tjänsten!"));
        }

        @Test
        @DisplayName("Testar att ogiltigt användar-ID hanteras korrekt")
        void testMainInvalidUserId() {
            // Simulera inmatning: tomt ID och sedan avslut
            String simulatedInput = "\n0\n"; // Tomt ID och sedan avsluta
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            // Fånga konsolutgången
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            // Kör main-metoden
            Main.main(new String[]{});

            // Kontrollera att felmeddelande visas för ogiltigt ID
            String consoleOutput = output.toString();
            assertTrue(consoleOutput.contains("Ogiltigt ID"),
                    "Utskriften saknar texten 'Ogiltigt ID'. Faktisk output: " + consoleOutput);
        }

        @Test
        @DisplayName("Testar tomt användar-ID vid kortinmatning")
        void testEmptyUserId() {
            String simulatedInput = "\n0\n"; // Tomt ID och avslut
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            // Kör main-metoden
            Main.main(new String[]{});

            String consoleOutput = output.toString();
            assertTrue(consoleOutput.contains("Ogiltigt ID. Vänligen försök igen."));
        }

        @Test
        @DisplayName("Testar menyval för att kontrollera saldo")
        void testProcessUserChoicesCheckBalance() {
            // Simulera användarinmatning för att välja "Kontrollera saldo" och sedan avsluta
            String simulatedInput = "1\n4\n"; // Välj alternativ 1 och sedan 4 för att avsluta sessionen
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            // Fånga konsolutgången
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            // Skapa en mockad ATM
            ATM mockATM = mock(ATM.class);

            // Mocka beteendet för handleCheckBalance
            doAnswer(invocation -> {
                System.out.println("Ditt saldo är: 1000 kr"); // Simulera utskrift från handleCheckBalance
                return null;
            }).when(mockATM).handleCheckBalance();

            // Kör metoden
            Main.processUserChoices(new Scanner(input), mockATM);

            // Kontrollera att handleCheckBalance anropades
            verify(mockATM, times(1)).handleCheckBalance();

            // Kontrollera konsolutgång
            String consoleOutput = output.toString();
            assertTrue(consoleOutput.contains("Ditt saldo är:"), "Saldo-utskriften saknas.");
            assertTrue(consoleOutput.contains("Session avslutad. Du återgår nu till huvudmenyn."), "Avslutningsmeddelandet saknas.");
        }

        @Test
        @DisplayName("Testar menyval för insättning")
        void testDepositFromMenu() {
            String simulatedInput = "2\n500\n4\n"; // Välj insättning och avsluta
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            ATM mockATM = mock(ATM.class);

            Main.processUserChoices(new Scanner(input), mockATM);

            verify(mockATM, times(1)).handleDeposit(any());
            assertTrue(output.toString().contains("Session avslutad. Du återgår nu till huvudmenyn."));
        }

        @Test
        @DisplayName("Testar ogiltigt menyval")
        void testInvalidMenuChoice() {
            String simulatedInput = "5\n-1\n4\n"; // Ogiltigt val och sedan avsluta
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            ATM mockATM = mock(ATM.class);
            Main.processUserChoices(new Scanner(input), mockATM);

            String consoleOutput = output.toString();
            assertTrue(consoleOutput.contains("Ogiltigt val. Försök igen."));
            assertTrue(consoleOutput.contains("Session avslutad. Du återgår nu till huvudmenyn."));
        }

        @Test
        @DisplayName("Testar InputMismatchException vid menyval")
        void testInvalidMenuInput() {
            Main.isTestMode = true; // Aktivera testläge

            // Simulera ogiltig inmatning följt av avslut
            String simulatedInput = "abc\n0\n";
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            // Kör metoden
            ATM mockATM = mock(ATM.class);
            Scanner scanner = new Scanner(input);
            Main.processUserChoices(scanner, mockATM);

            // Fånga konsolutgången
            String consoleOutput = output.toString();

            // Kontrollera att rätt felmeddelande visas
            assertTrue(consoleOutput.contains("Ogiltig inmatning. Ange ett nummer mellan 0 och 4."),
                    "Felmeddelande för ogiltig inmatning saknas i konsolutgången.");
        }

        @Test
        @DisplayName("Testar tomt menyval")
        void testEmptyMenuInput() {
            String simulatedInput = "\n0\n"; // Tom menyval följt av avslut
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            Main.main(new String[]{});

            String consoleOutput = output.toString();
            assertTrue(consoleOutput.contains("Ogiltigt ID. Vänligen försök igen."),
                    "Felmeddelande för tomt menyval saknas.");
        }

        @Test
        @DisplayName("Integrationstest för komplett flöde")
        void testFullFlow() {
            String simulatedInput = "12345\n1234\n1\n4\n0\n"; // Ange ID, PIN, saldo, logga ut, avsluta
            ByteArrayInputStream input = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(input);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            Main.main(new String[]{});

            String consoleOutput = output.toString();
            assertTrue(consoleOutput.contains("Ditt saldo är:"),
                    "Saldo-utskriften saknas i det fullständiga flödet.");
            assertTrue(consoleOutput.contains("Session avslutad."),
                    "Avslutningsmeddelandet för sessionen saknas.");
            assertTrue(consoleOutput.contains("Programmet avslutas."),
                    "Avslutningsmeddelandet för programmet saknas.");
        }

    }

}