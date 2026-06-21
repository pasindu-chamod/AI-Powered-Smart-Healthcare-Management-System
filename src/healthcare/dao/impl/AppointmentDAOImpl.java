package healthcare.dao.impl;

import healthcare.dao.AppointmentDAO;
import healthcare.model.Appointment;
import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAOImpl implements AppointmentDAO {
    
    @Override
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT a.*, p.full_name as patient_name, d.full_name as doctor_name " +
                          "FROM appointments a " +
                          "JOIN patients p ON a.patient_id = p.patient_id " +
                          "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                          "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                appointments.add(extractAppointment(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appointments;
    }
    
    @Override
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> appointments = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT a.*, p.full_name as patient_name, d.full_name as doctor_name " +
                          "FROM appointments a " +
                          "JOIN patients p ON a.patient_id = p.patient_id " +
                          "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                          "WHERE a.patient_id = ? " +
                          "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                appointments.add(extractAppointment(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appointments;
    }
    
    @Override
    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT a.*, p.full_name as patient_name, d.full_name as doctor_name " +
                          "FROM appointments a " +
                          "JOIN patients p ON a.patient_id = p.patient_id " +
                          "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                          "WHERE a.doctor_id = ? " +
                          "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                appointments.add(extractAppointment(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appointments;
    }
    
    @Override
    public boolean addAppointment(Appointment appointment) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason) " +
                          "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setString(3, appointment.getAppointmentDate());
            pstmt.setString(4, appointment.getAppointmentTime());
            pstmt.setString(5, appointment.getReason());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean updateStatus(int appointmentId, String status) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, status);
            pstmt.setInt(2, appointmentId);
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
            String query = "SELECT COUNT(*) FROM appointments";
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
    
    private Appointment extractAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("appointment_id"));
        appointment.setPatientId(rs.getInt("patient_id"));
        appointment.setDoctorId(rs.getInt("doctor_id"));
        appointment.setPatientName(rs.getString("patient_name"));
        appointment.setDoctorName(rs.getString("doctor_name"));
        appointment.setAppointmentDate(rs.getString("appointment_date"));
        appointment.setAppointmentTime(rs.getString("appointment_time"));
        appointment.setReason(rs.getString("reason"));
        appointment.setStatus(rs.getString("status"));
        appointment.setNotes(rs.getString("notes"));
        appointment.setCreatedAt(rs.getString("created_at"));
        return appointment;
    }
}