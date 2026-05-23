package com.example.gui.pages.admissionprocess;

import com.example.bus.AdmissionProcessBUS;
import com.example.dao.MajorDAO;
import com.example.dto.AspirationDTO;
import com.example.dto.MajorDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AdmissionProcessManagementPage extends JPanel {
    private AdmissionProcessBUS admissionProcessBUS;
    private MajorDAO majorDAO;

    private JComboBox<String> cbMajors;
    private JButton btnRunAdmission;
    private JButton btnViewResults;

    private JTable tbAdmitted;
    private DefaultTableModel admittedTableModel;

    private JTable tbStats;
    private DefaultTableModel statsTableModel;
    private JSplitPane splitPane; // Khai báo biến toàn cục để handle luồng invoke Later

    public AdmissionProcessManagementPage() {
        this.admissionProcessBUS = new AdmissionProcessBUS();
        this.majorDAO = new MajorDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.decode("#F8F9FA"));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        loadMajorsIntoComboBox();
        setupEvents();
    }

    private void initComponents() {
        // --- TOP PANEL: Các nút thao tác ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        btnRunAdmission = new JButton("Chạy Lọc Ảo & Xét Tuyển");
        btnRunAdmission.setBackground(Color.decode("#0284C7"));
        btnRunAdmission.setForeground(Color.WHITE);
        btnRunAdmission.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRunAdmission.setFocusPainted(false);

        topPanel.add(btnRunAdmission);
        topPanel.add(new JSeparator(SwingConstants.VERTICAL));
        topPanel.add(new JLabel("Chọn Ngành xem kết quả:"));

        cbMajors = new JComboBox<>();
        cbMajors.setPreferredSize(new Dimension(250, 30));
        topPanel.add(cbMajors);

        btnViewResults = new JButton("Xem Kết Quả");
        btnViewResults.setBackground(Color.decode("#16A34A"));
        btnViewResults.setForeground(Color.WHITE);
        btnViewResults.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnViewResults.setFocusPainted(false);
        topPanel.add(btnViewResults);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: Hiển thị dữ liệu ---
        
        // Bảng 1: Danh sách trúng tuyển (Chi tiết nhiều cột)
        String[] admittedColumns = {"CCCD", "Mã Ngành", "Tổ Hợp", "Tổng Điểm", "Phương Thức"};
        admittedTableModel = new DefaultTableModel(admittedColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tbAdmitted = new JTable(admittedTableModel);
        tbAdmitted.setRowHeight(30);
        JScrollPane scrollAdmitted = new JScrollPane(tbAdmitted);
        scrollAdmitted.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Danh Sách Trúng Tuyển Chi Tiết",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), Color.BLACK));

        // Bảng 2: Thống kê phương thức (Ít cột)
        String[] statsColumns = {"Phương Thức Xét Tuyển", "Số Lượng Trúng Tuyển"};
        statsTableModel = new DefaultTableModel(statsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tbStats = new JTable(statsTableModel);
        tbStats.setRowHeight(30);
        JScrollPane scrollStats = new JScrollPane(tbStats);
        scrollStats.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Thống Kê Theo Phương Thức",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), Color.BLACK));

        // Khởi tạo JSplitPane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollAdmitted, scrollStats);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        
        // Giữ resize weight phòng hờ khi co giãn frame tổng thể
        splitPane.setResizeWeight(0.7); 
        
        add(splitPane, BorderLayout.CENTER);

        // GIẢI PHÁP SỬA LỖI Y NGUYÊN: Ép phân tách tỷ lệ sau khi window/panel đã render xong kích thước thực tế
        SwingUtilities.invokeLater(() -> {
            splitPane.setDividerLocation(0.7);
        });
    }

    private void loadMajorsIntoComboBox() {
        try {
            List<MajorDTO> majors = majorDAO.getAll();
            cbMajors.removeAllItems();
            for (MajorDTO m : majors) {
                if (m.getMaNganh() != null) {
                    cbMajors.addItem(m.getMaNganh() + " - " + m.getTenNganh());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách Ngành: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupEvents() {
        btnRunAdmission.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn chạy lại quá trình xét tuyển toàn hệ thống?\n(Dữ liệu kết quả cũ sẽ bị xóa)",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            btnRunAdmission.setEnabled(false);
            btnRunAdmission.setText("Đang xử lý...");

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    admissionProcessBUS.thucHienXetTuyen();
                    return null;
                }

                @Override
                protected void done() {
                    btnRunAdmission.setEnabled(true);
                    btnRunAdmission.setText("Chạy Lọc Ảo & Xét Tuyển");
                    try {
                        get();
                        JOptionPane.showMessageDialog(AdmissionProcessManagementPage.this,
                                "Đã hoàn tất xét tuyển toàn hệ thống!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        loadResults(); 
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdmissionProcessManagementPage.this,
                                "Lỗi trong quá trình xét tuyển: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });

        btnViewResults.addActionListener(e -> loadResults());
    }

    private void loadResults() {
        if (cbMajors.getSelectedItem() == null) return;
        
        String selectedItem = cbMajors.getSelectedItem().toString();
        String maNganh = selectedItem.split(" - ")[0].trim();

        admittedTableModel.setRowCount(0);
        List<AspirationDTO> listAdmitted = admissionProcessBUS.layDanhSachTrTrungTuyenTheoNganh(maNganh);
        if (listAdmitted != null) {
            for (AspirationDTO asp : listAdmitted) {
                admittedTableModel.addRow(new Object[]{
                        asp.getNnCccd(),
                        asp.getNvManganh(),
                        asp.getTtThm(),
                        String.format("%.2f", asp.getDiemXettuyen()),
                        asp.getTtPhuongthuc() != null ? asp.getTtPhuongthuc() : "N/A"
                });
            }
        }

        statsTableModel.setRowCount(0);
        Map<String, Long> stats = admissionProcessBUS.thongKeSoLuongTrungTuyenTheoPhuongThuc(maNganh);
        if (stats != null) {
            for (Map.Entry<String, Long> entry : stats.entrySet()) {
                statsTableModel.addRow(new Object[]{
                        entry.getKey(),
                        entry.getValue()
                });
            }
        }
    }
}