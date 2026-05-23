package com.example.gui.components;

import com.example.gui.events.TableActionEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TableActionPanel extends JPanel {
    private final JPanel inner;
    private JButton btnView;
    private JButton btnEdit;
    private JButton btnDelete;

    public TableActionPanel() {
        setLayout(new GridBagLayout());
        setOpaque(true);

        inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        inner.setOpaque(false);

        btnView = createActionButton("Xem", "/assets/images/info.png");
        btnEdit = createActionButton("Sửa", "/assets/images/edit.png");
        btnDelete = createActionButton("Xóa", "/assets/images/delete.png");

        inner.add(btnView);
        inner.add(btnEdit);
        inner.add(btnDelete);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(inner, gbc);
    }

    // Gắn sự kiện click
    public void initEvent(TableActionEvent event, int row) {
        btnView.addActionListener(e -> event.onView(row));
        btnEdit.addActionListener(e -> event.onEdit(row));
        btnDelete.addActionListener(e -> event.onDelete(row));
    }

    private JButton createActionButton(String text, String iconPath) {
        JButton button = new JButton();
        ImageIcon icon = loadBlackIcon(iconPath, 16, 16);
        if (icon != null) {
            button.setIcon(icon);
        } else {
            button.setText(text);
        }
        button.setPreferredSize(new Dimension(26, 26));
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.BLACK);
        button.setCursor(Cursor.getDefaultCursor());
        return button;
    }

    private ImageIcon loadBlackIcon(String resourcePath, int width, int height) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return null;
        }

        ImageIcon baseIcon = new ImageIcon(resource);
        Image scaled = baseIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaled);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(scaledIcon.getImage(), 0, 0, null);
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, width, height);
        g2.dispose();
        return new ImageIcon(image);
    }
}