package healthcare.ai;

import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

/**
 * Advanced Naive Bayes Classifier for Disease Prediction
 * Uses probabilistic approach with training data from database
 */
public class NaiveBayesClassifier {
    
    // Training data structures
    private Map<String, Map<String, Double>> diseaseSymptomProb = new HashMap<>();
    private Map<String, Double> diseasePrior = new HashMap<>();
    private Set<String> allSymptoms = new HashSet<>();
    
    public NaiveBayesClassifier() {
        loadTrainingData();
    }
    
    /**
     * Load training data from database
     */
    private void loadTrainingData() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT disease_name, symptom_name, probability FROM symptom_disease_map";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                String disease = rs.getString("disease_name");
                String symptom = rs.getString("symptom_name");
                double prob = rs.getDouble("probability");
                
                diseaseSymptomProb.putIfAbsent(disease, new HashMap<>());
                diseaseSymptomProb.get(disease).put(symptom, prob);
                allSymptoms.add(symptom);
            }
            
            // Calculate uniform prior probabilities
            int numDiseases = diseaseSymptomProb.size();
            for (String disease : diseaseSymptomProb.keySet()) {
                diseasePrior.put(disease, 1.0 / numDiseases);
            }
            
            System.out.println("✅ Naive Bayes Model Loaded:");
            System.out.println("   - Diseases: " + numDiseases);
            System.out.println("   - Symptoms: " + allSymptoms.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error loading training data: " + e.getMessage());
        }
    }
    
    /**
     * Predict disease using Naive Bayes formula:
     * P(Disease|Symptoms) ∝ P(Disease) × ∏P(Symptom|Disease)
     */
    public Map<String, Object> predict(String[] symptoms) {
        Map<String, Object> result = new HashMap<>();
        
        if (symptoms == null || symptoms.length == 0) {
            result.put("status", "error");
            result.put("message", "No symptoms provided");
            return result;
        }
        
        System.out.println("\n🤖 === NAIVE BAYES PREDICTION ===");
        System.out.println("Input Symptoms: " + Arrays.toString(symptoms));
        
        // Calculate log posterior for each disease
        Map<String, Double> logPosterior = new HashMap<>();
        
        for (String disease : diseaseSymptomProb.keySet()) {
            // Start with log prior
            double logProb = Math.log(diseasePrior.get(disease));
            
            Map<String, Double> symptomProbs = diseaseSymptomProb.get(disease);
            
            // Multiply likelihoods (add in log space)
            for (String symptom : symptoms) {
                if (symptomProbs.containsKey(symptom)) {
                    logProb += Math.log(symptomProbs.get(symptom));
                } else {
                    // Laplace smoothing for unknown symptoms
                    logProb += Math.log(0.01);
                }
            }
            
            logPosterior.put(disease, logProb);
        }
        
        // Find disease with maximum posterior
        String predictedDisease = null;
        double maxLogProb = Double.NEGATIVE_INFINITY;
        
        for (Map.Entry<String, Double> entry : logPosterior.entrySet()) {
            if (entry.getValue() > maxLogProb) {
                maxLogProb = entry.getValue();
                predictedDisease = entry.getKey();
            }
        }
        
        if (predictedDisease == null) {
            result.put("status", "no_match");
            result.put("message", "Could not determine disease");
            return result;
        }
        
        // Convert log probabilities to percentages
        List<Map.Entry<String, Double>> sortedDiseases = new ArrayList<>(logPosterior.entrySet());
        sortedDiseases.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        // Normalize probabilities
        double sumExp = 0;
        for (Map.Entry<String, Double> entry : sortedDiseases) {
            sumExp += Math.exp(entry.getValue() - maxLogProb);
        }
        
        double confidence = (Math.exp(maxLogProb - maxLogProb) / sumExp) * 100;
        
        // Get top 3 predictions
        List<String> top3 = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sortedDiseases.size()); i++) {
            String disease = sortedDiseases.get(i).getKey();
            double prob = (Math.exp(sortedDiseases.get(i).getValue() - maxLogProb) / sumExp) * 100;
            top3.add(disease + " (" + String.format("%.1f%%", prob) + ")");
        }
        
        // Get disease info
        String description = getDiseaseDescription(predictedDisease);
        List<String> medicines = getMedicines(predictedDisease);
        
        // Build result
        result.put("status", "success");
        result.put("predicted_disease", predictedDisease);
        result.put("confidence", confidence);
        result.put("description", description);
        result.put("medicines", medicines);
        result.put("top_3_predictions", top3);
        result.put("algorithm", "Naive Bayes Classifier");
        
        System.out.println("✅ Predicted: " + predictedDisease);
        System.out.println("✅ Confidence: " + String.format("%.2f%%", confidence));
        System.out.println("================================\n");
        
        return result;
    }
    
    /**
     * Get disease description from database
     */
    private String getDiseaseDescription(String diseaseName) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT description FROM diseases WHERE disease_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, diseaseName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("description");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No description available.";
    }
    
    /**
     * Get recommended medicines from database
     */
    private List<String> getMedicines(String diseaseName) {
        List<String> medicines = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT m.medicine_name, m.dosage FROM medicines m " +
                          "JOIN diseases d ON m.disease_id = d.disease_id " +
                          "WHERE d.disease_name = ? AND m.is_available = TRUE LIMIT 5";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, diseaseName);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                medicines.add(rs.getString("medicine_name") + " - " + rs.getString("dosage"));
            }
            
            if (medicines.isEmpty()) {
                medicines.add("Consult doctor for medication");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return medicines;
    }
    
    /**
     * Get all available symptoms
     */
    public List<String> getAllSymptoms() {
        return new ArrayList<>(allSymptoms);
    }
    
    /**
     * Get all diseases
     */
    public List<String> getAllDiseases() {
        return new ArrayList<>(diseaseSymptomProb.keySet());
    }
}