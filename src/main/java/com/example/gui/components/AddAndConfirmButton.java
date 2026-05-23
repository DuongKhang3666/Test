package com.example.gui.components;

import javax.swing.*;
import java.awt.*;

public class AddAndConfirmButton extends JButton {

    public AddAndConfirmButton(String text) {
        super(text);

        putClientProperty("FlatLaf.style",
                "background: #0F4C81; " + // Xanh Navy
                "foreground: #FFFFFF; " +
                "hoverBackground: #0B3A64; " + 
                "arc: 8; borderWidth: 0; focusWidth: 0; font: bold 14"
        );

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(150, 40));
    }
}