package com.example.gui.pages.bonusscore;

import com.example.bus.BonusScoreBUS;
import com.example.dto.BonusScoreDTO;
import com.example.gui.components.ComboBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BonusScoreCreatePanel extends JPanel {

	private final BonusScoreBUS bonusScoreBUS = new BonusScoreBUS();
	private final Map<String, JLabel> errorLabels = new HashMap<>();
	private String checkedCccd;

	private JTextField tfCccd;
	private JButton btnCheck;
	private ComboBox<AspirationItem> cbDSNguyenVong;
	private JTextField tfDiemCC;
	private JTextField tfDiemUtxt;
	private JTextArea taGhiChu;
	private JButton btnSave;
	private boolean isUpdatingCombo;

	public static class AspirationItem {
		private final String maNganh;
		private final String maToHop;
		private final String phuongThuc;
		private final boolean placeholder;

		public AspirationItem(String maNganh, String maToHop, String phuongThuc) {
			this(maNganh, maToHop, phuongThuc, false);
		}

		private AspirationItem(String maNganh, String maToHop, String phuongThuc, boolean placeholder) {
			this.maNganh = maNganh;
			this.maToHop = maToHop;
			this.phuongThuc = phuongThuc;
			this.placeholder = placeholder;
		}

		public static AspirationItem placeholder() {
			return new AspirationItem("", "", "", true);
		}

		public boolean isPlaceholder() {
			return placeholder;
		}

		public String getMaNganh() {
			return maNganh;
		}

		public String getMaToHop() {
			return maToHop;
		}

		public String getPhuongThuc() {
			return phuongThuc;
		}

		@Override
		public String toString() {
			if (placeholder) {
				return "-- Chọn nguyện vọng --";
			}
			String maNganhText = (maNganh == null || maNganh.isBlank()) ? "--" : maNganh;
			String phuongThucText = (phuongThuc == null || phuongThuc.isBlank()) ? "--" : phuongThuc;
			String maToHopText = (maToHop == null || maToHop.isBlank()) ? "--" : maToHop;
			return "Ngành " + maNganhText + " - PT " + phuongThucText + " - Khối " + maToHopText;
		}
	}

	public BonusScoreCreatePanel(Consumer<BonusScoreDTO> onCreate, Runnable onCancel) {
		java.util.Objects.requireNonNull(onCreate, "Hành động onCreate không được rỗng!");
        java.util.Objects.requireNonNull(onCancel, "Hành động onCancel không được rỗng!");
		
		setLayout(new BorderLayout());
		setBackground(new Color(245, 246, 248));
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));
		card.add(new JScrollPane(createFormPanel()), BorderLayout.CENTER);
		card.add(createFooterPanel(onCreate, onCancel), BorderLayout.SOUTH);

		add(card, BorderLayout.CENTER);
		lockForm();
		SwingUtilities.invokeLater(() -> tfCccd.requestFocusInWindow());
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

		tfCccd = BonusScoreFormSupport.createTextField();
		btnCheck = new JButton("Kiểm tra");
		btnCheck.setPreferredSize(new Dimension(110, 36));
		btnCheck.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCheck.setFocusPainted(false);

		JPanel cccdBox = new JPanel(new BorderLayout(8, 0));
		cccdBox.setOpaque(false);
		cccdBox.add(tfCccd, BorderLayout.CENTER);
		cccdBox.add(btnCheck, BorderLayout.EAST);

		cbDSNguyenVong = new ComboBox<>(new ArrayList<>(), "-- Chọn nguyện vọng --");
		tfDiemCC = BonusScoreFormSupport.createBoundedDecimalTextField(2, 1, 2.0);
		tfDiemUtxt = BonusScoreFormSupport.createBoundedDecimalTextField(2, 2, 2.0);
		taGhiChu = new JTextArea(4, 20);
		taGhiChu.setLineWrap(true);
		taGhiChu.setWrapStyleWord(true);
		taGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		BonusScoreFormSupport.addField(wrapper, c, row, 0, "CCCD thí sinh", cccdBox, "cccd", true, null, errorLabels);
		row += 2;

		BonusScoreFormSupport.addField(wrapper, c, row, 0, "Chọn Nguyện Vọng", cbDSNguyenVong, "nguyenvong", true, null, errorLabels);
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

		wireEvents();

		return wrapper;
	}

	private JPanel createFooterPanel(Consumer<BonusScoreDTO> onCreate, Runnable onCancel) {
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		footer.setBackground(new Color(238, 240, 243));
		footer.setBorder(new EmptyBorder(14, 18, 14, 18));

		JButton btnCancel = new JButton("Hủy");
		btnCancel.setPreferredSize(new Dimension(90, 36));
		btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCancel.addActionListener(e -> onCancel.run());

		btnSave = new JButton("Lưu");
		btnSave.setPreferredSize(new Dimension(120, 36));
		btnSave.setBackground(Color.decode("#0066CC"));
		btnSave.setForeground(Color.WHITE);
		btnSave.setOpaque(true);
		btnSave.setBorderPainted(false);
		btnSave.setFocusPainted(false);
		btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnSave.addActionListener(e -> {
			clearErrors();
			if (!validateBeforeSave()) {
				return;
			}
			BonusScoreDTO dto = buildDtoFromForm();
			try {
				bonusScoreBUS.saveSingleBonusScoreAndSyncEnglishScore(dto);
				if (onCreate != null) {
					onCreate.accept(dto);
				}
			} catch (BonusScoreBUS.BonusScoreValidationException vex) {
				JOptionPane.showMessageDialog(this, vex.getMessageText(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "Không thể lưu điểm cộng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		});

		footer.add(btnCancel);
		footer.add(btnSave);
		return footer;
	}

	private void wireEvents() {
		btnCheck.addActionListener(e -> handleCheckCccd());
		cbDSNguyenVong.addActionListener(e -> {
			if (isUpdatingCombo) return;
			handleAspirationChanged();
		});
	}

	private void handleCheckCccd() {
		clearErrors();
		String cccd = tfCccd.getText() != null ? tfCccd.getText().trim() : "";
		if (cccd.isEmpty()) {
			setError("cccd", "CCCD không được để trống");
			lockForm();
			return;
		}

		List<Object[]> raw;
		try {
			raw = bonusScoreBUS.getNguyenVongByCccd(cccd);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Không thể kiểm tra nguyện vọng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			lockForm();
			return;
		}

		if (raw == null || raw.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Thí sinh chưa đăng ký nguyện vọng hoặc CCCD không hợp lệ", "Thông báo", JOptionPane.WARNING_MESSAGE);
			lockForm();
			return;
		}

		checkedCccd = cccd;
		loadAspirationItems(raw);
		cbDSNguyenVong.setEnabled(true);
		cbDSNguyenVong.setSelectedIndex(0);
		lockScoreInputs();
	}

	private void loadAspirationItems(List<Object[]> raw) {
		isUpdatingCombo = true;
		try {
			cbDSNguyenVong.removeAllItems();
			cbDSNguyenVong.addItem(AspirationItem.placeholder());
			for (Object[] row : raw) {
				if (row == null) continue;
				String maNganh = safeString(row.length > 0 ? row[0] : null);
				String maToHop = safeString(row.length > 1 ? row[1] : null);
				String phuongThuc = safeString(row.length > 2 ? row[2] : null);
				if (maNganh.isBlank()) continue;
				cbDSNguyenVong.addItem(new AspirationItem(maNganh, maToHop, phuongThuc));
			}
		} finally {
			isUpdatingCombo = false;
		}
	}

	private void handleAspirationChanged() {
		clearErrors();
		int idx = cbDSNguyenVong.getSelectedIndex();
		if (idx <= 0) {
			lockScoreInputs();
			return;
		}

		AspirationItem item = (AspirationItem) cbDSNguyenVong.getSelectedItem();
		if (item == null || item.isPlaceholder()) {
			lockScoreInputs();
			return;
		}

		String cccd = checkedCccd != null ? checkedCccd.trim() : "";
		if (cccd.isEmpty()) {
			lockForm();
			return;
		}

		String dcKeys = cccd + "_" + item.getMaNganh() + "_" + item.getMaToHop();
		boolean exists;
		try {
			exists = bonusScoreBUS.checkTonTaiDiemCong(dcKeys);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Không thể kiểm tra trùng điểm cộng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			lockScoreInputs();
			return;
		}

		if (exists) {
			setError("nguyenvong", "NV đã có điểm");
			lockScoreInputs();
			return;
		}

		unlockScoreInputs();
		try {
			double diemTA = bonusScoreBUS.getDiemTiengAnhDaCo(cccd);
			if (diemTA > 0) {
				tfDiemCC.setText(BonusScoreFormSupport.formatScore(diemTA));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean validateBeforeSave() {
		boolean isValid = true;

		if (checkedCccd == null || checkedCccd.isBlank()) {
			setError("cccd", "Vui lòng kiểm tra CCCD trước");
			return false;
		}

		if (cbDSNguyenVong.getSelectedIndex() <= 0) {
			setError("nguyenvong", "Vui lòng chọn nguyện vọng");
			isValid = false;
		}

		String diemCCRaw = tfDiemCC.getText() != null ? tfDiemCC.getText().trim() : "";
		if (diemCCRaw.isEmpty()) {
			setError("diemcc", "Điểm chứng chỉ không được để trống");
			isValid = false;
		}

		Double diemCC = BonusScoreFormSupport.parseScore(diemCCRaw);
		if (diemCC == null) {
			setError("diemcc", "Điểm chứng chỉ không hợp lệ");
			isValid = false;
		} else if (diemCC > 2.0) {
			setError("diemcc", "Điểm chứng chỉ tối đa 2.0");
			isValid = false;
		}

		String diemUtxtRaw = tfDiemUtxt.getText() != null ? tfDiemUtxt.getText().trim() : "";
		if (diemUtxtRaw.isEmpty()) {
			setError("diemuxt", "Điểm ưu tiên không được để trống");
			isValid = false;
		}

		Double diemUtxt = BonusScoreFormSupport.parseScore(diemUtxtRaw);
		if (diemUtxt == null) {
			setError("diemuxt", "Điểm ưu tiên không hợp lệ");
			isValid = false;
		} else if (diemUtxt > 2.0) {
			setError("diemuxt", "Điểm ưu tiên tối đa 2.00");
			isValid = false;
		}

		return isValid;
	}

	private BonusScoreDTO buildDtoFromForm() {
		AspirationItem item = (AspirationItem) cbDSNguyenVong.getSelectedItem();
		String cccd = checkedCccd != null ? checkedCccd.trim() : "";
		String maNganh = item != null ? safeString(item.getMaNganh()) : "";
		String maToHop = item != null ? safeString(item.getMaToHop()) : "";
		String phuongThuc = item != null ? safeString(item.getPhuongThuc()) : "";

		Double diemCC = BonusScoreFormSupport.parseScore(tfDiemCC.getText());
		Double diemUtxt = BonusScoreFormSupport.parseScore(tfDiemUtxt.getText());

		BonusScoreDTO dto = new BonusScoreDTO();
		dto.setTsCccd(cccd);
		dto.setMaNganh(maNganh);
		dto.setMaToHop(maToHop);
		dto.setPhuongThuc(phuongThuc);
		dto.setDiemCC(diemCC);
		dto.setDiemUtxt(diemUtxt);
		dto.setGhiChu(taGhiChu.getText() != null ? taGhiChu.getText().trim() : "");
		dto.setDcKeys(cccd + "_" + maNganh + "_" + maToHop);
		return dto;
	}

	private void lockForm() {
		checkedCccd = null;
		isUpdatingCombo = true;
		try {
			cbDSNguyenVong.removeAllItems();
			cbDSNguyenVong.addItem(AspirationItem.placeholder());
			cbDSNguyenVong.setSelectedIndex(0);
		} finally {
			isUpdatingCombo = false;
		}
		cbDSNguyenVong.setEnabled(false);
		lockScoreInputs();
	}

	private void lockScoreInputs() {
		tfDiemCC.setText("");
		tfDiemUtxt.setText("");
		taGhiChu.setText("");
		tfDiemCC.setEnabled(false);
		tfDiemUtxt.setEnabled(false);
		taGhiChu.setEnabled(false);
		if (btnSave != null) {
			btnSave.setEnabled(false);
		}
	}

	private void unlockScoreInputs() {
		tfDiemCC.setEnabled(true);
		tfDiemUtxt.setEnabled(true);
		taGhiChu.setEnabled(true);
		btnSave.setEnabled(true);
	}

	private void clearErrors() {
		for (JLabel lbl : errorLabels.values()) {
			lbl.setText(" ");
		}
	}

	private void setError(String key, String message) {
		JLabel errorLabel = errorLabels.get(key);
		if (errorLabel != null) {
			errorLabel.setText(message);
		}
	}

	private String safeString(Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}
}
