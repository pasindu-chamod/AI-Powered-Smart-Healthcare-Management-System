package healthcare.ui.auth;

import healthcare.service.AuthService;
import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    
    private JTextField nameField, emailField, phoneField, usernameField, dobField;
    private JTextField specField, licenseField;
    private JPasswordField passwordField, confirmField;
    private JComboBox<String> roleBox, genderBox, bloodBox;
    private JLabel statusLabel;
    private JPanel doctorPanel;
    private final AuthService authService = new AuthService();
    
    public RegisterFrame() {
        setTitle("Register - Healthcare System");
        setSize(680, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }
    
    private void buildUI() {
        setLayout(new BorderLayout());
        
        JPanel header = new JPanel();
        header.setBackground(new Color(46, 204, 113));
        header.setPreferredSize(new Dimension(0, 60));
        header.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        
        JLabel title = new JLabel("CREATE YOUR ACCOUNT");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title);
        
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 35, 20, 35));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Register As:"), gbc);
        gbc.gridx = 1;
        roleBox = new JComboBox<>(new String[]{"PATIENT", "DOCTOR"});
        styleCombo(roleBox);
        form.add(roleBox, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Full Name:"), gbc);
        gbc.gridx = 1;
        nameField = field();
        form.add(nameField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Email:"), gbc);
        gbc.gridx = 1;
        emailField = field();
        form.add(emailField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = field();
        form.add(phoneField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Date of Birth:"), gbc);
        gbc.gridx = 1;
        dobField = field();
        dobField.setText("YYYY-MM-DD");
        form.add(dobField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Gender:"), gbc);
        gbc.gridx = 1;
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        styleCombo(genderBox);
        form.add(genderBox, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Blood Group:"), gbc);
        gbc.gridx = 1;
        bloodBox = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
        styleCombo(bloodBox);
        form.add(bloodBox, gbc);
        row++;
        
        doctorPanel = new JPanel(new GridBagLayout());
        doctorPanel.setBackground(new Color(255, 248, 220));
        doctorPanel.setBorder(BorderFactory.createTitledBorder("Doctor Information"));
        doctorPanel.setVisible(false);
        
        GridBagConstraints dgbc = new GridBagConstraints();
        dgbc.insets = new Insets(5, 5, 5, 5);
        dgbc.fill = GridBagConstraints.HORIZONTAL;
        dgbc.weightx = 1.0;
        
        dgbc.gridx = 0; dgbc.gridy = 0;
        doctorPanel.add(label("Specialization:"), dgbc);
        dgbc.gridx = 1;
        specField = field();
        doctorPanel.add(specField, dgbc);
        
        dgbc.gridx = 0; dgbc.gridy = 1;
        doctorPanel.add(label("License Number:"), dgbc);
        dgbc.gridx = 1;
        licenseField = field();
        doctorPanel.add(licenseField, dgbc);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(doctorPanel, gbc);
        row++;
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = field();
        form.add(usernameField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        form.add(passwordField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(label("Confirm Password:"), gbc);
        gbc.gridx = 1;
        confirmField = new JPasswordField();
        styleTextField(confirmField);
        form.add(confirmField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        form.add(statusLabel, gbc);
        
        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(Color.WHITE);
        
        JButton registerBtn = new JButton("CREATE ACCOUNT");
        registerBtn.setBackground(new Color(46, 204, 113));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setPreferredSize(new Dimension(170, 38));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> handleRegister());
        
        JButton backBtn = new JButton("BACK");
        backBtn.setBackground(new Color(189, 195, 199));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setPreferredSize(new Dimension(100, 38));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> dispose());
        
        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);
        
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        
        roleBox.addActionListener(e -> {
            doctorPanel.setVisible("DOCTOR".equals(roleBox.getSelectedItem()));
            revalidate();
        });
    }
    
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }
    
    private JTextField field() {
        JTextField f = new JTextField();
        styleTextField(f);
        return f;
    }
    
    private void styleTextField(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        f.setPreferredSize(new Dimension(200, 35));
    }
    
    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(200, 35));
    }
    
    private void handleRegister() {
        String role = (String) roleBox.getSelectedItem();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String dob = dobField.getText().trim().equals("YYYY-MM-DD") ? "" : dobField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String blood = (String) bloodBox.getSelectedItem();
        
        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill all required fields!");
            return;
        }
        if (!password.equals(confirm)) {
            statusLabel.setText("Passwords do not match!");
            return;
        }
        if (password.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters!");
            return;
        }
        
        boolean success = false;
        if ("PATIENT".equals(role)) {
            success = authService.registerPatient(username, password, name, email, phone, dob, gender, blood);
        } else {
            String spec = specField.getText().trim();
            String license = licenseField.getText().trim();
            if (spec.isEmpty() || license.isEmpty()) {
                statusLabel.setText("Please fill doctor information!");
                return;
            }
            success = authService.registerDoctor(username, password, name, email, phone, spec, license);
        }
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Account created successfully!\n" +
                ("DOCTOR".equals(role) ? "Wait for admin approval." : "You can now login."),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            statusLabel.setText("Registration failed!");
        }
    }
}