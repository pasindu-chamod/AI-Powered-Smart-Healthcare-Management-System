package healthcare.dao.impl;

import healthcare.dao.DoctorDAO;
import healthcare.model.Doctor;
import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAOImpl implements DoctorDAO {
    
    @Override
    public Doctor getDoctorById(int doctorId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM doctors WHERE doctor_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractDoctor(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public Doctor getDoctorByUserId(int userId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM doctors WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractDoctor(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM doctors ORDER BY created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                doctors.add(extractDoctor(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doctors;
    }
    
    @Override
    public List<Doctor> getApprovedDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM doctors WHERE is_approved = TRUE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                doctors.add(extractDoctor(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doctors;
    }
    
    @Override
    public boolean addDoctor(Doctor doctor) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO doctors (user_id, full_name, email, phone, specialization, license_number) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, doctor.getUserId());
            pstmt.setString(2, doctor.getFullName());
            pstmt.setString(3, doctor.getEmail());
            pstmt.setString(4, doctor.getPhone());
            pstmt.setString(5, doctor.getSpecialization());
            pstmt.setString(6, doctor.getLicenseNumber());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean updateDoctor(Doctor doctor) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE doctors SET full_name = ?, email = ?, phone = ?, " +
                          "specialization = ?, license_number = ? WHERE doctor_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, doctor.getFullName());
            pstmt.setString(2, doctor.getEmail());
            pstmt.setString(3, doctor.getPhone());
            pstmt.setString(4, doctor.getSpecialization());
            pstmt.setString(5, doctor.getLicenseNumber());
            pstmt.setInt(6, doctor.getDoctorId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean approveDoctor(int doctorId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE doctors SET is_approved = TRUE WHERE doctor_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, doctorId);
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
            String query = "SELECT COUNT(*) FROM doctors";
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
    
    private Doctor extractDoctor(ResultSet rs) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getInt("doctor_id"));
        doctor.setUserId(rs.getInt("user_id"));
        doctor.setFullName(rs.getString("full_name"));
        doctor.setEmail(rs.getString("email"));
        doctor.setPhone(rs.getString("phone"));
        doctor.setSpecialization(rs.getString("specialization"));
        doctor.setLicenseNumber(rs.getString("license_number"));
        doctor.setApproved(rs.getBoolean("is_approved"));
        doctor.setCreatedAt(rs.getString("created_at"));
        return doctor;
    }
}