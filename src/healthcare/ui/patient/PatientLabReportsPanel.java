package healthcare.ui.patient;

import healthcare.dao.impl.LabReportDAOImpl;
import healthcare.model.LabReport;
import healthcare.model.Patient;
import healthcare.service.AuthService;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class PatientLabReportsPanel extends JPanel {

    private final LabReportDAOImpl reportDAO = new LabReportDAOImpl();
    private final Patient patient = AuthService.getCurrentPatient();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea detailArea;

    public PatientLabReportsPanel() {
        buildUI();
        loadData();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 247, 250));

        // ── Title bar ─────────────────────────────────────────────────────────
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(142, 68, 173));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        JLabel title = new JLabel("MY LAB REPORTS");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("Click a report to view details");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(220, 200, 255));
        titlePanel.add(sub, BorderLayout.EAST);
        add(titlePanel, BorderLayout.NORTH);

        // ── Split: table (top) + detail area (bottom) ─────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(280);
        split.setResizeWeight(0.6);
        split.setBorder(null);

        // Table
        String[] columns = {"ID", "Report Type", "Date", "Status", "Doctor"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        // hide ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Click to load details
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetails();
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        split.setTopComponent(tableScroll);

        // Detail panel
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 15, 15, 15),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            )
        ));

        JLabel detailTitle = new JLabel("Report Details");
        detailTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        detailTitle.setForeground(new Color(142, 68, 173));
        detailTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        detailPanel.add(detailTitle, BorderLayout.NORTH);

        detailArea = new JTextArea("Select a report from the table above to view full details.");
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailArea.setBackground(Color.WHITE);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setMargin(new Insets(5, 5, 5, 5));
        detailPanel.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        split.setBottomComponent(detailPanel);

        add(split, BorderLayout.CENTER);

        // Refresh button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        btnPanel.setBackground(new Color(245, 247, 250));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 220)));
        JButton refreshBtn = makeButton("↻ REFRESH", new Color(142, 68, 173));
        refreshBtn.addActionListener(e -> loadData());
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        detailArea.setText("Select a report from the table above to view full details.");
        if (patient == null) return;
        List<LabReport> list = reportDAO.getReportsByPatient(patient.getPatientId());
        for (LabReport r : list) {
            tableModel.addRow(new Object[]{
                r.getReportId(), r.getReportType(),
                r.getReportDate(), r.getStatus(), r.getDoctorName()
            });
        }
    }

    private void showDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return;

        int    reportId   = (int)    tableModel.getValueAt(selectedRow, 0);
        String type       = (String) tableModel.getValueAt(selectedRow, 1);
        String date       = (String) tableModel.getValueAt(selectedRow, 2);
        String status     = (String) tableModel.getValueAt(selectedRow, 3);
        String doctor     = (String) tableModel.getValueAt(selectedRow, 4);

        // Find full details from DB
        List<LabReport> all = reportDAO.getReportsByPatient(patient.getPatientId());
        for (LabReport r : all) {
            if (r.getReportId() == reportId) {
                String text =
                    "Report ID   : " + r.getReportId()   + "\n" +
                    "Type        : " + r.getReportType()  + "\n" +
                    "Date        : " + r.getReportDate()  + "\n" +
                    "Doctor      : " + r.getDoctorName()  + "\n" +
                    "Status      : " + r.getStatus()      + "\n\n" +
                    "Results\n" +
                    "───────────────────────────────────\n" +
                    (r.getResults() != null ? r.getResults() : "No results recorded yet.") + "\n\n" +
                    "Notes\n" +
                    "───────────────────────────────────\n" +
                    (r.getNotes() != null ? r.getNotes() : "No notes.");
                detailArea.setText(text);
                detailArea.setCaretPosition(0);
                return;
            }
        }
    }

    private void styleTable(JTable t) {
        t.setRowHeight(32);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setSelectionBackground(new Color(225, 190, 255));
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
                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 245, 255));
                setForeground(new Color(44, 62, 80));
                setFont(new Font("SansSerif", Font.PLAIN, 12));
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                if (col == 3 && val != null) {
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

        Color headerBg = new Color(142, 68, 173);
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