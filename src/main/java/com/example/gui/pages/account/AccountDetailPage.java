package com.example.gui.pages.account;

import com.example.dto.AccountDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;

public class AccountDetailPage extends JPanel {
	private final AccountManagementPage parent;
	private final AccountDTO account;

	public AccountDetailPage(AccountManagementPage parent, AccountDTO account, Runnable onClose) {
		this.parent = parent;
		this.account = account;

		setOpaque(false);
		setLayout(new BorderLayout());

		JPanel form = new JPanel(new GridBagLayout());
		form.setBorder(new EmptyBorder(18, 20, 12, 20));
		form.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 12, 0);

		addField(form, gbc, "Username", readOnlyField(account != null ? account.getUsername() : ""));
		addField(form, gbc, "Full Name", readOnlyField(account != null && account.getHoTen() != null ? account.getHoTen() : ""));
		addField(form, gbc, "Role", readOnlyField(account != null && account.getRole() != null ? account.getRole() : ""));
		addField(form, gbc, "Account Status", readOnlyField(account != null && account.isActive() ? "Active" : "Disabled"));

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		footer.setOpaque(false);
		footer.setBorder(new EmptyBorder(0, 20, 18, 20));

		JButton btnClose = createSecondaryButton("Cancel");
		JButton btnEdit = createSecondaryButton("Edit");
		JButton btnChangePassword = new JButton("Change Password");
		btnChangePassword.putClientProperty(FlatClientProperties.STYLE,
				"arc: 8; background: #0F4C81; foreground: #FFFFFF; hoverBackground: #0B3A64; borderWidth: 0; focusWidth: 0; font: bold 13;");
		btnChangePassword.setPreferredSize(new Dimension(140, 34));
		btnChangePassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		btnClose.addActionListener(e -> onClose.run());
		btnEdit.addActionListener(e -> parent.showEdit(account));
		btnChangePassword.addActionListener(e -> parent.showChangePassword(account));

		footer.add(btnClose);
		footer.add(btnEdit);
		footer.add(btnChangePassword);

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

	private JTextField readOnlyField(String value) {
		JTextField field = new JTextField(value);
		field.setEditable(false);
		field.setBackground(new Color(248, 250, 252));
		field.setForeground(new Color(31, 41, 55));
		field.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
		field.setPreferredSize(new Dimension(360, 36));
		return field;
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
