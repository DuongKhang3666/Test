package com.example.gui.pages.aspiration;

import com.example.bus.AdmissionProcessBUS;
import com.example.bus.AspirationBUS;
import com.example.dto.AspirationDTO;
import com.example.gui.components.*;
import com.example.gui.events.TableActionEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AspirationListPanel extends JPanel {
    private final AspirationManagement parent;
    private final AspirationBUS aspirationBUS;

    private TextBox txtSearch;
    private CustomTable table;
    private DefaultTableModel tableModel;
    private Pagination pagination;

    private int currentPage = 1;
    private final int limit = 15;
    private int totalRows = 0;

    /** Danh sách dòng hiện tại trên trang, để các nút Action lấy đúng đối tượng */
    private List<AspirationDTO> currentList;

    public AspirationListPanel(AspirationManagement parent) {
        this.parent       = parent;
        this.aspirationBUS = new AspirationBUS();

        setLayout(new BorderLayout(10, 10));
        setBackground(AppTheme.NEUTRAL);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshData();
    }

    // ── Khởi tạo giao diện ─────────────────────────────────────────────────
    private void initComponents() {

        // 1. Header + Controls
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setOpaque(false);

        PageHeader header = new PageHeader(
                "Quản Lý Nguyện Vọng Xét Tuyển",
                "Xem, tìm kiếm và quản lý danh sách nguyện vọng của thí sinh.");
        topPanel.add(header, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setOpaque(false);

        txtSearch = new TextBox("Tìm theo CCCD hoặc Mã Ngành...");
        txtSearch.setPreferredSize(new Dimension(280, 35));
        txtSearch.addActionListener(e -> { currentPage = 1; refreshData(); });
        actionPanel.add(txtSearch);

        // Tải lại
        ActionButton btnRefresh = new ActionButton("Tải lại", "/assets/images/refresh.png", "SECONDARY");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            currentPage = 1;
            refreshData();
        });
        actionPanel.add(btnRefresh);

        // Tính điểm đồng bộ
        ActionButton btnCalculate = new ActionButton("Tính Điểm Đồng Bộ", "/assets/images/refresh.png", "PRIMARY");
        btnCalculate.addActionListener(e -> handleCalculateScores(btnCalculate));
        actionPanel.add(btnCalculate);

        // Import Excel
        ActionButton btnImport = new ActionButton("Nhập Excel", "/assets/images/upload_file.png", "PRIMARY");
        btnImport.addActionListener(e -> parent.showImportOverlay()); // Gọi sang hàm của AspirationManagement
        actionPanel.add(btnImport);

        // Thêm mới
        ActionButton btnAdd = new ActionButton("Thêm Nguyện Vọng", null, "TERTIARY");
        btnAdd.addActionListener(e -> parent.showCreateOverlay());
        actionPanel.add(btnAdd);

        topPanel.add(actionPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // 2. Bảng dữ liệu
        String[] columns = {"STT", "CCCD Thí Sinh", "Mã Ngành", "Thứ Tự NV",
                             "Tổ Hợp", "Điểm XT", "Kết Quả", "Hành Động"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 7; }
        };

        table = new CustomTable(tableModel);

        TableActionEvent event = new TableActionEvent() {
            @Override
            public void onEdit(int row) {
                if (currentList != null && row >= 0 && row < currentList.size()) {
                    parent.showEditOverlay(currentList.get(row));
                }
            }

            @Override
            public void onDelete(int row) {
                if (currentList != null && row >= 0 && row < currentList.size()) {
                    AspirationDTO asp = currentList.get(row);
                    int confirm = JOptionPane.showConfirmDialog(
                            AspirationListPanel.this,
                            "Bạn có chắc chắn muốn xoá nguyện vọng này?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        aspirationBUS.delete(asp);
                        refreshData();
                    }
                }
            }

            @Override
            public void onView(int row) {
                if (currentList != null && row >= 0 && row < currentList.size()) {
                    AspirationDTO asp = currentList.get(row);
                    Window parentWindow = SwingUtilities.getWindowAncestor(AspirationListPanel.this);
                    AspirationDetailDialog dlg =
                            new AspirationDetailDialog(parentWindow, asp, AspirationListPanel.this);
                    dlg.setVisible(true);
                }
            }
        };

        table.getColumnModel().getColumn(7).setCellRenderer(new TableActionCellRender());
        table.getColumnModel().getColumn(7).setCellEditor(new TableActionCellEditor(event));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // 3. Phân trang
        pagination = new Pagination();
        pagination.addPageChangeListener(page -> {
            this.currentPage = page;
            refreshData();
        });
        add(pagination, BorderLayout.SOUTH);
    }

    // ── Tính điểm đồng bộ (chạy nền để không đóng băng UI) ─────────────────
    private void handleCalculateScores(ActionButton btnCalculate) {
        int total = (int) aspirationBUS.getCount();
        if (total == 0) {
            JOptionPane.showMessageDialog(this,
                    "Chưa có nguyện vọng nào trong hệ thống.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Hệ thống sẽ tính toán lại điểm cho " + total + " nguyện vọng.\n"
                + "Các trường thiếu dữ liệu sẽ được để 0.0 và vẫn được lưu.\n"
                + "Quá trình này có thể mất vài giây. Tiếp tục?",
                "Xác nhận tính điểm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Vô hiệu nút, đổi con trỏ
        btnCalculate.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                AdmissionProcessBUS bus = new AdmissionProcessBUS();
                return bus.calculateAndSyncAllAspirations();
            }

            @Override
            protected void done() {
                // Luôn khôi phục trạng thái UI
                btnCalculate.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());

                try {
                    int processed = get();
                    // Luôn làm mới bảng dù có lỗi dữ liệu hay không
                    refreshData();

                    if (processed == 0) {
                        JOptionPane.showMessageDialog(AspirationListPanel.this,
                                "Không có nguyện vọng nào được tính điểm.\n"
                                ,
                                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AspirationListPanel.this,
                                "✅ Đã tính và cập nhật điểm cho " + processed + " nguyện vọng!\n"
                                + "Các trường thiếu dữ liệu được để 0.0.",
                                "Hoàn thành", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    refreshData(); // Vẫn refresh để hiển thị những gì đã lưu được
                    JOptionPane.showMessageDialog(AspirationListPanel.this,
                            "Lỗi trong quá trình tính điểm: " + ex.getMessage()
                            + "\nMột số nguyện vọng có thể đã được cập nhật.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ── Làm mới dữ liệu bảng ────────────────────────────────────────────────
    public void refreshData() {
        String keyword = txtSearch.getText().trim();
        if (keyword.equals("Tìm theo CCCD hoặc Mã Ngành...")) keyword = "";

        int offset = (currentPage - 1) * limit;

        currentList = aspirationBUS.searchByCccdOrMaNganh(keyword, offset, limit);
        totalRows   = (int) aspirationBUS.countSearch(keyword);

        tableModel.setRowCount(0);

        int stt = offset + 1;
        for (AspirationDTO a : currentList) {
            // Điểm xét tuyển: hiển thị giá trị thực hoặc "Chưa tính" nếu null/0
            String diemXt;
            if (a.getDiemXettuyen() != null && a.getDiemXettuyen() > 0) {
                diemXt = String.format("%.3f", a.getDiemXettuyen());
            } else {
                diemXt = "Chưa tính";
            }

            // Tổ hợp
            String toHop = (a.getTtThm() != null && !a.getTtThm().isBlank())
                    ? a.getTtThm() : "N/A";

            // Kết quả
            String ketQua = (a.getNvKetqua() != null && !a.getNvKetqua().isBlank())
                    ? a.getNvKetqua() : "Chờ xét";

            tableModel.addRow(new Object[]{
                stt++,
                a.getNnCccd(),
                a.getNvManganh(),
                a.getNvTt(),
                toHop,
                diemXt,
                ketQua,
                null
            });
        }

        pagination.setTotalItems(totalRows, limit);
        pagination.setCurrentPageSilently(currentPage);
    }
}