package healthcare.ui.doctor;

import healthcare.model.Doctor;
import healthcare.service.AuthService;
import healthcare.ui.auth.LoginFrame;
import java.awt.*;
import javax.swing.*;

public class DoctorDashboard extends JFrame {

    private Doctor doctor;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;

    public DoctorDashboard() {
        doctor = AuthService.getCurrentDoctor();
        if (doctor == null) {
            doctor = new Doctor();
            doctor.setDoctorId(0);
            doctor.setFullName("Doctor");
            doctor.setSpecialization("General");
            doctor.setLicenseNumber("N/A");
        }
        setTitle("Doctor Portal - Dr. " + doctor.getFullName());
        setSize(1150, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(22, 160, 133));
        topBar.setPreferredSize(new Dimension(0, 60));
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        JLabel titleLabel = new JLabel("DOCTOR PORTAL");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightSection.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Dr. " + doctor.getFullName() + " | " + doctor.getSpecialization());
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        welcomeLabel.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setPreferredSize(new Dimension(100, 35));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setOpaque(true);
        logoutBtn.addActionListener(e -> {
            new AuthService().logout();
            dispose();
            new LoginFrame().setVisible(true);
        });

        rightSection.add(welcomeLabel);
        rightSection.add(logoutBtn);
        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(rightSection, BorderLayout.EAST);

        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(22, 160, 133));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        String[][] items = {
            {"DASHBOARD", "home"},
            {"APPOINTMENTS", "appointments"},
            {"PATIENT RECORDS", "patients"},
            {"WRITE PRESCRIPTION", "prescribe"},
            {"AI PREDICTIONS", "ai"},
            {"LAB REPORTS", "lab"}
        };

        for (String[] item : items) {
            JButton btn = sidebarBtn(item[0], item[1]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }
        sidebar.add(Box.createVerticalGlue());

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(245, 247, 250));

        contentPanel.add(homePanel(), "home");
        contentPanel.add(new DoctorAppointmentsPanel(), "appointments");
        contentPanel.add(new PatientRecordsPanel(), "patients");
        contentPanel.add(new WritePrescriptionPanel(), "prescribe");
        contentPanel.add(new ViewAIPredictionsPanel(), "ai");
        contentPanel.add(new DoctorLabReportsPanel(), "lab");

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "home");
    }

    private JButton sidebarBtn(String text, String card) {
        JButton btn = new JButton("  " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = 18;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText().trim(), x, y);
            }
        };
        btn.setMaximumSize(new Dimension(210, 42));
        btn.setBackground(new Color(22, 160, 133));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(210, 42));

        btn.addActionListener(e -> {
            if (activeButton != null) activeButton.setBackground(new Color(22, 160, 133));
            btn.setBackground(new Color(15, 130, 110));
            activeButton = btn;
            btn.repaint();
            cardLayout.show(contentPanel, card);
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(new Color(30, 175, 145));
                    btn.repaint();
                }
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(new Color(22, 160, 133));
                    btn.repaint();
                }
            }
        });
        return btn;
    }

    private JPanel homePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);

        JLabel title = new JLabel("Welcome, Dr. " + doctor.getFullName());
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(44, 62, 80));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel(doctor.getSpecialization() + " | License: " + doctor.getLicenseNumber());
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(new Color(120, 140, 160));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel stats = new JPanel(new GridLayout(1, 4, 15, 0));
        stats.setBackground(Color.WHITE);
        stats.setMaximumSize(new Dimension(800, 120));
        stats.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);

        stats.add(statCard("APPOINTMENTS", "8", new Color(22, 160, 133)));
        stats.add(statCard("PATIENTS", "24", new Color(52, 152, 219)));
        stats.add(statCard("PRESCRIPTIONS", "12", new Color(155, 89, 182)));
        stats.add(statCard("LAB REPORTS", "5", new Color(243, 156, 18)));

        center.add(title);
        center.add(Box.createVerticalStrut(10));
        center.add(sub);
        center.add(stats);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel statCard(String label, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("SansSerif", Font.BOLD, 32));
        val.setForeground(Color.WHITE);
        val.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(new Color(255, 255, 255, 220));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(val);
        card.add(Box.createVerticalStrut(5));
        card.add(lbl);
        return card;
    }
}