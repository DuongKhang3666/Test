package com.example.gui.pages.score;

import com.example.bus.BonusScoreBUS;
import com.example.bus.ExamScoreBUS;
import com.example.bus.ScoreCalculationService;
import com.example.dto.BonusScoreDTO;
import com.example.dto.ExamScoreDTO;
import com.example.dto.MajorGroupDTO;
import com.example.gui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ScoreListPanel extends JPanel {

    private final ScoreManagement parent;
    private final ExamScoreBUS examScoreBUS;
    private final BonusScoreBUS bonusScoreBUS;
    private final ScoreCalculationService calcService;

    private TextBox searchBox;
    private CustomTable table;
    private Pagination pagination;
    private DefaultTableModel tableModel;

    private String filterPhuongThuc = null;
    private boolean isSearching = false;
    private String currentKeyword = "";
    private final int LIMIT = 20;

    public ScoreListPanel(ScoreManagement parent) {
        this.parent = parent;
        this.examScoreBUS = new ExamScoreBUS();
        this.bonusScoreBUS = new BonusScoreBUS();
        this.calcService = examScoreBUS.getCalculationService();

        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F8F9FA"));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(createToolBarPanel(), BorderLayout.NORTH);
        content.add(createTablePanel(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý điểm thí sinh");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel lblSub = new JLabel("Xem, quản lý và theo dõi điểm thi theo 3 phương thức: THPT, VSAT, ĐGNL");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(Color.GRAY);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblSub);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224)),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        searchBox = new TextBox("Tìm kiếm theo CCCD...");
        searchBox.setPreferredSize(new Dimension(360, 40));
        searchBox.getDocument().addDocumentListener(new DocumentListener() {
            private Timer timer;
            private void schedule() {
                if (timer != null && timer.isRunning()) timer.stop();
                timer = new Timer(450, e -> handleSearch());
                timer.setRepeats(false);
                timer.start();
            }
            @Override public void insertUpdate(DocumentEvent e) { schedule(); }
            @Override public void removeUpdate(DocumentEvent e) { schedule(); }
            @Override public void changedUpdate(DocumentEvent e) { schedule(); }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnFilter = new ActionButton("Tất cả phương thức", null, "TERTIARY");
        btnFilter.setPreferredSize(new Dimension(170, 36));

        JPopupMenu filterMenu = new JPopupMenu();
        String[] options = {"Tất cả", "THPT", "VSAT", "DGNL"};
        for (String opt : options) {
            JMenuItem item = new JMenuItem(opt);
            item.addActionListener(e -> {
                filterPhuongThuc = opt.equals("Tất cả") ? null : opt;
                btnFilter.setText(opt);
                if (pagination != null) pagination.setCurrentPage(1);
                refreshData();
            });
            filterMenu.add(item);
        }
        btnFilter.addActionListener(e -> filterMenu.show(btnFilter, 0, btnFilter.getHeight()));

        JButton btnImport = new ActionButton("Import Excel", null, "PRIMARY");
        JButton btnAdd = new AddAndConfirmButton("+ Thêm điểm");

        btnImport.addActionListener(e -> { if (parent != null) parent.showImportOverlay(); });
        btnAdd.addActionListener(e -> { if (parent != null) parent.showCreateOverlay(); });

        right.add(btnFilter);
        right.add(btnImport);
        right.add(btnAdd);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTablePanel() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(false);
        
        String[] columns = {"CCCD", "Phương thức", "Math", "Physics", "Chemistry", "English", "Total Score", "Hành động"};
        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override 
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
        };
    
        table = new CustomTable(tableModel) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int col) {
                if (col == 1) return new MethodBadgeRenderer();
                if (col == 6) return new TotalScoreRenderer();
                if (col == 7) return new ActionCellRenderer();
                return super.getCellRenderer(row, col);
            }
        };
    
        // ====================== MOUSE LISTENER CHO 3 NÚT XEM - SỬA - XÓA ======================
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
            
                if (row < 0 || col != 7) return;   // Chỉ xử lý cột "Hành động"
            
                String cccd = (String) tableModel.getValueAt(row, 0);
                String phuongThuc = (String) tableModel.getValueAt(row, 1);
            
                if (cccd == null || phuongThuc == null) return;
            
                Rectangle cellRect = table.getCellRect(row, col, false);
                int clickX = e.getX() - cellRect.x;
                int zoneWidth = cellRect.width / 3;
            
                if (clickX < zoneWidth) {
                    // NÚT XEM
                    handleView(cccd, phuongThuc);
                } 
                else if (clickX < zoneWidth * 2) {
                    // NÚT SỬA
                    handleEdit(cccd, phuongThuc);
                } 
                else {
                    // NÚT XÓA
                    handleDelete(cccd, phuongThuc);
                }
            }
        });
    
        // ====================== CÀI ĐẶT ĐỘ RỘNG CỘT ======================
        int[] widths = {130, 95, 75, 80, 85, 80, 105, 140};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
    
        pagination = new Pagination(0, LIMIT, 1);
        pagination.addPageChangeListener(this::onPageChanged);
    
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(pagination, BorderLayout.SOUTH);
    
        refreshData();
        return card;
    }

    public void refreshData() {
        tableModel.setRowCount(0);

        int currentPage = pagination.getCurrentPage();
        int pageIndex = Math.max(0, currentPage - 1); // 0-based page index for DAO
        int offset = pageIndex * LIMIT; // offset used for subList when filtering/searching

        List<ExamScoreDTO> list;

        if (isSearching && !currentKeyword.isEmpty()) {
            list = examScoreBUS.timKiemTheoCccd(currentKeyword);
        } else if (filterPhuongThuc != null) {
            list = examScoreBUS.layTheoPhuongThuc(filterPhuongThuc);
        } else {
            list = examScoreBUS.layTheoTrang(pageIndex, LIMIT);
        }

        // Phân trang thủ công cho filter/search
        if ((isSearching && !currentKeyword.isEmpty()) || filterPhuongThuc != null) {
            int total = list.size();
            int from = Math.min(offset, total);
            int to = Math.min(from + LIMIT, total);
            if (from < total) {
                list = list.subList(from, to);
            } else {
                list = List.of();
            }
        }

        for (ExamScoreDTO dto : list) {
            addRowWithCalculatedScore(dto);
        }

        updatePagination();
    }

    private void updatePagination() {
        long total = 0;

        if (isSearching && !currentKeyword.isEmpty()) {
            total = examScoreBUS.timKiemTheoCccd(currentKeyword).size();
        } else if (filterPhuongThuc != null) {
            total = examScoreBUS.thongKeSoLuongTheoPhuongThuc(filterPhuongThuc);
        } else {
            total = examScoreBUS.demTatCa();
        }

        pagination.setTotalItems((int) total, LIMIT);
    }

    private void onPageChanged(int page) {
        refreshData();
    }

    private void addRowWithCalculatedScore(ExamScoreDTO dto) {
        String pt = dto.getPhuongThuc() != null ? dto.getPhuongThuc().toUpperCase() : "THPT";

        String math, physics, chemistry, english, totalScore;

        if ("DGNL".equals(pt)) {
            math = physics = chemistry = english = "-";
            double diem30 = calcService.quyDoiDiemTheoPhuongThuc(dto, "DGNL");
            BonusScoreDTO bonus = bonusScoreBUS.layDiemCongTheoCccd(dto.getCccd());
            double diemCong = bonus != null && bonus.getDiemTong() != null ? Math.min(bonus.getDiemTong(), 3.0) : 0.0;
            double diemUuTien = examScoreBUS.layDiemUuTienTuDoiTuongKhuVuc(dto.getCccd());
            double dxt = calcService.tinhDiemXetTuyen(diem30, diemCong, diemUuTien);
            totalScore = dxt > 0 ? String.format("%.2f", dxt) : "-";

        } else if ("VSAT".equals(pt)) {
            double nk1 = dto.getNk1() != null ? dto.getNk1() : 0.0;
            double nk2 = dto.getNk2() != null ? dto.getNk2() : 0.0;
            math      = nk1 > 0 ? String.format("%.1f", nk1) : "-";
            physics   = nk2 > 0 ? String.format("%.1f", nk2) : "-";
            chemistry = "-";
            english   = "-";

            double diem30 = calcService.quyDoiDiemTheoPhuongThuc(dto, "VSAT");
            BonusScoreDTO bonus = bonusScoreBUS.layDiemCongTheoCccd(dto.getCccd());
            double diemCong = bonus != null && bonus.getDiemTong() != null ? Math.min(bonus.getDiemTong(), 3.0) : 0.0;
            double diemUuTien = examScoreBUS.layDiemUuTienTuDoiTuongKhuVuc(dto.getCccd());
            double dxt = calcService.tinhDiemXetTuyen(diem30, diemCong, diemUuTien);
            totalScore = dxt > 0 ? String.format("%.2f", dxt) : "-";

        } else {
            double maxDXT = 0.0;
            String[] toHopList = {"A00", "A01", "B00", "C00", "C01", "D01", "D07"};

            BonusScoreDTO bonus = bonusScoreBUS.layDiemCongTheoCccd(dto.getCccd());
            double diemCong = bonus != null && bonus.getDiemTong() != null ? Math.min(bonus.getDiemTong(), 3.0) : 0.0;
            double diemUuTien = examScoreBUS.layDiemUuTienTuDoiTuongKhuVuc(dto.getCccd());

            for (String toHop : toHopList) {
                MajorGroupDTO mg = createMajorGroup(toHop);
                double dxt = calcService.tinhDiemXetTuyen(dto, mg, bonus, diemUuTien);
                if (dxt > maxDXT) maxDXT = dxt;
            }

            math      = formatScore(dto.getDiemToan());
            physics   = formatScore(dto.getDiemLy());
            chemistry = formatScore(dto.getDiemHoa());
            english   = formatScore(dto.getN1Cc());
            totalScore = maxDXT > 0 ? String.format("%.2f", maxDXT) : "-";
        }

        tableModel.addRow(new Object[]{
            dto.getCccd(),
            pt,
            math,
            physics,
            chemistry,
            english,
            totalScore,
            "edit|delete"
        });
    }

    private MajorGroupDTO createMajorGroup(String maToHop) {
        MajorGroupDTO mg = new MajorGroupDTO();
        mg.setMaToHop(maToHop);

        switch (maToHop.toUpperCase()) {
            case "A00" -> { mg.setThMon1("TO"); mg.setHsMon1(1); mg.setThMon2("LI"); mg.setHsMon2(1); mg.setThMon3("HO"); mg.setHsMon3(1); }
            case "A01" -> { mg.setThMon1("TO"); mg.setHsMon1(1); mg.setThMon2("LI"); mg.setHsMon2(1); mg.setThMon3("N1"); mg.setHsMon3(1); }
            case "B00" -> { mg.setThMon1("TO"); mg.setHsMon1(1); mg.setThMon2("HO"); mg.setHsMon2(1); mg.setThMon3("SI"); mg.setHsMon3(1); }
            case "C00" -> { mg.setThMon1("VA"); mg.setHsMon1(1); mg.setThMon2("SU"); mg.setHsMon2(1); mg.setThMon3("DI"); mg.setHsMon3(1); }
            case "C01" -> { mg.setThMon1("VA"); mg.setHsMon1(1); mg.setThMon2("TO"); mg.setHsMon2(1); mg.setThMon3("LI"); mg.setHsMon3(1); }
            case "D01" -> { mg.setThMon1("VA"); mg.setHsMon1(1); mg.setThMon2("TO"); mg.setHsMon2(1); mg.setThMon3("N1"); mg.setHsMon3(1); }
            case "D07" -> { mg.setThMon1("TO"); mg.setHsMon1(1); mg.setThMon2("HO"); mg.setHsMon2(1); mg.setThMon3("N1"); mg.setHsMon3(1); }
            default -> { mg.setThMon1("TO"); mg.setHsMon1(1); mg.setThMon2("LI"); mg.setHsMon2(1); mg.setThMon3("HO"); mg.setHsMon3(1); }
        }
        return mg;
    }

    private String formatScore(Double score) {
        if (score == null || score == 0.0) return "-";
        return String.format("%.2f", score);
    }

    private void handleSearch() {
        String kw = searchBox.getText().trim();
        if (kw.isEmpty()) {
            isSearching = false;
            currentKeyword = "";
        } else {
            isSearching = true;
            currentKeyword = kw;
        }
        if (pagination != null) pagination.setCurrentPage(1);
        refreshData();
    }

    // ==================== RENDERERS ====================
    private static class MethodBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        public MethodBadgeRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            setOpaque(true);
            badge.setOpaque(true);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(4, 10, 4, 10));
            add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? Color.decode("#F1F5F9") : Color.WHITE);
            String method = (value == null) ? "" : value.toString().toUpperCase();

            switch (method) {
                case "THPT" -> { badge.setText("THPT"); badge.setBackground(Color.decode("#DBEAFE")); badge.setForeground(Color.decode("#1E40AF")); }
                case "VSAT" -> { badge.setText("V-SAT"); badge.setBackground(Color.decode("#DCFCE7")); badge.setForeground(Color.decode("#166534")); }
                case "DGNL" -> { badge.setText("ĐGNL"); badge.setBackground(Color.decode("#E0E7FF")); badge.setForeground(Color.decode("#4338CA")); }
                default -> { badge.setText(method); badge.setBackground(Color.decode("#F3F4F6")); badge.setForeground(Color.decode("#6B7280")); }
            }
            return this;
        }
    }

    private static class TotalScoreRenderer extends DefaultTableCellRenderer {
        public TotalScoreRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            if (!isSelected) {
                setForeground(Color.decode("#111827"));
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    private static class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnView   = makeBtn("Xem",  new Color(22, 101, 52),   new Color(220, 252, 231));
        private final JButton btnEdit   = makeBtn("Sửa",  new Color(30, 64, 175),   new Color(219, 234, 254));
        private final JButton btnDelete = makeBtn("Xóa",  new Color(185, 28, 28),   new Color(254, 226, 226));

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 3));
            setOpaque(true);
            add(btnView);
            add(btnEdit);
            add(btnDelete);
        }

        private static JButton makeBtn(String text, Color fg, Color bg) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setForeground(fg);
            btn.setBackground(bg);
            btn.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setOpaque(true);
            return btn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? Color.decode("#F1F5F9") : Color.WHITE);
            btnView.setBackground(isSelected   ? Color.decode("#DCFCE7") : new Color(220, 252, 231));
            btnEdit.setBackground(isSelected   ? Color.decode("#DBEAFE") : new Color(219, 234, 254));
            btnDelete.setBackground(isSelected ? Color.decode("#FEE2E2") : new Color(254, 226, 226));
            return this;
        }
    }
    private void handleView(String cccd, String phuongThuc) {
        ExamScoreDTO dto = examScoreBUS.getScoreByCccdAndMethod(cccd, phuongThuc);
        if (dto != null && parent != null) {
            parent.showViewOverlay(dto);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu điểm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleEdit(String cccd, String phuongThuc) {
        ExamScoreDTO dto = examScoreBUS.getScoreByCccdAndMethod(cccd, phuongThuc);
        if (dto != null && parent != null) {
            parent.showEditOverlay(dto);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu để sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleDelete(String cccd, String phuongThuc) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa điểm " + phuongThuc + 
                " của thí sinh CCCD: " + cccd + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = examScoreBUS.deleteScoreByCccdAndMethod(cccd, phuongThuc);
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa thành công phương thức " + phuongThuc + "!", 
                                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}