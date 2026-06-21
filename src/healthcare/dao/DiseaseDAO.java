package healthcare.dao;

import healthcare.model.Disease;
import java.util.List;

public interface DiseaseDAO {
    List<Disease> getAllDiseases();
    Disease getDiseaseById(int diseaseId);
    Disease getDiseaseByName(String name);
}