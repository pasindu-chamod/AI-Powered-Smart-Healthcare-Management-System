package healthcare.dao.impl;

import healthcare.dao.DiseaseDAO;
import healthcare.model.Disease;
import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiseaseDAOImpl implements DiseaseDAO {
    
    @Override
    public List<Disease> getAllDiseases() {
        List<Disease> diseases = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM diseases ORDER BY disease_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                diseases.add(extractDisease(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diseases;
    }
    
    @Override
    public Disease getDiseaseById(int diseaseId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM diseases WHERE disease_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, diseaseId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractDisease(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public Disease getDiseaseByName(String name) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM diseases WHERE disease_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractDisease(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private Disease extractDisease(ResultSet rs) throws SQLException {
        Disease disease = new Disease();
        disease.setDiseaseId(rs.getInt("disease_id"));
        disease.setDiseaseName(rs.getString("disease_name"));
        disease.setDescription(rs.getString("description"));
        disease.setSeverity(rs.getString("severity"));
        disease.setCreatedAt(rs.getString("created_at"));
        return disease;
    }
}