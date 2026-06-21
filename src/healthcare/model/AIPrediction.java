package healthcare.model;

public class AIPrediction {
    private int predictionId;
    private int patientId;
    private String symptomsEntered;
    private String predictedDisease;
    private double confidenceScore;
    private String recommendedMeds;
    private boolean doctorReviewed;
    private String createdAt;
    
    public AIPrediction() {}
    
    public int getPredictionId() { return predictionId; }
    public void setPredictionId(int predictionId) { this.predictionId = predictionId; }
    
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    
    public String getSymptomsEntered() { return symptomsEntered; }
    public void setSymptomsEntered(String symptomsEntered) { this.symptomsEntered = symptomsEntered; }
    
    public String getPredictedDisease() { return predictedDisease; }
    public void setPredictedDisease(String predictedDisease) { this.predictedDisease = predictedDisease; }
    
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public String getRecommendedMeds() { return recommendedMeds; }
    public void setRecommendedMeds(String recommendedMeds) { this.recommendedMeds = recommendedMeds; }
    
    public boolean isDoctorReviewed() { return doctorReviewed; }
    public void setDoctorReviewed(boolean doctorReviewed) { this.doctorReviewed = doctorReviewed; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}