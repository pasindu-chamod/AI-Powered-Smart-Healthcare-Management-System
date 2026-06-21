package healthcare.ui.auth;

import healthcare.service.AuthService;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class RegisterFrame extends JFrame {
    
    private JTextField nameField, emailField, phoneField, usernameField, dobField;
    private JTextField specField, licenseField;
    private JPasswordField passwordField, confirmField;
    private JComboBox<String> roleBox, genderBox, bloodBox;
    private JLabel statusLabel;
    private JPanel doctorPanel;
    private final AuthService authService = new AuthService();
    private BufferedImage headerImage;
    
    public RegisterFrame() {
        setTitle("Register - Healthcare System");
        setSize(680, 760);
        setLocationRelativeTo(null);
        setResizable(false);
        loadHeaderImage();
        buildUI();
    }

    /**
     * Loads the doctor photo used as the register page header background.
     * Same source image as LoginFrame (resources/doctor.jpg). Falls back
     * to the plain green header if the file can't be found.
     */
    private void loadHeaderImage() {
        try {
            File imageFile = new File("resources/doctor.jpg");
            if (imageFile.exists()) {
                headerImage = ImageIO.read(imageFile);
            }
        } catch (Exception e) {
            System.err.println("Could not load resources/doctor.jpg: " + e.getMessage());
        }
    }
    
    private void buildUI() {
        setLayout(new BorderLayout());
        
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int panelW = getWidth();
                int panelH = getHeight();

                if (headerImage != null) {
                    // Cover-fit: scale the photo to fill the header, cropping overflow
                    int imgW = headerImage.getWidth();
                    int imgH = headerImage.getHeight();
                    double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
                    int drawW = (int) (imgW * scale);
                    int drawH = (int) (imgH * scale);
                    int drawX = (panelW - drawW) / 2;
                    int drawY = (panelH - drawH) / 2;
                    g2.drawImage(headerImage, drawX, drawY, drawW, drawH, null);

                    // Green-blue tint overlay so the white title text stays readable
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(20, 90, 70, 180),
                        panelW, panelH, new Color(20, 70, 110, 190)
                    );
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, panelW, panelH);
                } else {
                    // Fallback: plain green background if the image failed to load
                    g2.setColor(new Color(46, 204, 113));
                    g2.fillRect(0, 0, panelW, panelH);
                }
            }
        };
        header.setPreferredSize(new Dimension(0, 130));
        header.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 50));
        
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