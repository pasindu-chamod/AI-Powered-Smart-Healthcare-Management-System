package healthcare.ui.auth;

import healthcare.service.AuthService;
import healthcare.ui.admin.AdminDashboard;
import healthcare.ui.doctor.DoctorDashboard;
import healthcare.ui.patient.PatientDashboard;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JLabel statusLabel;
    private final AuthService authService = new AuthService();
    private BufferedImage sideImage;

    public LoginFrame() {
        setTitle("Healthcare System - Login");
        setSize(950, 580);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        loadSideImage();
        buildUI();
    }

    /**
     * Loads the doctor photo used as the background of the left panel.
     * The image lives at resources/doctor.jpg (project root, alongside src/).
     * If the file is missing, sideImage stays null and the panel falls
     * back to the plain blue gradient so the app never crashes.
     */
    private void loadSideImage() {
        try {
            File imageFile = new File("resources/doctor.jpg");
            if (imageFile.exists()) {
                sideImage = ImageIO.read(imageFile);
            }
        } catch (Exception e) {
            System.err.println("Could not load resources/doctor.jpg: " + e.getMessage());
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // LEFT PANEL
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                if (sideImage != null) {
                    // Draw the doctor photo scaled to cover the whole panel
                    // (cover behavior: scale up + crop, so it never stretches/distorts)
                    int panelW = getWidth();
                    int panelH = getHeight();
                    int imgW = sideImage.getWidth();
                    int imgH = sideImage.getHeight();

                    double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
                    int drawW = (int) (imgW * scale);
                    int drawH = (int) (imgH * scale);
                    int drawX = (panelW - drawW) / 2;
                    int drawY = (panelH - drawH) / 2;

                    g2.drawImage(sideImage, drawX, drawY, drawW, drawH, null);

                    // Blue tint overlay on top of the photo (semi-transparent gradient)
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(20, 60, 110, 175),
                        0, panelH, new Color(30, 100, 160, 195)
                    );
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, panelW, panelH);
                } else {
                    // Fallback: plain blue gradient if the image failed to load
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(41, 128, 185),
                        0, getHeight(), new Color(109, 213, 250)
                    );
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        leftPanel.setPreferredSize(new Dimension(420, 580));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(80, 40, 80, 40));

        JLabel logoLabel = new JLabel("HEALTHCARE", SwingConstants.CENTER);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel systemLabel = new JLabel("MANAGEMENT SYSTEM", SwingConstants.CENTER);
        systemLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        systemLabel.setForeground(new Color(255, 255, 255, 230));
        systemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] features = {
            "> AI Disease Prediction",
            "> Smart Appointments",
            "> Digital Prescriptions",
            "> Lab Report Management",
            "> Medicine Database",
            "> Multi-User System"
        };

        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setOpaque(false);
        featuresPanel.setBorder(BorderFactory.createEmptyBorder(35, 0, 0, 0));

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            featureLabel.setForeground(Color.WHITE);
            featureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            featuresPanel.add(featureLabel);
            featuresPanel.add(Box.createVerticalStrut(10));
        }

        leftPanel.add(logoLabel);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(systemLabel);
        leftPanel.add(featuresPanel);

        // RIGHT PANEL
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        JLabel loginTitle = new JLabel("Welcome Back!");
        loginTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        loginTitle.setForeground(new Color(44, 62, 80));
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loginSubtitle = new JLabel("Sign in to continue to your account");
        loginSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        loginSubtitle.setForeground(new Color(127, 140, 141));
        loginSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("LOGIN AS");
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        roleLabel.setForeground(new Color(52, 73, 94));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleBox = new JComboBox<>(new String[]{"PATIENT", "DOCTOR", "ADMIN"});
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        roleBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roleBox.setBackground(Color.WHITE);
        roleBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        roleBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        userLabel.setForeground(new Color(52, 73, 94));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLabel.setForeground(new Color(52, 73, 94));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.addActionListener(e -> handleLogin());

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(231, 76, 60));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginBtn.setBackground(new Color(52, 152, 219));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleLogin());
        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(52, 152, 219));
            }
        });

        JButton registerBtn = new JButton("CREATE NEW ACCOUNT");
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        registerBtn.setBackground(Color.WHITE);
        registerBtn.setForeground(new Color(46, 204, 113));
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> new RegisterFrame().setVisible(true));
        registerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(new Color(46, 204, 113));
                registerBtn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(Color.WHITE);
                registerBtn.setForeground(new Color(46, 204, 113));
            }
        });

       /*  JLabel testInfo = new JLabel("<html><center>"
            + "<b>Test Accounts:</b><br>"
            + "Patient: patient1 / patient123<br>"
            + "Doctor: doctor1 / doctor123<br>"
            + "Admin: admin / admin123"
            + "</center></html>");
        testInfo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        testInfo.setForeground(new Color(150, 150, 150));
        testInfo.setAlignmentX(Component.LEFT_ALIGNMENT);*/

        rightPanel.add(loginTitle);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(loginSubtitle);
        rightPanel.add(Box.createVerticalStrut(30));
        rightPanel.add(roleLabel);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(roleBox);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(userLabel);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(usernameField);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(passLabel);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(passwordField);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(statusLabel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(loginBtn);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(registerBtn);
        rightPanel.add(Box.createVerticalStrut(12));
       // rightPanel.add(testInfo);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty()) {
            statusLabel.setText("Please enter username.");
            return;
        }

        if (password.isEmpty()) {
            statusLabel.setText("Please enter password.");
            return;
        }

        boolean success = authService.login(username, password, role);

        if (!success) {
            String error = authService.getLastError();

            if (error == null || error.trim().isEmpty()) {
                error = "Login failed.";
            }

            statusLabel.setText(error);
            JOptionPane.showMessageDialog(
                    this,
                    error,
                    "Login Failed",
                    JOptionPane.WARNING_MESSAGE
            );

            passwordField.setText("");
            return;
        }

        try {
            JFrame dashboard = null;

            if ("PATIENT".equals(role)) {
                dashboard = new PatientDashboard();
            } else if ("DOCTOR".equals(role)) {
                dashboard = new DoctorDashboard();
            } else if ("ADMIN".equals(role)) {
                dashboard = new AdminDashboard();
            }

            if (dashboard != null) {
                dashboard.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Unknown role selected.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Login success, but dashboard could not open.\n\nError:\n" + ex.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}