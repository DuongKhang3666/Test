package com.example.gui.components;

import javax.swing.*;
import java.awt.*;

public class PageHeader extends JPanel {
    
    public PageHeader(String title, String subTitle) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode("#0F172A"));

        JLabel subTitleLabel = new JLabel(subTitle);
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitleLabel.setForeground(Color.decode("#64748B"));

        add(titleLabel);
        add(Box.createVerticalStrut(5));
        add(subTitleLabel);
    }
}