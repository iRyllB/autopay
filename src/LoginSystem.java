import java.util.*;

public class LoginSystem {
    private List<User> users;

    public LoginSystem(String usersFilePath) {
        users = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV(usersFilePath);
        for (int i = 1; i < data.size(); i++) { // skip header
            String[] row = data.get(i);
            if (row.length < 3) continue; // defensive against malformed/empty lines
            users.add(new User(row[0], row[1], row[2]));
        }
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
