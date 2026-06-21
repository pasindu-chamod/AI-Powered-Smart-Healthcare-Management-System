package healthcare.dao.impl;

import healthcare.dao.AIPredictionDAO;
import healthcare.model.AIPrediction;
import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AIPredictionDAOImpl implements AIPredictionDAO {
    
    @Override
    public boolean savePrediction(AIPrediction prediction) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO ai_predictions (patient_id, symptoms_entered, predicted_disease, confidence_score, recommended_meds) " +
                          "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, prediction.getPatientId());
            pstmt.setString(2, prediction.getSymptomsEntered());
            pstmt.setString(3, prediction.getPredictedDisease());
            pstmt.setDouble(4, prediction.getConfidenceScore());
            pstmt.setString(5, prediction.getRecommendedMeds());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public List<AIPrediction> getAllPredictions() {
        List<AIPrediction> predictions = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM ai_predictions ORDER BY created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                predictions.add(extractPrediction(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return predictions;
    }
    
    @Override
    public List<AIPrediction> getPredictionsByPatient(int patientId) {
        List<AIPrediction> predictions = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM ai_predictions WHERE patient_id = ? ORDER BY created_at DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                predictions.add(extractPrediction(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return predictions;
    }
    
    @Override
    public boolean markReviewed(int predictionId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE ai_predictions SET doctor_reviewed = TRUE WHERE prediction_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, predictionId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private AIPrediction extractPrediction(ResultSet rs) throws SQLException {
        AIPrediction prediction = new AIPrediction();
        prediction.setPredictionId(rs.getInt("prediction_id"));
        prediction.setPatientId(rs.getInt("patient_id"));
        prediction.setSymptomsEntered(rs.getString("symptoms_entered"));
        prediction.setPredictedDisease(rs.getString("predicted_disease"));
        prediction.setConfidenceScore(rs.getDouble("confidence_score"));
        prediction.setRecommendedMeds(rs.getString("recommended_meds"));
        prediction.setDoctorReviewed(rs.getBoolean("doctor_reviewed"));
        prediction.setCreatedAt(rs.getString("created_at"));
        return prediction;
    }
}