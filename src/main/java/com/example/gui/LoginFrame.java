package com.example.gui;

import com.example.bus.AccountBUS;
import com.example.dto.AccountDTO;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final AccountBUS accountBUS;

    public LoginFrame(AccountBUS accountBUS) {
        this.accountBUS = accountBUS;

        setTitle("Admissions Admin Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Mở rộng giả lập màn hình web
        setLocationRelativeTo(null);

        // NỀN MAIN (Màu xám nhạt)
        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBackground(Color.decode("#F5F7FA"));

        // KHUNG ĐĂNG NHẬP (Thẻ trắng bo góc)
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        cardPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 20"); 

        // TIÊU ĐỀ
        JLabel titleLabel = new JLabel("Admissions Admin Portal");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.decode("#0F172A")); // Xanh Navy tối
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(30));

        // FORM NHẬP LIỆU
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setPreferredSize(new Dimension(300, 180));

        usernameField = new JTextField();
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
        usernameField.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 4,10,4,10");
        formPanel.add(usernameField);

        passwordField = new JPasswordField();
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        // Nút con mắt thần thánh của FlatLaf
        passwordField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; arc: 10; margin: 4,10,4,10");
        formPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(Color.decode("#0F4C81")); // Màu Primary
        loginButton.setForeground(Color.WHITE);
        loginButton.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0; focusWidth: 0;");
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> doLogin());
        
        formPanel.add(new JLabel()); // Ô trống đẩy nút xuống
        formPanel.add(loginButton);

        cardPanel.add(formPanel);
        backgroundPanel.add(cardPanel);

        setContentPane(backgroundPanel);
        getRootPane().setDefaultButton(loginButton); // Ấn Enter tự bấm Login
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        AccountDTO account = accountBUS.login(username, password);
        if (account == null) {
            JOptionPane.showMessageDialog(this, "Sai thông tin đăng nhập.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            MainLayout mainLayout = new MainLayout(account);
            mainLayout.setVisible(true);
        });
        dispose();
    }
}