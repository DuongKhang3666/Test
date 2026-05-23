package com.example.gui.pages.account;

import com.example.bus.AccountBUS;
import com.example.dto.AccountDTO;
import com.example.gui.components.AddAndConfirmButton;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AccountCreatePage extends JPanel {
    private final AccountBUS accountBUS = new AccountBUS();
    private final JTextField usernameField = new JTextField();
    private final JTextField fullNameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"User", "Admin"});
    private final JRadioButton activeRadio = new JRadioButton("Active");
    private final JRadioButton disabledRadio = new JRadioButton("Disabled");

    public AccountCreatePage(Runnable onSaved) {
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 20, 12, 20));
        form.setOpaque(false);

        usernameField.setPreferredSize(new Dimension(360, 36));
        usernameField.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.setForeground(new Color(31, 41, 55));
        usernameField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        
        fullNameField.setPreferredSize(new Dimension(360, 36));
        fullNameField.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        fullNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fullNameField.setForeground(new Color(31, 41, 55));
        fullNameField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        passwordField.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        passwordField.setPreferredSize(new Dimension(360, 36));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setForeground(new Color(31, 41, 55));
        passwordField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        confirmPasswordField.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        confirmPasswordField.setPreferredSize(new Dimension(360, 36));
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        confirmPasswordField.setForeground(new Color(31, 41, 55));
        confirmPasswordField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        roleCombo.setPreferredSize(new Dimension(360, 36));

        ButtonGroup statusGroup = new ButtonGroup();
        statusGroup.add(activeRadio);
        statusGroup.add(disabledRadio);
        activeRadio.setSelected(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        addField(form, gbc, "Username", usernameField);
        addField(form, gbc, "Full Name", fullNameField);
        addPasswordField(form, gbc, "Password", passwordField);
        addPasswordField(form, gbc, "Confirm Password", confirmPasswordField);
        addField(form, gbc, "Role", roleCombo);
        addStatusField(form, gbc);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 20, 18, 20));

        JButton btnCancel = createSecondaryButton("Hủy");
        JButton btnCreate = createPrimaryButton("Tạo tài khoản");

        btnCancel.addActionListener(e -> onSaved.run());
        btnCreate.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ username và mật khẩu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AccountDTO account = new AccountDTO(username, password, fullName, role, activeRadio.isSelected());
            try {
                accountBUS.save(account);
                JOptionPane.showMessageDialog(this, "Tạo tài khoản thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                onSaved.run();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể tạo tài khoản: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnCreate);
        add(form, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent component) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        if (label != null) {
            JLabel jLabel = new JLabel(label);
            jLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            jLabel.setForeground(new Color(75, 85, 99));
            wrapper.add(jLabel, BorderLayout.NORTH);
        }
        // Normalize component appearance for inputs
        if (component instanceof JTextField) {
            JTextField f = (JTextField) component;
            f.setOpaque(true);
            f.setBackground(Color.WHITE);
            f.setForeground(new Color(31, 41, 55));
            f.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
            f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            f.setPreferredSize(new Dimension(360, 36));
        } else if (component instanceof JPasswordField) {
            JPasswordField f = (JPasswordField) component;
            f.setOpaque(true);
            f.setBackground(Color.WHITE);
            f.setForeground(new Color(31, 41, 55));
            f.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
            f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            f.setPreferredSize(new Dimension(360, 36));
        } else if (component instanceof JComboBox) {
            JComboBox<?> combo = (JComboBox<?>) component;
            combo.setOpaque(true);
            combo.setBackground(Color.WHITE);
            combo.setPreferredSize(new Dimension(360, 36));
            combo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            // Ensure renderer respects padding/background
            combo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    lbl.setOpaque(true);
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(31, 41, 55));
                    return lbl;
                }
            });
        }

        wrapper.add(component, BorderLayout.CENTER);
        panel.add(wrapper, gbc);
        gbc.gridy++;
    }

    private void addPasswordField(JPanel panel, GridBagConstraints gbc, String label, JPasswordField field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);

        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLabel.setForeground(new Color(75, 85, 99));

        JPanel input = new JPanel(new BorderLayout(8, 0));
        input.setOpaque(false);
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(31, 41, 55));
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        field.setPreferredSize(new Dimension(360, 36));
        input.add(field, BorderLayout.CENTER);

        JToggleButton toggle = new JToggleButton("Show");
        toggle.setPreferredSize(new Dimension(64, 32));
        toggle.putClientProperty(FlatClientProperties.STYLE, "arc: 6; background: #FFFFFF; borderColor: #CBD5E1; borderWidth: 1; focusWidth: 0; font: 12;");
        toggle.addActionListener(e -> togglePassword(toggle, field));
        input.add(toggle, BorderLayout.EAST);

        wrapper.add(jLabel, BorderLayout.NORTH);
        wrapper.add(input, BorderLayout.CENTER);
        panel.add(wrapper, gbc);
        gbc.gridy++;
    }

    private void addStatusField(JPanel panel, GridBagConstraints gbc) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);

        JLabel jLabel = new JLabel("Account Status");
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLabel.setForeground(new Color(75, 85, 99));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        row.setOpaque(false);
        row.add(activeRadio);
        row.add(disabledRadio);

        wrapper.add(jLabel, BorderLayout.NORTH);
        wrapper.add(row, BorderLayout.CENTER);
        panel.add(wrapper, gbc);
        gbc.gridy++;
    }

    private void togglePassword(JToggleButton toggle, JPasswordField field) {
        if (toggle.isSelected()) {
            field.setEchoChar((char) 0);
            toggle.setText("Hide");
        } else {
            field.setEchoChar('\u2022');
            toggle.setText("Show");
        }
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc: 8; background: #FFFFFF; foreground: #1F2937; borderColor: #CBD5E1; borderWidth: 1; focusWidth: 0; font: bold 13;");
        button.setPreferredSize(new Dimension(92, 36));
        button.setCursor(Cursor.getDefaultCursor());
        return button;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc: 8; background: #0F4C81; foreground: #FFFFFF; hoverBackground: #0B3A64; borderWidth: 0; focusWidth: 0; font: bold 13;");
        button.setPreferredSize(new Dimension(140, 36));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}