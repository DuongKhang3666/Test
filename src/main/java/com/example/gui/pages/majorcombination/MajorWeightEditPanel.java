package com.example.gui.pages.majorcombination;

import com.example.bus.MajorGroupBUS;
import com.example.bus.SubjectGroupBUS;
import com.example.dto.MajorGroupDTO;
import com.example.dto.SubjectGroupDTO;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MajorWeightEditPanel extends JPanel {
    private final MajorGroupBUS majorGroupBUS;
    private final SubjectGroupDTO[] dummy = new SubjectGroupDTO[0];
    private final Consumer<MajorGroupDTO> onSuccess;
    private final Runnable onCancel;
    private final MajorGroupDTO existing;

    public MajorWeightEditPanel(MajorGroupDTO existing, Consumer<MajorGroupDTO> onSuccess, Runnable onCancel) {
        this.majorGroupBUS = new MajorGroupBUS();
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;
        this.existing = existing;

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

        JLabel lblMajor = new JLabel(existing.getMaNganh());
        JLabel lblCombo = new JLabel(existing.getMaToHop());

        JLabel lblW1 = new JLabel("Hệ số môn 1");
        JLabel lblW2 = new JLabel("Hệ số môn 2");
        JLabel lblW3 = new JLabel("Hệ số môn 3");

        Integer[] weightOptions = new Integer[]{1, 2, 3, 4, 5};
        JComboBox<Integer> cmbW1 = new JComboBox<>(weightOptions);
        JComboBox<Integer> cmbW2 = new JComboBox<>(weightOptions);
        JComboBox<Integer> cmbW3 = new JComboBox<>(weightOptions);

        if (existing.getHsMon1() != null) cmbW1.setSelectedItem(existing.getHsMon1());
        if (existing.getHsMon2() != null) cmbW2.setSelectedItem(existing.getHsMon2());
        if (existing.getHsMon3() != null) cmbW3.setSelectedItem(existing.getHsMon3());

        fields.add(createFieldGroup("Ngành", lblMajor), gbc);
        gbc.gridy = 1;
        fields.add(createFieldGroup("Tổ hợp", lblCombo), gbc);
        gbc.gridy = 2;
        fields.add(createFieldGroup(lblW1, cmbW1), gbc);
        gbc.gridy = 3;
        fields.add(createFieldGroup(lblW2, cmbW2), gbc);
        gbc.gridy = 4;
        fields.add(createFieldGroup(lblW3, cmbW3), gbc);

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
            Integer w1 = (Integer) cmbW1.getSelectedItem();
            Integer w2 = (Integer) cmbW2.getSelectedItem();
            Integer w3 = (Integer) cmbW3.getSelectedItem();
            if (w1 == null) w1 = 1;
            if (w2 == null) w2 = 1;
            if (w3 == null) w3 = 1;

            try {
                MajorGroupDTO dto = new MajorGroupDTO();
                dto.setId(existing.getId());
                dto.setMaNganh(existing.getMaNganh());
                dto.setMaToHop(existing.getMaToHop());
                dto.setTbKeys(existing.getTbKeys());
                dto.setThMon1(existing.getThMon1());
                dto.setThMon2(existing.getThMon2());
                dto.setThMon3(existing.getThMon3());
                dto.setHsMon1(w1);
                dto.setHsMon2(w2);
                dto.setHsMon3(w3);
                dto.setDoLech(existing.getDoLech());
                majorGroupBUS.update(dto);
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
