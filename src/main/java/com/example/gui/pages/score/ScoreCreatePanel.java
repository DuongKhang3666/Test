package com.example.gui.pages.score;

import com.example.dto.ExamScoreDTO;
import com.example.gui.components.AddAndConfirmButton;
import com.example.gui.components.CancelButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

public class ScoreCreatePanel extends JPanel {

    private JTextField tfCccd;
    private JComboBox<String> cbPhuongThuc;

    // Natural Science
    private JTextField tfToan, tfLy, tfHoa, tfSinh;
    // Social Science
    private JTextField tfVan, tfSu, tfDia, tfKtpl;
    // Languages & Tech
    private JTextField tfN1Thi, tfN1Cc, tfCncn, tfCnnn;
    // Special Exams
    private JTextField tfNl1, tfNk1, tfNk2;

    public ScoreCreatePanel(Consumer<ExamScoreDTO> onSave, Runnable onCancel) {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        add(createHeader(onCancel), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        add(createFooter(onSave, onCancel), BorderLayout.SOUTH);

        updateFieldsByPhuongThuc("THPT");
    }

    private JPanel createHeader(Runnable onCancel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Thêm điểm thí sinh");
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
        body.add(Box.createVerticalStrut(20));
        body.add(createScoreGroups());

        return body;
    }

    private JPanel createTopRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel cccdGroup = new JPanel(new BorderLayout(0, 6));
        cccdGroup.setOpaque(false);
        cccdGroup.add(makeLabel("Candidate ID (CCCD)"), BorderLayout.NORTH);
        tfCccd = makeTextField("");
        tfCccd.putClientProperty("JTextField.placeholderText", "Nhập CCCD");
        cccdGroup.add(tfCccd, BorderLayout.CENTER);

        JPanel methodGroup = new JPanel(new BorderLayout(0, 6));
        methodGroup.setOpaque(false);
        methodGroup.add(makeLabel("Phương thức xét tuyển"), BorderLayout.NORTH);
        cbPhuongThuc = new JComboBox<>(new String[]{"THPT", "VSAT", "DGNL"});
        cbPhuongThuc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbPhuongThuc.setPreferredSize(new Dimension(0, 38));
        cbPhuongThuc.addItemListener(this::onPhuongThucChanged);
        methodGroup.add(cbPhuongThuc, BorderLayout.CENTER);

        row.add(cccdGroup);
        row.add(methodGroup);
        return row;
    }

    private JPanel createScoreGroups() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        tfToan = makeScoreField(); tfLy = makeScoreField();
        tfHoa = makeScoreField(); tfSinh = makeScoreField();
        row.add(createGroup("Natural Science", new String[]{"Math (TO)", "Physics (LI)", "Chemistry (HO)", "Biology (SI)"}, 
                new JTextField[]{tfToan, tfLy, tfHoa, tfSinh}));

        tfVan = makeScoreField(); tfSu = makeScoreField();
        tfDia = makeScoreField(); tfKtpl = makeScoreField();
        row.add(createGroup("Social Science", new String[]{"Literature (VA)", "History (SU)", "Geography (DI)", "Civic Edu (KTPL)"}, 
                new JTextField[]{tfVan, tfSu, tfDia, tfKtpl}));

        tfN1Thi = makeScoreField(); tfN1Cc = makeScoreField();
        tfCncn = makeScoreField(); tfCnnn = makeScoreField();
        row.add(createGroup("Languages & Tech", new String[]{"English (N1_THI)", "Cert Score (N1_CC)", "Tech Industry (CNCN)", "Tech Agri (CNNN)"}, 
                new JTextField[]{tfN1Thi, tfN1Cc, tfCncn, tfCnnn}));

        tfNl1 = makeScoreField(); tfNk1 = makeScoreField(); tfNk2 = makeScoreField();
        row.add(createGroup("Special Exams", new String[]{"Competency (NL1)", "Aptitude 1 (NK1)", "Aptitude 2 (NK2)"}, 
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
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
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
            lbl.setPreferredSize(new Dimension(120, 28));

            r.add(lbl, BorderLayout.WEST);
            r.add(fields[i], BorderLayout.CENTER);
            group.add(r);
        }
        return group;
    }

    private void onPhuongThucChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            updateFieldsByPhuongThuc((String) cbPhuongThuc.getSelectedItem());
        }
    }

    private void updateFieldsByPhuongThuc(String phuongThuc) {
        boolean isTHPT = "THPT".equals(phuongThuc);
        boolean isVSAT = "VSAT".equals(phuongThuc);
        boolean isDGNL = "DGNL".equals(phuongThuc);

        setEnabledFields(new JTextField[]{tfToan, tfLy, tfHoa, tfSinh, tfVan, tfSu, tfDia, tfKtpl}, isTHPT);

        tfN1Thi.setEnabled(isTHPT);
        tfN1Cc.setEnabled(false);
        tfN1Cc.setToolTipText("Điểm N1_CC được quy đổi tự động từ chứng chỉ");
        tfCncn.setEnabled(isTHPT);
        tfCnnn.setEnabled(isTHPT);

        tfNl1.setEnabled(isDGNL);
        tfNk1.setEnabled(isVSAT);
        tfNk2.setEnabled(isVSAT);
    }

    private void setEnabledFields(JTextField[] fields, boolean enabled) {
        for (JTextField tf : fields) {
            tf.setEnabled(enabled);
            if (!enabled) tf.setText("0.0");
        }
    }

    private JPanel createFooter(Consumer<ExamScoreDTO> onSave, Runnable onCancel) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        CancelButton btnCancel = new CancelButton("Hủy");
        btnCancel.addActionListener(e -> onCancel.run());

        AddAndConfirmButton btnSave = new AddAndConfirmButton("Lưu điểm");
        btnSave.setPreferredSize(new Dimension(140, 40));
        btnSave.addActionListener(e -> {
            if (!validateInput()) return;
            onSave.accept(buildDto());
        });

        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private boolean validateInput() {
        String cccd = tfCccd.getText().trim();
        if (cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "CCCD không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            tfCccd.requestFocus();
            return false;
        }

        String phuongThuc = (String) cbPhuongThuc.getSelectedItem();

        // THPT: các môn chuẩn 0-10
        if ("THPT".equals(phuongThuc)) {
            JTextField[] fields = {tfToan, tfLy, tfHoa, tfSinh, tfVan, tfSu, tfDia, tfKtpl,
                    tfN1Thi, tfCncn, tfCnnn};
            for (JTextField tf : fields) {
                Double v = parseDouble(tf);
                if (v != null && (v < 0 || v > 10)) {
                    JOptionPane.showMessageDialog(this, "Điểm phải nằm trong khoảng 0.00 - 10.00!", "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
                    tf.requestFocus();
                    return false;
                }
            }
        }

        // VSAT: NK1/NK2 là raw score (thang 150), kiểm tra nếu trường được bật
        if ("VSAT".equals(phuongThuc)) {
            Double nk1 = parseDouble(tfNk1);
            Double nk2 = parseDouble(tfNk2);
            if (nk1 != null && (nk1 < 0 || nk1 > 150)) {
                JOptionPane.showMessageDialog(this, "NK1 phải nằm trong khoảng 0 - 150!", "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
                tfNk1.requestFocus();
                return false;
            }
            if (nk2 != null && (nk2 < 0 || nk2 > 150)) {
                JOptionPane.showMessageDialog(this, "NK2 phải nằm trong khoảng 0 - 150!", "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
                tfNk2.requestFocus();
                return false;
            }
        }

        // DGNL: NL1 là điểm ĐGNL (thang 1200)
        if ("DGNL".equals(phuongThuc)) {
            Double nl1 = parseDouble(tfNl1);
            if (nl1 != null && (nl1 < 0 || nl1 > 1200)) {
                JOptionPane.showMessageDialog(this, "Điểm ĐGNL (NL1) phải nằm trong khoảng 0 - 1200!", "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
                tfNl1.requestFocus();
                return false;
            }
        }

        // Nếu tới đây không lỗi, chấp nhận
        return true;
    }

    private ExamScoreDTO buildDto() {
        ExamScoreDTO d = new ExamScoreDTO();
        d.setCccd(tfCccd.getText().trim());
        d.setPhuongThuc((String) cbPhuongThuc.getSelectedItem());

        d.setDiemToan(parseDouble(tfToan));
        d.setDiemLy(parseDouble(tfLy));
        d.setDiemHoa(parseDouble(tfHoa));
        d.setDiemSinh(parseDouble(tfSinh));
        d.setDiemVan(parseDouble(tfVan));
        d.setDiemSu(parseDouble(tfSu));
        d.setDiemDia(parseDouble(tfDia));
        d.setDiemKtpl(parseDouble(tfKtpl));
        d.setN1Thi(parseDouble(tfN1Thi));
        d.setN1Cc(parseDouble(tfN1Cc));
        d.setDiemCncn(parseDouble(tfCncn));
        d.setDiemCnnn(parseDouble(tfCnnn));
        d.setNl1(parseDouble(tfNl1));
        d.setNk1(parseDouble(tfNk1));
        d.setNk2(parseDouble(tfNk2));

        return d;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.decode("#374151"));
        return lbl;
    }

    private JTextField makeTextField(String val) {
        JTextField tf = new JTextField(val);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(0, 38));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#D1D5DB")),
                new EmptyBorder(4, 10, 4, 10)));
        return tf;
    }

    private JTextField makeScoreField() {
        JTextField tf = new JTextField("0.0");
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setPreferredSize(new Dimension(80, 34));
        tf.setMaximumSize(new Dimension(100, 34));
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