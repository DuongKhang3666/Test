package com.example.gui.components;

import javax.swing.*;
import  java.awt.*;

public class Details extends JPanel {
    private JLabel lbattribute;
//    private JTextField txInput;
    //constructor chỉ truyền string
    public Details(String attribute, String input) {
        this(attribute, new JTextField(input));
    }

    //constructor có thể truyền cả component khác
    // public Details(String attribute, JComponent inputComponent) {
    //     setLayout(new FlowLayout(FlowLayout.LEFT,10,5));
    //     setOpaque(false);

    //     lbattribute = new JLabel(attribute);
    //     lbattribute.setForeground(new Color(0,0,0));
    //     lbattribute.setFont(new Font("SansSerif", Font.BOLD, 14));
    //     //set size để cố định label
    //     lbattribute.setPreferredSize(new Dimension(200, 30));
    //     //set size cho component chung
    //     inputComponent.setPreferredSize(new Dimension(500, 30));
    //     add(lbattribute);
    //     add(inputComponent);
    // }

    public Details(String attribute, JComponent inputComponent) {
        setLayout(new BorderLayout(10, 0)); // 10px khoảng cách giữa Label và Input
        setOpaque(false);

        lbattribute = new JLabel(attribute);
        lbattribute.setForeground(Color.decode("#475569")); // Màu xám đậm
        lbattribute.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Chữ nhỏ lại một chút
        lbattribute.setPreferredSize(new Dimension(120, 30)); // Cố định chiều rộng nhãn

        add(lbattribute, BorderLayout.WEST);
        add(inputComponent, BorderLayout.CENTER); // Input sẽ tự giãn hết phần còn lại
    }

    public JLabel getLabel() {
    // Giả sử biến JLabel của bạn tên là 'lb' hoặc 'label'
        return this.lbattribute; 
    }

   public static void main(String[] args) {
       JFrame frame = new JFrame();
       frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
       frame.setSize(800,200);
       frame.setLocationRelativeTo(null);
       frame.add(new Details("mã tài khoản:","20"));
       frame.setVisible(true);
   }
}
