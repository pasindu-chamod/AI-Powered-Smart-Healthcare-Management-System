package healthcare.dao.impl;

import healthcare.dao.UserDAO;
import healthcare.model.User;
import healthcare.util.DatabaseConnection;
import java.sql.*;

public class UserDAOImpl implements UserDAO {
    
    @Override
    public User getUserByUsername(String username) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                user.setCreatedAt(rs.getString("created_at"));
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean addUser(User user) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean usernameExists(String username) {
        return getUserByUsername(username) != null;
    }
    
    @Override
    public boolean updateUser(User user) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE users SET password = ?, is_active = ? WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, user.getPassword());
            pstmt.setBoolean(2, user.isActive());
            pstmt.setInt(3, user.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}