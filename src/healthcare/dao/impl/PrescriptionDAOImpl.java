package healthcare.dao.impl;

import healthcare.dao.PrescriptionDAO;
import healthcare.model.Prescription;
import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAOImpl implements PrescriptionDAO {
    
    @Override
    public List<Prescription> getAllPrescriptions() {
        List<Prescription> prescriptions = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT pr.*, p.full_name as patient_name, d.full_name as doctor_name, ds.disease_name " +
                          "FROM prescriptions pr " +
                          "JOIN patients p ON pr.patient_id = p.patient_id " +
                          "JOIN doctors d ON pr.doctor_id = d.doctor_id " +
                          "LEFT JOIN diseases ds ON pr.disease_id = ds.disease_id " +
                          "ORDER BY pr.created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                prescriptions.add(extractPrescription(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prescriptions;
    }
    
    @Override
    public List<Prescription> getPrescriptionsByPatient(int patientId) {
        List<Prescription> prescriptions = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT pr.*, p.full_name as patient_name, d.full_name as doctor_name, ds.disease_name " +
                          "FROM prescriptions pr " +
                          "JOIN patients p ON pr.patient_id = p.patient_id " +
                          "JOIN doctors d ON pr.doctor_id = d.doctor_id " +
                          "LEFT JOIN diseases ds ON pr.disease_id = ds.disease_id " +
                          "WHERE pr.patient_id = ? " +
                          "ORDER BY pr.created_at DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                prescriptions.add(extractPrescription(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prescriptions;
    }
    
    @Override
    public List<Prescription> getPrescriptionsByDoctor(int doctorId) {
        List<Prescription> prescriptions = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT pr.*, p.full_name as patient_name, d.full_name as doctor_name, ds.disease_name " +
                          "FROM prescriptions pr " +
                          "JOIN patients p ON pr.patient_id = p.patient_id " +
                          "JOIN doctors d ON pr.doctor_id = d.doctor_id " +
                          "LEFT JOIN diseases ds ON pr.disease_id = ds.disease_id " +
                          "WHERE pr.doctor_id = ? " +
                          "ORDER BY pr.created_at DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                prescriptions.add(extractPrescription(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prescriptions;
    }
    
    @Override
    public boolean addPrescription(Prescription prescription) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO prescriptions (patient_id, doctor_id, disease_id, diagnosis_notes) " +
                          "VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, prescription.getPatientId());
            pstmt.setInt(2, prescription.getDoctorId());
            pstmt.setInt(3, prescription.getDiseaseId());
            pstmt.setString(4, prescription.getDiagnosisNotes());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private Prescription extractPrescription(ResultSet rs) throws SQLException {
        Prescription prescription = new Prescription();
        prescription.setPrescriptionId(rs.getInt("prescription_id"));
        prescription.setPatientId(rs.getInt("patient_id"));
        prescription.setDoctorId(rs.getInt("doctor_id"));
        prescription.setDiseaseId(rs.getInt("disease_id"));
        prescription.setPatientName(rs.getString("patient_name"));
        prescription.setDoctorName(rs.getString("doctor_name"));
        prescription.setDiseaseName(rs.getString("disease_name"));
        prescription.setDiagnosisNotes(rs.getString("diagnosis_notes"));
        prescription.setCreatedAt(rs.getString("created_at"));
        return prescription;
    }
}