package healthcare.ui.patient;

import healthcare.model.Appointment;
import healthcare.model.Patient;
import healthcare.service.AppointmentService;
import healthcare.service.AuthService;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class MyAppointmentsPanel extends JPanel {

    private final Patient patient = AuthService.getCurrentPatient();
    private final AppointmentService service = new AppointmentService();
    private JTable table;
    private DefaultTableModel tableModel;

    public MyAppointmentsPanel() {
        buildUI();
        loadData();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(52, 152, 219));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        JLabel title = new JLabel("MY APPOINTMENTS");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Doctor", "Date", "Time", "Reason", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table, new Color(52, 152, 219));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220), 1));
        add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = makeButton("REFRESH", new Color(52, 152, 219));
        JButton cancelBtn = makeButton("CANCEL", new Color(231, 76, 60));
        refreshBtn.addActionListener(e -> loadData());
        cancelBtn.addActionListener(e -> cancelAppointment());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 220)));
        btnPanel.add(refreshBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void styleTable(JTable t, Color headerBg) {
    t.setRowHeight(32);
    t.setFont(new Font("SansSerif", Font.PLAIN, 12));
    t.setSelectionBackground(new Color(214, 234, 248));
    t.setSelectionForeground(Color.BLACK);
    t.setGridColor(new Color(200, 210, 220));
    t.setShowGrid(true);
    t.setIntercellSpacing(new Dimension(1, 1));
    t.setFillsViewportHeight(true);
    t.setBackground(Color.WHITE);

    DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(tbl, val, sel, focus, row, col);
            if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252));
            setForeground(new Color(44, 62, 80));
            setFont(new Font("SansSerif", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            return this;
        }
    };
    t.setDefaultRenderer(Object.class, r);

    // FIX: Header with BLACK text on colored background
    DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val,
                boolean sel, boolean focus, int row, int col) {
            JLabel label = new JLabel(val.toString());
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            label.setBackground(headerBg);
            label.setForeground(Color.BLACK);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return label;
        }
    };
    for (int i = 0; i < t.getColumnCount(); i++) {
        t.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
    }

    JTableHeader h = t.getTableHeader();
    h.setPreferredSize(new Dimension(0, 40));
    h.setReorderingAllowed(false);
}

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(150, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Appointment> list = service.getForPatient(patient.getPatientId());
        for (Appointment a : list) {
            tableModel.addRow(new Object[]{
                a.getAppointmentId(), a.getDoctorName(), a.getAppointmentDate(),
                a.getAppointmentTime(), a.getReason(), a.getStatus()
            });
        }
    }

    private void cancelAppointment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel this appointment?");
        if (confirm == JOptionPane.YES_OPTION) {
            service.cancel((int) tableModel.getValueAt(row, 0));
            JOptionPane.showMessageDialog(this, "Appointment cancelled!");
            loadData();
        }
    }
}