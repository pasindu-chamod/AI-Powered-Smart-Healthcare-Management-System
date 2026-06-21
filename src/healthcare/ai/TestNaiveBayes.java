package healthcare.ai;

import java.util.Map;

/**
 * Test class for Naive Bayes Classifier (DATABASE VERSION)
 * Requires MySQL/XAMPP running with the `disease_symptom_training` table populated.
 * See NaiveBayesStandaloneTest.java for a version that needs no database.
 */
public class TestNaiveBayes {
    
    public static void main(String[] args) {
        System.out.println("🧪 TESTING NAIVE BAYES CLASSIFIER\n");
        
        NaiveBayesClassifier classifier = new NaiveBayesClassifier();
        
        // Test Case 1: Common Cold
        System.out.println("📋 Test 1: Common Cold Symptoms");
        String[] symptoms1 = {"runny_nose", "sneezing", "sore_throat"};
        Map<String, Object> result1 = classifier.predict(symptoms1);
        printResult(result1);
        
        // Test Case 2: COVID-19
        System.out.println("\n📋 Test 2: COVID-19 Symptoms");
        String[] symptoms2 = {"fever", "dry_cough", "loss_of_taste", "fatigue"};
        Map<String, Object> result2 = classifier.predict(symptoms2);
        printResult(result2);
        
        // Test Case 3: Malaria
        System.out.println("\n📋 Test 3: Malaria Symptoms");
        String[] symptoms3 = {"high_fever", "chills", "sweating", "headache"};
        Map<String, Object> result3 = classifier.predict(symptoms3);
        printResult(result3);
        
        System.out.println("\n✅ All tests completed!");
    }
    
    private static void printResult(Map<String, Object> result) {
        if ("success".equals(result.get("status"))) {
            System.out.println("Predicted Disease: " + result.get("predicted_disease"));
            System.out.println("Confidence: " + String.format("%.2f%%", result.get("confidence")));
            System.out.println("Top 3: " + result.get("top_3_predictions"));
        } else {
            System.out.println("Error: " + result.get("message"));
        }
    }
}