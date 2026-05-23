package com.example.gui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TableActionCellRender extends DefaultTableCellRenderer {
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Luôn tạo Panel mới để JTable không bị kẹt kích thước 0x0
        TableActionPanel actionPanel = new TableActionPanel();

        Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
        actionPanel.setBackground(bg);
        actionPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        
        return actionPanel;
    }
}