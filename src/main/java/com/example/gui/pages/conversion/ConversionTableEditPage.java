package com.example.gui.pages.conversion;

import com.example.bus.ConversionTableBUS;
import com.example.dto.ConversionTableDTO;
import com.example.gui.components.AddAndConfirmButton;
import com.example.gui.components.CancelButton;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class ConversionTableEditPage extends JPanel {
    private final ConversionTableBUS conversionTableBUS = new ConversionTableBUS();
    private final ConversionTableDTO dto;
    private final JTextField txtMaQuydoi = new JTextField();
    private final JComboBox<String> cbPhuongthuc = new JComboBox<>(new String[]{"V-SAT", "ĐGNL", "THPT", "Xét tuyển"});
    private final JTextField txtTohop = new JTextField();
    private final JTextField txtMon = new JTextField();
    private final JTextField txtPhanvi = new JTextField();
    private final JTextField txtDiemA = new JTextField();
    private final JTextField txtDiemB = new JTextField();
    private final JTextField txtDiemC = new JTextField();
    private final JTextField txtDiemD = new JTextField();

    public ConversionTableEditPage(ConversionTableDTO dto, Runnable onSaved) {
        this.dto = dto;
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 20, 12, 20));
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 0.5;

        txtMaQuydoi.setText(dto.getMaQuydoi());
        cbPhuongthuc.setSelectedItem(dto.getPhuongthuc());
        txtTohop.setText(dto.getTohop() != null ? dto.getTohop() : "");
        txtMon.setText(dto.getMon() != null ? dto.getMon() : "");
        txtPhanvi.setText(dto.getPhanvi() != null ? dto.getPhanvi() : "");
        txtDiemA.setText(dto.getDiemA() != null ? dto.getDiemA().toString() : "0");
        txtDiemB.setText(dto.getDiemB() != null ? dto.getDiemB().toString() : "0");
        txtDiemC.setText(dto.getDiemC() != null ? dto.getDiemC().toString() : "0");
        txtDiemD.setText(dto.getDiemD() != null ? dto.getDiemD().toString() : "0");

        txtMaQuydoi.setEditable(false);
        txtMaQuydoi.setEnabled(false);

        gbc.gridx = 0; gbc.gridy = 0;
        addField(form, gbc, "Mã Quy Đổi (Không thể sửa)", txtMaQuydoi, "");
        gbc.gridx = 1;
        addComboBoxField(form, gbc, "Phương thức xét tuyển", cbPhuongthuc);

        gbc.gridx = 0; gbc.gridy = 1;
        addField(form, gbc, "Tổ hợp (nếu có)", txtTohop, "Ví dụ: A00, B00");
        gbc.gridx = 1;
        addField(form, gbc, "Môn học (nếu có)", txtMon, "Ví dụ: Toán, Tiếng Anh");

        gbc.gridx = 0; gbc.gridy = 2;
        addField(form, gbc, "Phân vị / Nhóm điểm", txtPhanvi, "Ví dụ: 20% hoặc IELTS 6.5");
        gbc.gridx = 1;
        form.add(Box.createGlue(), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        addField(form, gbc, "Điểm mốc thô A (Mốc dưới x)", txtDiemA, "");
        gbc.gridx = 1;
        addField(form, gbc, "Điểm mốc thô B (Mốc trên x)", txtDiemB, "");

        gbc.gridx = 0; gbc.gridy = 4;
        addField(form, gbc, "Điểm quy đổi C (Tương ứng mốc A)", txtDiemC, "");
        gbc.gridx = 1;
        addField(form, gbc, "Điểm quy đổi D (Tương ứng mốc B)", txtDiemD, "");

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton btnCancel = new CancelButton("Hủy bỏ");
        JButton btnSave = new AddAndConfirmButton("Lưu thay đổi");

        btnCancel.addActionListener(e -> onSaved.run());
        btnSave.addActionListener(e -> {
            try {
                this.dto.setPhuongthuc(cbPhuongthuc.getSelectedItem().toString());
                this.dto.setTohop(txtTohop.getText().trim().isEmpty() ? null : txtTohop.getText().trim());
                this.dto.setMon(txtMon.getText().trim().isEmpty() ? null : txtMon.getText().trim());
                this.dto.setPhanvi(txtPhanvi.getText().trim().isEmpty() ? null : txtPhanvi.getText().trim());
                
                this.dto.setDiemA(parseScoreOrZero(txtDiemA.getText().trim()));
                this.dto.setDiemB(parseScoreOrZero(txtDiemB.getText().trim()));
                this.dto.setDiemC(parseScoreOrZero(txtDiemC.getText().trim()));
                this.dto.setDiemD(parseScoreOrZero(txtDiemD.getText().trim()));

                boolean updated = conversionTableBUS.update(this.dto);
                if (!updated) {
                    JOptionPane.showMessageDialog(this, "Không thể lưu cập nhật. Vui lòng kiểm tra lại thông tin.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this, "Cập nhật dữ liệu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                onSaved.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Điểm phải là số hợp lệ.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu cập nhật: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);

        add(form, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JTextField field, String placeholder) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel jLbl = new JLabel(label);
        jLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLbl.setForeground(Color.decode("#475569"));

        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        field.setPreferredSize(new Dimension(340, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        wrapper.add(jLbl, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        panel.add(wrapper, gbc);
    }

    private void addComboBoxField(JPanel panel, GridBagConstraints gbc, String label, JComboBox<String> combo) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel jLbl = new JLabel(label);
        jLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLbl.setForeground(Color.decode("#475569"));

        combo.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        combo.setPreferredSize(new Dimension(340, 38));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        wrapper.add(jLbl, BorderLayout.NORTH);
        wrapper.add(combo, BorderLayout.CENTER);
        panel.add(wrapper, gbc);
    }

    private BigDecimal parseScoreOrZero(String raw) {
        return raw == null || raw.isEmpty() ? BigDecimal.ZERO : new BigDecimal(raw);
    }
}