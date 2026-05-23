package com.example.gui.pages.majorcombination;

import com.example.bus.MajorBUS;
import com.example.bus.MajorGroupBUS;
import com.example.bus.SubjectGroupBUS;
import com.example.dto.MajorDTO;
import com.example.dto.MajorGroupDTO;
import com.example.dto.SubjectGroupDTO;
import com.example.gui.components.CustomTable;
import com.example.gui.components.ImportPanel;
import com.example.gui.components.OverlayUtil;
import com.example.gui.components.Pagination;
import com.example.gui.pages.majorcombination.MajorGroupCreatePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MajorGroupManagementPage extends JPanel {
    private final MajorGroupBUS majorGroupBUS;
    private final MajorBUS majorBUS;
    private final SubjectGroupBUS subjectGroupBUS;
    private JTabbedPane tabbedPane;
    private Map<String, MajorDTO> majorByCode = new HashMap<>();
    
    // --- Combinations Tab ---
    private DefaultTableModel combinationsTableModel;
    private CustomTable combinationsTable;
    private List<MajorGroupDTO> allCombinations;
    private JLabel pageInfoLabel;
    private Pagination pagination;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    public MajorGroupManagementPage() {
        this.majorGroupBUS = new MajorGroupBUS();
        this.majorBUS = new MajorBUS();
        this.subjectGroupBUS = new SubjectGroupBUS();
        
        setLayout(new BorderLayout());
        setOpaque(false);

        // --- Tabbed Pane ---
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setOpaque(false);

        // Tab 1: Majors
        tabbedPane.addTab("Majors", createMajorsPanel());
        
        // Tab 2: Combinations (mặc định)
        tabbedPane.addTab("Combinations", createCombinationsPanel());
        tabbedPane.setSelectedIndex(1);
        
        // Tab 3: Weight Setup
        tabbedPane.addTab("Weight Setup", createWeightSetupPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCombinationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);

        JLabel titleLabel = new JLabel("Majors & Combinations");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.decode("#0F172A"));

        JLabel subTitleLabel = new JLabel("quản lý tổ hợp ngành và tổ hợp môn thi");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        leftHeader.add(titleLabel);
        leftHeader.add(Box.createVerticalStrut(3));
        leftHeader.add(subTitleLabel);

        headerPanel.add(leftHeader, BorderLayout.WEST);

        // --- SEARCH AND ACTION BUTTONS ---
        JPanel searchActionPanel = new JPanel(new BorderLayout()); 
        searchActionPanel.setOpaque(false);

        JTextField txtSearch = new JTextField(20);
        txtSearch.setText("Tim kiếm theo mã tổ hợp...");
        txtSearch.setForeground(Color.decode("#999999"));
        txtSearch.setPreferredSize(new Dimension(200, 32));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().equals("Tim kiếm theo mã tổ hợp...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tim kiếm theo mã tổ hợp...");
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
        searchActionPanel.add(txtSearch, BorderLayout.WEST);

        JButton btnReload = new JButton("⟳ Tải lại");
        btnReload.setBackground(Color.decode("#0066CC"));
        btnReload.setForeground(Color.WHITE);
        btnReload.setOpaque(true);
        btnReload.setBorderPainted(false);
        btnReload.setPreferredSize(new Dimension(130, 32));
        btnReload.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReload.addActionListener(e -> {
            loadCombinationsData();
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
        
        JButton btnAdd = new JButton("+ Thêm Tổ Hợp");
        btnAdd.setBackground(Color.decode("#0066CC"));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setOpaque(true);
        btnAdd.setBorderPainted(false);
        btnAdd.setPreferredSize(new Dimension(140, 32));

        btnImport.addActionListener(e -> showImportOverlay());
        btnAdd.addActionListener(e -> showAddEditOverlay(null));

        JPanel buttonGroupsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonGroupsPanel.setOpaque(false);
        buttonGroupsPanel.add(btnReload);
        buttonGroupsPanel.add(btnImport);
        buttonGroupsPanel.add(btnAdd);

        searchActionPanel.add(buttonGroupsPanel, BorderLayout.EAST);

        // --- TABLE ---
        combinationsTableModel = new DefaultTableModel(
            new Object[]{"MÃ TỔ HỢP", "MÔN THI 1", "MÔN THI 2", "MÔN THI 3", "MÃ NGÀNH", "TÊN NGÀNH", "ĐỘ LỆCH", "ACTIONS"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
            return column == 7;
            }
        };

        combinationsTable = new CustomTable(combinationsTableModel);
        combinationsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        combinationsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        combinationsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        combinationsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        combinationsTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        combinationsTable.getColumnModel().getColumn(5).setPreferredWidth(260);
        combinationsTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        combinationsTable.getColumnModel().getColumn(7).setPreferredWidth(120);
        combinationsTable.setRowHeight(34);
        combinationsTable.getColumnModel().getColumn(7).setCellRenderer(new ActionCellRenderer());
        combinationsTable.getColumnModel().getColumn(7).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(combinationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0")));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- PAGINATION ---
       if (allCombinations == null) {
            allCombinations = majorGroupBUS.getAll();
        }
        
        int totalSize = (allCombinations != null) ? allCombinations.size() : 0;
        int totalPages = (int) Math.ceil((double) totalSize / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        
        pagination = new Pagination(totalPages, currentPage + 1);
        pagination.addPageChangeListener(newPage -> {
            currentPage = newPage - 1;
            refreshCombinationsTable();
            updatePaginationInfo(pageInfoLabel);
        });

        JPanel paginationWrapper = new JPanel(new BorderLayout());
        paginationWrapper.setOpaque(false);
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        pageInfoLabel = new JLabel("Showing 1 to 1 of 1 entries");
        pageInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pageInfoLabel.setForeground(Color.decode("#64748B"));
        leftPanel.add(pageInfoLabel);
        
        paginationWrapper.add(pagination, BorderLayout.CENTER);
        
        updatePaginationInfo(pageInfoLabel);

        // --- FOOTER ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel contentWrapper = new JPanel(new BorderLayout(0, 10));
        contentWrapper.setOpaque(false);
        contentWrapper.add(searchActionPanel, BorderLayout.NORTH);
        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        contentWrapper.add(paginationWrapper, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentWrapper, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        loadCombinationsData();
        return panel;
    }

    private void updatePaginationInfo(JLabel pageInfoLabel) {
        if (allCombinations == null || allCombinations.isEmpty()) {
            pageInfoLabel.setText("Showing 0 to 0 of 0 entries");
            return;
        }
        int totalPages = (int) Math.ceil((double) allCombinations.size() / PAGE_SIZE);
        int start = currentPage * PAGE_SIZE + 1;
        int end = Math.min((currentPage + 1) * PAGE_SIZE, allCombinations.size());
        pageInfoLabel.setText(String.format("Showing %d to %d of %d entries", start, end, allCombinations.size()));
    }

    private void onSearchChange(JTextField txtSearch) {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty() || keyword.equals("Search by Combo Code...".toLowerCase())) {
            loadCombinationsData();
            return;
        }
        
        // Filter by combo code (maToHop) or major (maNganh)
        List<MajorGroupDTO> all = majorGroupBUS.getAll();
        allCombinations = all.stream()
            .filter(mg -> (mg.getMaToHop() != null && mg.getMaToHop().toLowerCase().contains(keyword)) ||
                         (mg.getMaNganh() != null && mg.getMaNganh().toLowerCase().contains(keyword)))
            .sorted(Comparator.comparing(
                    MajorGroupDTO::getMaToHop,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ))
            .toList();
        currentPage = 0;
        if (pagination != null) {
            int totalPages = (int) Math.ceil((double) allCombinations.size() / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;
            pagination.setTotalPages(totalPages);
            pagination.setCurrentPage(1);
        }
        if (pageInfoLabel != null) {
            updatePaginationInfo(pageInfoLabel);
        }
        refreshCombinationsTable();
    }

    private JPanel createMajorsPanel() {
        return new MajorManagementPanel();
    }

    private JPanel createWeightSetupPanel() {
        return new MajorWeightManagementPanel();
    }

    private void loadCombinationsData() {
        allCombinations = majorGroupBUS.getAll().stream()
            .sorted(Comparator.comparing(
                MajorGroupDTO::getMaToHop,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ))
            .toList();
        majorByCode = new HashMap<>();
        for (MajorDTO major : majorBUS.getAll()) {
            if (major.getMaNganh() != null) {
                majorByCode.put(major.getMaNganh(), major);
            }
        }
        currentPage = 0;
        refreshCombinationsTable();
        
        // Update pagination
        if (pagination != null) {
            int totalPages = (int) Math.ceil((double) allCombinations.size() / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;
            pagination.setTotalPages(totalPages);
            pagination.setCurrentPage(1);
        }
        
        if (pageInfoLabel != null) {
            updatePaginationInfo(pageInfoLabel);
        }
    }

    private void refreshCombinationsTable() {
        combinationsTableModel.setRowCount(0);
        if (allCombinations == null || allCombinations.isEmpty()) {
            return;
        }

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allCombinations.size());

        for (int i = start; i < end; i++) {
            MajorGroupDTO mg = allCombinations.get(i);
            MajorDTO major = mg.getMaNganh() != null ? majorByCode.get(mg.getMaNganh()) : null;
            Object[] rowData = new Object[]{
                mg.getMaToHop(),
                mg.getThMon1() != null ? mg.getThMon1() : "-",
                mg.getThMon2() != null ? mg.getThMon2() : "-",
                mg.getThMon3() != null ? mg.getThMon3() : "-",
                mg.getMaNganh() != null ? mg.getMaNganh() : "-",
                major != null && major.getTenNganh() != null ? major.getTenNganh() : "-",
                mg.getDoLech() != null ? mg.getDoLech().toPlainString() : "-",
                mg
            };
            combinationsTableModel.addRow(rowData);
        }
    }

    private void showImportOverlay() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (owner == null) {
            return;
        }

        ImportPanel importPanel = new ImportPanel(
            file -> {
                Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                owner.setCursor(waitCursor);

                SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() {
                        try {
                            List<MajorGroupDTO> imported = majorGroupBUS.importFromExcelFile(file);
                            if (imported.isEmpty()) {
                                return "Không có bản ghi thay đổi sau import. Dữ liệu có thể đã trùng hoàn toàn, hoặc thiếu cột MANGANH và MA_TO_HOP.";
                            }
                            return "Đã import/cập nhật " + imported.size() + " bản ghi.";
                        } catch (Exception e) {
                            return "Error importing file: " + e.getMessage();
                        }
                    }

                    @Override
                    protected void done() {
                        owner.setCursor(Cursor.getDefaultCursor());
                        try {
                            String message = get();
                            JOptionPane.showMessageDialog(owner, message, "Import Result", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(owner, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        loadCombinationsData();
                        OverlayUtil.hideOverlay(owner);
                    }
                };
                worker.execute();
            },
            () -> OverlayUtil.hideOverlay(owner)
        );

        OverlayUtil.showOverlay(owner, "Import Combinations", importPanel, new Dimension(750, 380));
    }

    private void showAddEditOverlay(MajorGroupDTO existingData) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (owner == null) {
            return;
        }

        if (existingData == null) {
            MajorGroupCreatePanel createPanel = new MajorGroupCreatePanel(majorGroup -> {
                try {
                    upsertSubjectGroupFromMajorGroup(majorGroup);
                    majorGroupBUS.save(majorGroup);
                    JOptionPane.showMessageDialog(owner, "Saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCombinationsData();
                    OverlayUtil.hideOverlay(owner);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(owner,
                            "Không thể lưu tổ hợp: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }, () -> OverlayUtil.hideOverlay(owner));

            OverlayUtil.showOverlay(owner, "Thêm tổ hợp", createPanel, new Dimension(700, 540));
            return;
        }

        MajorGroupEditPanel editPanel = new MajorGroupEditPanel(existingData, majorGroup -> {
            try {
                upsertSubjectGroupFromMajorGroup(majorGroup);
                majorGroupBUS.update(majorGroup);
                JOptionPane.showMessageDialog(owner, "Saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCombinationsData();
                OverlayUtil.hideOverlay(owner);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner,
                        "Không thể lưu tổ hợp: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Sửa tổ hợp", editPanel, new Dimension(700, 540));
    }

    private void upsertSubjectGroupFromMajorGroup(MajorGroupDTO majorGroup) {
        if (majorGroup == null) {
            return;
        }

        String maToHop = majorGroup.getMaToHop();
        if (maToHop == null || maToHop.trim().isEmpty()) {
            return;
        }

        String mon1 = majorGroup.getThMon1() != null ? majorGroup.getThMon1().trim() : "-";
        String mon2 = majorGroup.getThMon2() != null ? majorGroup.getThMon2().trim() : "-";
        String mon3 = majorGroup.getThMon3() != null ? majorGroup.getThMon3().trim() : "-";
        MajorDTO major = majorBUS.findByMaNganh(majorGroup.getMaNganh());
        String tenToHop = major != null && major.getTenNganh() != null && !major.getTenNganh().trim().isEmpty()
            ? major.getTenNganh().trim()
            : majorGroup.getTbKeys();

        SubjectGroupDTO existing = subjectGroupBUS.findByMaToHop(maToHop);
        if (existing == null) {
            SubjectGroupDTO subjectGroupDTO = new SubjectGroupDTO(maToHop, mon1, mon2, mon3, tenToHop);
            subjectGroupBUS.save(subjectGroupDTO);
            return;
        }

        boolean changed = false;
        if (existing.getMon1() == null || !existing.getMon1().equals(mon1)) {
            existing.setMon1(mon1);
            changed = true;
        }
        if (existing.getMon2() == null || !existing.getMon2().equals(mon2)) {
            existing.setMon2(mon2);
            changed = true;
        }
        if (existing.getMon3() == null || !existing.getMon3().equals(mon3)) {
            existing.setMon3(mon3);
            changed = true;
        }
        if (tenToHop != null && !tenToHop.trim().isEmpty()) {
            String normalized = tenToHop.trim();
            if (existing.getTenToHop() == null || !existing.getTenToHop().equals(normalized)) {
                existing.setTenToHop(normalized);
                changed = true;
            }
        }

        if (changed) {
            subjectGroupBUS.update(existing);
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

    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
            setOpaque(true);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            removeAll();
            add(createActionButton("Sửa", "/assets/images/edit.png", Color.decode("#0066CC")));
            add(createActionButton("Xóa", "/assets/images/delete.png", Color.decode("#DC3545")));
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

            btnEdit = createActionButton("Sửa", "/assets/images/edit.png", Color.decode("#0066CC"));
            btnDelete = createActionButton("Xóa", "/assets/images/delete.png", Color.decode("#DC3545"));

            btnEdit.addActionListener(e -> {
                if (currentItem != null) {
                    showAddEditOverlay(currentItem);
                }
                stopCellEditing();
            });

            btnDelete.addActionListener(e -> {
                if (currentItem != null) {
                    int confirm = JOptionPane.showConfirmDialog(MajorGroupManagementPage.this, "Xóa tổ hợp này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        majorGroupBUS.delete(currentItem);
                        loadCombinationsData();
                    }
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
}
