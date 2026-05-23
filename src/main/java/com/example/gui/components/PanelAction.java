package com.example.gui.components;

import javax.swing.*;
import java.awt.*;
import com.example.gui.events.TableActionEvent;


public class PanelAction extends JPanel {
    private JButton btnEdit;
    private JButton btnDelete;

    public PanelAction() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        setOpaque(true);

        btnEdit = new JButton("Edit"); 
        btnDelete = new JButton("Delete");

        // Trang trí nút theo chuẩn FlatLaf cho "sang"
        btnEdit.putClientProperty("FlatLaf.style", "arc: 10; margin: 2,8,2,8; background: #3B82F6; foreground: #FFFFFF; borderWidth: 0;");
        btnDelete.putClientProperty("FlatLaf.style", "arc: 10; margin: 2,8,2,8; background: #EF4444; foreground: #FFFFFF; borderWidth: 0;");

        // Chống viền khi focus
        btnEdit.setFocusPainted(false);
        btnDelete.setFocusPainted(false);

        add(btnEdit);
        add(btnDelete);
    }

    public void initEvent(TableActionEvent event, int row) {
        btnEdit.addActionListener(e -> event.onEdit(row));
        btnDelete.addActionListener(e -> event.onDelete(row));
    }
}