package com.example.gui.pages.majorcombination;

import com.example.bus.AdmissionProcessBUS;
import com.example.bus.MajorBUS;
import com.example.bus.AspirationBUS;
import com.example.dto.MajorDTO;
import com.example.gui.components.CustomTable;
import com.example.gui.components.ImportPanel;
import com.example.gui.components.OverlayUtil;
import com.example.gui.components.Pagination;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MajorManagementPanel extends JPanel {
    private static final int PAGE_SIZE = 20;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###");

    private final MajorBUS majorBUS;
    private final AspirationBUS aspirationBUS;
    private DefaultTableModel majorsTableModel;
    private CustomTable majorsTable;
    private Pagination pagination;
    private List<MajorDTO> allMajors;
    private int currentPage = 0;

    public MajorManagementPanel() {
        this.majorBUS = new MajorBUS();
        this.aspirationBUS = new AspirationBUS();

        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadMajorsData();
    }

    private JPanel buildHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Khối chữ tiêu đề và sub-title căn trái
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Majors Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.decode("#0F172A"));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subTitleLabel = new JLabel("Quản lý danh mục ngành đào tạo.");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subTitleLabel.setForeground(Color.decode("#64748B"));
        subTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subTitleLabel);
        
        // Khối thanh công cụ chứa tìm kiếm và nút bấm
        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.setOpaque(false);
        controlsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Ô tìm kiếm nằm ở bên trái (WEST)
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchWrapper.setOpaque(false);

        JTextField txtSearch = new JTextField(22);
        txtSearch.setText("Tìm kiếm theo mã/tên ngành...");
        txtSearch.setForeground(Color.decode("#999999"));
        txtSearch.setPreferredSize(new Dimension(230, 32));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if ("Tìm kiếm theo mã/tên ngành...".equals(txtSearch.getText())) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText("Tìm kiếm theo mã/tên ngành...");
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

        // Nhóm nút bấm nằm ở bên phải (EAST)
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
            loadMajorsData();
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (owner != null) {
                JOptionPane.showMessageDialog(owner, "Dữ liệu đã được tải lại", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnImport = new JButton("📥 Nhập Excel");
        btnImport.setBackground(Color.decode("#0066CC"));
        btnImport.setForeground(Color.WHITE);
        btnImport.setOpaque(true);
        btnImport.setBorderPainted(false);
        btnImport.setPreferredSize(new Dimension(130, 32));
        btnImport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnImport.addActionListener(e -> showMajorImportOverlay());

        JButton btnAdd = new JButton("+ Thêm Ngành");
        btnAdd.setBackground(Color.decode("#0066CC"));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setOpaque(true);
        btnAdd.setBorderPainted(false);
        btnAdd.setPreferredSize(new Dimension(140, 32));
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> showMajorFormOverlay(null));

        buttonGroup.add(btnReload);
        buttonGroup.add(btnImport);
        buttonGroup.add(btnAdd);
        controlsPanel.add(buttonGroup, BorderLayout.EAST);

        // Gom nhóm textPanel và controlsPanel theo trục dọc
        JPanel mainHeaderWrapper = new JPanel();
        mainHeaderWrapper.setLayout(new BoxLayout(mainHeaderWrapper, BoxLayout.Y_AXIS));
        mainHeaderWrapper.setOpaque(false);
        
        mainHeaderWrapper.add(textPanel);
        mainHeaderWrapper.add(Box.createVerticalStrut(12)); 
        mainHeaderWrapper.add(controlsPanel);

        headerPanel.add(mainHeaderWrapper, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Hiển thị thêm CHỈ TIÊU và ĐIỂM SÀN trên giao diện
        majorsTableModel = new DefaultTableModel(
            new Object[]{"MÃ NGÀNH", "TÊN NGÀNH", "HĐT", "CHỈ TIÊU", "ĐIỂM SÀN", "ĐIỂM TRÚNG TUYỂN", "ACTIONS"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        majorsTable = new CustomTable(majorsTableModel);
        majorsTable.setRowHeight(45);
        majorsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        majorsTable.getTableHeader().setBackground(Color.decode("#F8FAFC"));
        majorsTable.getTableHeader().setForeground(Color.decode("#64748B"));

        majorsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        majorsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        majorsTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        majorsTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        majorsTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        majorsTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        majorsTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        majorsTable.getColumnModel().getColumn(6).setCellRenderer(new MajorActionCellRenderer());
        majorsTable.getColumnModel().getColumn(6).setCellEditor(new MajorActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(majorsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0")));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel paginationWrapper = new JPanel(new BorderLayout());
        paginationWrapper.setOpaque(false);

        pagination = new Pagination(1, 1);
        pagination.addPageChangeListener(newPage -> {
            currentPage = newPage - 1;
            refreshMajorsTable();
        });

        paginationWrapper.add(pagination, BorderLayout.CENTER);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(paginationWrapper, BorderLayout.SOUTH);

        return panel;
    }

    private void onSearchChange(JTextField txtSearch) {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty() || "tìm kiếm theo mã/tên ngành...".equals(keyword)) {
            loadMajorsData();
            return;
        }

        List<MajorDTO> all = majorBUS.getAll();
        allMajors = all.stream()
                .filter(m -> (m.getMaNganh() != null && m.getMaNganh().toLowerCase().contains(keyword))
                        || (m.getTenNganh() != null && m.getTenNganh().toLowerCase().contains(keyword)))
                .sorted(Comparator.comparing(
                        MajorDTO::getMaNganh,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .toList();
        currentPage = 0;
        updatePaginationState();
        refreshMajorsTable();
    }

    private void loadMajorsData() {
        allMajors = majorBUS.getAll().stream()
                .sorted(Comparator.comparing(
                        MajorDTO::getMaNganh,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .toList();
        currentPage = 0;
        updatePaginationState();
        refreshMajorsTable();
    }

    private void updatePaginationState() {
        int totalPages = (int) Math.ceil((double) Math.max(0, allMajors == null ? 0 : allMajors.size()) / PAGE_SIZE);
        if (totalPages == 0) {
            totalPages = 1;
        }
        pagination.setTotalPages(totalPages);
        pagination.setCurrentPage(currentPage + 1);
    }

    private void refreshMajorsTable() {
        majorsTableModel.setRowCount(0);
        if (allMajors == null || allMajors.isEmpty()) {
            return;
        }

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allMajors.size());

        for (int i = start; i < end; i++) {
            MajorDTO major = allMajors.get(i);
            majorsTableModel.addRow(new Object[]{
                    major.getMaNganh(),
                    major.getTenNganh(),
                    extractTrainingSystem(major.getMaNganh(), major.getTenNganh()), // Nhận diện tự động lên UI
                    major.getNChiTieu(),
                    major.getNDiemSan() != null ? major.getNDiemSan().toPlainString() : "-",
                    major.getNDiemTrungTuyen() != null ? major.getNDiemTrungTuyen().toPlainString() : "-",
                    major
            });
        }
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "-" : DECIMAL_FORMAT.format(value);
    }

    private void showMajorDetailsDialog(MajorDTO major) {
        if (major == null) {
            return;
        }

        long registeredCount = aspirationBUS.countRegisteredByMaNganh(major.getMaNganh());

        JDialog dialog = new JDialog(getOwnerFrame(), "Chi tiết ngành", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(840, 560);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E2E8F0")),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addDetailRow(infoPanel, gbc, 0, 0, "Tên ngành", major.getTenNganh());
        addDetailRow(infoPanel, gbc, 1, 0, "Mã ngành", major.getMaNganh());
        addDetailRow(infoPanel, gbc, 2, 0, "Chỉ tiêu", String.valueOf(major.getNChiTieu()));
        addDetailRow(infoPanel, gbc, 3, 0, "Điểm sàn", formatDecimal(major.getNDiemSan()));
        addDetailRow(infoPanel, gbc, 4, 0, "Điểm trúng tuyển", formatDecimal(major.getNDiemTrungTuyen()));
        addDetailRow(infoPanel, gbc, 5, 0, "Số NV đăng kí", String.valueOf(registeredCount));
        addDetailRow(infoPanel, gbc, 6, 0, "Tổ hợp gốc", major.getNToHopGoc() != null ? major.getNToHopGoc() : "-");

        DefaultTableModel methodModel = new DefaultTableModel(new Object[]{"Phương thức xét tuyển", "Số thí sinh trúng tuyển"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Map<String, Long> methodSummary = new LinkedHashMap<>();
        long slThpt = 0L;
        try {
            if (major.getSlThpt() != null && !major.getSlThpt().isBlank()) {
                slThpt = Long.parseLong(major.getSlThpt().trim());
            }
        } catch (NumberFormatException ignored) {}
        
        methodSummary.put("THPT", slThpt);
        methodSummary.put("DGNL", major.getSlDgnl() != null ? major.getSlDgnl().longValue() : 0L);
        methodSummary.put("VSAT", major.getSlVsat() != null ? major.getSlVsat().longValue() : 0L);

        methodSummary.forEach((method, count) -> methodModel.addRow(new Object[]{method, count}));

        JTable methodTable = new JTable(methodModel);
        methodTable.setRowHeight(28);
        methodTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        methodTable.getTableHeader().setBackground(Color.decode("#F8FAFC"));
        methodTable.getTableHeader().setForeground(Color.decode("#334155"));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        JLabel methodLabel = new JLabel("Phương thức xét tuyển của thí sinh trúng tuyển");
        methodLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        centerPanel.add(methodLabel, BorderLayout.NORTH);
        JScrollPane methodScrollPane = new JScrollPane(methodTable);
        methodScrollPane.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0")));
        centerPanel.add(methodScrollPane, BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnClose);

        root.add(infoPanel, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, int col, String label, String value) {
        gbc.gridy = row;
        gbc.gridx = col * 2;
        gbc.weightx = 0;

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.decode("#475569"));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl.setPreferredSize(new Dimension(120, 30));
        panel.add(lbl, gbc);

        gbc.gridx = col * 2 + 1;
        gbc.weightx = 1;
        JTextField field = new JTextField(value != null ? value : "-");
        field.setEditable(false);
        field.setBackground(new Color(248, 250, 252));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#CBD5E1")),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        panel.add(field, gbc);
    }

    private String normalizeMethodLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            return "THPT";
        }

        String method = raw.trim().toUpperCase();
        if (method.contains("DGNL") || method.contains("ĐGNL") || method.contains("NL")) {
            return "DGNL";
        }
        if (method.contains("VSAT") || method.contains("V-SAT") || method.contains("SAT") || method.contains("TUYEN THANG")) {
            return "VSAT";
        }
        return "THPT";
    }

    // Hàm phân tích chuỗi tự động hiển thị hệ đào tạo dựa trên mã hoặc tên ngành
    private String extractTrainingSystem(String majorCode, String majorName) {
        String code = majorCode != null ? majorCode.toUpperCase() : "";
        String name = majorName != null ? majorName.toUpperCase() : "";
        
        if (code.contains("CLC") || name.contains("CLC")) {
            return "CLC";
        }
        return "Đại trà";
    }

    private void showMajorImportOverlay() {
        JFrame owner = getOwnerFrame();
        if (owner == null) {
            return;
        }

        ImportPanel importPanel = new ImportPanel(
                file -> {
                    Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                    owner.setCursor(waitCursor);

                    SwingWorker<String, Void> worker = new SwingWorker<>() {
                        @Override
                        protected String doInBackground() {
                            try {
                                List<MajorDTO> imported = majorBUS.importFromExcelFile(file);
                                if (imported.isEmpty()) {
                                    return "Không có ngành mới được import. Dữ liệu có thể đã tồn tại hoặc file không đúng format.";
                                }
                                return "Đã import thành công " + imported.size() + " ngành.";
                            } catch (Exception e) {
                                return "Lỗi import file: " + e.getMessage();
                            }
                        }

                        @Override
                        protected void done() {
                            owner.setCursor(Cursor.getDefaultCursor());
                            try {
                                JOptionPane.showMessageDialog(owner, get(), "Kết quả import", JOptionPane.INFORMATION_MESSAGE);
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(owner, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                            loadMajorsData();
                            syncAdmissionResults(owner);
                            OverlayUtil.hideOverlay(owner);
                        }
                    };
                    worker.execute();
                },
                () -> OverlayUtil.hideOverlay(owner)
        );

        OverlayUtil.showOverlay(owner, "Import Majors", importPanel, new Dimension(750, 380));
    }

    private void syncAdmissionResults(JFrame owner) {
        SwingWorker<Void, Void> syncWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                new AdmissionProcessBUS().thucHienXetTuyen();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadMajorsData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            MajorManagementPanel.this,
                            "Đã import ngành nhưng đồng bộ xét tuyển thất bại: " + ex.getMessage(),
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        };
        syncWorker.execute();
    }

    private void showMajorFormOverlay(MajorDTO existing) {
        JFrame owner = getOwnerFrame();
        if (owner == null) {
            return;
        }

        if (existing == null) {
            MajorCreatePanel createPanel = new MajorCreatePanel(
                    major -> {
                        loadMajorsData();
                        OverlayUtil.hideOverlay(owner);
                    },
                    () -> OverlayUtil.hideOverlay(owner)
            );
            OverlayUtil.showOverlay(owner, "Thêm ngành mới", createPanel, new Dimension(620, 420));
        } else {
            MajorEditPanel editPanel = new MajorEditPanel(
                    existing,
                    major -> {
                        loadMajorsData();
                        OverlayUtil.hideOverlay(owner);
                    },
                    () -> OverlayUtil.hideOverlay(owner)
            );
            OverlayUtil.showOverlay(owner, "Sửa ngành", editPanel, new Dimension(620, 420));
        }
    }

    private JFrame getOwnerFrame() {
        Window window = SwingUtilities.getWindowAncestor(this);
        return window instanceof JFrame ? (JFrame) window : null;
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

    private class MajorActionCellRenderer extends JPanel implements TableCellRenderer {
        public MajorActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
            setOpaque(true);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.decode("#E2E8F0")));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            removeAll();
            add(createActionButton("Sửa", "/assets/images/edit.png", Color.decode("#0066CC")));
            add(createActionButton("Chi tiết", "/assets/images/info.png", Color.decode("#198754")));
            add(createActionButton("Xóa", "/assets/images/delete.png", Color.decode("#DC3545")));
            return this;
        }
    }

    private class MajorActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton btnEdit;
        private final JButton btnDelete;
        private MajorDTO currentItem;

        public MajorActionCellEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.decode("#E2E8F0")));

            btnEdit = createActionButton("Sửa", "/assets/images/edit.png", Color.decode("#0066CC"));
            JButton btnDetails = createActionButton("Chi tiết", "/assets/images/info.png", Color.decode("#198754"));
            btnDelete = createActionButton("Xóa", "/assets/images/delete.png", Color.decode("#DC3545"));

            btnEdit.addActionListener(e -> {
                if (currentItem != null) {
                    showMajorFormOverlay(currentItem);
                }
                stopCellEditing();
            });

            btnDetails.addActionListener(e -> {
                if (currentItem != null) {
                    showMajorDetailsDialog(currentItem);
                }
                stopCellEditing();
            });

            btnDelete.addActionListener(e -> {
                if (currentItem != null) {
                    int confirm = JOptionPane.showConfirmDialog(
                            MajorManagementPanel.this,
                            "Xóa ngành này?",
                            "Xác nhận",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        majorBUS.delete(currentItem);
                        loadMajorsData();
                    }
                }
                stopCellEditing();
            });

            panel.add(btnEdit);
            panel.add(btnDetails);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentItem = value instanceof MajorDTO ? (MajorDTO) value : null;
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentItem;
        }
    }
}