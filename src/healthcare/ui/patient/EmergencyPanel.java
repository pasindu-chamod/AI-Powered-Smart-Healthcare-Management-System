package healthcare.ui.patient;

import java.awt.*;
import javax.swing.*;

public class EmergencyPanel extends JPanel {
    
    public EmergencyPanel() {
        buildUI();
    }
    
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(231, 76, 60));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        JLabel title = new JLabel("EMERGENCY CONTACTS");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);
        
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        center.add(contactCard("EMERGENCY HELPLINE", "911", new Color(231, 76, 60)));
        center.add(Box.createVerticalStrut(15));
        center.add(contactCard("AMBULANCE", "108", new Color(243, 156, 18)));
        center.add(Box.createVerticalStrut(15));
        center.add(contactCard("HOSPITAL HELPLINE", "1234567890", new Color(52, 152, 219)));
        center.add(Box.createVerticalStrut(15));
        center.add(contactCard("POISON CONTROL", "1-800-222-1222", new Color(155, 89, 182)));
        
        add(center, BorderLayout.CENTER);
    }
    
    private JPanel contactCard(String name, String number, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setMaximumSize(new Dimension(500, 90));
        card.setPreferredSize(new Dimension(500, 90));
        card.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        
        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        numberLabel.setForeground(Color.WHITE);
        
        card.add(nameLabel, BorderLayout.NORTH);
        card.add(numberLabel, BorderLayout.CENTER);
        
        return card;
    }
}