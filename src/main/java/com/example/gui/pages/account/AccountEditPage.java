package com.example.gui.pages.account;

import com.example.bus.AccountBUS;
import com.example.dto.AccountDTO;
import com.example.gui.components.AddAndConfirmButton;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AccountEditPage extends JPanel {
    private final AccountBUS accountBUS = new AccountBUS();
    private final AccountDTO account;
    private final JTextField usernameField = new JTextField();
    private final JTextField fullNameField = new JTextField();
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"User", "Admin"});
    private final JRadioButton activeRadio = new JRadioButton("Active");
    private final JRadioButton disabledRadio = new JRadioButton("Disabled");

    public AccountEditPage(AccountDTO account, Runnable onSaved) {
        this.account = account;
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 20, 12, 20));
        form.setOpaque(false);

        usernameField.setText(account != null ? account.getUsername() : "");
        usernameField.setPreferredSize(new Dimension(360, 36));
        usernameField.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");

        fullNameField.setText(account != null && account.getHoTen() != null ? account.getHoTen() : "");
        fullNameField.setPreferredSize(new Dimension(360, 36));
        fullNameField.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");

        roleCombo.setSelectedItem(account != null && account.getRole() != null ? account.getRole() : "User");
        roleCombo.setPreferredSize(new Dimension(360, 36));

        ButtonGroup statusGroup = new ButtonGroup();
        statusGroup.add(activeRadio);
        statusGroup.add(disabledRadio);
        activeRadio.setSelected(account == null || account.isActive());
        disabledRadio.setSelected(account != null && !account.isActive());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        addField(form, gbc, "Username", usernameField);
        addField(form, gbc, "Full Name", fullNameField);
        addField(form, gbc, "Role", roleCombo);
        addStatusField(form, gbc);

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

            String newUsername = usernameField.getText().trim();
            if (newUsername.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String fullName = fullNameField.getText().trim();
            this.account.setUsername(newUsername);

            this.account.setHoTen(fullName);
            this.account.setRole((String) roleCombo.getSelectedItem());
            this.account.setActive(activeRadio.isSelected());

            try {
                accountBUS.update(this.account);
                JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                onSaved.run();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật tài khoản: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);

        add(form, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent component) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLabel.setForeground(new Color(75, 85, 99));
        wrapper.add(jLabel, BorderLayout.NORTH);
        wrapper.add(component, BorderLayout.CENTER);
        panel.add(wrapper, gbc);
        gbc.gridy++;
    }

    private void addStatusField(JPanel panel, GridBagConstraints gbc) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel label = new JLabel("Account Status");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(75, 85, 99));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        row.setOpaque(false);
        row.add(activeRadio);
        row.add(disabledRadio);

        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(row, BorderLayout.CENTER);
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