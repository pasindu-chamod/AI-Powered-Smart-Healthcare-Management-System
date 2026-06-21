package healthcare.dao.impl;

import healthcare.dao.MedicineDAO;
import healthcare.model.Medicine;
import healthcare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAOImpl implements MedicineDAO {
    
    @Override
    public List<Medicine> getAllMedicines() {
        List<Medicine> medicines = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM medicines ORDER BY medicine_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                medicines.add(extractMedicine(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return medicines;
    }
    
    @Override
    public List<Medicine> getMedicinesByDisease(int diseaseId) {
        List<Medicine> medicines = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM medicines WHERE disease_id = ? AND is_available = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, diseaseId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                medicines.add(extractMedicine(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return medicines;
    }
    
    @Override
    public boolean addMedicine(Medicine medicine) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO medicines (medicine_name, disease_id, dosage) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, medicine.getMedicineName());
            pstmt.setInt(2, medicine.getDiseaseId());
            pstmt.setString(3, medicine.getDosage());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean deleteMedicine(int medicineId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "DELETE FROM medicines WHERE medicine_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, medicineId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private Medicine extractMedicine(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setMedicineId(rs.getInt("medicine_id"));
        medicine.setMedicineName(rs.getString("medicine_name"));
        medicine.setDiseaseId(rs.getInt("disease_id"));
        medicine.setDosage(rs.getString("dosage"));
        medicine.setAvailable(rs.getBoolean("is_available"));
        medicine.setCreatedAt(rs.getString("created_at"));
        return medicine;
    }
}