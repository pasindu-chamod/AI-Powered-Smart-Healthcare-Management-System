package healthcare.ai;

import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

/**
 * ============================================================================
 *  NAIVE BAYES CLASSIFIER FOR DISEASE PREDICTION
 * ============================================================================
 *
 *  This class implements the FULL Naive Bayes calculation in Java:
 *
 *   STEP 1 (TRAINING): Read raw training records (disease + symptom cases)
 *                       from the database and COUNT occurrences.
 *
 *   STEP 2 (LEARNING):  Use those counts to CALCULATE:
 *                          - Prior probability   P(Disease)
 *                          - Likelihood          P(Symptom | Disease)
 *                        (with Laplace / add-1 smoothing for unseen symptoms)
 *
 *   STEP 3 (PREDICTION): Given a new list of symptoms, apply Bayes' Theorem
 *                          P(Disease | Symptoms) ∝ P(Disease) x ∏ P(Symptom_i | Disease)
 *                        to find the most probable disease.
 *
 *  Bayes' Theorem reminder:
 *
 *      P(Disease | Symptoms) = [ P(Symptoms | Disease) x P(Disease) ] / P(Symptoms)
 *
 *  Since P(Symptoms) is the same for every disease being compared, it is
 *  dropped (we only need it to pick the BEST disease, and then we
 *  re-normalize at the end to turn the scores into percentages).
 *
 *  "Naive" assumption: every symptom is conditionally independent given the
 *  disease, so:
 *
 *      P(Symptoms | Disease) = P(Symptom_1|Disease) x P(Symptom_2|Disease) x ...
 *
 * ============================================================================
 */
public class NaiveBayesClassifier {

    // ---- Raw counts gathered from training data (the "counting" step) ----
    /** diseaseCaseCount.get(disease) = number of training cases recorded for that disease */
    private Map<String, Integer> diseaseCaseCount = new HashMap<>();

    /** symptomCountPerDisease.get(disease).get(symptom) = how many times that
     *  symptom appeared in training cases for that disease */
    private Map<String, Map<String, Integer>> symptomCountPerDisease = new HashMap<>();

    /** Total number of training cases (all diseases combined) */
    private int totalCases = 0;

    // ---- Calculated probabilities (the "learning" step, derived from counts above) ----
    /** P(Disease) for every disease */
    private Map<String, Double> diseasePrior = new HashMap<>();

    /** P(Symptom | Disease) for every (disease, symptom) pair seen in training */
    private Map<String, Map<String, Double>> diseaseSymptomProb = new HashMap<>();

    private Set<String> allSymptoms = new HashSet<>();
    private Set<String> allDiseases = new HashSet<>();

    /** Laplace smoothing constant (add-1 smoothing) - avoids zero probabilities
     *  for symptoms never seen with a particular disease during training. */
    private static final double LAPLACE_SMOOTHING = 1.0;

    public NaiveBayesClassifier() {
        boolean loaded = loadRawTrainingData();
        if (loaded && totalCases > 0) {
            trainModel();
        } else {
            System.err.println("⚠️  No training data found - classifier has nothing to learn from.");
        }
    }

    // ========================================================================
    //  STEP 1: LOAD RAW TRAINING DATA AND COUNT (no probabilities yet - just counts)
    // ========================================================================
    /**
     * Reads raw training cases from the database table `disease_symptom_training`.
     * Expected schema (one row per observed symptom occurring in a case):
     *
     *   CREATE TABLE disease_symptom_training (
     *       training_id  INT AUTO_INCREMENT PRIMARY KEY,
     *       case_id      INT NOT NULL,        -- groups symptoms belonging to the same case
     *       disease_name VARCHAR(100) NOT NULL,
     *       symptom_name VARCHAR(100) NOT NULL
     *   );
     *
     * Each distinct case_id represents one training example/patient case.
     * Counting how many distinct case_ids exist per disease gives us the
     * raw frequency needed to calculate the prior P(Disease).
     * Counting how many of those cases mention each symptom gives us the
     * raw frequency needed to calculate the likelihood P(Symptom|Disease).
     */
    private boolean loadRawTrainingData() {
        // caseDiseaseMap: case_id -> disease_name  (so we count each case once)
        Map<Integer, String> caseDiseaseMap = new HashMap<>();
        // caseSymptomsMap: case_id -> set of symptoms in that case
        Map<Integer, Set<String>> caseSymptomsMap = new HashMap<>();

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT case_id, disease_name, symptom_name FROM disease_symptom_training";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int caseId = rs.getInt("case_id");
                String disease = rs.getString("disease_name");
                String symptom = rs.getString("symptom_name");

                caseDiseaseMap.put(caseId, disease);
                caseSymptomsMap.putIfAbsent(caseId, new HashSet<>());
                caseSymptomsMap.get(caseId).add(symptom);

                allSymptoms.add(symptom);
                allDiseases.add(disease);
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.err.println("❌ Error loading raw training data: " + e.getMessage());
            return false;
        }

        if (caseDiseaseMap.isEmpty()) {
            return false;
        }

        // ---- COUNTING: tally up cases per disease, and symptom occurrences per disease ----
        for (Map.Entry<Integer, String> entry : caseDiseaseMap.entrySet()) {
            int caseId = entry.getKey();
            String disease = entry.getValue();

            // Count this training case towards its disease
            diseaseCaseCount.merge(disease, 1, Integer::sum);
            totalCases++;

            // Count each symptom that appeared in this case, towards that disease
            symptomCountPerDisease.putIfAbsent(disease, new HashMap<>());
            Map<String, Integer> symptomCounts = symptomCountPerDisease.get(disease);

            Set<String> symptomsInThisCase = caseSymptomsMap.get(caseId);
            for (String symptom : symptomsInThisCase) {
                symptomCounts.merge(symptom, 1, Integer::sum);
            }
        }

        System.out.println("📊 Raw training data loaded:");
        System.out.println("   - Total training cases : " + totalCases);
        System.out.println("   - Distinct diseases     : " + allDiseases.size());
        System.out.println("   - Distinct symptoms     : " + allSymptoms.size());

        return true;
    }

    // ========================================================================
    //  STEP 2: TRAIN THE MODEL -> CALCULATE PRIOR & LIKELIHOOD FROM THE COUNTS
    // ========================================================================
    /**
     * This is where the actual Naive Bayes "learning" happens.
     * Nothing here comes pre-calculated from the database - every probability
     * is derived mathematically from the raw counts gathered in Step 1.
     */
    private void trainModel() {
        System.out.println("\n🧮 === TRAINING NAIVE BAYES MODEL (calculating probabilities) ===");

        int vocabularySize = allSymptoms.size(); // needed for Laplace smoothing denominator

        for (String disease : allDiseases) {

            // ---------- PRIOR PROBABILITY ----------
            //   P(Disease) = (number of training cases for this disease) / (total cases)
            int casesForDisease = diseaseCaseCount.getOrDefault(disease, 0);
            double prior = (double) casesForDisease / totalCases;
            diseasePrior.put(disease, prior);

            System.out.printf("   P(%s) = %d / %d = %.4f%n",
                    disease, casesForDisease, totalCases, prior);

            // ---------- LIKELIHOOD PROBABILITY (per symptom), WITH LAPLACE SMOOTHING ----------
            //   P(Symptom | Disease) = (count of this symptom in this disease's cases) + 1
            //                          -----------------------------------------------------
            //                           (total cases of this disease) + (number of distinct symptoms)
            //
            //   The "+1" / "+vocabularySize" is Laplace (add-one) smoothing: it stops the
            //   probability from ever being exactly zero just because one symptom was
            //   never seen with this disease in the training set.
            Map<String, Integer> symptomCounts = symptomCountPerDisease.getOrDefault(disease, new HashMap<>());
            Map<String, Double> likelihoods = new HashMap<>();

            for (String symptom : allSymptoms) {
                int symptomCount = symptomCounts.getOrDefault(symptom, 0);
                double likelihood = (symptomCount + LAPLACE_SMOOTHING)
                                   / (casesForDisease + LAPLACE_SMOOTHING * vocabularySize);
                likelihoods.put(symptom, likelihood);
            }

            diseaseSymptomProb.put(disease, likelihoods);
        }

        System.out.println("✅ Training complete - probabilities calculated for "
                + allDiseases.size() + " disease(s) and " + vocabularySize + " symptom(s).");
        System.out.println("===================================================================\n");
    }

    // ========================================================================
    //  STEP 3: PREDICTION - APPLY BAYES' THEOREM USING THE TRAINED PROBABILITIES
    // ========================================================================
    /**
     * Predict disease using Naive Bayes formula:
     * P(Disease|Symptoms) ∝ P(Disease) × ∏ P(Symptom|Disease)
     *
     * Calculations are done in log-space (summing logs instead of multiplying
     * raw probabilities) to avoid floating point underflow when there are
     * many symptoms - this is standard practice for Naive Bayes in Java.
     */
    public Map<String, Object> predict(String[] symptoms) {
        Map<String, Object> result = new HashMap<>();

        if (symptoms == null || symptoms.length == 0) {
            result.put("status", "error");
            result.put("message", "No symptoms provided");
            return result;
        }

        if (diseaseSymptomProb.isEmpty()) {
            result.put("status", "error");
            result.put("message", "Model has not been trained - no training data available");
            return result;
        }

        System.out.println("\n🤖 === NAIVE BAYES PREDICTION ===");
        System.out.println("Input Symptoms: " + Arrays.toString(symptoms));

        // Calculate log posterior for each disease:
        //   log P(Disease|Symptoms) = log P(Disease) + Σ log P(Symptom_i|Disease)
        Map<String, Double> logPosterior = new HashMap<>();

        for (String disease : diseaseSymptomProb.keySet()) {
            double logProb = Math.log(diseasePrior.get(disease));
            System.out.printf("   [%s] start with log P(Disease) = log(%.4f) = %.4f%n",
                    disease, diseasePrior.get(disease), logProb);

            Map<String, Double> symptomProbs = diseaseSymptomProb.get(disease);

            for (String symptom : symptoms) {
                double symptomProb;
                if (symptomProbs.containsKey(symptom)) {
                    symptomProb = symptomProbs.get(symptom);
                } else {
                    // Symptom never seen at all during training (not even for any disease) -
                    // fall back to a small smoothed probability instead of zero.
                    symptomProb = LAPLACE_SMOOTHING
                            / (diseaseCaseCount.getOrDefault(disease, 0) + LAPLACE_SMOOTHING * allSymptoms.size());
                }
                logProb += Math.log(symptomProb);
                System.out.printf("      + log P(%s|%s) = log(%.4f) = %.4f  -> running total = %.4f%n",
                        symptom, disease, symptomProb, Math.log(symptomProb), logProb);
            }

            logPosterior.put(disease, logProb);
        }

        // Find disease with maximum posterior (most probable disease)
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

        // Convert log probabilities to normalized percentages (softmax-style normalization)
        List<Map.Entry<String, Double>> sortedDiseases = new ArrayList<>(logPosterior.entrySet());
        sortedDiseases.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

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

        // Get disease info from the diseases reference table (not the training table)
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
     * Get all available symptoms (learned from training data)
     */
    public List<String> getAllSymptoms() {
        return new ArrayList<>(allSymptoms);
    }

    /**
     * Get all diseases (learned from training data)
     */
    public List<String> getAllDiseases() {
        return new ArrayList<>(diseaseSymptomProb.keySet());
    }

    /**
     * Exposes the calculated prior probability P(Disease) - useful for
     * displaying the "working" of the model (e.g. in an admin/demo screen).
     */
    public double getPrior(String disease) {
        return diseasePrior.getOrDefault(disease, 0.0);
    }

    /**
     * Exposes the calculated likelihood P(Symptom|Disease) - useful for
     * displaying the "working" of the model (e.g. in an admin/demo screen).
     */
    public double getLikelihood(String disease, String symptom) {
        Map<String, Double> probs = diseaseSymptomProb.get(disease);
        return (probs == null) ? 0.0 : probs.getOrDefault(symptom, 0.0);
    }
}