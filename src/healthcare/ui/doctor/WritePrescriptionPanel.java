package healthcare.ui.doctor;

import healthcare.dao.*;
import healthcare.dao.impl.*;
import healthcare.model.*;
import healthcare.service.AuthService;
import healthcare.service.PrescriptionService;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WritePrescriptionPanel extends JPanel {
    
    private final Doctor doctor = AuthService.getCurrentDoctor();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final DiseaseDAO diseaseDAO = new DiseaseDAOImpl();
    private final PrescriptionService service = new PrescriptionService();
    private JComboBox<String> patientBox, diseaseBox;
    private JTextArea diagnosisArea;
    private List<Patient> patients;
    private List<Disease> diseases;
    
    public WritePrescriptionPanel() {
        buildUI();
        loadData();
    }
    
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(243, 156, 18));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        JLabel title = new JLabel("WRITE PRESCRIPTION");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);
        
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        int row = 0;
        
        addLabel(form, gbc, row, "Select Patient:");
        gbc.gridx = 1;
        patientBox = new JComboBox<>();
        patientBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        patientBox.setPreferredSize(new Dimension(300, 38));
        form.add(patientBox, gbc);
        row++;
        
        addLabel(form, gbc, row, "Select Disease:");
        gbc.gridx = 1;
        diseaseBox = new JComboBox<>();
        diseaseBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        diseaseBox.setPreferredSize(new Dimension(300, 38));
        form.add(diseaseBox, gbc);
        row++;
        
        addLabel(form, gbc, row, "Diagnosis Notes:");
        gbc.gridx = 1;
        diagnosisArea = new JTextArea(5, 25);
        diagnosisArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        diagnosisArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        diagnosisArea.setLineWrap(true);
        form.add(new JScrollPane(diagnosisArea), gbc);
        row++;
        
        JButton prescribeBtn = new JButton("CREATE PRESCRIPTION");
        prescribeBtn.setBackground(new Color(46, 204, 113));
        prescribeBtn.setForeground(Color.WHITE);
        prescribeBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        prescribeBtn.setFocusPainted(false);
        prescribeBtn.setBorderPainted(false);
        prescribeBtn.setPreferredSize(new Dimension(200, 38));
        prescribeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        prescribeBtn.addActionListener(e -> createPrescription());
        gbc.gridx = 1; gbc.gridy = row;
        form.add(prescribeBtn, gbc);
        
        add(form, BorderLayout.CENTER);
    }
    
    private void addLabel(JPanel panel, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(44, 62, 80));
        panel.add(lbl, gbc);
    }
    
    private void loadData() {
        patients = patientDAO.getAllPatients();
        patientBox.removeAllItems();
        for (Patient p : patients) patientBox.addItem(p.getFullName() + " (ID:" + p.getPatientId() + ")");
        
        diseases = diseaseDAO.getAllDiseases();
        diseaseBox.removeAllItems();
        for (Disease d : diseases) diseaseBox.addItem(d.getDiseaseName());
    }
    
    private void createPrescription() {
        if (patientBox.getSelectedIndex() < 0 || diseaseBox.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Select patient and disease!");
            return;
        }
        Prescription p = new Prescription();
        p.setPatientId(patients.get(patientBox.getSelectedIndex()).getPatientId());
        p.setDoctorId(doctor.getDoctorId());
        p.setDiseaseId(diseases.get(diseaseBox.getSelectedIndex()).getDiseaseId());
        p.setDiagnosisNotes(diagnosisArea.getText());
        
        if (service.addPrescription(p)) {
            JOptionPane.showMessageDialog(this, "Prescription created!");
            diagnosisArea.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed!");
        }
    }
}