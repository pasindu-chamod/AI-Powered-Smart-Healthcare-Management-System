package healthcare.dao;

import healthcare.model.Patient;
import java.util.List;

public interface PatientDAO {
    Patient getPatientById(int patientId);
    Patient getPatientByUserId(int userId);
    List<Patient> getAllPatients();
    boolean addPatient(Patient patient);
    boolean updatePatient(Patient patient);
    int getTotalCount();
}