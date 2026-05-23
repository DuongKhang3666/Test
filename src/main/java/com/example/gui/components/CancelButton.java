package com.example.gui.components;

import javax.swing.*;
import java.awt.*;

public class CancelButton extends JButton {

    public CancelButton(String text) {
        super(text);

        putClientProperty("FlatLaf.style",
                "background: #FFFFFF; " +
                "foreground: #111827; " + // Chữ đen
                "hoverBackground: #F3F4F6; " + // Hover xám nhạt
                "borderColor: #D1D5DB; borderWidth: 1; " + // Thêm viền xám
                "arc: 8; focusWidth: 0; font: 14"
        );

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(120, 40));
    }
}
