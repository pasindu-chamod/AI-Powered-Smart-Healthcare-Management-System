package healthcare.ui.admin;

import healthcare.dao.impl.*;
import java.awt.*;
import javax.swing.*;

public class AdminOverviewPanel extends JPanel {
    
    public AdminOverviewPanel() {
        buildUI();
    }
    
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(52, 152, 219));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        JLabel title = new JLabel("SYSTEM OVERVIEW");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);
        
        JPanel grid = new JPanel(new GridLayout(2, 3, 15, 15));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        int patients = 0, doctors = 0, appointments = 0, prescriptions = 0, aiPredictions = 0, labReports = 0;
        try {
            patients = new PatientDAOImpl().getTotalCount();
            doctors = new DoctorDAOImpl().getTotalCount();
            appointments = new AppointmentDAOImpl().getTotalCount();
            // These three DAOs don't have a dedicated count query, so use the
            // size of the full list they already expose - same real numbers,
            // no new DB round trip needed.
            prescriptions = new PrescriptionDAOImpl().getAllPrescriptions().size();
            aiPredictions = new AIPredictionDAOImpl().getAllPredictions().size();
            labReports = new LabReportDAOImpl().getAllReports().size();
        } catch (Exception e) {}
        
        grid.add(bigCard("TOTAL PATIENTS", String.valueOf(patients), new Color(52, 152, 219)));
        grid.add(bigCard("TOTAL DOCTORS", String.valueOf(doctors), new Color(46, 204, 113)));
        grid.add(bigCard("APPOINTMENTS", String.valueOf(appointments), new Color(155, 89, 182)));
        grid.add(bigCard("PRESCRIPTIONS", String.valueOf(prescriptions), new Color(243, 156, 18)));
        grid.add(bigCard("AI PREDICTIONS", String.valueOf(aiPredictions), new Color(231, 76, 60)));
        grid.add(bigCard("LAB REPORTS", String.valueOf(labReports), new Color(26, 188, 156)));
        
        add(grid, BorderLayout.CENTER);
    }
    
    private JPanel bigCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 40));
        val.setForeground(Color.WHITE);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(new Color(255, 255, 255, 200));
        
        card.add(val, BorderLayout.WEST);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }
}