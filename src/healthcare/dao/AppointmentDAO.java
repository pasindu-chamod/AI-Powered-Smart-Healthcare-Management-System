package healthcare.dao;

import healthcare.model.Appointment;
import java.util.List;

public interface AppointmentDAO {
    List<Appointment> getAllAppointments();
    List<Appointment> getAppointmentsByPatient(int patientId);
    List<Appointment> getAppointmentsByDoctor(int doctorId);
    boolean addAppointment(Appointment appointment);
    boolean updateStatus(int appointmentId, String status);
    int getTotalCount();
}