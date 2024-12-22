public interface BankInterface {
     // Metod för att hämta en användare baserat på ID
     User getUserById(String id);

     // Metod för att kontrollera om ett kort är låst baserat på användar-ID
     boolean isCardLocked(String userId);

     // Metod för att verifiera om en given PIN-kod är korrekt för ett användar-ID
     boolean verifyPin(String userId, String pin);

     // Metod för att lägga till en användare i banken
     void addUser(User user);
}
