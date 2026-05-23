package com.example.gui.components;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.net.URL;

public class NormalItem extends JButton {
    
    public NormalItem(String name, String pathIcon) {
        
        setHorizontalAlignment(SwingConstants.LEFT);
        setMargin(new Insets(0, 30, 0, 0));

        // pathIcon có thể null từ DB, bỏ qua icon để tránh crash lúc đăng nhập.
        if (pathIcon != null && !pathIcon.isBlank()) {
            URL icon = getClass().getResource(pathIcon);
            if (icon != null) {
                ImageIcon imgIcon = new ImageIcon(icon);
                setIcon(imgIcon);
            } else {
                System.err.println("Không tìm thấy icon tại: " + pathIcon);
            }
        }

        setText(name == null ? "" : name);

        setIconTextGap(20);

        // Thiết lập style cho button
        // Trong class NormalItem.java, thay thế dòng putClientProperty bằng:
        putClientProperty("FlatLaf.style", 
            "arc: 15; " +
            "background: #0F4C8100; " +           // Trong suốt khi bình thường
            "foreground: #FFFFFF; " +
            "hoverBackground: #3E5C76; " +        // Chuyển sang SECONDARY khi di chuột
            "pressedBackground: #0F4C81; " +      // Chuyển sang PRIMARY khi bấm
            "borderWidth: 0; focusWidth: 0; margin: 4,15,4,15;"
        );
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setBorderPainted(false);
        setPreferredSize(new Dimension(200, 40));
    }
}
