package healthcare.dao;

import healthcare.model.AIPrediction;
import java.util.List;

public interface AIPredictionDAO {
    boolean savePrediction(AIPrediction prediction);
    List<AIPrediction> getAllPredictions();
    List<AIPrediction> getPredictionsByPatient(int patientId);
    boolean markReviewed(int predictionId);
}