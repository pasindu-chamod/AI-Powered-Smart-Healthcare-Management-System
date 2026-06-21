package healthcaresystem;

import healthcare.ui.auth.LoginFrame;
import healthcare.util.DatabaseConnection;
import javax.swing.*;

public class HealthcareSystem {
    
    public static void main(String[] args) {
        // Test database connection
        System.out.println("═══════════════════════════════════════");
        System.out.println("    🏥 HEALTHCARE SYSTEM v1.0");
        System.out.println("═══════════════════════════════════════\n");
        
        try {
            DatabaseConnection.getInstance().getConnection();
            System.out.println("✅ System initialized successfully!\n");
            
            // Set Look and Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("⚠ Using default Look and Feel");
            }
            
            // Launch Login Frame
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
            
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR: System failed to start!");
            System.err.println("Please check:");
            System.err.println("1. XAMPP MySQL is running");
            System.err.println("2. Database 'healthcare_db' exists");
            System.err.println("3. MySQL password is correct in DatabaseConnection.java");
            e.printStackTrace();
        }
    }
}