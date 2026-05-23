package com.example.gui.components;

import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatClientProperties;

public class TextBox extends JTextField {
    // empty constructor
    public TextBox() {

    }

    // component 8: search box
    public TextBox(String placeHolder, Icon icon) {
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeHolder);
        putClientProperty(FlatClientProperties.STYLE, "arc: 15;");

        // dùng để tùy chỉnh icon
        if (icon != null) {
            putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON, icon);
        } else {
            // Demo:dùng icon mặc định của Swing để test khi ko có icon
            putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON, UIManager.getIcon("Tree.searchIcon"));
        }
        setPreferredSize(new Dimension(250, 40));
    }

    public TextBox(String placeHolder) {
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeHolder);
        putClientProperty(FlatClientProperties.STYLE, "arc: 15;");
        setPreferredSize(new Dimension(250, 40));
    }

    // tùy chỉnh icon
    public void setIcon(Icon icon) {
        putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON, icon);
    }

    // //component 15: dropdown box (old version)
    public TextBox(String placeHolder, JComboBox<?> comboBox) {
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeHolder);
        putClientProperty(FlatClientProperties.STYLE, "arc: 15;"); // Bo tròn 15px

        if (comboBox != null) {
            // Gắn ComboBox vào vị trí trailing (đuôi)
            putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, comboBox);
            comboBox.putClientProperty(FlatClientProperties.STYLE, "borderWidth: 0; arrowType: chevron;");
        }
    }

}
