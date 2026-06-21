package healthcare.ai;

import java.util.*;

/**
 * ============================================================================
 *  STANDALONE NAIVE BAYES CORE - NO DATABASE REQUIRED
 * ============================================================================
 *  This class contains the EXACT same training + prediction calculation as
 *  NaiveBayesClassifier, but takes raw training data directly as a Java
 *  List instead of reading it from MySQL. This is useful for:
 *
 *    1. Demonstrating the calculation to your lecturer without needing
 *       XAMPP/MySQL running.
 *    2. Unit testing the math in isolation.
 *
 *  The real classifier used by the application (NaiveBayesClassifier.java)
 *  loads training cases from the `disease_symptom_training` table and does
 *  the identical counting + probability calculation shown here.
 * ============================================================================
 */
public class NaiveBayesCore {

    /** One row of raw training data: a single observed case */
    public static class TrainingCase {
        int caseId;
        String disease;
        Set<String> symptoms;

        public TrainingCase(int caseId, String disease, String... symptoms) {
            this.caseId = caseId;
            this.disease = disease;
            this.symptoms = new HashSet<>(Arrays.asList(symptoms));
        }
    }

    private Map<String, Integer> diseaseCaseCount = new HashMap<>();
    private Map<String, Map<String, Integer>> symptomCountPerDisease = new HashMap<>();
    private int totalCases = 0;

    private Map<String, Double> diseasePrior = new HashMap<>();
    private Map<String, Map<String, Double>> diseaseSymptomProb = new HashMap<>();

    private Set<String> allSymptoms = new HashSet<>();
    private Set<String> allDiseases = new HashSet<>();

    private static final double LAPLACE_SMOOTHING = 1.0;

    // ------------------------------------------------------------------
    //  STEP 1: COUNT raw training cases
    // ------------------------------------------------------------------
    public void train(List<TrainingCase> trainingData) {
        for (TrainingCase tc : trainingData) {
            diseaseCaseCount.merge(tc.disease, 1, Integer::sum);
            totalCases++;
            allDiseases.add(tc.disease);

            symptomCountPerDisease.putIfAbsent(tc.disease, new HashMap<>());
            Map<String, Integer> symptomCounts = symptomCountPerDisease.get(tc.disease);

            for (String symptom : tc.symptoms) {
                symptomCounts.merge(symptom, 1, Integer::sum);
                allSymptoms.add(symptom);
            }
        }

        System.out.println("📊 Training data summary:");
        System.out.println("   - Total cases   : " + totalCases);
        System.out.println("   - Diseases      : " + allDiseases);
        System.out.println("   - Symptoms      : " + allSymptoms);
        System.out.println();

        calculateProbabilities();
    }

    // ------------------------------------------------------------------
    //  STEP 2: CALCULATE prior P(Disease) and likelihood P(Symptom|Disease)
    // ------------------------------------------------------------------
    private void calculateProbabilities() {
        System.out.println("🧮 === CALCULATING PROBABILITIES ===");
        int vocabularySize = allSymptoms.size();

        for (String disease : allDiseases) {
            int casesForDisease = diseaseCaseCount.getOrDefault(disease, 0);
            double prior = (double) casesForDisease / totalCases;
            diseasePrior.put(disease, prior);

            System.out.printf("%nDisease: %s%n", disease);
            System.out.printf("  Prior  P(%s) = %d / %d = %.4f%n", disease, casesForDisease, totalCases, prior);

            Map<String, Integer> symptomCounts = symptomCountPerDisease.getOrDefault(disease, new HashMap<>());
            Map<String, Double> likelihoods = new HashMap<>();

            for (String symptom : allSymptoms) {
                int symptomCount = symptomCounts.getOrDefault(symptom, 0);
                double likelihood = (symptomCount + LAPLACE_SMOOTHING)
                                   / (casesForDisease + LAPLACE_SMOOTHING * vocabularySize);
                likelihoods.put(symptom, likelihood);

                if (symptomCount > 0) {
                    System.out.printf("  P(%s | %s) = (%d + 1) / (%d + %d) = %.4f%n",
                            symptom, disease, symptomCount, casesForDisease, vocabularySize, likelihood);
                }
            }
            diseaseSymptomProb.put(disease, likelihoods);
        }
        System.out.println("\n✅ Training complete.\n");
    }

    // ------------------------------------------------------------------
    //  STEP 3: PREDICT using Bayes' Theorem
    // ------------------------------------------------------------------
    public Map<String, Object> predict(String[] symptoms) {
        Map<String, Object> result = new HashMap<>();
        System.out.println("🤖 Predicting for symptoms: " + Arrays.toString(symptoms));

        Map<String, Double> logPosterior = new HashMap<>();

        for (String disease : diseaseSymptomProb.keySet()) {
            double logProb = Math.log(diseasePrior.get(disease));
            Map<String, Double> symptomProbs = diseaseSymptomProb.get(disease);

            for (String symptom : symptoms) {
                double p = symptomProbs.containsKey(symptom)
                        ? symptomProbs.get(symptom)
                        : LAPLACE_SMOOTHING / (diseaseCaseCount.getOrDefault(disease, 0) + LAPLACE_SMOOTHING * allSymptoms.size());
                logProb += Math.log(p);
            }
            logPosterior.put(disease, logProb);
        }

        String predicted = null;
        double maxLog = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, Double> e : logPosterior.entrySet()) {
            if (e.getValue() > maxLog) {
                maxLog = e.getValue();
                predicted = e.getKey();
            }
        }

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(logPosterior.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        double sumExp = 0;
        for (Map.Entry<String, Double> e : sorted) sumExp += Math.exp(e.getValue() - maxLog);

        List<String> ranked = new ArrayList<>();
        for (Map.Entry<String, Double> e : sorted) {
            double pct = (Math.exp(e.getValue() - maxLog) / sumExp) * 100;
            ranked.add(e.getKey() + " (" + String.format("%.1f%%", pct) + ")");
        }

        result.put("predicted_disease", predicted);
        result.put("ranking", ranked);
        return result;
    }
}