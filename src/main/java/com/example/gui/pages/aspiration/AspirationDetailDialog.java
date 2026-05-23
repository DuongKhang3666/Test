package com.example.gui.pages.aspiration;

import com.example.bus.AspirationBUS;
import com.example.dto.AspirationDTO;
import com.example.gui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class AspirationDetailDialog extends JDialog {
    private final AspirationBUS aspirationBUS;
    private final AspirationDTO aspiration;
    private final AspirationListPanel listPanel;

    private TextBox txtCccd, txtMaNganh, txtThuTu, txtToHop, txtDiemThxt, txtDiemUtqd, txtDiemCong, txtDiemXt, txtPhuongThuc;
    private JComboBox<String> cbKetQua;

    public AspirationDetailDialog(Window owner, AspirationDTO aspiration, AspirationListPanel listPanel) {
        super(owner, "Phiếu Chi Tiết & Chỉnh Sửa Nguyện Vọng", ModalityType.APPLICATION_MODAL);
        this.listPanel = listPanel;
        this.aspirationBUS = new AspirationBUS();

        // GIẢI QUYẾT LỖI 1: Luôn nạp bản ghi mới nhất từ Database lên RAM để tránh stale dữ liệu từ JTable
        AspirationDTO freshAspiration = this.aspirationBUS.findById(aspiration.getIdnv());
        this.aspiration = freshAspiration != null ? freshAspiration : aspiration;

        setSize(700, 550);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.NEUTRAL);

        initComponents();
        loadDataToForm();
        setupLiveScoreCalculation(); // GIẢI QUYẾT LỖI 2: Kích hoạt lắng nghe sự kiện thay đổi điểm số real-time
    }

    private void initComponents() {
        // 1. Header Dialog
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 20, 0, 20));
        
        JLabel lblTitle = new JLabel("Thông Tin Phiếu Nguyện Vọng Chi Tiết");
        lblTitle.setFont(AppTheme.FONT_HEADLINE);
        lblTitle.setForeground(AppTheme.TEXT_DARK);
        
        JLabel lblSub = new JLabel("Mã số định danh hệ thống (ID NV): " + aspiration.getIdnv());
        lblSub.setFont(AppTheme.FONT_BODY);
        
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Form Grid Fields (Chia thành 2 cột hiển thị trực quan)
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 20, 15));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        txtCccd = new TextBox("");
        txtMaNganh = new TextBox("");
        txtThuTu = new TextBox("");
        txtToHop = new TextBox("");
        txtPhuongThuc = new TextBox("");
        txtDiemThxt = new TextBox("");
        txtDiemUtqd = new TextBox("");
        txtDiemCong = new TextBox("");
        
        // Khóa không cho sửa trực tiếp ô Tổng điểm hệ thống vì nó phải tự động tính toán
        txtDiemXt = new TextBox("");
        txtDiemXt.setEditable(false); 
        txtDiemXt.setBackground(new Color(245, 245, 245));
        
        cbKetQua = new JComboBox<>(new String[]{"Chờ xét", "Đúng hạn trúng tuyển", "Tạch tuyển", "Hủy bỏ"});
        cbKetQua.setFont(AppTheme.FONT_BODY);
        cbKetQua.putClientProperty("FlatLaf.style", "arc: 8; background: #FFFFFF;");

        // Đổ các ô vào Panel Grid
        formPanel.add(createFormGroup("Số CCCD Thí Sinh *", txtCccd));
        formPanel.add(createFormGroup("Mã Ngành Xét Tuyển *", txtMaNganh));
        formPanel.add(createFormGroup("Thứ Tự Nguyện Vọng *", txtThuTu));
        formPanel.add(createFormGroup("Tổ Hợp Môn Xét Tuyển", txtToHop));
        formPanel.add(createFormGroup("Phương Thức Đăng Ký", txtPhuongThuc));
        formPanel.add(createFormGroup("Điểm Tổ Hợp Thô (THXT)", txtDiemThxt));
        formPanel.add(createFormGroup("Điểm Ưu Tiên Quy Định (UTQD)", txtDiemUtqd));
        formPanel.add(createFormGroup("Điểm Cộng Khác", txtDiemCong));
        formPanel.add(createFormGroup("Tổng Điểm Xét Tuyển (Hệ Thống)", txtDiemXt));
        formPanel.add(createFormGroup("Trạng Thái Kết Quả", cbKetQua));

        add(formPanel, BorderLayout.CENTER);

        // 3. Thanh tác vụ nút bấm ở dưới cùng
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 15, 20));

        CancelButton btnClose = new CancelButton("Đóng Lại");
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);

        ActionButton btnUpdate = new ActionButton("Lưu Thay Đổi", null, "PRIMARY");
        btnUpdate.addActionListener(e -> handleSaveChanges());
        bottomPanel.add(btnUpdate);

        add(bottomPanel, BorderLayout.SOUTH);
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

    private void loadDataToForm() {
        txtCccd.setText(aspiration.getNnCccd());
        txtMaNganh.setText(aspiration.getNvManganh());
        txtThuTu.setText(String.valueOf(aspiration.getNvTt()));
        txtToHop.setText(aspiration.getTtThm() != null ? aspiration.getTtThm() : "");
        txtPhuongThuc.setText(aspiration.getTtPhuongthuc() != null ? aspiration.getTtPhuongthuc() : "");
        txtDiemThxt.setText(aspiration.getDiemThxt() != null ? String.valueOf(aspiration.getDiemThxt()) : "0.0");
        txtDiemUtqd.setText(aspiration.getDiemUtqd() != null ? String.valueOf(aspiration.getDiemUtqd()) : "0.0");
        txtDiemCong.setText(aspiration.getDiemCong() != null ? String.valueOf(aspiration.getDiemCong()) : "0.0");
        
        if (aspiration.getDiemXettuyen() != null) {
            txtDiemXt.setText(String.format("%.3f", aspiration.getDiemXettuyen()));
        } else {
            txtDiemXt.setText("Chưa tính");
        }
        
        if (aspiration.getNvKetqua() != null) {
            cbKetQua.setSelectedItem(aspiration.getNvKetqua().trim());
        }
    }

    /**
     * RÀNG BUỘC SỰ KIỆN: Tự động tính toán lại tổng điểm bất kể khi nào người dùng gõ thay đổi số.
     */
    private void setupLiveScoreCalculation() {
        DocumentListener liveCalcListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateLiveTotalScore(); }
            @Override public void removeUpdate(DocumentEvent e) { updateLiveTotalScore(); }
            @Override public void changedUpdate(DocumentEvent e) { updateLiveTotalScore(); }
        };

        txtDiemThxt.getDocument().addDocumentListener(liveCalcListener);
        txtDiemUtqd.getDocument().addDocumentListener(liveCalcListener);
        txtDiemCong.getDocument().addDocumentListener(liveCalcListener);
    }

    private void updateLiveTotalScore() {
        SwingUtilities.invokeLater(() -> {
            try {
                String thxtRaw = txtDiemThxt.getText().trim();
                String utqdRaw = txtDiemUtqd.getText().trim();
                String dcongRaw = txtDiemCong.getText().trim();

                double thxt = thxtRaw.isEmpty() ? 0.0 : Double.parseDouble(thxtRaw);
                double utqd = utqdRaw.isEmpty() ? 0.0 : Double.parseDouble(utqdRaw);
                double dcong = dcongRaw.isEmpty() ? 0.0 : Double.parseDouble(dcongRaw);

                // Tính điểm và chặn trần tối đa 30 điểm đúng quy chế bộ GD
                double total = Math.min(thxt + utqd + dcong, 30.0);
                txtDiemXt.setText(String.format("%.3f", total));
            } catch (NumberFormatException ignored) {
                txtDiemXt.setText("Đang nhập...");
            }
        });
    }

    private void handleSaveChanges() {
        try {
            aspiration.setNnCccd(txtCccd.getText().trim());
            aspiration.setNvManganh(txtMaNganh.getText().trim());
            aspiration.setNvTt(Integer.parseInt(txtThuTu.getText().trim()));
            aspiration.setTtThm(txtToHop.getText().trim());
            aspiration.setTtPhuongthuc(txtPhuongThuc.getText().trim());
            
            double thxt = Double.parseDouble(txtDiemThxt.getText().trim());
            double utqd = Double.parseDouble(txtDiemUtqd.getText().trim());
            double dcong = Double.parseDouble(txtDiemCong.getText().trim());

            aspiration.setDiemThxt(thxt);
            aspiration.setDiemUtqd(utqd);
            aspiration.setDiemCong(dcong);
            
            // Đồng bộ chặn trần lưu xuống DB
            double tongDiem = Math.min(thxt + utqd + dcong, 30.0);
            aspiration.setDiemXettuyen(tongDiem);
            
            aspiration.setNvKetqua((String) cbKetQua.getSelectedItem());

            // Lưu qua Hibernate
            aspirationBUS.update(aspiration);
            
            JOptionPane.showMessageDialog(this, "Cập nhật dữ liệu phiếu nguyện vọng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            
            listPanel.refreshData(); // Refresh bảng JTable chính ngay lập tức
            dispose(); 
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (AspirationBUS.AspirationValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessageText(), "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật hệ thống: " + ex.getMessage(), "Hệ thống lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}