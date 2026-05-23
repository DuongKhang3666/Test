package com.example.gui.pages.majorcombination;

import com.example.bus.SubjectGroupBUS;
import com.example.dto.MajorGroupDTO;
import com.example.dto.SubjectGroupDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.function.Consumer;

/**
 * Panel để tạo mới hoặc sửa MajorGroup (tổ hợp ngành-tổ hợp môn)
 * UI cập nhật: Giữ nguyên Tiếng Việt, chia tỷ lệ ô nhập liệu hợp lý (không bị giãn quá to).
 */
public class MajorGroupEditPanel extends JPanel {
    private static final Pattern COMBO_CODE_PATTERN = Pattern.compile("^[A-Z]\\d{2}$");
    private static final String[] FALLBACK_SUBJECT_CODES = {
            "TO", "LI", "HO", "SI", "SU", "DI", "VA", "TI",
            "KTPL", "CNCN", "CNNN", "N1", "NK1", "NK2"
    };

    private SubjectGroupBUS subjectGroupBUS = new SubjectGroupBUS();
    private JTextField txtComboCode;
    private JTextField txtComboName;
    private JTextField txtDoLech;
    private JComboBox<String> cmbSubj1;
    private JComboBox<String> cmbSubj2;
    private JComboBox<String> cmbSubj3;

    public MajorGroupEditPanel(MajorGroupDTO existingData, Consumer<MajorGroupDTO> onSave, Runnable onCancel) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 1. TITLE SECTION ---
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EmptyBorder(20, 25, 10, 25));
        
        // --- 2. FORM SECTION ---
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setBorder(new EmptyBorder(10, 25, 20, 25));

        // Row 1: Mã tổ hợp (Ngắn) + Tên tổ hợp (Dài)
        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 15); // Khoảng cách giữa 2 ô
        
        txtComboCode = createStyledTextField();
        if (existingData != null) txtComboCode.setText(existingData.getMaToHop());
        gbc.weightx = 0.35; // Chiếm 35% chiều rộng
        row1.add(createFieldGroup("Mã tổ hợp", txtComboCode), gbc);

        txtComboName = createStyledTextField();
        if (existingData != null) txtComboName.setText(existingData.getTbKeys());
        gbc.gridx = 1;
        gbc.weightx = 0.65; // Chiếm 65% chiều rộng
        gbc.insets = new Insets(0, 0, 0, 0); // Bỏ khoảng cách bên phải ô cuối
        row1.add(createFieldGroup("Tên ngành", txtComboName), gbc);

        JPanel rowDoLech = new JPanel(new GridLayout(1, 1, 0, 0));
        rowDoLech.setBackground(Color.WHITE);
        txtDoLech = createStyledTextField();
        if (existingData != null && existingData.getDoLech() != null) {
            txtDoLech.setText(existingData.getDoLech().toPlainString());
        }
        rowDoLech.add(createFieldGroup("Độ lệch", txtDoLech));

        // Separator line
        JPanel sepPanel = new JPanel();
        sepPanel.setBackground(Color.WHITE);
        sepPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.decode("#E5E7EB")));
        sepPanel.setPreferredSize(new Dimension(10, 30));

        // Row 2: 3 Môn học (Chia đều 3 cột)
        JPanel row2 = new JPanel(new GridLayout(1, 3, 15, 0));
        row2.setBackground(Color.WHITE);

        String[] subjNames = buildSubjectOptions(existingData);

        cmbSubj1 = createStyledComboBox(subjNames);
        cmbSubj2 = createStyledComboBox(subjNames);
        cmbSubj3 = createStyledComboBox(subjNames);

        if (existingData != null) {
            cmbSubj1.setSelectedItem(existingData.getThMon1() == null ? "Chọn môn học" : existingData.getThMon1());
            cmbSubj2.setSelectedItem(existingData.getThMon2() == null ? "Chọn môn học" : existingData.getThMon2());
            cmbSubj3.setSelectedItem(existingData.getThMon3() == null ? "Chọn môn học" : existingData.getThMon3());
        }

        row2.add(createFieldGroup("Môn học 1", cmbSubj1));
        row2.add(createFieldGroup("Môn học 2", cmbSubj2));
        row2.add(createFieldGroup("Môn học 3", cmbSubj3));

        formWrapper.add(row1);
        formWrapper.add(Box.createVerticalStrut(10));
        formWrapper.add(rowDoLech);
        formWrapper.add(sepPanel);
        formWrapper.add(Box.createVerticalStrut(10));
        formWrapper.add(row2);
        
        // MẸO: Đẩy formWrapper lên trên cùng để các ô nhập không bị kéo giãn dọc
        JPanel topAlignedContainer = new JPanel(new BorderLayout());
        topAlignedContainer.setBackground(Color.WHITE);
        topAlignedContainer.add(formWrapper, BorderLayout.NORTH);

        // --- 3. FOOTER SECTION ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        footerPanel.setBackground(Color.decode("#F9FAFB")); 
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.decode("#E5E7EB")));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(90, 38));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> onCancel.run());

        JButton btnSave = new JButton("Lưu tổ hợp");
        btnSave.setPreferredSize(new Dimension(130, 38));
        btnSave.setBackground(Color.decode("#1D4ED8")); 
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> handleSave(existingData, onSave));

        footerPanel.add(btnCancel);
        footerPanel.add(btnSave);

        // Thêm các phần vào Panel chính
        add(titlePanel, BorderLayout.NORTH);
        add(topAlignedContainer, BorderLayout.CENTER); // Dùng container đã chặn kéo giãn dọc
        add(footerPanel, BorderLayout.SOUTH);
    }

    // Helper: Tạo cụm Label trên Component dưới
    private JPanel createFieldGroup(String labelText, JComponent component) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.decode("#4B5563"));
        p.add(lbl, BorderLayout.NORTH);
        p.add(component, BorderLayout.CENTER);
        return p;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        // Đặt kích thước cơ bản để GridBagLayout không bóp méo
        tf.setPreferredSize(new Dimension(150, 38)); 
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#D1D5DB"), 1),
            new EmptyBorder(0, 10, 0, 10)
        ));
        return tf;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setPreferredSize(new Dimension(150, 38));
        cb.setBackground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return cb;
    }

    private String[] buildSubjectOptions(MajorGroupDTO existingData) {
        List<SubjectGroupDTO> allSubjectGroups = subjectGroupBUS.getAll();
        Set<String> uniqueSubjects = new LinkedHashSet<>();

        for (SubjectGroupDTO sg : allSubjectGroups) {
            addIfNotBlank(uniqueSubjects, sg.getMon1());
            addIfNotBlank(uniqueSubjects, sg.getMon2());
            addIfNotBlank(uniqueSubjects, sg.getMon3());
        }

        if (existingData != null) {
            addIfNotBlank(uniqueSubjects, existingData.getThMon1());
            addIfNotBlank(uniqueSubjects, existingData.getThMon2());
            addIfNotBlank(uniqueSubjects, existingData.getThMon3());
        }

        if (uniqueSubjects.isEmpty()) {
            for (String fallbackSubject : FALLBACK_SUBJECT_CODES) {
                addIfNotBlank(uniqueSubjects, fallbackSubject);
            }
        }

        List<String> subjects = new ArrayList<>(uniqueSubjects);
        subjects.sort(Comparator.naturalOrder());
        subjects.add(0, "Chọn môn học");
        return subjects.toArray(new String[0]);
    }

    private void addIfNotBlank(Set<String> target, String value) {
        if (value != null && !value.trim().isEmpty()) {
            target.add(value.trim());
        }
    }

    private void handleSave(MajorGroupDTO existingData, Consumer<MajorGroupDTO> onSave) {
        String maToHop = txtComboCode.getText().trim();
        String tenToHop = txtComboName.getText().trim();
        if (maToHop.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã tổ hợp", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!COMBO_CODE_PATTERN.matcher(maToHop).matches()) {
            JOptionPane.showMessageDialog(this, "Mã tổ hợp phải đúng dạng A00, B89,...", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (tenToHop.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập  tên ngành", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MajorGroupDTO dto = new MajorGroupDTO();
        if (existingData != null) {
            dto.setId(existingData.getId());
            dto.setMaNganh(existingData.getMaNganh());
        }
        dto.setMaToHop(maToHop);
        dto.setTbKeys(tenToHop);

        String s1 = (String) cmbSubj1.getSelectedItem();
        String s2 = (String) cmbSubj2.getSelectedItem();
        String s3 = (String) cmbSubj3.getSelectedItem();

        if ("Chọn môn học".equals(s1) || "Chọn môn học".equals(s2) || "Chọn môn học".equals(s3)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đủ 3 môn học", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (s1.equals(s2) || s1.equals(s3) || s2.equals(s3)) {
            JOptionPane.showMessageDialog(this, "3 môn học không được trùng nhau", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal doLech = null;
        String doLechRaw = txtDoLech != null ? txtDoLech.getText().trim() : "";
        if (!doLechRaw.isEmpty()) {
            try {
                String cleaned = doLechRaw.replaceAll("[,\\s]", "");
                doLech = new BigDecimal(cleaned);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Độ lệch phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        dto.setThMon1(s1);
        dto.setThMon2(s2);
        dto.setThMon3(s3);
        dto.setDoLech(doLech);

        onSave.accept(dto);
    }
}