package com.example.gui.components;

import java.awt.Dimension;
import java.util.List;
import javax.swing.JComboBox;

public class ComboBox<T> extends JComboBox<T> {

    @SuppressWarnings("unchecked")
    public ComboBox(String[] options) {
        super();
        if (options != null) {
            for (String option : options) {
                this.addItem((T) option);
            }
        }
        initStyle();
    }

    // Constructor Generic mới: dùng cho cả TermDTO, SubjectDTO...
    // defaultOptionName chưa được sử dụng
    public ComboBox(List<T> items, String defaultOptionName) {
        super();
        if (items != null) {
            for (T item : items) {
                this.addItem(item);
            }
        }
        initStyle();
    }

    private void initStyle() {
        setPreferredSize(new Dimension(250, 40));
        putClientProperty("FlatLaf.style", "arc: 15;");
    }
}