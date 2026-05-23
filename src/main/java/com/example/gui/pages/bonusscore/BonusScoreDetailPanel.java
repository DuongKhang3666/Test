package com.example.gui.pages.bonusscore;

import com.example.dto.BonusScoreDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BonusScoreDetailPanel extends JPanel {

	private final BonusScoreDTO dto;

	public BonusScoreDetailPanel(BonusScoreDTO bonusScore, Runnable onClose) {
		java.util.Objects.requireNonNull(bonusScore, "Dữ liệu điểm cộng chi tiết không được rỗng!");
        java.util.Objects.requireNonNull(onClose, "Hành động onClose không được rỗng!");
		
		this.dto = bonusScore;

		setLayout(new BorderLayout());
		setBackground(new Color(245, 246, 248));
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));
		card.add(new JScrollPane(createFormPanel()), BorderLayout.CENTER);
		card.add(createFooterPanel(onClose), BorderLayout.SOUTH);

		add(card, BorderLayout.CENTER);
	}

	private JPanel createFormPanel() {
		JPanel wrapper = new JPanel(new GridBagLayout());
		wrapper.setBackground(Color.WHITE);
		wrapper.setBorder(new EmptyBorder(18, 18, 18, 18));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(6, 8, 6, 8);
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;

		int row = 0;

		JTextField tfCccd = createReadOnlyTextField(dto.getTsCccd());
		JTextField tfMaNganh = createReadOnlyTextField(dto.getMaNganh());
		JTextField tfMaToHop = createReadOnlyTextField(dto.getMaToHop());
		JTextField tfPhuongThuc = createReadOnlyTextField(dto.getPhuongThuc());
		JTextField tfDiemCC = createReadOnlyTextField(BonusScoreFormSupport.formatScore(dto.getDiemCC()));
		JTextField tfDiemUtxt = createReadOnlyTextField(BonusScoreFormSupport.formatScore(dto.getDiemUtxt()));
		JTextField tfDiemTong = createReadOnlyTextField(BonusScoreFormSupport.formatScore(dto.getDiemTong()));
		JTextArea taGhiChu = createReadOnlyTextArea(dto.getGhiChu());

		BonusScoreFormSupport.addReadOnlyField(wrapper, c, row, 0, "CCCD thí sinh", tfCccd);
		BonusScoreFormSupport.addReadOnlyField(wrapper, c, row, 2, "Mã ngành", tfMaNganh);
		row += 2;

		BonusScoreFormSupport.addReadOnlyField(wrapper, c, row, 0, "Phương thức xét tuyển", tfPhuongThuc);
		BonusScoreFormSupport.addReadOnlyField(wrapper, c, row, 2, "Mã tổ hợp", tfMaToHop);
		row += 2;

		BonusScoreFormSupport.addReadOnlyField(wrapper, c, row, 0, "Điểm chứng chỉ", tfDiemCC);
		BonusScoreFormSupport.addReadOnlyField(wrapper, c, row, 2, "Điểm ưu tiên", tfDiemUtxt);
		row += 2;

		BonusScoreFormSupport.addReadOnlyField(wrapper, c, row, 0, "Tổng điểm cộng", tfDiemTong);
		row += 2;

		JScrollPane noteScroll = new JScrollPane(taGhiChu);
		noteScroll.setPreferredSize(new Dimension(10, 110));
		noteScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
		BonusScoreFormSupport.addReadOnlyField(wrapper, c, row, 0, "Ghi chú", noteScroll);
		row += 2;

		GridBagConstraints spacer = new GridBagConstraints();
		spacer.gridx = 0;
		spacer.gridy = row;
		spacer.gridwidth = 4;
		spacer.weighty = 1.0;
		spacer.fill = GridBagConstraints.BOTH;
		wrapper.add(new JLabel(""), spacer);

		return wrapper;
	}

	private JTextField createReadOnlyTextField(String value) {
		JTextField field = BonusScoreFormSupport.createTextField();
		field.setText(BonusScoreFormSupport.formatDisplay(value));
		field.setEditable(false);
		field.setFocusable(false);
		field.setBackground(Color.WHITE);
		field.setDisabledTextColor(new Color(40, 40, 40));
		return field;
	}

	private JTextArea createReadOnlyTextArea(String value) {
		JTextArea area = new JTextArea();
		area.setText(BonusScoreFormSupport.formatDisplay(value));
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		area.setBackground(Color.WHITE);
		area.setForeground(new Color(40, 40, 40));
		area.setBorder(new EmptyBorder(10, 12, 10, 12));
		area.setFocusable(false);
		return area;
	}

	private JPanel createFooterPanel(Runnable onClose) {
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		footer.setBackground(new Color(238, 240, 243));
		footer.setBorder(new EmptyBorder(14, 18, 14, 18));

		JButton btnClose = new JButton("Đóng");
		btnClose.setPreferredSize(new Dimension(100, 36));
		btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnClose.addActionListener(e -> onClose.run());

		footer.add(btnClose);
		return footer;
	}
}
