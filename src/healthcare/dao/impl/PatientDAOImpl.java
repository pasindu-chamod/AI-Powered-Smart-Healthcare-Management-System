package healthcare.dao.impl;

import healthcare.dao.PatientDAO;
import healthcare.model.Patient;
import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAOImpl implements PatientDAO {
    
    @Override
    public Patient getPatientById(int patientId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM patients WHERE patient_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractPatient(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public Patient getPatientByUserId(int userId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM patients WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractPatient(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM patients ORDER BY created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                patients.add(extractPatient(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return patients;
    }
    
    @Override
    public boolean addPatient(Patient patient) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO patients (user_id, full_name, email, phone, date_of_birth, gender, blood_group, address) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, patient.getUserId());
            pstmt.setString(2, patient.getFullName());
            pstmt.setString(3, patient.getEmail());
            pstmt.setString(4, patient.getPhone());
            pstmt.setString(5, patient.getDateOfBirth());
            pstmt.setString(6, patient.getGender());
            pstmt.setString(7, patient.getBloodGroup());
            pstmt.setString(8, patient.getAddress());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean updatePatient(Patient patient) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE patients SET full_name = ?, email = ?, phone = ?, " +
                          "date_of_birth = ?, gender = ?, blood_group = ?, address = ? " +
                          "WHERE patient_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, patient.getFullName());
            pstmt.setString(2, patient.getEmail());
            pstmt.setString(3, patient.getPhone());
            pstmt.setString(4, patient.getDateOfBirth());
            pstmt.setString(5, patient.getGender());
            pstmt.setString(6, patient.getBloodGroup());
            pstmt.setString(7, patient.getAddress());
            pstmt.setInt(8, patient.getPatientId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public int getTotalCount() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT COUNT(*) FROM patients";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private Patient extractPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setUserId(rs.getInt("user_id"));
        patient.setFullName(rs.getString("full_name"));
        patient.setEmail(rs.getString("email"));
        patient.setPhone(rs.getString("phone"));
        patient.setDateOfBirth(rs.getString("date_of_birth"));
        patient.setGender(rs.getString("gender"));
        patient.setBloodGroup(rs.getString("blood_group"));
        patient.setAddress(rs.getString("address"));
        patient.setCreatedAt(rs.getString("created_at"));
        return patient;
    }
}