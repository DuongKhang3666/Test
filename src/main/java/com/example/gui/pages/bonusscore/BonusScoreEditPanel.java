package com.example.gui.pages.bonusscore;

import com.example.bus.BonusScoreBUS;
import com.example.dto.BonusScoreDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class BonusScoreEditPanel extends JPanel {

	private final BonusScoreBUS bonusScoreBUS = new BonusScoreBUS();
	private final BonusScoreDTO dto;
	private final Map<String, JLabel> errorLabels = new HashMap<>();

	// Key fields (must be read-only)
	private JTextField tfCccd;
	private JTextField tfMaNganh;
	private JTextField tfMaToHop;
	private JTextField tfPhuongThuc;

	// Editable fields
	private JTextField tfDiemCC;
	private JTextField tfDiemUtxt;
	private JTextArea taGhiChu;

	public BonusScoreEditPanel(BonusScoreDTO bonusScore, Consumer<BonusScoreDTO> onSave, Runnable onCancel) {
		java.util.Objects.requireNonNull(bonusScore, "Dữ liệu điểm cộng cần sửa không được rỗng!");
        java.util.Objects.requireNonNull(onSave, "Hành động onSave không được rỗng!");
        java.util.Objects.requireNonNull(onCancel, "Hành động onCancel không được rỗng!");
		
		this.dto = bonusScore;

		setLayout(new BorderLayout());
		setBackground(new Color(245, 246, 248));
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));
		card.add(new JScrollPane(createFormPanel()), BorderLayout.CENTER);
		card.add(createFooterPanel(onSave, onCancel), BorderLayout.SOUTH);

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

		// Read-only fields
		tfCccd = BonusScoreFormSupport.createTextField();
		tfCccd.setText(dto.getTsCccd() != null ? dto.getTsCccd() : "");
		lockField(tfCccd);

		tfMaNganh = BonusScoreFormSupport.createTextField();
		tfMaNganh.setText(dto.getMaNganh() != null ? dto.getMaNganh() : "");
		lockField(tfMaNganh);

		tfMaToHop = BonusScoreFormSupport.createTextField();
		tfMaToHop.setText(dto.getMaToHop() != null ? dto.getMaToHop() : "");
		lockField(tfMaToHop);

		tfPhuongThuc = BonusScoreFormSupport.createTextField();
		tfPhuongThuc.setText(dto.getPhuongThuc() != null ? dto.getPhuongThuc() : "");
		lockField(tfPhuongThuc);

		// Editable fields
		tfDiemCC = BonusScoreFormSupport.createBoundedDecimalTextField(2, 1, 2.0);
		tfDiemCC.setText(formatScore(dto.getDiemCC(), 1));

		tfDiemUtxt = BonusScoreFormSupport.createBoundedDecimalTextField(2, 2, 2.0);
		tfDiemUtxt.setText(formatScore(dto.getDiemUtxt(), 2));

		taGhiChu = new JTextArea(4, 20);
		taGhiChu.setLineWrap(true);
		taGhiChu.setWrapStyleWord(true);
		taGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		taGhiChu.setText(dto.getGhiChu() != null ? dto.getGhiChu() : "");

		// Layout
		BonusScoreFormSupport.addField(wrapper, c, row, 0, "CCCD thí sinh", tfCccd, "cccd", false, null, errorLabels);
		BonusScoreFormSupport.addField(wrapper, c, row, 2, "Mã ngành", tfMaNganh, "maganht", false, null, errorLabels);
		row += 2;

		BonusScoreFormSupport.addField(wrapper, c, row, 0, "Phương thức xét tuyển", tfPhuongThuc, "phuongthuc", false, null, errorLabels);
		BonusScoreFormSupport.addField(wrapper, c, row, 2, "Mã tổ hợp", tfMaToHop, "matohop", false, null, errorLabels);
		row += 2;

		BonusScoreFormSupport.addField(wrapper, c, row, 0, "Điểm chứng chỉ", tfDiemCC, "diemcc", true, "Nhập số thập phân, ví dụ: 1.5", errorLabels);
		BonusScoreFormSupport.addField(wrapper, c, row, 2, "Điểm ưu tiên", tfDiemUtxt, "diemuxt", true, "Nhập số thập phân, ví dụ: 0.25", errorLabels);
		row += 2;

		JScrollPane noteScroll = new JScrollPane(taGhiChu);
		noteScroll.setPreferredSize(new Dimension(10, 110));
		BonusScoreFormSupport.addField(wrapper, c, row, 0, "Ghi chú", noteScroll, "ghichu", false, null, errorLabels);
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

	private JPanel createFooterPanel(Consumer<BonusScoreDTO> onSave, Runnable onCancel) {
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		footer.setBackground(new Color(238, 240, 243));
		footer.setBorder(new EmptyBorder(14, 18, 14, 18));

		JButton btnCancel = new JButton("Hủy");
		btnCancel.setPreferredSize(new Dimension(90, 36));
		btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCancel.addActionListener(e -> onCancel.run());

		JButton btnSave = new JButton("Lưu");
		btnSave.setPreferredSize(new Dimension(120, 36));
		btnSave.setBackground(Color.decode("#0066CC"));
		btnSave.setForeground(Color.WHITE);
		btnSave.setOpaque(true);
		btnSave.setBorderPainted(false);
		btnSave.setFocusPainted(false);
		btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSave.addActionListener(e -> {
			if (!validateForm()) {
				return;
			}
			updateDTO();
			bonusScoreBUS.updateBonusScore(dto);
			if (onSave != null) {
				onSave.accept(dto);
			}
		});

		footer.add(btnCancel);
		footer.add(btnSave);
		return footer;
	}

	private boolean validateForm() {
		boolean isValid = true;
		for (JLabel lbl : errorLabels.values()) {
			lbl.setText(" ");
		}

		String diemCCRaw = tfDiemCC.getText() != null ? tfDiemCC.getText().trim() : "";
		if (diemCCRaw.isEmpty()) {
			setError("diemcc", "Điểm chứng chỉ không được để trống");
			isValid = false;
		}
        
        Double diemCC = BonusScoreFormSupport.parseScore(tfDiemCC.getText());
		if (diemCC == null) {
			setError("diemcc", "Điểm chứng chỉ không hợp lệ");
			isValid = false;
		}

        String diemUtxtRaw = tfDiemUtxt.getText() != null ? tfDiemUtxt.getText().trim() : "";
		if (diemUtxtRaw.isEmpty()) {
			setError("diemuxt", "Điểm ưu tiên không được để trống");
			isValid = false;
		}

		Double diemUtxt = BonusScoreFormSupport.parseScore(tfDiemUtxt.getText());
		if (diemUtxt == null) {
			setError("diemuxt", "Điểm ưu tiên không hợp lệ");
			isValid = false;
		}

		return isValid;
	}

	private void updateDTO() {
		// Do NOT override key fields (cccd/nganh/tohop/phuongthuc)
		Double diemCC = BonusScoreFormSupport.parseScore(tfDiemCC.getText());
		Double diemUtxt = BonusScoreFormSupport.parseScore(tfDiemUtxt.getText());
		double total = (diemCC == null ? 0.0 : diemCC) + (diemUtxt == null ? 0.0 : diemUtxt);

		dto.setDiemCC(diemCC);
		dto.setDiemUtxt(diemUtxt);
		dto.setDiemTong(total);
		dto.setGhiChu(taGhiChu.getText() != null ? taGhiChu.getText().trim() : "");
	}

	private void setError(String key, String message) {
		JLabel errorLabel = errorLabels.get(key);
		if (errorLabel != null) {
			errorLabel.setText(message);
		}
	}

	private void lockField(JTextField field) {
		field.setEditable(false);
		field.setFocusable(false);
		field.setBackground(new Color(245, 246, 248));
	}

	private String formatScore(Double value, int fractionDigits) {
		double v = value == null ? 0.0 : value;
		return String.format(Locale.ROOT, "%." + fractionDigits + "f", v);
	}
}
