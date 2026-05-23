package com.example.gui.pages.aspiration;

import com.example.bus.AspirationBUS;
import com.example.dto.AspirationDTO;
import com.example.gui.components.*;

import javax.swing.*;
import java.awt.*;

public class AspirationEditPanel extends JPanel {
    private final AspirationBUS aspirationBUS;
    private final AspirationDTO currentAspiration;

    private final OnSuccessCallback onSuccess;
    private final OnCancelCallback onCancel;

    private TextBox txtCccd, txtMaNganh, txtThuTu, txtDiemThxt, txtPhuongThuc, txtToHop;

    @FunctionalInterface
    public interface OnSuccessCallback {
        void run(AspirationDTO updatedCandidate);
    }

    @FunctionalInterface
    public interface OnCancelCallback {
        void run();
    }

    public AspirationEditPanel(AspirationDTO aspiration, OnSuccessCallback onSuccess, OnCancelCallback onCancel) {
        this.aspirationBUS = new AspirationBUS();
        this.currentAspiration = aspiration;
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;

        setLayout(new BorderLayout(10, 10));
        setBackground(AppTheme.NEUTRAL);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        setAspirationData(aspiration);
    }

    private void initComponents() {
        PageHeader header = new PageHeader("Cập Nhật Nguyện Vọng", "Chỉnh sửa thông tin nguyện vọng đăng ký.");
        add(header, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 20, 20));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        txtCccd = new TextBox("");
        txtMaNganh = new TextBox("");
        txtThuTu = new TextBox("");
        txtToHop = new TextBox("");
        txtDiemThxt = new TextBox("");
        txtPhuongThuc = new TextBox("");

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
        btnCancel.addActionListener(e -> onCancel.run());
        btnPanel.add(btnCancel);

        AddAndConfirmButton btnSave = new AddAndConfirmButton("Cập Nhật");
        btnSave.addActionListener(e -> handleUpdate());
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

    private void setAspirationData(AspirationDTO aspiration) {
        if (aspiration == null) return;
        txtCccd.setText(aspiration.getNnCccd());
        txtMaNganh.setText(aspiration.getNvManganh());
        txtThuTu.setText(String.valueOf(aspiration.getNvTt()));
        txtToHop.setText(aspiration.getTtThm() != null ? aspiration.getTtThm() : "");
        txtDiemThxt.setText(aspiration.getDiemThxt() != null ? String.valueOf(aspiration.getDiemThxt()) : "");
        txtPhuongThuc.setText(aspiration.getTtPhuongthuc() != null ? aspiration.getTtPhuongthuc() : "");
    }

    private void handleUpdate() {
        if (currentAspiration == null) return;
        try {
            currentAspiration.setNnCccd(txtCccd.getText().trim());
            currentAspiration.setNvManganh(txtMaNganh.getText().trim());
            currentAspiration.setNvTt(Integer.parseInt(txtThuTu.getText().trim()));
            currentAspiration.setTtThm(txtToHop.getText().trim());
            currentAspiration.setTtPhuongthuc(txtPhuongThuc.getText().trim());
            
            String scoreRaw = txtDiemThxt.getText().trim();
            currentAspiration.setDiemThxt(scoreRaw.isEmpty() ? null : Double.parseDouble(scoreRaw));

            aspirationBUS.update(currentAspiration);
            
            if (onSuccess != null) {
                onSuccess.run(currentAspiration);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Thứ tự nguyện vọng hoặc điểm sai định dạng số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (AspirationBUS.AspirationValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessageText(), "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }
}