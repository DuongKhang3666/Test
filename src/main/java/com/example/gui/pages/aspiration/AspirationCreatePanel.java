package com.example.gui.pages.aspiration;

import com.example.bus.AspirationBUS;
import com.example.dto.AspirationDTO;
import com.example.gui.components.*;

import javax.swing.*;
import java.awt.*;

public class AspirationCreatePanel extends JPanel {
    private final AspirationBUS aspirationBUS;
    
    // Khai báo Callback chức năng theo cấu trúc chuẩn dự án
    private final OnSuccessCallback onSuccess;
    private final OnCancelCallback onCancel;

    private TextBox txtCccd, txtMaNganh, txtThuTu, txtDiemThxt, txtPhuongThuc, txtToHop;

    @FunctionalInterface
    public interface OnSuccessCallback {
        void run(AspirationDTO createdCandidate);
    }

    @FunctionalInterface
    public interface OnCancelCallback {
        void run();
    }

    public AspirationCreatePanel(OnSuccessCallback onSuccess, OnCancelCallback onCancel) {
        this.aspirationBUS = new AspirationBUS();
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;

        setLayout(new BorderLayout(10, 10));
        setBackground(AppTheme.NEUTRAL);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        PageHeader header = new PageHeader("Thêm Nguyện Vọng Mới", "Nhập thông tin đăng ký xét tuyển của thí sinh.");
        add(header, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 20, 20));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        txtCccd = new TextBox("Nhập 12 số CCCD");
        txtMaNganh = new TextBox("Ví dụ: 7480201");
        txtThuTu = new TextBox("Ví dụ: 1");
        txtToHop = new TextBox("Ví dụ: A00");
        txtDiemThxt = new TextBox("Ví dụ: 24.5");
        txtPhuongThuc = new TextBox("Ví dụ: Xét điểm THPT");

        formPanel.add(createFormGroup("Số CCCD Thí sinh *", txtCccd));
        formPanel.add(createFormGroup("Mã Ngành Xét Tuyển *", txtMaNganh));
        formPanel.add(createFormGroup("Thứ Tự Nguyện Vọng *", txtThuTu));
        formPanel.add(createFormGroup("Tổ Hợp Môn", txtToHop));
        formPanel.add(createFormGroup("Điểm Tổ Hợp (Chưa cộng UT)", txtDiemThxt));
        formPanel.add(createFormGroup("Phương Thức Xét Tuyển", txtPhuongThuc));

        add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);

        CancelButton btnCancel = new CancelButton("Hủy bỏ");
        btnCancel.addActionListener(e -> onCancel.run()); // Thực hiện gọi lệnh đóng đóng overlay
        btnPanel.add(btnCancel);

        AddAndConfirmButton btnSave = new AddAndConfirmButton("Lưu Lại");
        btnSave.addActionListener(e -> handleSave());
        btnPanel.add(btnSave);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormGroup(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(AppTheme.FONT_LABEL);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void handleSave() {
        try {
            AspirationDTO dto = new AspirationDTO();
            dto.setNnCccd(txtCccd.getText().trim());
            dto.setNvManganh(txtMaNganh.getText().trim());
            dto.setNvTt(Integer.parseInt(txtThuTu.getText().trim()));
            dto.setTtThm(txtToHop.getText().trim());
            dto.setTtPhuongthuc(txtPhuongThuc.getText().trim());
            
            String scoreRaw = txtDiemThxt.getText().trim();
            if (!scoreRaw.isEmpty() && !scoreRaw.contains("Ví dụ")) {
                dto.setDiemThxt(Double.parseDouble(scoreRaw));
            }

            aspirationBUS.save(dto);
            
            if (onSuccess != null) {
                onSuccess.run(dto); // Trả dữ liệu về lớp điều khiển xử lý tiếp
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Thứ tự nguyện vọng hoặc điểm phải là số!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (AspirationBUS.AspirationValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessageText(), "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}