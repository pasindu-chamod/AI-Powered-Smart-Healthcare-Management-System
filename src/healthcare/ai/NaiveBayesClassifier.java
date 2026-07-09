package healthcare.ai;

import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.*;


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

    
    //  STEP 1: LOAD RAW TRAINING DATA AND COUNT (no probabilities yet - just counts)
   
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

        // -- COUNTING: tally up cases per disease, and symptom occurrences per disease --
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

   
    //   TRAIN THE MODEL 
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

        // Human-readable trace of the actual Bayes-theorem working, built in the
        // exact classic textbook style: P(X|Ci) = product of P(symptom|Ci), then
        // compare P(X|Ci) x P(Ci) across all classes and pick the largest one.
        // (Direct multiplication - not log-space - so it matches how it's taught
        // and worked by hand in the course material.)
        StringBuilder trace = new StringBuilder();
        trace.append("Naive Bayes Classifier - Maximum A Posteriori (MAP) rule:\n");
        trace.append("Predict the class Ci that maximizes P(X|Ci) x P(Ci)\n");
        trace.append("where P(X|Ci) = P(symptom_1|Ci) x P(symptom_2|Ci) x ... (class-conditional independence)\n\n");

        // Direct-multiplication score for each disease (this is what actually
        // gets compared and reported - the classic P(X|Ci)P(Ci) style).
        Map<String, Double> diseaseScore = new HashMap<>();
        // Also keep log-scores internally as a numerically-safe tie breaker for
        // ranking when raw products underflow to 0.0 with many symptoms.
        Map<String, Double> logPosterior = new HashMap<>();

        for (String disease : diseaseSymptomProb.keySet()) {
            double prior = diseasePrior.get(disease);
            double logProb = Math.log(prior);

            double likelihoodProduct = 1.0;
            StringBuilder factorLine = new StringBuilder();
            factorLine.append(String.format("P(X|%s) = ", disease));

            Map<String, Double> symptomProbs = diseaseSymptomProb.get(disease);

            for (int i = 0; i < symptoms.length; i++) {
                String symptom = symptoms[i];
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
                likelihoodProduct *= symptomProb;

                factorLine.append(String.format("P(%s|%s)", symptom, disease));
                factorLine.append(i < symptoms.length - 1 ? " x " : "");
            }

            double score = prior * likelihoodProduct;
            diseaseScore.put(disease, score);
            logPosterior.put(disease, logProb);

            trace.append(String.format("[%s]%n", disease));
            trace.append(String.format("  %s%n", factorLine));
            trace.append(String.format("       = %.6f%n", likelihoodProduct));
            trace.append(String.format("  P(X|%s) x P(%s) = %.6f x %.4f = %.8f%n%n",
                    disease, disease, likelihoodProduct, prior, score));

            System.out.printf("   [%s] P(X|Disease)=%.6f  P(Disease)=%.4f  Score=%.8f%n",
                    disease, likelihoodProduct, prior, score);
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

        // Classic textbook conclusion line: compare the P(X|Ci) x P(Ci) scores
        // directly and declare the single winning class - this is the exact
        // "Since P(X|No)P(No) > P(X|Yes)P(Yes) => Class = No" style used in
        // the course material.
        List<Map.Entry<String, Double>> scoreRanking = new ArrayList<>(diseaseScore.entrySet());
        scoreRanking.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        trace.append("----------------------------------------\n");
        trace.append("Comparing P(X|Ci) x P(Ci) across all classes:\n");
        for (Map.Entry<String, Double> entry : scoreRanking) {
            trace.append(String.format("  %s : %.8f%n", entry.getKey(), entry.getValue()));
        }
        trace.append(String.format("%nSince P(X|%s)P(%s) is the largest value among all classes,%n",
                predictedDisease, predictedDisease));
        trace.append(String.format("the Naive Bayes classifier predicts:  Disease = %s%n", predictedDisease));

        // Convert log probabilities to normalized percentages (softmax-style normalization)
        List<Map.Entry<String, Double>> sortedDiseases = new ArrayList<>(logPosterior.entrySet());
        sortedDiseases.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        double sumExp = 0;
        for (Map.Entry<String, Double> entry : sortedDiseases) {
            sumExp += Math.exp(entry.getValue() - maxLogProb);
        }

        double confidence = (Math.exp(maxLogProb - maxLogProb) / sumExp) * 100;

        // Full posterior probability distribution - EVERY disease the model
        // knows about, each with its own calculated probability (not just the
        // top guess). This is the real Bayes-theorem output: P(Disease|Symptoms)
        // for every class, which is what the ML syllabus expects to see.
        List<String> allPredictions = new ArrayList<>();
        Map<String, Double> allProbabilities = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : sortedDiseases) {
            double prob = (Math.exp(entry.getValue() - maxLogProb) / sumExp) * 100;
            allPredictions.add(entry.getKey() + " (" + String.format("%.2f%%", prob) + ")");
            allProbabilities.put(entry.getKey(), prob);
        }

        // Top 3 predictions
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
        result.put("all_predictions", allPredictions);
        result.put("all_probabilities", allProbabilities);
        result.put("calculation_trace", trace.toString());
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