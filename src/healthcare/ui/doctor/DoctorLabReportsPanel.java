package healthcare.ui.doctor;

import healthcare.dao.impl.LabReportDAOImpl;
import healthcare.model.Doctor;
import healthcare.model.LabReport;
import healthcare.service.AuthService;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class DoctorLabReportsPanel extends JPanel {

    private final LabReportDAOImpl reportDAO = new LabReportDAOImpl();
    private final Doctor doctor = AuthService.getCurrentDoctor();
    private JTable table;
    private DefaultTableModel tableModel;

    public DoctorLabReportsPanel() {
        buildUI();
        loadData();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 247, 250));

        // ── Title bar ────────────────────────────────────────────────────────
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(26, 188, 156));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        JLabel title = new JLabel("LAB REPORTS");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] columns = {"ID", "Patient", "Type", "Date", "Results", "Status", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        // hide ID column (still accessible via model)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        add(scroll, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnPanel.setBackground(new Color(245, 247, 250));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 220)));

        JButton addBtn    = makeButton("+ ADD REPORT",   new Color(26, 188, 156));
        JButton updateBtn = makeButton("✎ UPDATE",        new Color(52, 152, 219));
        JButton deleteBtn = makeButton("✕ DELETE",        new Color(231, 76, 60));
        JButton refreshBtn= makeButton("↻ REFRESH",       new Color(149, 165, 166));

        addBtn.addActionListener(e -> showAddDialog());
        updateBtn.addActionListener(e -> showUpdateDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // ── Load data from DB ─────────────────────────────────────────────────────
    private void loadData() {
        tableModel.setRowCount(0);
        List<LabReport> list = (doctor != null)
                ? reportDAO.getReportsByDoctor(doctor.getDoctorId())
                : reportDAO.getAllReports();
        for (LabReport r : list) {
            tableModel.addRow(new Object[]{
                r.getReportId(), r.getPatientName(), r.getReportType(),
                r.getReportDate(), r.getResults(), r.getStatus(), r.getNotes()
            });
        }
    }

    // ── Add Report Dialog ─────────────────────────────────────────────────────
    private void showAddDialog() {
        JTextField patientIdField = new JTextField(10);
        JTextField typeField      = new JTextField(20);
        JTextArea  resultsArea    = new JTextArea(3, 20);
        JTextArea  notesArea      = new JTextArea(3, 20);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Pending","Completed","Reviewed"});
        JTextField dateField = new JTextField(LocalDate.now().toString(), 10);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addFormRow(form, gbc, row++, "Patient ID:",    patientIdField);
        addFormRow(form, gbc, row++, "Report Type:",   typeField);
        addFormRow(form, gbc, row++, "Report Date:",   dateField);
        addFormRow(form, gbc, row++, "Results:",       new JScrollPane(resultsArea));
        addFormRow(form, gbc, row++, "Status:",        statusBox);
        addFormRow(form, gbc, row++, "Notes:",         new JScrollPane(notesArea));

        int opt = JOptionPane.showConfirmDialog(this, form,
                "Add Lab Report", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opt == JOptionPane.OK_OPTION) {
            try {
                LabReport r = new LabReport();
                r.setPatientId(Integer.parseInt(patientIdField.getText().trim()));
                r.setDoctorId(doctor != null ? doctor.getDoctorId() : 1);
                r.setReportType(typeField.getText().trim());
                r.setResults(resultsArea.getText().trim());
                r.setStatus((String) statusBox.getSelectedItem());
                r.setNotes(notesArea.getText().trim());
                r.setReportDate(dateField.getText().trim());

                if (reportDAO.addReport(r)) {
                    JOptionPane.showMessageDialog(this, "Lab report added successfully!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add report.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid Patient ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Update Report Dialog ──────────────────────────────────────────────────
    private void showUpdateDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a report to update.");
            return;
        }

        int reportId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentResults = (String) tableModel.getValueAt(selectedRow, 4);
        String currentStatus  = (String) tableModel.getValueAt(selectedRow, 5);
        String currentNotes   = (String) tableModel.getValueAt(selectedRow, 6);

        JTextArea  resultsArea = new JTextArea(currentResults != null ? currentResults : "", 3, 20);
        JTextArea  notesArea   = new JTextArea(currentNotes   != null ? currentNotes   : "", 3, 20);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Pending","Completed","Reviewed"});
        statusBox.setSelectedItem(currentStatus);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addFormRow(form, gbc, row++, "Results:", new JScrollPane(resultsArea));
        addFormRow(form, gbc, row++, "Status:",  statusBox);
        addFormRow(form, gbc, row++, "Notes:",   new JScrollPane(notesArea));

        int opt = JOptionPane.showConfirmDialog(this, form,
                "Update Lab Report #" + reportId,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opt == JOptionPane.OK_OPTION) {
            LabReport r = new LabReport();
            r.setReportId(reportId);
            r.setResults(resultsArea.getText().trim());
            r.setStatus((String) statusBox.getSelectedItem());
            r.setNotes(notesArea.getText().trim());

            if (reportDAO.updateReport(r)) {
                JOptionPane.showMessageDialog(this, "Report updated successfully!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update report.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Delete selected report ────────────────────────────────────────────────
    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a report to delete.");
            return;
        }
        int reportId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete report #" + reportId + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (reportDAO.deleteReport(reportId)) {
                JOptionPane.showMessageDialog(this, "Report deleted.");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Form helper ───────────────────────────────────────────────────────────
    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                            String label, Component field) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
        gbc.fill = GridBagConstraints.NONE;
    }

    // ── Table styling ─────────────────────────────────────────────────────────
    private void styleTable(JTable t) {
        t.setRowHeight(32);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setSelectionBackground(new Color(214, 234, 248));
        t.setSelectionForeground(Color.BLACK);
        t.setGridColor(new Color(200, 210, 220));
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(1, 1));
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, focus, row, col);
                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252));
                setForeground(new Color(44, 62, 80));
                setFont(new Font("SansSerif", Font.PLAIN, 12));
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

                // Colour-code the Status column
                if (col == 5 && val != null) {
                    switch (val.toString()) {
                        case "Completed": setForeground(new Color(39, 174, 96));  break;
                        case "Reviewed":  setForeground(new Color(41, 128, 185)); break;
                        case "Pending":   setForeground(new Color(211, 84, 0));   break;
                    }
                }
                return this;
            }
        };
        t.setDefaultRenderer(Object.class, renderer);

        Color headerBg = new Color(26, 188, 156);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(val != null ? val.toString() : "");
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                lbl.setBackground(headerBg);
                lbl.setForeground(Color.WHITE);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return lbl;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);

        JTableHeader h = t.getTableHeader();
        h.setPreferredSize(new Dimension(0, 40));
        h.setReorderingAllowed(false);
    }

    // ── Button factory ────────────────────────────────────────────────────────
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
}