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

public class ConversionTableCreatePage extends JPanel {
    private final ConversionTableBUS conversionTableBUS = new ConversionTableBUS();
    private final JTextField txtMaQuydoi = new JTextField();
    private final JComboBox<String> cbPhuongthuc = new JComboBox<>(new String[]{"V-SAT", "ĐGNL", "THPT", "Xét tuyển"});
    private final JTextField txtTohop = new JTextField();
    private final JTextField txtMon = new JTextField();
    private final JTextField txtPhanvi = new JTextField();
    private final JTextField txtDiemA = new JTextField();
    private final JTextField txtDiemB = new JTextField();
    private final JTextField txtDiemC = new JTextField();
    private final JTextField txtDiemD = new JTextField();

    public ConversionTableCreatePage(Runnable onSaved) {
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 20, 12, 20));
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 0.5;

        // Dòng 1
        gbc.gridx = 0; gbc.gridy = 0;
        addField(form, gbc, "Mã Quy Đổi", txtMaQuydoi, "Ví dụ: QD_VSAT_TOAN");
        gbc.gridx = 1;
        addComboBoxField(form, gbc, "Phương thức xét tuyển", cbPhuongthuc);

        // Dòng 2
        gbc.gridx = 0; gbc.gridy = 1;
        addField(form, gbc, "Tổ hợp (nếu có)", txtTohop, "Ví dụ: A00, B00");
        gbc.gridx = 1;
        addField(form, gbc, "Môn học (nếu có)", txtMon, "Ví dụ: Toán, Tiếng Anh");

        // Dòng 3
        gbc.gridx = 0; gbc.gridy = 2;
        addField(form, gbc, "Phân vị / Nhóm điểm", txtPhanvi, "Ví dụ: 20% hoặc IELTS 6.5");
        gbc.gridx = 1;
        form.add(Box.createGlue(), gbc);

        // Dòng 4
        gbc.gridx = 0; gbc.gridy = 3;
        addField(form, gbc, "Điểm mốc thô A (Mốc dưới x)", txtDiemA, "Ví dụ: 114.5");
        gbc.gridx = 1;
        addField(form, gbc, "Điểm mốc thô B (Mốc trên x)", txtDiemB, "Ví dụ: 122.5");

        // Dòng 5
        gbc.gridx = 0; gbc.gridy = 4;
        addField(form, gbc, "Điểm quy đổi C (Tương ứng mốc A)", txtDiemC, "Ví dụ: 7.0");
        gbc.gridx = 1;
        addField(form, gbc, "Điểm quy đổi D (Tương ứng mốc B)", txtDiemD, "Ví dụ: 7.75");

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton btnCancel = new CancelButton("Hủy bỏ");
        JButton btnSave = new AddAndConfirmButton("Xác nhận lưu");

        btnCancel.addActionListener(e -> onSaved.run());
        btnSave.addActionListener(e -> {
            try {
                ConversionTableDTO dto = new ConversionTableDTO();
                dto.setMaQuydoi(txtMaQuydoi.getText().trim());
                dto.setPhuongthuc(cbPhuongthuc.getSelectedItem().toString());
                dto.setTohop(txtTohop.getText().trim().isEmpty() ? null : txtTohop.getText().trim());
                dto.setMon(txtMon.getText().trim().isEmpty() ? null : txtMon.getText().trim());
                dto.setPhanvi(txtPhanvi.getText().trim().isEmpty() ? null : txtPhanvi.getText().trim());

                dto.setDiemA(parseScoreOrZero(txtDiemA.getText().trim()));
                dto.setDiemB(parseScoreOrZero(txtDiemB.getText().trim()));
                dto.setDiemC(parseScoreOrZero(txtDiemC.getText().trim()));
                dto.setDiemD(parseScoreOrZero(txtDiemD.getText().trim()));

                boolean saved = conversionTableBUS.save(dto);
                if (!saved) {
                    JOptionPane.showMessageDialog(this, "Không thể lưu dữ liệu. Vui lòng kiểm tra lại thông tin.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this, "Thêm mức quy đổi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                onSaved.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Điểm phải là số hợp lệ.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
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