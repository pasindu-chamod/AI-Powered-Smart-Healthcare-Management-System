package healthcare.ui.patient;

import healthcare.ai.AIPredictor;
import healthcare.dao.AIPredictionDAO;
import healthcare.dao.impl.AIPredictionDAOImpl;
import healthcare.model.AIPrediction;
import healthcare.model.Patient;
import healthcare.service.AuthService;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class AISymptomPanel extends JPanel {

    private final Patient patient = AuthService.getCurrentPatient();
    private final AIPredictor aiPredictor = new AIPredictor();
    private final AIPredictionDAO predictionDAO = new AIPredictionDAOImpl();
    private final List<JToggleButton> symptomButtons = new ArrayList<>();
    private JTextArea resultArea;
    private DefaultTableModel historyModel;
    private JLabel selectedCountLabel;
    private String lastCalculationTrace = "Run a prediction first to see the Bayes' Theorem calculation steps.";

    public AISymptomPanel() {
        buildUI();
        loadHistory();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 247, 250));

        // TITLE BAR
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(44, 62, 80));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        JLabel title = new JLabel("AI DISEASE PREDICTION SYSTEM");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.WEST);

        selectedCountLabel = new JLabel("Selected: 0 symptoms");
        selectedCountLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        selectedCountLabel.setForeground(new Color(155, 89, 182));
        titlePanel.add(selectedCountLabel, BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        // MAIN CONTENT
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(new Color(245, 247, 250));
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // LEFT: Symptoms Selection Card
        JPanel symptomsCard = new JPanel(new BorderLayout(0, 10));
        symptomsCard.setBackground(Color.WHITE);
        symptomsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Header
        JLabel selectTitle = new JLabel("SELECT YOUR SYMPTOMS");
        selectTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        selectTitle.setForeground(new Color(44, 62, 80));
        symptomsCard.add(selectTitle, BorderLayout.NORTH);

        // Symptom buttons in wrap layout - sorted alphabetically, compact size so
        // every symptom learned from the database is visible without huge tiles
        java.util.List<String> allSymptomsList = new ArrayList<>(aiPredictor.getAllSymptoms());
        java.util.Collections.sort(allSymptomsList);

        JPanel buttonGrid = new JPanel(new java.awt.GridLayout(0, 6, 6, 6));
        buttonGrid.setBackground(Color.WHITE);

        List<String> allSymptoms = allSymptomsList;
        for (String symptom : allSymptoms) {
            String displayName = symptom.replace("_", " ").toUpperCase();
            JToggleButton btn = new JToggleButton(displayName);
            btn.setFont(new Font("SansSerif", Font.BOLD, 9));
            btn.setBackground(new Color(235, 240, 248));
            btn.setForeground(new Color(44, 62, 80));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(4, 3, 4, 3)
            ));
            btn.setPreferredSize(new Dimension(95, 26));
            btn.setToolTipText(displayName);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.addItemListener(e -> {
                if (btn.isSelected()) {
                    btn.setBackground(new Color(155, 89, 182));
                    btn.setForeground(Color.WHITE);
                    btn.setBorder(BorderFactory.createLineBorder(new Color(155, 89, 182), 2));
                } else {
                    btn.setBackground(new Color(235, 240, 248));
                    btn.setForeground(new Color(44, 62, 80));
                    btn.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
                }
                updateSelectedCount();
            });

            symptomButtons.add(btn);
            buttonGrid.add(btn);
        }

        JScrollPane symptomScroll = new JScrollPane(buttonGrid);
        symptomScroll.setBorder(null);
        symptomScroll.getVerticalScrollBar().setUnitIncrement(16);
        symptomScroll.setPreferredSize(new Dimension(0, 380));
        symptomsCard.add(symptomScroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton predictBtn = createButton("  RUN AI PREDICTION  ", new Color(155, 89, 182), new Color(130, 70, 160));
        predictBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        predictBtn.setPreferredSize(new Dimension(220, 42));
        predictBtn.addActionListener(e -> runPrediction());

        JButton clearBtn = createButton("  CLEAR ALL  ", new Color(189, 195, 199), new Color(160, 170, 180));
        clearBtn.setPreferredSize(new Dimension(140, 42));
        clearBtn.addActionListener(e -> clearAll());

        JButton showWorkBtn = createButton("  SHOW CALCULATION  ", new Color(52, 152, 219), new Color(41, 128, 185));
        showWorkBtn.setPreferredSize(new Dimension(190, 42));
        showWorkBtn.addActionListener(e -> showCalculation());

        btnPanel.add(predictBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(showWorkBtn);
        symptomsCard.add(btnPanel, BorderLayout.SOUTH);

        // RIGHT PANEL: Results + History
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        rightPanel.setBackground(new Color(245, 247, 250));

        // Result Card
        JPanel resultCard = new JPanel(new BorderLayout());
        resultCard.setBackground(Color.WHITE);
        resultCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel resultTitle = new JLabel("PREDICTION RESULT");
        resultTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        resultTitle.setForeground(new Color(44, 62, 80));
        resultTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        resultCard.add(resultTitle, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBackground(new Color(248, 250, 252));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setText("Select symptoms and click RUN AI PREDICTION to get results.");
        resultCard.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // History Card
        JPanel historyCard = new JPanel(new BorderLayout());
        historyCard.setBackground(Color.WHITE);
        historyCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel historyTitle = new JLabel("PREDICTION HISTORY");
        historyTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        historyTitle.setForeground(new Color(44, 62, 80));
        historyTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        historyCard.add(historyTitle, BorderLayout.NORTH);

        String[] cols = {"Date", "Disease", "Confidence"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = new JTable(historyModel);
        styleSmallTable(historyTable);
        historyCard.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        rightPanel.add(resultCard);
        rightPanel.add(historyCard);

        mainContent.add(symptomsCard, BorderLayout.CENTER);
        mainContent.add(rightPanel, BorderLayout.EAST);

        add(mainContent, BorderLayout.CENTER);
    }

    private void updateSelectedCount() {
        int count = 0;
        for (JToggleButton btn : symptomButtons) {
            if (btn.isSelected()) count++;
        }
        selectedCountLabel.setText("Selected: " + count + " symptoms");
    }

    private void styleSmallTable(JTable t) {
        t.setRowHeight(26);
        t.setFont(new Font("SansSerif", Font.PLAIN, 11));
        t.setSelectionBackground(new Color(214, 234, 248));
        t.setGridColor(new Color(220, 225, 230));
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(1, 1));
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, focus, row, col);
                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                setForeground(new Color(44, 62, 80));
                setFont(new Font("SansSerif", Font.PLAIN, 11));
                setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                return this;
            }
        };
        t.setDefaultRenderer(Object.class, r);

        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("SansSerif", Font.BOLD, 11));
        h.setBackground(new Color(44, 62, 80));
        h.setForeground(Color.BLACK);
        h.setPreferredSize(new Dimension(0, 32));
        h.setReorderingAllowed(false);

        // Force header text color
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel label = new JLabel(val.toString());
                label.setFont(new Font("SansSerif", Font.BOLD, 11));
                label.setBackground(new Color(44, 62, 80));
                label.setForeground(Color.BLACK);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return label;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }

    private JButton createButton(String text, Color bg, Color hoverBg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hoverBg);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private void runPrediction() {
        List<String> symptoms = new ArrayList<>();
        for (JToggleButton btn : symptomButtons) {
            if (btn.isSelected()) {
                symptoms.add(btn.getText().replace(" ", "_").toLowerCase());
            }
        }
        if (symptoms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one symptom!");
            return;
        }

        resultArea.setText("Analyzing " + symptoms.size() + " symptoms...\nPlease wait...");

        Map<String, Object> result = aiPredictor.predict(symptoms.toArray(new String[0]));

        if ("success".equals(result.get("status"))) {
            String disease = (String) result.get("predicted_disease");
            double confidence = (Double) result.get("confidence");
            String description = (String) result.get("description");
            List<String> medicines = (List<String>) result.get("medicines");
            lastCalculationTrace = (String) result.get("calculation_trace");

            StringBuilder sb = new StringBuilder();
            sb.append("========================================\n");
            sb.append("        AI PREDICTION RESULT\n");
            sb.append("========================================\n\n");
            sb.append("Naive Bayes Classifier (MAP rule):\n");
            sb.append("Predicts the single class that maximizes\n");
            sb.append("P(X|Disease) x P(Disease)\n\n");
            sb.append(">>> Predicted Disease: ").append(disease).append(" <<<\n\n");
            sb.append("Description:\n  ").append(description).append("\n\n");
            sb.append("Recommended Medicines:\n");
            for (String med : medicines) sb.append("  - ").append(med).append("\n");

            sb.append("\n(Relative confidence among candidates: ")
              .append(String.format("%.2f%%", confidence)).append(")\n");
            sb.append("\nClick 'SHOW CALCULATION' to see the full\n");
            sb.append("P(X|Ci) x P(Ci) working for every disease\n");
            sb.append("compared, exactly as taught in the course.\n");

            sb.append("\nIMPORTANT: Please consult a doctor!\n");
            resultArea.setText(sb.toString());
            resultArea.setCaretPosition(0);

            AIPrediction pred = new AIPrediction();
            pred.setPatientId(patient.getPatientId());
            pred.setSymptomsEntered(String.join(", ", symptoms));
            pred.setPredictedDisease(disease);
            pred.setConfidenceScore(confidence);
            pred.setRecommendedMeds(String.join(", ", medicines));
            predictionDAO.savePrediction(pred);
            loadHistory();
        } else {
            resultArea.setText("No disease matched your symptoms.\nPlease consult a doctor.");
        }
    }

    /**
     * Shows the raw Bayes' Theorem working (Prior probability and Likelihood
     * for each symptom, per disease) used to reach the last prediction -
     * demonstrates that this is a genuine probability-based classifier and
     * not a single hardcoded lookup answer.
     */
    private void showCalculation() {
        JTextArea traceArea = new JTextArea(lastCalculationTrace);
        traceArea.setEditable(false);
        traceArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        traceArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(traceArea);
        scroll.setPreferredSize(new Dimension(650, 500));
        JOptionPane.showMessageDialog(this, scroll,
                "Naive Bayes Calculation Steps", JOptionPane.PLAIN_MESSAGE);
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        List<AIPrediction> list = predictionDAO.getPredictionsByPatient(patient.getPatientId());
        for (AIPrediction p : list) {
            historyModel.addRow(new Object[]{
                p.getCreatedAt(), p.getPredictedDisease(),
                String.format("%.1f%%", p.getConfidenceScore())
            });
        }
    }

    private void clearAll() {
        for (JToggleButton btn : symptomButtons) {
            btn.setSelected(false);
            btn.setBackground(new Color(235, 240, 248));
            btn.setForeground(new Color(44, 62, 80));
            btn.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        }
        resultArea.setText("Select symptoms and click RUN AI PREDICTION to get results.");
        updateSelectedCount();
    }
}