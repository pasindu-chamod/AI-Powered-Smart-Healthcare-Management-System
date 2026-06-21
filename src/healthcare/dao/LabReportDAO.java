package healthcare.dao;

import healthcare.model.LabReport;
import java.util.List;

public interface LabReportDAO {
    List<LabReport> getReportsByPatient(int patientId);
    List<LabReport> getAllReports();
    boolean addReport(LabReport report);
    boolean updateReport(LabReport report);
}