package healthcare.service;

import healthcare.dao.AppointmentDAO;
import healthcare.dao.impl.AppointmentDAOImpl;
import healthcare.model.Appointment;
import java.util.List;

public class AppointmentService {
    
    private final AppointmentDAO appointmentDAO = new AppointmentDAOImpl();
    
    public boolean bookAppointment(Appointment appointment) {
        return appointmentDAO.addAppointment(appointment);
    }
    
    public List<Appointment> getForPatient(int patientId) {
        return appointmentDAO.getAppointmentsByPatient(patientId);
    }
    
    public List<Appointment> getForDoctor(int doctorId) {
        return appointmentDAO.getAppointmentsByDoctor(doctorId);
    }
    
    public boolean confirm(int appointmentId) {
        return appointmentDAO.updateStatus(appointmentId, "CONFIRMED");
    }
    
    public boolean complete(int appointmentId) {
        return appointmentDAO.updateStatus(appointmentId, "COMPLETED");
    }
    
    public boolean cancel(int appointmentId) {
        return appointmentDAO.updateStatus(appointmentId, "CANCELLED");
    }
}