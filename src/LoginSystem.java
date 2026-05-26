import java.util.*;

public class LoginSystem {
    private List<User> users;

    public LoginSystem(String usersFilePath) {
        users = new ArrayList<>();
        // Centralized: users are read from data/master.csv via DataStore
        users.addAll(DataStore.loadUsers(DataStore.currentMonth()));
    }

    public User validateLogin(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}
