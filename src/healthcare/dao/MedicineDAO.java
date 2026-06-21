package healthcare.dao;

import healthcare.model.Medicine;
import java.util.List;

public interface MedicineDAO {
    List<Medicine> getAllMedicines();
    List<Medicine> getMedicinesByDisease(int diseaseId);
    boolean addMedicine(Medicine medicine);
    boolean deleteMedicine(int medicineId);
}