package com.example.gui.pages.majorcombination;

import com.example.bus.MajorBUS;
import com.example.bus.MajorGroupBUS;
import com.example.bus.SubjectGroupBUS;
import com.example.dto.MajorDTO;
import com.example.dto.MajorGroupDTO;
import com.example.dto.SubjectGroupDTO;
import com.example.gui.components.AppTheme;
import com.example.gui.components.CustomTable;
import com.example.gui.components.OverlayUtil;
import com.example.gui.components.ImportPanel;
import com.example.gui.components.Pagination;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Comparator;

public class MajorWeightManagementPanel extends JPanel {
	private static final int PAGE_SIZE = 20;

	private final MajorGroupBUS majorGroupBUS;
	private final MajorBUS majorBUS;
	private final SubjectGroupBUS subjectGroupBUS;

	private DefaultTableModel tableModel;
	private CustomTable table;

	private List<MajorGroupDTO> allRows;
	private List<MajorGroupDTO> filteredRows;
	private Map<String, MajorDTO> majorByCode;
	private Pagination pagination;
	private JLabel pageInfoLabel;
	private int currentPage = 0;

	public MajorWeightManagementPanel() {
		this.majorGroupBUS = new MajorGroupBUS();
		this.majorBUS = new MajorBUS();
		this.subjectGroupBUS = new SubjectGroupBUS();

		setLayout(new BorderLayout(15, 15));
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		add(buildHeader(), BorderLayout.NORTH);
		add(buildContent(), BorderLayout.CENTER);

		loadData();
	}

	private JPanel buildHeader() {
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setOpaque(false);

		// Text + controls stacked like other panels
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setOpaque(false);
		textPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel title = new JLabel("Weight Setup");
		title.setFont(AppTheme.FONT_HEADLINE);
		title.setForeground(AppTheme.TEXT_DARK);

		JLabel subtitle = new JLabel("Gán tổ hợp và hệ số cho từng ngành.");
		subtitle.setFont(AppTheme.FONT_BODY);
		subtitle.setForeground(AppTheme.SECONDARY);

		textPanel.add(title);
		textPanel.add(Box.createVerticalStrut(3));
		textPanel.add(subtitle);

		JPanel controlsPanel = new JPanel(new BorderLayout());
		controlsPanel.setOpaque(false);
		controlsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		searchWrapper.setOpaque(false);
		JTextField txtSearch = new JTextField(22);
		txtSearch.setText("Tìm kiếm theo mã ngành / tổ hợp...");
		txtSearch.setForeground(Color.decode("#999999"));
		txtSearch.setPreferredSize(new Dimension(260, 32));
		txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent e) {
				if ("Tìm kiếm theo mã ngành / tổ hợp...".equals(txtSearch.getText())) {
					txtSearch.setText("");
					txtSearch.setForeground(Color.BLACK);
				}
			}

			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
				if (txtSearch.getText().trim().isEmpty()) {
					txtSearch.setText("Tìm kiếm theo mã ngành / tổ hợp...");
					txtSearch.setForeground(Color.decode("#999999"));
				}
			}
		});
		txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent e) {
				onSearchChange(txtSearch);
			}
		});
		searchWrapper.add(txtSearch);
		controlsPanel.add(searchWrapper, BorderLayout.WEST);

		JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonGroup.setOpaque(false);

		JButton btnReload = new JButton("⟳ Tải lại");
        btnReload.setBackground(Color.decode("#0066CC"));
        btnReload.setForeground(Color.WHITE);
        btnReload.setOpaque(true);
        btnReload.setBorderPainted(false);
        btnReload.setPreferredSize(new Dimension(130, 32));
        btnReload.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReload.addActionListener(e -> {
            loadData();
            Window owner = SwingUtilities.getWindowAncestor(this);
            if (owner != null) {
                JOptionPane.showMessageDialog(owner, "Dữ liệu đã được tải lại", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        buttonGroup.add(btnReload);

		JButton btnAdd = new JButton("➕ Thêm Hệ số");
        btnAdd.setBackground(Color.decode("#0066CC"));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setOpaque(true);
        btnAdd.setBorderPainted(false);
        btnAdd.setPreferredSize(new Dimension(130, 32));
		btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnAdd.addActionListener(e -> showAddOverlay());

		buttonGroup.add(btnReload);
		buttonGroup.add(btnAdd);
		controlsPanel.add(buttonGroup, BorderLayout.EAST);

		JPanel mainHeaderWrapper = new JPanel();
		mainHeaderWrapper.setLayout(new BoxLayout(mainHeaderWrapper, BoxLayout.Y_AXIS));
		mainHeaderWrapper.setOpaque(false);
		mainHeaderWrapper.add(textPanel);
		mainHeaderWrapper.add(Box.createVerticalStrut(12));
		mainHeaderWrapper.add(controlsPanel);

		headerPanel.add(mainHeaderWrapper, BorderLayout.CENTER);
		return headerPanel;
	}

	private JComponent buildContent() {
		JPanel panel = new JPanel(new BorderLayout(0, 10));
		panel.setOpaque(false);

		JComponent tableComp = buildTable();
		panel.add(tableComp, BorderLayout.CENTER);

		JPanel paginationWrapper = new JPanel(new BorderLayout());
		paginationWrapper.setOpaque(false);

		pagination = new Pagination(1, 1);
		pagination.addPageChangeListener(newPage -> {
			currentPage = newPage - 1;
			refreshTable();
			updatePaginationInfo();
		});
		paginationWrapper.add(pagination, BorderLayout.CENTER);

		panel.add(paginationWrapper, BorderLayout.SOUTH);
		return panel;
	}

	private JComponent buildTable() {
		tableModel = new DefaultTableModel(
				new Object[]{"MÃ NGÀNH", "TÊN NGÀNH", "TỔ HỢP", "MÔN 1 & HỆ SỐ", "MÔN 2 & HỆ SỐ", "MÔN 3 & HỆ SỐ", "ACTIONS"},
				0
		) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 6;
			}
		};

		table = new CustomTable(tableModel);
		table.setRowHeight(34);
		table.getColumnModel().getColumn(0).setPreferredWidth(110);
		table.getColumnModel().getColumn(1).setPreferredWidth(220);
		table.getColumnModel().getColumn(2).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setPreferredWidth(160);
		table.getColumnModel().getColumn(4).setPreferredWidth(160);
		table.getColumnModel().getColumn(5).setPreferredWidth(160);
		table.getColumnModel().getColumn(6).setPreferredWidth(120);

		table.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
		table.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());

		JScrollPane scrollPane = new JScrollPane(table);
		// Keep default LAF border/colors
		return scrollPane;
	}

	private void loadData() {
		allRows = majorGroupBUS.getAll().stream()
				.sorted(Comparator.comparing(
						MajorGroupDTO::getMaNganh,
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
				))
				.toList();
		filteredRows = allRows;
		majorByCode = new HashMap<>();
		for (MajorDTO major : majorBUS.getAll()) {
			if (major.getMaNganh() != null) {
				majorByCode.put(major.getMaNganh(), major);
			}
		}
		currentPage = 0;
		updatePaginationState();
		refreshTable();
		updatePaginationInfo();
	}

	private void refreshTable() {
		tableModel.setRowCount(0);
		if (filteredRows == null || filteredRows.isEmpty()) {
			return;
		}
		int start = currentPage * PAGE_SIZE;
		int end = Math.min(start + PAGE_SIZE, filteredRows.size());
		for (int i = start; i < end; i++) {
			MajorGroupDTO mg = filteredRows.get(i);
			String maNganh = mg.getMaNganh();
			MajorDTO major = maNganh != null ? majorByCode.get(maNganh) : null;
			String tenNganh = major != null ? major.getTenNganh() : "-";

			Object[] rowData = new Object[]{
					maNganh != null ? maNganh : "-",
					tenNganh != null ? tenNganh : "-",
					mg.getMaToHop() != null ? mg.getMaToHop() : "-",
					formatSubjectWeight(mg.getThMon1(), mg.getHsMon1()),
					formatSubjectWeight(mg.getThMon2(), mg.getHsMon2()),
					formatSubjectWeight(mg.getThMon3(), mg.getHsMon3()),
					mg
			};
			tableModel.addRow(rowData);
		}
	}

	private void onSearchChange(JTextField txtSearch) {
		String keyword = txtSearch.getText().trim().toLowerCase();
		if (keyword.isEmpty() || "tìm kiếm theo mã ngành / tổ hợp...".equals(keyword)) {
			filteredRows = allRows;
			currentPage = 0;
			updatePaginationState();
			refreshTable();
			updatePaginationInfo();
			return;
		}

		filteredRows = allRows.stream()
				.filter(mg -> (mg.getMaNganh() != null && mg.getMaNganh().toLowerCase().contains(keyword))
						|| (mg.getMaToHop() != null && mg.getMaToHop().toLowerCase().contains(keyword)))
				.sorted(Comparator.comparing(
						MajorGroupDTO::getMaNganh,
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
				))
				.toList();
		currentPage = 0;
		updatePaginationState();
		refreshTable();
		updatePaginationInfo();
	}

	private void updatePaginationState() {
		int total = filteredRows == null ? 0 : filteredRows.size();
		int totalPages = (int) Math.ceil((double) Math.max(0, total) / PAGE_SIZE);
		if (totalPages == 0) {
			totalPages = 1;
		}
		if (pagination != null) {
			pagination.setTotalPages(totalPages);
			pagination.setCurrentPage(currentPage + 1);
		}
	}

	private void updatePaginationInfo() {
		if (pageInfoLabel == null) {
			return;
		}
		int total = filteredRows == null ? 0 : filteredRows.size();
		int start = currentPage * PAGE_SIZE + 1;
		int end = Math.min((currentPage + 1) * PAGE_SIZE, total);
	}

	private String formatSubjectWeight(String subjectCode, Integer weight) {
		String subject = subjectCode != null ? subjectCode.trim() : "-";
		String display = SUBJECT_DISPLAY.getOrDefault(subject.toUpperCase(Locale.ROOT), subject);
		int w = weight != null ? weight : 1;
		return display + " (x" + w + ")";
	}

	private void showAddOverlay() {
		JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
		if (owner == null) {
			return;
		}

		MajorWeightCreatePanel form = new MajorWeightCreatePanel(saved -> {
			try {
				// Unique key: maNganh_maToHop
				String key = saved.getMaNganh() + "_" + saved.getMaToHop();
				MajorGroupDTO existing = majorGroupBUS.findByTbKeys(key);
				if (existing == null) {
					existing = majorGroupBUS.findByMaNganhAndMaToHop(saved.getMaNganh(), saved.getMaToHop());
				}
				if (existing != null) {
					JOptionPane.showMessageDialog(owner,
							"Ngành này đã được gán tổ hợp này rồi, vui lòng chọn sửa dòng hiện tại.",
							"Lỗi",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				saved.setTbKeys(key);
				majorGroupBUS.save(saved);
				JOptionPane.showMessageDialog(owner, "Saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
				OverlayUtil.hideOverlay(owner);
				loadData();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(owner, "Không thể lưu mapping: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		}, () -> OverlayUtil.hideOverlay(owner));

		OverlayUtil.showOverlay(owner, "Thiết lập hệ số", form, new Dimension(720, 520));
	}

	private void showEditOverlay(MajorGroupDTO existing) {
		JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
		if (owner == null || existing == null) {
			return;
		}

		MajorWeightEditPanel form = new MajorWeightEditPanel(existing, saved -> {
			try {
				// Keep the same key on edit
				String key = existing.getMaNganh() + "_" + existing.getMaToHop();
				saved.setId(existing.getId());
				saved.setMaNganh(existing.getMaNganh());
				saved.setMaToHop(existing.getMaToHop());
				saved.setTbKeys(key);
				saved.setDoLech(existing.getDoLech());
				saved.setN1(existing.getN1());
				saved.setTo(existing.getTo());
				saved.setLi(existing.getLi());
				saved.setHo(existing.getHo());
				saved.setSi(existing.getSi());
				saved.setVa(existing.getVa());
				saved.setSu(existing.getSu());
				saved.setDi(existing.getDi());
				saved.setTi(existing.getTi());
				saved.setKhac(existing.getKhac());
				saved.setKtpl(existing.getKtpl());
				majorGroupBUS.update(saved);
				JOptionPane.showMessageDialog(owner, "Saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
				OverlayUtil.hideOverlay(owner);
				loadData();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(owner, "Không thể lưu mapping: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		}, () -> OverlayUtil.hideOverlay(owner));

		OverlayUtil.showOverlay(owner, "Sửa hệ số", form, new Dimension(720, 520));
	}

	private void deleteRow(MajorGroupDTO item) {
		if (item == null) {
			return;
		}
		JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
		int confirm = JOptionPane.showConfirmDialog(this, "Xóa mapping này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}
		try {
			majorGroupBUS.delete(item);
			loadData();
			if (owner != null) {
				JOptionPane.showMessageDialog(owner, "Deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception ex) {
			if (owner != null) {
				JOptionPane.showMessageDialog(owner, "Không thể xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void showImportOverlay() {
		JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
		if (owner == null) return;

		ImportPanel importPanel = new ImportPanel(file -> {
			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				@Override
				protected Void doInBackground() throws Exception {
					majorGroupBUS.importFromExcelFile(file);
					return null;
				}

				@Override
				protected void done() {
					try {
						get();
						JOptionPane.showMessageDialog(owner, "Import hoàn tất", "Success", JOptionPane.INFORMATION_MESSAGE);
						loadData();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(owner, "Import thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
					}
					OverlayUtil.hideOverlay(owner);
				}
			};
			worker.execute();
		}, () -> OverlayUtil.hideOverlay(owner));

		OverlayUtil.showOverlay(owner, "Nhập Excel - Hệ số", importPanel, new Dimension(720, 480));
	}

	private class ActionCellRenderer extends JPanel implements TableCellRenderer {
		public ActionCellRenderer() {
			setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			removeAll();
			add(createActionButton("Sửa", "/assets/images/edit.png", AppTheme.TERTIARY));
			add(createActionButton("Xóa", "/assets/images/delete.png", AppTheme.DANGER));
			return this;
		}
	}

	private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
		private final JPanel panel;
		private final JButton btnEdit;
		private final JButton btnDelete;
		private MajorGroupDTO currentItem;

		public ActionCellEditor() {
			panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
			panel.setOpaque(true);

			btnEdit = createActionButton("Sửa", "/assets/images/edit.png", AppTheme.TERTIARY);
			btnDelete = createActionButton("Xóa", "/assets/images/delete.png", AppTheme.DANGER);

			btnEdit.addActionListener(e -> {
				if (currentItem != null) {
					showEditOverlay(currentItem);
				}
				stopCellEditing();
			});

			btnDelete.addActionListener(e -> {
				if (currentItem != null) {
					deleteRow(currentItem);
				}
				stopCellEditing();
			});

			panel.add(btnEdit);
			panel.add(btnDelete);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			currentItem = value instanceof MajorGroupDTO ? (MajorGroupDTO) value : null;
			panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			return panel;
		}

		@Override
		public Object getCellEditorValue() {
			return currentItem;
		}
	}

	private ImageIcon loadScaledIcon(String resourcePath, int width, int height) {
		java.net.URL resource = getClass().getResource(resourcePath);
		if (resource == null) {
			return null;
		}
		ImageIcon icon = new ImageIcon(resource);
		Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}

	private JButton createActionButton(String tooltip, String iconPath, Color accentColor) {
		JButton button = new JButton();
		ImageIcon icon = loadScaledIcon(iconPath, 16, 16);
		if (icon != null) {
			button.setIcon(icon);
		} else {
			button.setText(tooltip);
		}
		button.setToolTipText(tooltip);
		button.setPreferredSize(new Dimension(26, 26));
		button.setMargin(new Insets(1, 1, 1, 1));
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setOpaque(false);
		button.setForeground(accentColor);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return button;
	}

    

	public static final Map<String, String> SUBJECT_DISPLAY = Map.ofEntries(
			Map.entry("TO", "Toán"),
			Map.entry("LI", "Vật lý"),
			Map.entry("HO", "Hóa học"),
			Map.entry("VA", "Ngữ văn"),
			Map.entry("SI", "Sinh học"),
			Map.entry("SU", "Lịch sử"),
			Map.entry("DI", "Địa lý"),
			Map.entry("TI", "Tin học"),
			Map.entry("KTPL", "KTPL"),
			Map.entry("N1", "Ngoại ngữ"),
			Map.entry("CNNN", "CN nghề"),
			Map.entry("CNCN", "CN công nghiệp")
	);
}
