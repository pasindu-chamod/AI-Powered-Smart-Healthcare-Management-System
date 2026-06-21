package healthcare.dao.impl;

import healthcare.dao.LabReportDAO;
import healthcare.model.LabReport;
import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LabReportDAOImpl implements LabReportDAO {
    
    @Override
    public List<LabReport> getReportsByPatient(int patientId) {
        List<LabReport> reports = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT lr.*, p.full_name as patient_name, d.full_name as doctor_name " +
                          "FROM lab_reports lr " +
                          "JOIN patients p ON lr.patient_id = p.patient_id " +
                          "LEFT JOIN doctors d ON lr.doctor_id = d.doctor_id " +
                          "WHERE lr.patient_id = ? " +
                          "ORDER BY lr.report_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reports.add(extractReport(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reports;
    }
    
    @Override
    public List<LabReport> getAllReports() {
        List<LabReport> reports = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT lr.*, p.full_name as patient_name, d.full_name as doctor_name " +
                          "FROM lab_reports lr " +
                          "JOIN patients p ON lr.patient_id = p.patient_id " +
                          "LEFT JOIN doctors d ON lr.doctor_id = d.doctor_id " +
                          "ORDER BY lr.report_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                reports.add(extractReport(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reports;
    }
    
    @Override
    public boolean addReport(LabReport report) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO lab_reports (patient_id, doctor_id, report_type, report_date, results) " +
                          "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, report.getPatientId());
            pstmt.setInt(2, report.getDoctorId());
            pstmt.setString(3, report.getReportType());
            pstmt.setString(4, report.getReportDate());
            pstmt.setString(5, report.getResults());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean updateReport(LabReport report) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE lab_reports SET results = ?, ai_analysis = ?, status = ? WHERE report_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, report.getResults());
            pstmt.setString(2, report.getAiAnalysis());
            pstmt.setString(3, report.getStatus());
            pstmt.setInt(4, report.getReportId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private LabReport extractReport(ResultSet rs) throws SQLException {
        LabReport report = new LabReport();
        report.setReportId(rs.getInt("report_id"));
        report.setPatientId(rs.getInt("patient_id"));
        report.setDoctorId(rs.getInt("doctor_id"));
        report.setPatientName(rs.getString("patient_name"));
        report.setDoctorName(rs.getString("doctor_name"));
        report.setReportType(rs.getString("report_type"));
        report.setReportDate(rs.getString("report_date"));
        report.setFilePath(rs.getString("file_path"));
        report.setResults(rs.getString("results"));
        report.setAiAnalysis(rs.getString("ai_analysis"));
        report.setStatus(rs.getString("status"));
        report.setCreatedAt(rs.getString("created_at"));
        return report;
    }
}