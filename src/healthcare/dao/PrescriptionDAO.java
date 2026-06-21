package healthcare.dao;

import healthcare.model.Prescription;
import java.util.List;

public interface PrescriptionDAO {
    List<Prescription> getAllPrescriptions();
    List<Prescription> getPrescriptionsByPatient(int patientId);
    List<Prescription> getPrescriptionsByDoctor(int doctorId);
    boolean addPrescription(Prescription prescription);
}