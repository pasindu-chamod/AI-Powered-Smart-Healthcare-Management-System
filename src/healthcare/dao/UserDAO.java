package healthcare.dao;

import healthcare.model.User;

public interface UserDAO {
    User getUserByUsername(String username);
    boolean addUser(User user);
    boolean usernameExists(String username);
    boolean updateUser(User user);
}