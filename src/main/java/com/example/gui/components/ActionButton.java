package com.example.gui.components;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.Cursor;
import java.awt.Dimension;
import java.net.URL;

public class ActionButton extends JButton {
    
    // Type: "PRIMARY" (Xanh), "TERTIARY" (Nâu), "DANGER" (Đỏ)
    public ActionButton(String text, String iconPath, String type) {
        super(text); // Gán chữ cho nút (có thể truyền null hoặc "" nếu chỉ muốn hiện icon)
        
        // Tải và gắn icon (nếu có)
        if (iconPath != null && !iconPath.trim().isEmpty()) {
            ImageIcon icon = loadIcon(iconPath);
            if (icon != null) {
                // Tự động thu nhỏ icon cho vừa với nút
                java.awt.Image img = icon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(img));
                setIconTextGap(8); // Khoảng cách giữa icon và chữ
            }
        }
        
        if (text == null || text.trim().isEmpty()) {
            setPreferredSize(new Dimension(36, 36));
        } else {
            setPreferredSize(new Dimension(getPreferredSize().width + 20, 36)); 
        }
        
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        
        // --- XỬ LÝ MÀU SẮC THEO TYPE ---
        String bgColor = "#0F4C81";       // PRIMARY mặc định
        String hoverColor = "#3E5C76";    // Xanh xám khi di chuột
        String pressedColor = "#0A3153";  // Xanh đậm khi bấm
        
        if ("TERTIARY".equalsIgnoreCase(type)) {
            bgColor = "#743B00";
            hoverColor = "#8E4C06";
            pressedColor = "#542A00";
        } else if ("DANGER".equalsIgnoreCase(type)) {
            bgColor = "#C92A2A";
            hoverColor = "#E03131";
            pressedColor = "#A61E1E";
        }
        
        putClientProperty("FlatLaf.style", 
            "arc: 8; " +
            "background: " + bgColor + "; " +
            "foreground: #FFFFFF; " +
            "hoverBackground: " + hoverColor + "; " +
            "pressedBackground: " + pressedColor + "; " +
            "font: bold 12; " +
            "borderWidth: 0; focusWidth: 0;"
        );
    }

    private ImageIcon loadIcon(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url);
            }
        } catch (Exception e) {
            System.err.println("Không thể tải icon tại: " + path);
        }
        return null;
    }
}