package healthcare.ui.patient;

import healthcare.dao.DoctorDAO;
import healthcare.dao.impl.DoctorDAOImpl;
import healthcare.model.*;
import healthcare.service.AppointmentService;
import healthcare.service.AuthService;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class BookAppointmentPanel extends JPanel {
    
    private final Patient patient = AuthService.getCurrentPatient();
    private final DoctorDAO doctorDAO = new DoctorDAOImpl();
    private final AppointmentService service = new AppointmentService();
    private JComboBox<String> doctorBox;
    private JTextField dateField, timeField;
    private JTextArea reasonArea;
    private List<Doctor> doctors;
    
    public BookAppointmentPanel() {
        buildUI();
        loadDoctors();
    }
    
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(155, 89, 182));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        JLabel title = new JLabel("BOOK APPOINTMENT");
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
        
        addLabel(form, gbc, row, "Select Doctor:");
        gbc.gridx = 1;
        doctorBox = new JComboBox<>();
        doctorBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        doctorBox.setPreferredSize(new Dimension(300, 38));
        form.add(doctorBox, gbc);
        row++;
        
        addLabel(form, gbc, row, "Date (YYYY-MM-DD):");
        gbc.gridx = 1;
        dateField = styledField();
        form.add(dateField, gbc);
        row++;
        
        addLabel(form, gbc, row, "Time (HH:MM):");
        gbc.gridx = 1;
        timeField = styledField();
        form.add(timeField, gbc);
        row++;
        
        addLabel(form, gbc, row, "Reason:");
        gbc.gridx = 1;
        reasonArea = new JTextArea(4, 25);
        reasonArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        reasonArea.setLineWrap(true);
        form.add(new JScrollPane(reasonArea), gbc);
        row++;
        
        JButton bookBtn = new JButton("BOOK APPOINTMENT");
        bookBtn.setBackground(new Color(46, 204, 113));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        bookBtn.setFocusPainted(false);
        bookBtn.setBorderPainted(false);
        bookBtn.setPreferredSize(new Dimension(200, 38));
        bookBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bookBtn.addActionListener(e -> bookAppointment());
        gbc.gridx = 1; gbc.gridy = row;
        form.add(bookBtn, gbc);
        
        add(form, BorderLayout.CENTER);
    }
    
    private void addLabel(JPanel panel, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(44, 62, 80));
        panel.add(lbl, gbc);
    }
    
    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        f.setPreferredSize(new Dimension(300, 38));
        return f;
    }
    
    private void loadDoctors() {
        doctors = doctorDAO.getApprovedDoctors();
        doctorBox.removeAllItems();
        for (Doctor d : doctors) {
            doctorBox.addItem(d.getFullName() + " - " + d.getSpecialization());
        }
    }
    
   private void bookAppointment() {
    if (doctorBox.getSelectedIndex() < 0) {
        JOptionPane.showMessageDialog(this, "Please select a doctor!");
        return;
    }
    if (dateField.getText().trim().isEmpty() || !dateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
        JOptionPane.showMessageDialog(this, "Please enter a valid date (YYYY-MM-DD)!");
        return;
    }
    if (timeField.getText().trim().isEmpty() || !timeField.getText().matches("\\d{2}:\\d{2}")) {
        JOptionPane.showMessageDialog(this, "Please enter a valid time (HH:MM)!");
        return;
    }
    if (reasonArea.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a reason!");
        return;
    }

    Doctor doc = doctors.get(doctorBox.getSelectedIndex());
    Appointment apt = new Appointment();
    apt.setPatientId(patient.getPatientId());
    apt.setDoctorId(doc.getDoctorId());
    apt.setAppointmentDate(dateField.getText().trim());
    apt.setAppointmentTime(timeField.getText().trim() + ":00");
    apt.setReason(reasonArea.getText().trim());

    if (service.bookAppointment(apt)) {
        JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
        dateField.setText(""); timeField.setText(""); reasonArea.setText("");
    } else {
        JOptionPane.showMessageDialog(this, "Booking failed!");
    }
}
}