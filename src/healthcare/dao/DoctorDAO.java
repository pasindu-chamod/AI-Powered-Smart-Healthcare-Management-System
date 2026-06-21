package healthcare.dao;

import healthcare.model.Doctor;
import java.util.List;

public interface DoctorDAO {
    Doctor getDoctorById(int doctorId);
    Doctor getDoctorByUserId(int userId);
    List<Doctor> getAllDoctors();
    List<Doctor> getApprovedDoctors();
    boolean addDoctor(Doctor doctor);
    boolean updateDoctor(Doctor doctor);
    boolean approveDoctor(int doctorId);
    int getTotalCount();
}