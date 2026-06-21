package healthcare.ai;

import java.util.*;

/**
 * Wrapper class for AI Prediction
 */
public class AIPredictor {
    
    private NaiveBayesClassifier classifier;
    
    public AIPredictor() {
        this.classifier = new NaiveBayesClassifier();
    }
    
    public Map<String, Object> predict(String[] symptoms) {
        return classifier.predict(symptoms);
    }
    
    public List<String> getAllSymptoms() {
        return classifier.getAllSymptoms();
    }
    
    public List<String> getAllDiseases() {
        return classifier.getAllDiseases();
    }
}