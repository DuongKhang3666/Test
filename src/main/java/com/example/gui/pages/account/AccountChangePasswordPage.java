package com.example.gui.pages.account;

import com.example.bus.AccountBUS;
import com.example.dto.AccountDTO;
import com.example.gui.components.AddAndConfirmButton;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AccountChangePasswordPage extends JPanel {
    private final AccountBUS accountBUS = new AccountBUS();
    private final AccountDTO account;
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();

    public AccountChangePasswordPage(AccountDTO account, Runnable onSaved) {
        this.account = account;

        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 20, 12, 20));
        form.setOpaque(false);

        JLabel note = new JLabel("Updating password for user: " + (account != null ? account.getUsername() : ""));
        note.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        note.setForeground(new Color(75, 85, 99));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        form.add(note, gbc);
        gbc.gridy++;

        addPasswordField(form, gbc, "New Password", passwordField);
        addPasswordField(form, gbc, "Confirm Password", confirmPasswordField);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 20, 18, 20));

        JButton btnCancel = createSecondaryButton("Cancel");
        JButton btnSave = new AddAndConfirmButton("Save Changes");

        btnCancel.addActionListener(e -> onSaved.run());
        btnSave.addActionListener(e -> {
            if (this.account == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ mật khẩu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            this.account.setPassword(password);
            try {
                accountBUS.update(this.account);
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                onSaved.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể đổi mật khẩu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);

        add(form, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void addPasswordField(JPanel panel, GridBagConstraints gbc, String label, JPasswordField field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);

        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLabel.setForeground(new Color(75, 85, 99));

        JPanel input = new JPanel(new BorderLayout(8, 0));
        input.setOpaque(false);

        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter password");
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        field.setPreferredSize(new Dimension(360, 36));

        JToggleButton toggle = new JToggleButton("Show");
        toggle.setPreferredSize(new Dimension(72, 36));
        toggle.addActionListener(e -> {
            if (toggle.isSelected()) {
                field.setEchoChar((char) 0);
                toggle.setText("Hide");
            } else {
                field.setEchoChar('\u2022');
                toggle.setText("Show");
            }
        });

        input.add(field, BorderLayout.CENTER);
        input.add(toggle, BorderLayout.EAST);

        wrapper.add(jLabel, BorderLayout.NORTH);
        wrapper.add(input, BorderLayout.CENTER);
        panel.add(wrapper, gbc);
        gbc.gridy++;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc: 8; background: #FFFFFF; foreground: #1F2937; borderColor: #CBD5E1; borderWidth: 1; focusWidth: 0; font: bold 13;");
        button.setPreferredSize(new Dimension(92, 36));
        button.setCursor(Cursor.getDefaultCursor());
        return button;
    }
}