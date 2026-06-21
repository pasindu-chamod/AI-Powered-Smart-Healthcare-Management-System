package healthcare.service;

import healthcare.dao.PrescriptionDAO;
import healthcare.dao.impl.PrescriptionDAOImpl;
import healthcare.model.Prescription;
import java.util.List;

public class PrescriptionService {
    
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAOImpl();
    
    public boolean addPrescription(Prescription prescription) {
        return prescriptionDAO.addPrescription(prescription);
    }
    
    public List<Prescription> getForPatient(int patientId) {
        return prescriptionDAO.getPrescriptionsByPatient(patientId);
    }
    
    public List<Prescription> getForDoctor(int doctorId) {
        return prescriptionDAO.getPrescriptionsByDoctor(doctorId);
    }
}