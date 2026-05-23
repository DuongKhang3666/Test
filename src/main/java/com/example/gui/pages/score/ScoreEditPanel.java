package com.example.gui.pages.score;

import com.example.bus.BonusScoreBUS;
import com.example.bus.ExamScoreBUS;
import com.example.bus.ScoreCalculationService;
import com.example.dto.BonusScoreDTO;
import com.example.dto.ExamScoreDTO;
import com.example.dto.MajorGroupDTO;
import com.example.gui.components.AddAndConfirmButton;
import com.example.gui.components.CancelButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

public class ScoreEditPanel extends JPanel {

    private final ExamScoreDTO dto;
    private final ExamScoreBUS bus = new ExamScoreBUS();
    private final BonusScoreBUS bonusBus = new BonusScoreBUS();
    private final ScoreCalculationService calcService = bus.getCalculationService();
    
    private final boolean isReadOnly;

    private JTextField tfCccd;
    private JComboBox<String> cbPhuongThuc;
    
    // THAY THẾ: Chuyển từ JTextField sang JComboBox danh sách tổ hợp yêu cầu
    private JComboBox<String> cbMaToHop;

    // Natural Science
    private JTextField tfToan, tfLy, tfHoa, tfSinh;
    // Social Science
    private JTextField tfVan, tfSu, tfDia, tfKtpl;
    // Languages & Tech
    private JTextField tfN1Thi, tfN1Cc, tfCncn, tfCnnn;
    // Special Exams
    private JTextField tfNl1, tfNk1, tfNk2;

    // Kết quả điểm hiển thị
    private JLabel lblDTHXT, lblDC, lblDUT, lblDXT;

    public ScoreEditPanel(ExamScoreDTO dto, Consumer<ExamScoreDTO> onSave, Runnable onCancel) {
        this(dto, onSave, onCancel, false);
    }

    public ScoreEditPanel(ExamScoreDTO dto, Consumer<ExamScoreDTO> onSave, Runnable onCancel, boolean isReadOnly) {
        this.dto = dto;
        this.isReadOnly = isReadOnly;

        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        add(createHeader(onCancel), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        add(createFooter(onSave, onCancel), BorderLayout.SOUTH);

        // 1. Đồng bộ dữ liệu từ DTO lên các TextFields trước
        loadDataFields();                    
        
        // 2. Cập nhật trạng thái đóng/mở các ô nhập theo Phương thức tuyển sinh
        updateFieldsByPhuongThuc(dto.getPhuongThuc() != null ? dto.getPhuongThuc() : "THPT");
        
        // 3. Tự động tìm kiếm tổ hợp tốt nhất và tính toán hiển thị điểm ngay lập tức khi mở
        initDefaultCombinationAndCalculate();
    }

    private JPanel createHeader(Runnable onCancel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        String titleText = isReadOnly ? "Chi tiết điểm thí sinh" : "Sửa điểm thí sinh";
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.decode("#111827"));

        JButton btnX = new JButton("✕");
        btnX.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnX.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        btnX.setContentAreaFilled(false);
        btnX.setFocusPainted(false);
        btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnX.setForeground(Color.decode("#6B7280"));
        btnX.addActionListener(e -> onCancel.run());

        p.add(title, BorderLayout.WEST);
        p.add(btnX, BorderLayout.EAST);
        return p;
    }

    private JPanel createBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        body.add(createTopRow());
        body.add(Box.createVerticalStrut(15));
        body.add(createScoreGroups());
        body.add(Box.createVerticalStrut(20));
        body.add(createCombinationSelectionPanel()); // Vùng chọn tổ hợp mới
        body.add(Box.createVerticalStrut(15));
        body.add(createResultPanel());

        return body;
    }

    private JPanel createTopRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel cccdGroup = new JPanel(new BorderLayout(0, 6));
        cccdGroup.setOpaque(false);
        cccdGroup.add(makeLabel("CCCD"), BorderLayout.NORTH);
        tfCccd = makeTextField(dto.getCccd());
        tfCccd.setEditable(false);
        tfCccd.setBackground(Color.decode("#F9FAFB"));
        cccdGroup.add(tfCccd, BorderLayout.CENTER);

        JPanel methodGroup = new JPanel(new BorderLayout(0, 6));
        methodGroup.setOpaque(false);
        methodGroup.add(makeLabel("Phương thức xét tuyển"), BorderLayout.NORTH);
        cbPhuongThuc = new JComboBox<>(new String[]{"THPT", "VSAT", "DGNL"});
        cbPhuongThuc.setSelectedItem(dto.getPhuongThuc() != null ? dto.getPhuongThuc() : "THPT");
        cbPhuongThuc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbPhuongThuc.addItemListener(this::onPhuongThucChanged);
        
        if (isReadOnly) {
            cbPhuongThuc.setEnabled(false);
        }
        
        methodGroup.add(cbPhuongThuc, BorderLayout.CENTER);

        row.add(cccdGroup);
        row.add(methodGroup);
        return row;
    }

    private JPanel createScoreGroups() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        tfToan = makeScoreField(dto.getDiemToan()); tfLy = makeScoreField(dto.getDiemLy());
        tfHoa = makeScoreField(dto.getDiemHoa()); tfSinh = makeScoreField(dto.getDiemSinh());
        row.add(createGroup("Natural Science", new String[]{"TO", "LI", "HO", "SI"}, 
                new JTextField[]{tfToan, tfLy, tfHoa, tfSinh}));

        tfVan = makeScoreField(dto.getDiemVan()); tfSu = makeScoreField(dto.getDiemSu());
        tfDia = makeScoreField(dto.getDiemDia()); tfKtpl = makeScoreField(dto.getDiemKtpl());
        row.add(createGroup("Social Science", new String[]{"VA", "SU", "DI", "KTPL"}, 
                new JTextField[]{tfVan, tfSu, tfDia, tfKtpl}));

        tfN1Thi = makeScoreField(dto.getN1Thi()); tfN1Cc = makeScoreField(dto.getN1Cc());
        tfCncn = makeScoreField(dto.getDiemCncn()); tfCnnn = makeScoreField(dto.getDiemCnnn());
        row.add(createGroup("Languages & Tech", new String[]{"N1_THI", "N1_CC", "CNCN", "CNNN"}, 
                new JTextField[]{tfN1Thi, tfN1Cc, tfCncn, tfCnnn}));

        tfNl1 = makeScoreField(dto.getNl1()); tfNk1 = makeScoreField(dto.getNk1()); tfNk2 = makeScoreField(dto.getNk2());
        row.add(createGroup("Special Exams", new String[]{"NL1", "NK1", "NK2"}, 
                new JTextField[]{tfNl1, tfNk1, tfNk2}));

        return row;
    }

    private JPanel createGroup(String title, String[] labels, JTextField[] fields) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(Color.decode("#111827"));
        lblTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        group.add(lblTitle);

        for (int i = 0; i < labels.length; i++) {
            JPanel r = new JPanel(new BorderLayout(10, 0));
            r.setOpaque(false);
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            r.setBorder(new EmptyBorder(3, 0, 3, 0));

            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(Color.decode("#374151"));
            lbl.setPreferredSize(new Dimension(100, 28));

            r.add(lbl, BorderLayout.WEST);
            r.add(fields[i], BorderLayout.CENTER);
            group.add(r);
        }
        return group;
    }

    // TẠO MỚI: Thiết kế JComboBox thay thế hoàn toàn cho JTextField cũ
    private JPanel createCombinationSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        String[] toHopList = {"A00", "A01", "B00", "C00", "C01", "D01", "D07"};
        cbMaToHop = new JComboBox<>(toHopList);
        cbMaToHop.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbMaToHop.setPreferredSize(new Dimension(0, 38));
        
        // Mỗi lần người dùng đổi tổ hợp trên ComboBox -> Tự động tính toán lại điểm tức thì
        cbMaToHop.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                calculateAndShowResult();
            }
        });

        String pt = dto.getPhuongThuc() != null ? dto.getPhuongThuc() : "THPT";
        cbMaToHop.setEnabled(!"DGNL".equals(pt) && !isReadOnly);

        JPanel wrapper = new JPanel(new BorderLayout(0, 5));
        wrapper.setOpaque(false);
        wrapper.add(makeLabel("Chọn Tổ Hợp Môn Xét Tuyển (Không áp dụng cho DGNL)"), BorderLayout.NORTH);
        wrapper.add(cbMaToHop, BorderLayout.CENTER);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JPanel row = new JPanel(new GridLayout(1, 4, 15, 0));
        row.setOpaque(false);

        lblDTHXT = new JLabel("ĐTHXT: -");
        lblDC = new JLabel("ĐC: -");
        lblDUT = new JLabel("ĐƯT: -");
        lblDXT = new JLabel("ĐXT: -");

        styleResultLabel(lblDTHXT);
        styleResultLabel(lblDC);
        styleResultLabel(lblDUT);
        styleResultLabel(lblDXT);

        row.add(lblDTHXT);
        row.add(lblDC);
        row.add(lblDUT);
        row.add(lblDXT);

        JLabel lblNote = new JLabel("Ghi chú: ĐC bao gồm Điểm cộng và Quy đổi Tiếng Anh.");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNote.setForeground(Color.decode("#6B7280"));
        lblNote.setBorder(new EmptyBorder(8, 0, 0, 0));

        panel.add(row);
        panel.add(lblNote);
        return panel;
    }

    private void styleResultLabel(JLabel lbl) {
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.decode("#1E40AF"));
    }

    private void onPhuongThucChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            String pt = (String) cbPhuongThuc.getSelectedItem();
            updateFieldsByPhuongThuc(pt);
            if (cbMaToHop != null) {
                cbMaToHop.setEnabled(!"DGNL".equals(pt) && !isReadOnly);
            }
            calculateAndShowResult();
        }
    }

    private void updateFieldsByPhuongThuc(String phuongThuc) {
        boolean isTHPT = "THPT".equals(phuongThuc);
        boolean isVSAT = "VSAT".equals(phuongThuc);
        boolean isDGNL = "DGNL".equals(phuongThuc);

        JTextField[] thptFields = {tfToan, tfLy, tfHoa, tfSinh, tfVan, tfSu, tfDia, tfKtpl, tfN1Thi, tfCncn, tfCnnn};
        for (JTextField tf : thptFields) tf.setEnabled(isTHPT && !isReadOnly);

        tfN1Cc.setEnabled(false);
        tfNl1.setEnabled(isDGNL && !isReadOnly);
        tfNk1.setEnabled(isVSAT && !isReadOnly);
        tfNk2.setEnabled(isVSAT && !isReadOnly);

        if (isReadOnly) {
            for (JTextField tf : thptFields) tf.setEnabled(false);
            tfN1Cc.setEnabled(false);
            tfNl1.setEnabled(false);
            tfNk1.setEnabled(false);
            tfNk2.setEnabled(false);
        }
    }

    private JPanel createFooter(Consumer<ExamScoreDTO> onSave, Runnable onCancel) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        if (isReadOnly) {
            CancelButton btnClose = new CancelButton("Đóng");
            btnClose.setPreferredSize(new Dimension(120, 40));
            btnClose.addActionListener(e -> onCancel.run());
            p.add(btnClose);
        } else {
            JButton btnCalculate = new JButton("Tính Điểm Xét Tuyển");
            btnCalculate.setPreferredSize(new Dimension(190, 40));
            btnCalculate.addActionListener(e -> calculateAndShowResult());

            CancelButton btnCancel = new CancelButton("Hủy");
            AddAndConfirmButton btnSave = new AddAndConfirmButton("Lưu thay đổi");

            btnCancel.addActionListener(e -> onCancel.run());
            btnSave.addActionListener(e -> {
                if (!validateInput()) return;
                applyToDto();
                onSave.accept(dto);
            });

            p.add(btnCalculate);
            p.add(btnCancel);
            p.add(btnSave);
        }
        return p;
    }

    // LOGIC CHỦ CHỐT: Tự động chạy quét 7 tổ hợp để tìm ra tổ hợp cao điểm nhất và thiết lập lên ComboBox
    private void initDefaultCombinationAndCalculate() {
        String currentPt = dto.getPhuongThuc() != null ? dto.getPhuongThuc() : "THPT";
        
        if ("THPT".equals(currentPt) && cbMaToHop != null) {
            String bestToHop = "A00";
            double maxScore = -1.0;
            String[] toHopList = {"A00", "A01", "B00", "C00", "C01", "D01", "D07"};

            for (String toHop : toHopList) {
                MajorGroupDTO mg = createMajorGroup(toHop, currentPt);
                double diemToHop = calcService.tinhDiemToHop(dto, mg);
                if (diemToHop > maxScore) {
                    maxScore = diemToHop;
                    bestToHop = toHop;
                }
            }
            cbMaToHop.setSelectedItem(bestToHop);
        } else if (cbMaToHop != null) {
            cbMaToHop.setSelectedIndex(0);
        }

        calculateAndShowResult();
    }

        private void calculateAndShowResult() {
        try {
            applyToDto();

            String phuongThuc = (String) cbPhuongThuc.getSelectedItem();
            String cccd = dto.getCccd();

            double diemToHop = 0.0;
            double diemUuTien;
            double diemXT = 0.0;

            ExamScoreBUS.DiemUuTienDetail dutDetail = bus.tinhDiemUuTienChiTiet(cccd);

            BonusScoreDTO bonus = bonusBus.layDiemCongTheoCccd(cccd);
            double diemCong = bonus != null && bonus.getDiemTong() != null ? Math.min(bonus.getDiemTong(), 3.0) : 0.0;
            diemUuTien = bus.layDiemUuTienTuDoiTuongKhuVuc(cccd);

            if ("DGNL".equals(phuongThuc)) {
                diemToHop = calcService.quyDoiDiemTheoPhuongThuc(dto, "DGNL");
                diemXT = calcService.tinhDiemXetTuyen(diemToHop, diemCong, diemUuTien);

                lblDTHXT.setText(String.format("ĐTHXT (DGNL): %.2f", diemToHop));

            } else if ("VSAT".equals(phuongThuc)) {
                diemToHop = calcService.quyDoiDiemTheoPhuongThuc(dto, "VSAT");
                diemXT = calcService.tinhDiemXetTuyen(diemToHop, diemCong, diemUuTien);

                lblDTHXT.setText(String.format("ĐTHXT (VSAT): %.2f", diemToHop));

            } else { // THPT
                String selectedToHop = (String) cbMaToHop.getSelectedItem();
                MajorGroupDTO mg = bus.layMajorGroupTheoToHop(selectedToHop); // Dùng BUS thay vì createMajorGroup cũ

                if (mg == null) mg = createMajorGroup(selectedToHop, phuongThuc); // fallback

                diemToHop = calcService.tinhDiemToHop(dto, mg);
                diemXT = calcService.tinhDiemXetTuyen(dto, mg, bonus, diemUuTien);

                lblDTHXT.setText(String.format("ĐTHXT (%s): %.2f", selectedToHop, diemToHop));
            }

            lblDC.setText(String.format("ĐC: %.2f", diemCong));
            lblDUT.setText(String.format("ĐƯT: %.3f (%s)", diemUuTien, dutDetail.lyDo));
            lblDXT.setText(String.format("ĐXT: %.3f", diemXT));

        } catch (Exception ex) {
            lblDTHXT.setText("ĐTHXT: Lỗi");
            lblDUT.setText("ĐƯT: Lỗi");
            lblDXT.setText("ĐXT: Lỗi");
            ex.printStackTrace();
        }
    }

    private MajorGroupDTO createMajorGroup(String maToHop, String phuongThuc) {
        MajorGroupDTO mg = new MajorGroupDTO();
        mg.setMaToHop(maToHop);
        
        if ("VSAT".equals(phuongThuc)) {
            mg.setThMon1("NK1"); mg.setHsMon1(1);
            mg.setThMon2("NK2"); mg.setHsMon2(1);
            mg.setThMon3("NK1"); mg.setHsMon3(0); 
        } else {
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
        }
        return mg;
    }

    private void loadDataFields() {
        // Hàm này có nhiệm vụ hiển thị toàn bộ điểm ban đầu lên TextFields (nếu có)
        // Trường hợp DTO chứa giá trị null, makeScoreField sẽ tự hiển thị "0.0"
    }

    private void applyToDto() {
        dto.setPhuongThuc((String) cbPhuongThuc.getSelectedItem());
        dto.setDiemToan(parseDouble(tfToan));
        dto.setDiemLy(parseDouble(tfLy));
        dto.setDiemHoa(parseDouble(tfHoa));
        dto.setDiemSinh(parseDouble(tfSinh));
        dto.setDiemVan(parseDouble(tfVan));
        dto.setDiemSu(parseDouble(tfSu));
        dto.setDiemDia(parseDouble(tfDia));
        dto.setDiemKtpl(parseDouble(tfKtpl));
        dto.setN1Thi(parseDouble(tfN1Thi));
        dto.setN1Cc(parseDouble(tfN1Cc));
        dto.setDiemCncn(parseDouble(tfCncn));
        dto.setDiemCnnn(parseDouble(tfCnnn));
        dto.setNl1(parseDouble(tfNl1));
        dto.setNk1(parseDouble(tfNk1));
        dto.setNk2(parseDouble(tfNk2));
    }

    private boolean validateInput() {
        if (tfCccd.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "CCCD không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            tfCccd.requestFocus();
            return false;
        }
        return true;
    }

    // ==================== HELPER MỸ THUẬT GIAO DIỆN ====================
    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.decode("#374151"));
        return lbl;
    }

    private JTextField makeTextField(String val) {
        JTextField tf = new JTextField(val != null ? val : "");
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(0, 38));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#D1D5DB")),
                new EmptyBorder(4, 10, 4, 10)));
        return tf;
    }

    private JTextField makeScoreField(Double val) {
        String text = (val != null) ? String.format("%.2f", val) : "0.0";
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setPreferredSize(new Dimension(80, 34));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#D1D5DB")),
                new EmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    private Double parseDouble(JTextField tf) {
        try {
            return Double.parseDouble(tf.getText().trim());
        } catch (Exception e) {
            return null;
        }
    }
}