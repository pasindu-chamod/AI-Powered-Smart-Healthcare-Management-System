package healthcare.ai;

import java.util.*;

/**
 * Demonstrates the Naive Bayes training + prediction calculation with
 * sample data, with NO database required. Run this directly to show
 * the full working of the calculation (counts -> probabilities -> prediction).
 */
public class NaiveBayesStandaloneTest {

    public static void main(String[] args) {
        List<NaiveBayesCore.TrainingCase> trainingData = new ArrayList<>();

        // ---- Sample training cases (in a real system these come from
        //      the disease_symptom_training table) ----

        // Common Cold cases
        trainingData.add(new NaiveBayesCore.TrainingCase(1, "Common Cold", "runny_nose", "sneezing", "sore_throat"));
        trainingData.add(new NaiveBayesCore.TrainingCase(2, "Common Cold", "runny_nose", "sneezing"));
        trainingData.add(new NaiveBayesCore.TrainingCase(3, "Common Cold", "sore_throat", "sneezing", "mild_fever"));
        trainingData.add(new NaiveBayesCore.TrainingCase(4, "Common Cold", "runny_nose", "sore_throat"));

        // COVID-19 cases
        trainingData.add(new NaiveBayesCore.TrainingCase(5, "COVID-19", "fever", "dry_cough", "loss_of_taste", "fatigue"));
        trainingData.add(new NaiveBayesCore.TrainingCase(6, "COVID-19", "fever", "dry_cough", "fatigue"));
        trainingData.add(new NaiveBayesCore.TrainingCase(7, "COVID-19", "loss_of_taste", "fatigue"));

        // Malaria cases
        trainingData.add(new NaiveBayesCore.TrainingCase(8, "Malaria", "high_fever", "chills", "sweating", "headache"));
        trainingData.add(new NaiveBayesCore.TrainingCase(9, "Malaria", "high_fever", "chills", "sweating"));
        trainingData.add(new NaiveBayesCore.TrainingCase(10, "Malaria", "high_fever", "headache"));

        System.out.println("================================================================");
        System.out.println(" NAIVE BAYES CLASSIFIER - STANDALONE DEMO (no database needed)");
        System.out.println("================================================================\n");

        NaiveBayesCore classifier = new NaiveBayesCore();
        classifier.train(trainingData);

        System.out.println("================================================================");
        System.out.println(" PREDICTIONS");
        System.out.println("================================================================");

        System.out.println("\n--- Test 1: Common Cold-like symptoms ---");
        Map<String, Object> r1 = classifier.predict(new String[]{"runny_nose", "sneezing", "sore_throat"});
        System.out.println("Predicted: " + r1.get("predicted_disease"));
        System.out.println("Ranking  : " + r1.get("ranking"));

        System.out.println("\n--- Test 2: COVID-19-like symptoms ---");
        Map<String, Object> r2 = classifier.predict(new String[]{"fever", "dry_cough", "loss_of_taste", "fatigue"});
        System.out.println("Predicted: " + r2.get("predicted_disease"));
        System.out.println("Ranking  : " + r2.get("ranking"));

        System.out.println("\n--- Test 3: Malaria-like symptoms ---");
        Map<String, Object> r3 = classifier.predict(new String[]{"high_fever", "chills", "sweating", "headache"});
        System.out.println("Predicted: " + r3.get("predicted_disease"));
        System.out.println("Ranking  : " + r3.get("ranking"));

        System.out.println("\n✅ Demo complete!");
    }
}