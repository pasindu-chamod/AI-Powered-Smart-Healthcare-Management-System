package healthcare.ui.patient;

import healthcare.dao.PatientDAO;
import healthcare.dao.impl.PatientDAOImpl;
import healthcare.model.Patient;
import healthcare.service.AuthService;
import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {
    
    private final Patient patient = AuthService.getCurrentPatient();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private JTextField nameField, emailField, phoneField, dobField, addressField;
    private JComboBox<String> genderBox, bloodBox;
    
    public ProfilePanel() {
        buildUI();
        loadData();
    }
    
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(46, 204, 113));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        JLabel title = new JLabel("MY PROFILE");
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
        addField(form, gbc, row++, "Full Name:", nameField = styledField());
        addField(form, gbc, row++, "Email:", emailField = styledField());
        addField(form, gbc, row++, "Phone:", phoneField = styledField());
        addField(form, gbc, row++, "Date of Birth:", dobField = styledField());
        
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        genderBox.setPreferredSize(new Dimension(250, 38));
        addField(form, gbc, row++, "Gender:", genderBox);
        
        bloodBox = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
        bloodBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bloodBox.setPreferredSize(new Dimension(250, 38));
        addField(form, gbc, row++, "Blood Group:", bloodBox);
        
        addField(form, gbc, row++, "Address:", addressField = styledField());
        
        JButton updateBtn = new JButton("UPDATE PROFILE");
        updateBtn.setBackground(new Color(46, 204, 113));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        updateBtn.setFocusPainted(false);
        updateBtn.setBorderPainted(false);
        updateBtn.setPreferredSize(new Dimension(180, 38));
        updateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateBtn.addActionListener(e -> updateProfile());
        gbc.gridx = 1; gbc.gridy = row;
        form.add(updateBtn, gbc);
        
        add(form, BorderLayout.CENTER);
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(44, 62, 80));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
    
    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        f.setPreferredSize(new Dimension(250, 38));
        return f;
    }
    
    private void loadData() {
        nameField.setText(patient.getFullName());
        emailField.setText(patient.getEmail());
        phoneField.setText(patient.getPhone());
        dobField.setText(patient.getDateOfBirth());
        genderBox.setSelectedItem(patient.getGender());
        bloodBox.setSelectedItem(patient.getBloodGroup());
        addressField.setText(patient.getAddress());
    }
    
    private void updateProfile() {
        patient.setFullName(nameField.getText());
        patient.setEmail(emailField.getText());
        patient.setPhone(phoneField.getText());
        patient.setDateOfBirth(dobField.getText());
        patient.setGender((String) genderBox.getSelectedItem());
        patient.setBloodGroup((String) bloodBox.getSelectedItem());
        patient.setAddress(addressField.getText());
        
        if (patientDAO.updatePatient(patient)) {
            JOptionPane.showMessageDialog(this, "Profile updated!");
        } else {
            JOptionPane.showMessageDialog(this, "Update failed!");
        }
    }
}