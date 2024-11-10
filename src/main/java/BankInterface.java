public interface BankInterface {
     User getUserById(String id);
     boolean isCardLocked(String userId);
     boolean verifyPin(String userId, String pin);
     int getFailedAttempts(String userId);
     void addUser(User user);
     void updateUser(User user);
}
