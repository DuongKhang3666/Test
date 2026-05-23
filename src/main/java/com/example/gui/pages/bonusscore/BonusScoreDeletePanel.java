package com.example.gui.pages.bonusscore;

import com.example.dto.BonusScoreDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel xác nhận xóa điểm cộng cho thí sinh.
 * Chỉ xác nhận hành động xóa bản ghi điểm cộng (không xóa hồ sơ thí sinh).
 */
public class BonusScoreDeletePanel extends JPanel {

	private final BonusScoreDTO bonusScore;
	private final Consumer<BonusScoreDTO> onDelete;
	private final Runnable onCancel;
	private final int bonusScoreId;

	private JButton btnDelete;
	private JButton btnCancel;

	public BonusScoreDeletePanel(BonusScoreDTO bonusScore, Consumer<BonusScoreDTO> onDelete, Runnable onCancel) {
        java.util.Objects.requireNonNull(bonusScore, "Dữ liệu điểm cộng cần xóa không được rỗng!");
        java.util.Objects.requireNonNull(onDelete, "Hành động onDelete không được rỗng!");
        java.util.Objects.requireNonNull(onCancel, "Hành động onCancel không được rỗng!");

        this.bonusScore = bonusScore;
        this.onDelete = onDelete;
        this.onCancel = onCancel;
        
        this.bonusScoreId = bonusScore.getIdDiemCong();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

	private JComponent buildContent() {
		JPanel content = new JPanel(new GridBagLayout());
		content.setOpaque(false);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(6, 8, 6, 8);
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;

		int row = 0;

		// Header
		JPanel header = new JPanel(new BorderLayout(12, 0));
		header.setOpaque(false);
		JLabel icon = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
		header.add(icon, BorderLayout.WEST);

		JPanel headerText = new JPanel();
		headerText.setOpaque(false);
		headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));

		JLabel title = new JLabel("Xác nhận xóa điểm cộng");
		title.setFont(new Font("Segoe UI", Font.BOLD, 16));
		headerText.add(title);

		String cccd = safeText(bonusScore != null ? bonusScore.getTsCccd() : null);
		JLabel subtitle = new JLabel("Hãy kiểm tra đúng thông tin điểm cộng thí sinh trước khi xác nhận.");
		subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		subtitle.setForeground(new Color(110, 117, 125));
		headerText.add(subtitle);

		header.add(headerText, BorderLayout.CENTER);

		GridBagConstraints headerC = (GridBagConstraints) c.clone();
		headerC.gridx = 0;
		headerC.gridy = row;
		headerC.gridwidth = 4;
		headerC.insets = new Insets(0, 8, 10, 8);
		content.add(header, headerC);
		row += 1;

		JSeparator sep = new JSeparator();
		GridBagConstraints sepC = (GridBagConstraints) c.clone();
		sepC.gridx = 0;
		sepC.gridy = row;
		sepC.gridwidth = 4;
		sepC.insets = new Insets(0, 8, 12, 8);
		content.add(sep, sepC);
		row += 1;

		// Details fields (read-only)
		JTextField tfCccd = BonusScoreFormSupport.createTextField();
		tfCccd.setText(cccd);
		lockField(tfCccd);

		JTextField tfId = BonusScoreFormSupport.createTextField();
		tfId.setText(bonusScoreId > 0 ? String.valueOf(bonusScoreId) : "--");
		lockField(tfId);

		JTextField tfMaNganh = BonusScoreFormSupport.createTextField();
		tfMaNganh.setText(safeText(bonusScore != null ? bonusScore.getMaNganh() : null));
		lockField(tfMaNganh);

		JTextField tfPhuongThuc = BonusScoreFormSupport.createTextField();
		tfPhuongThuc.setText(safeText(bonusScore != null ? bonusScore.getPhuongThuc() : null));
		lockField(tfPhuongThuc);

		JTextField tfMaToHop = BonusScoreFormSupport.createTextField();
		tfMaToHop.setText(safeText(bonusScore != null ? bonusScore.getMaToHop() : null));
		lockField(tfMaToHop);

		JTextField tfDiemCC = BonusScoreFormSupport.createTextField();
		Double diemCC = bonusScore != null ? bonusScore.getDiemCC() : null;
		tfDiemCC.setText(BonusScoreFormSupport.formatScore(diemCC));
		lockField(tfDiemCC);

		JTextField tfDiemUtxt = BonusScoreFormSupport.createTextField();
		Double diemUtxt = bonusScore != null ? bonusScore.getDiemUtxt() : null;
		tfDiemUtxt.setText(BonusScoreFormSupport.formatScore(diemUtxt));
		lockField(tfDiemUtxt);

		JTextField tfDiemTong = BonusScoreFormSupport.createTextField();
		Double diemTong = bonusScore != null ? bonusScore.getDiemTong() : null;
		if (diemTong == null) {
			double t = (diemCC == null ? 0.0 : diemCC) + (diemUtxt == null ? 0.0 : diemUtxt);
			diemTong = t;
		}
		tfDiemTong.setText(BonusScoreFormSupport.formatScore(diemTong));
		lockField(tfDiemTong);

		BonusScoreFormSupport.addReadOnlyField(content, c, row, 0, "CCCD", tfCccd);
		BonusScoreFormSupport.addReadOnlyField(content, c, row, 2, "Mã điểm cộng", tfId);
		row += 2;

		BonusScoreFormSupport.addReadOnlyField(content, c, row, 0, "Mã ngành", tfMaNganh);
		BonusScoreFormSupport.addReadOnlyField(content, c, row, 2, "Phương thức", tfPhuongThuc);
		row += 2;

		BonusScoreFormSupport.addReadOnlyField(content, c, row, 0, "Tổ hợp", tfMaToHop);
		row += 2;

		BonusScoreFormSupport.addReadOnlyField(content, c, row, 0, "Điểm chứng chỉ", tfDiemCC);
		BonusScoreFormSupport.addReadOnlyField(content, c, row, 2, "Điểm ưu tiên", tfDiemUtxt);
		row += 2;

		BonusScoreFormSupport.addReadOnlyField(content, c, row, 0, "Tổng điểm cộng", tfDiemTong);
		row += 2;

		// Warning note
		JPanel note = new JPanel(new BorderLayout(10, 0));
		note.setOpaque(false);
		JLabel noteIcon = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
		note.add(noteIcon, BorderLayout.WEST);
		JLabel noteText = new JLabel("<html><b>Lưu ý:</b> Xóa điểm cộng không đồng nghĩa với xóa hồ sơ thí sinh cũng như các dữ liệu có liên quan khác.</html>");
		noteText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		noteText.setForeground(new Color(73, 80, 87));
		note.add(noteText, BorderLayout.CENTER);

		GridBagConstraints noteC = (GridBagConstraints) c.clone();
		noteC.gridx = 0;
		noteC.gridy = row;
		noteC.gridwidth = 4;
		noteC.insets = new Insets(12, 8, 0, 8);
		content.add(note, noteC);
		row += 1;

		// Spacer
		GridBagConstraints spacer = new GridBagConstraints();
		spacer.gridx = 0;
		spacer.gridy = row;
		spacer.gridwidth = 4;
		spacer.weighty = 1.0;
		spacer.fill = GridBagConstraints.BOTH;
		content.add(new JLabel(""), spacer);

		return content;
	}

	private JComponent buildFooter() {
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		footer.setBackground(Color.WHITE);
		footer.setBorder(new EmptyBorder(14, 18, 14, 18));

		btnCancel = new JButton("Hủy");
		btnCancel.setPreferredSize(new Dimension(90, 36));
		btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCancel.addActionListener(e -> {
			if (onCancel != null) {
				onCancel.run();
			}
		});

		btnDelete = new JButton("Xóa");
		btnDelete.setPreferredSize(new Dimension(120, 36));
		btnDelete.setBackground(new Color(200, 30, 30));
		btnDelete.setForeground(Color.WHITE);
		btnDelete.setOpaque(true);
		btnDelete.setBorderPainted(false);
		btnDelete.setFocusPainted(false);
		btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnDelete.addActionListener(e -> {
			setBusy(true);
			try {
				if (onDelete != null) {
					onDelete.accept(bonusScore);
				}
			} finally {
				setBusy(false);
			}
		});

		footer.add(btnCancel);
		footer.add(btnDelete);
		return footer;
	}

	/**
	 * Cho phép panel chuyển sang trạng thái đang xử lý (vô hiệu nút, đổi con trỏ).
	 */
	public void setBusy(boolean busy) {
		if (btnDelete != null) {
			btnDelete.setEnabled(!busy);
		}
		if (btnCancel != null) {
			btnCancel.setEnabled(!busy);
		}
		setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
	}

	private void lockField(JTextField field) {
		field.setEditable(false);
		field.setFocusable(false);
		field.setBackground(new Color(245, 246, 248));
	}

	private String safeText(String value) {
		if (value == null) {
			return "--";
		}
		String v = value.trim();
		return v.isEmpty() ? "--" : v;
	}
}
