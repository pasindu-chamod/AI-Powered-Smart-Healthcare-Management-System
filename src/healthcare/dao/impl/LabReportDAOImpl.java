package healthcare.dao.impl;

import healthcare.dao.LabReportDAO;
import healthcare.model.LabReport;
import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LabReportDAOImpl implements LabReportDAO {

    // ── Get all reports (doctor/admin view) ─────────────────────────────────
    @Override
    public List<LabReport> getAllReports() {
        List<LabReport> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query =
                "SELECT lr.*, " +
                "p.full_name AS patient_name, " +
                "d.full_name AS doctor_name " +
                "FROM lab_reports lr " +
                "JOIN patients  p ON lr.patient_id = p.patient_id " +
                "JOIN doctors   d ON lr.doctor_id  = d.doctor_id " +
                "ORDER BY lr.created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) list.add(extract(rs));
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── Get reports for one patient ──────────────────────────────────────────
    @Override
    public List<LabReport> getReportsByPatient(int patientId) {
        List<LabReport> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query =
                "SELECT lr.*, " +
                "p.full_name AS patient_name, " +
                "d.full_name AS doctor_name " +
                "FROM lab_reports lr " +
                "JOIN patients  p ON lr.patient_id = p.patient_id " +
                "JOIN doctors   d ON lr.doctor_id  = d.doctor_id " +
                "WHERE lr.patient_id = ? " +
                "ORDER BY lr.created_at DESC";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(extract(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── Get reports for one doctor ───────────────────────────────────────────
    public List<LabReport> getReportsByDoctor(int doctorId) {
        List<LabReport> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query =
                "SELECT lr.*, " +
                "p.full_name AS patient_name, " +
                "d.full_name AS doctor_name " +
                "FROM lab_reports lr " +
                "JOIN patients  p ON lr.patient_id = p.patient_id " +
                "JOIN doctors   d ON lr.doctor_id  = d.doctor_id " +
                "WHERE lr.doctor_id = ? " +
                "ORDER BY lr.created_at DESC";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(extract(rs));
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── Add new lab report ───────────────────────────────────────────────────
    @Override
    public boolean addReport(LabReport r) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query =
                "INSERT INTO lab_reports " +
                "(patient_id, doctor_id, report_type, results, status, notes, report_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getDoctorId());
            ps.setString(3, r.getReportType());
            ps.setString(4, r.getResults());
            ps.setString(5, r.getStatus() != null ? r.getStatus() : "Pending");
            ps.setString(6, r.getNotes());
            ps.setString(7, r.getReportDate());
            boolean ok = ps.executeUpdate() > 0;
            ps.close();
            return ok;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Update existing report (results + status + notes) ───────────────────
    @Override
    public boolean updateReport(LabReport r) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query =
                "UPDATE lab_reports SET results=?, status=?, notes=? " +
                "WHERE report_id=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, r.getResults());
            ps.setString(2, r.getStatus());
            ps.setString(3, r.getNotes());
            ps.setInt(4, r.getReportId());
            boolean ok = ps.executeUpdate() > 0;
            ps.close();
            return ok;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Delete report ────────────────────────────────────────────────────────
    public boolean deleteReport(int reportId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM lab_reports WHERE report_id = ?");
            ps.setInt(1, reportId);
            boolean ok = ps.executeUpdate() > 0;
            ps.close();
            return ok;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Helper: map ResultSet row → LabReport object ─────────────────────────
    private LabReport extract(ResultSet rs) throws SQLException {
        LabReport r = new LabReport();
        r.setReportId(rs.getInt("report_id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setDoctorId(rs.getInt("doctor_id"));
        r.setPatientName(rs.getString("patient_name"));
        r.setDoctorName(rs.getString("doctor_name"));
        r.setReportType(rs.getString("report_type"));
        r.setResults(rs.getString("results"));
        r.setStatus(rs.getString("status"));
        r.setNotes(rs.getString("notes"));
        r.setReportDate(rs.getString("report_date"));
        r.setCreatedAt(rs.getString("created_at"));
        return r;
    }
}