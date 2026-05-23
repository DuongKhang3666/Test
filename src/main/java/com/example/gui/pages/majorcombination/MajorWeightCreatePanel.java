package com.example.gui.pages.majorcombination;

import com.example.bus.MajorBUS;
import com.example.bus.MajorGroupBUS;
import com.example.bus.SubjectGroupBUS;
import com.example.dto.MajorDTO;
import com.example.dto.MajorGroupDTO;
import com.example.dto.SubjectGroupDTO;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class MajorWeightCreatePanel extends JPanel {
    private final MajorBUS majorBUS;
    private final MajorGroupBUS majorGroupBUS;
    private final SubjectGroupBUS subjectGroupBUS;

    private final Consumer<MajorGroupDTO> onSuccess;
    private final Runnable onCancel;

    public MajorWeightCreatePanel(Consumer<MajorGroupDTO> onSuccess, Runnable onCancel) {
        this.majorBUS = new MajorBUS();
        this.majorGroupBUS = new MajorGroupBUS();
        this.subjectGroupBUS = new SubjectGroupBUS();
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;

        JComboBox<MajorDTO> cmbMajor = new JComboBox<>(majorBUS.getAll().toArray(new MajorDTO[0]));
        cmbMajor.setPreferredSize(new Dimension(520, 36));
        cmbMajor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MajorDTO m) {
                    String code = m.getMaNganh() != null ? m.getMaNganh() : "";
                    String name = m.getTenNganh() != null ? m.getTenNganh() : "";
                    setText(code + " - " + name);
                }
                return this;
            }
        });

        JComboBox<SubjectGroupDTO> cmbSubjectGroup = new JComboBox<>(subjectGroupBUS.getAll().toArray(new SubjectGroupDTO[0]));
        cmbSubjectGroup.setPreferredSize(new Dimension(520, 36));
        cmbSubjectGroup.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SubjectGroupDTO sg) {
                    String code = sg.getMaToHop() != null ? sg.getMaToHop() : "";
                    String name = sg.getTenToHop() != null ? sg.getTenToHop() : "";
                    setText(name == null || name.isBlank() ? code : (code + " - " + name));
                }
                return this;
            }
        });

        JLabel lblW1 = new JLabel("Hệ số môn 1");
        JLabel lblW2 = new JLabel("Hệ số môn 2");
        JLabel lblW3 = new JLabel("Hệ số môn 3");

        Integer[] weightOptions = new Integer[]{1, 2, 3, 4, 5};
        JComboBox<Integer> cmbW1 = new JComboBox<>(weightOptions);
        JComboBox<Integer> cmbW2 = new JComboBox<>(weightOptions);
        JComboBox<Integer> cmbW3 = new JComboBox<>(weightOptions);
        cmbW1.setPreferredSize(new Dimension(520, 36));
        cmbW2.setPreferredSize(new Dimension(520, 36));
        cmbW3.setPreferredSize(new Dimension(520, 36));

        cmbSubjectGroup.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                SubjectGroupDTO sg = (SubjectGroupDTO) e.getItem();
                updateWeightLabels(lblW1, lblW2, lblW3, sg);
            }
        });

        fields.add(createFieldGroup("Chọn ngành", cmbMajor), gbc);
        gbc.gridy = 1;
        fields.add(createFieldGroup("Chọn tổ hợp môn", cmbSubjectGroup), gbc);
        gbc.gridy = 2;
        fields.add(createFieldGroup(lblW1, cmbW1), gbc);
        gbc.gridy = 3;
        fields.add(createFieldGroup(lblW2, cmbW2), gbc);
        gbc.gridy = 4;
        fields.add(createFieldGroup(lblW3, cmbW3), gbc);

        SubjectGroupDTO selected = (SubjectGroupDTO) cmbSubjectGroup.getSelectedItem();
        updateWeightLabels(lblW1, lblW2, lblW3, selected);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        footer.setBackground(Color.decode("#F9FAFB"));

        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu thay đổi");

        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnSave.setPreferredSize(new Dimension(130, 36));
        btnSave.setBackground(Color.decode("#1D4ED8"));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);

        btnCancel.addActionListener(e -> onCancel.run());
        btnSave.addActionListener(e -> {
            MajorDTO major = (MajorDTO) cmbMajor.getSelectedItem();
            SubjectGroupDTO sg = (SubjectGroupDTO) cmbSubjectGroup.getSelectedItem();
            if (major == null || sg == null || major.getMaNganh() == null || sg.getMaToHop() == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đủ ngành và tổ hợp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String maNganh = major.getMaNganh().trim();
            String maToHop = sg.getMaToHop().trim();
            String key = maNganh + "_" + maToHop;

            MajorGroupDTO existing = majorGroupBUS.findByTbKeys(key);
            if (existing == null) {
                existing = majorGroupBUS.findByMaNganhAndMaToHop(maNganh, maToHop);
            }
            if (existing != null) {
                JOptionPane.showMessageDialog(this,
                        "Ngành này đã được gán tổ hợp này rồi, vui lòng chọn sửa dòng hiện tại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Integer w1 = (Integer) cmbW1.getSelectedItem();
            Integer w2 = (Integer) cmbW2.getSelectedItem();
            Integer w3 = (Integer) cmbW3.getSelectedItem();
            if (w1 == null) w1 = 1;
            if (w2 == null) w2 = 1;
            if (w3 == null) w3 = 1;

            try {
                MajorGroupDTO dto = new MajorGroupDTO();
                dto.setMaNganh(maNganh);
                dto.setMaToHop(maToHop);
                dto.setTbKeys(key);
                dto.setThMon1(safeUpper(sg.getMon1()));
                dto.setThMon2(safeUpper(sg.getMon2()));
                dto.setThMon3(safeUpper(sg.getMon3()));
                dto.setHsMon1(w1);
                dto.setHsMon2(w2);
                dto.setHsMon3(w3);
                dto.setDoLech(BigDecimal.ZERO);

                majorGroupBUS.save(dto);
                onSuccess.accept(dto);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể lưu mapping: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);

        add(fields, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void updateWeightLabels(JLabel w1, JLabel w2, JLabel w3, SubjectGroupDTO sg) {
        if (sg == null) {
            w1.setText("Hệ số môn 1");
            w2.setText("Hệ số môn 2");
            w3.setText("Hệ số môn 3");
            return;
        }
        w1.setText("Hệ số môn " + displaySubject(sg.getMon1()));
        w2.setText("Hệ số môn " + displaySubject(sg.getMon2()));
        w3.setText("Hệ số môn " + displaySubject(sg.getMon3()));
    }

    private String displaySubject(String code) {
        if (code == null || code.isBlank()) {
            return "-";
        }
        String key = code.trim().toUpperCase(Locale.ROOT);
        return MajorWeightManagementPanel.SUBJECT_DISPLAY.getOrDefault(key, code.trim());
    }

    private String safeUpper(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private JPanel createFieldGroup(String labelText, JComponent component) {
        JLabel lbl = new JLabel(labelText);
        return createFieldGroup(lbl, component);
    }

    private JPanel createFieldGroup(JLabel label, JComponent component) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);

        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.decode("#4B5563"));

        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#D1D5DB"), 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        p.add(label, BorderLayout.NORTH);
        p.add(component, BorderLayout.CENTER);
        return p;
    }
}
