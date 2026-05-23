package com.example.gui.pages.candidate;

import com.example.bus.CandidateBUS;
import com.example.bus.CandidateBUS.CandidateValidationException;
import com.example.dto.CandidateDTO;
import com.example.gui.components.ComboBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Form để chỉnh sửa thí sinh (Edit)
 */
public class CandidateEditPanel extends JPanel {

    private Map<String, JLabel> errorLabels = new HashMap<>();

    private JTextField tfCccd;
    private JTextField tfSoBaoDanh;
    private JTextField tfHo;
    private JTextField tfTen;
    private JTextField tfNgaySinh;
    private JTextField tfSoDienThoai;
    private JTextField tfEmail;
    private JTextField tfNoiSinh;
    private ComboBox<String> cbGioiTinh;
    private ComboBox<String> cbDoiTuong;
    private ComboBox<String> cbKhuVuc;

    private final CandidateDTO dto;
    private CandidateBUS candidateBUS = new CandidateBUS();

    public CandidateEditPanel(CandidateDTO dto, Consumer<CandidateDTO> onSave, Runnable onCancel) {
        java.util.Objects.requireNonNull(dto, "Dữ liệu thí sinh không được rỗng!");
        java.util.Objects.requireNonNull(onSave, "Hành động onSave không được rỗng!");
        java.util.Objects.requireNonNull(onCancel, "Hành động onCancel không được rỗng!");
        this.dto = dto;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 248));
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));
        card.add(new JScrollPane(createFormPanel()), BorderLayout.CENTER);
        card.add(createFooterPanel(onSave, onCancel), BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 8, 6, 8);
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;

        // Init tất cả fields
        tfCccd = CandidateFormSupport.createTextField();
        tfCccd.setText(dto.getCccd() != null ? dto.getCccd() : "");
        tfSoBaoDanh = CandidateFormSupport.createTextField();
        tfSoBaoDanh.setText(dto.getSoBaoDanh() != null ? dto.getSoBaoDanh() : "");
        tfHo = CandidateFormSupport.createTextField();
        tfHo.setText(dto.getHo() != null ? dto.getHo() : "");
        tfTen = CandidateFormSupport.createTextField();
        tfTen.setText(dto.getTen() != null ? dto.getTen() : "");
        tfNgaySinh = CandidateFormSupport.createDateTextField();
        tfNgaySinh.setText(CandidateFormSupport.formatDateForDisplay(dto.getNgaySinh()));
        tfSoDienThoai = CandidateFormSupport.createNumericTextField(10);
        tfSoDienThoai.setText(dto.getSoDienThoai() != null ? dto.getSoDienThoai() : "");
        tfEmail = CandidateFormSupport.createTextField();
        tfEmail.setText(dto.getEmail() != null ? dto.getEmail() : "");
        tfNoiSinh = CandidateFormSupport.createTextField();
        tfNoiSinh.setText(dto.getNoiSinh() != null ? dto.getNoiSinh() : "");
        cbGioiTinh = new ComboBox<>(new String[]{"-- Vui lòng chọn --", "Nam", "Nữ"});
        if (dto.getGioiTinh() != null) cbGioiTinh.setSelectedItem(dto.getGioiTinh());
        cbDoiTuong = new ComboBox<>(new String[]{"Không có", "01", "02a", "02b", "03a", "03b", "03c", "03d", "03đ", "04a", "04b", "05a", "05b", "05c", "06a", "06b", "06c"});
        if (dto.getDoiTuong() != null && !dto.getDoiTuong().trim().isEmpty()) {
            cbDoiTuong.setSelectedItem(dto.getDoiTuong());
        } else {
            cbDoiTuong.setSelectedItem("Không có");
        }
        cbKhuVuc = new ComboBox<>(new String[]{"-- Vui lòng chọn --", "1", "2", "2NT", "3"});
        if (dto.getKhuVuc() != null) cbKhuVuc.setSelectedItem(dto.getKhuVuc());

        CandidateFormSupport.addField(wrapper, c, row, 0, "Họ", tfHo, "ho", false, null, errorLabels);
        CandidateFormSupport.addField(wrapper, c, row, 2, "Tên", tfTen, "ten", true, null, errorLabels);
        row += 2;
        
        CandidateFormSupport.addField(wrapper, c, row, 0, "CCCD", tfCccd, "cccd", true, null, errorLabels);
        CandidateFormSupport.addField(wrapper, c, row, 2, "Số báo danh", tfSoBaoDanh, "sbd", false, null, errorLabels);
        row += 2;
        
        CandidateFormSupport.addField(wrapper, c, row, 0, "Ngày sinh", tfNgaySinh, "ngaysinh", true, "Định dạng: dd/MM/yyyy (ngày/tháng/năm)", errorLabels);
        CandidateFormSupport.addField(wrapper, c, row, 2, "Nơi sinh", tfNoiSinh, "noisinh", true, null, errorLabels);
        row += 2;
        
        CandidateFormSupport.addField(wrapper, c, row, 0, "Giới tính", cbGioiTinh, "gioitinh", true, null, errorLabels);
        CandidateFormSupport.addField(wrapper, c, row, 2, "Đối tượng ưu tiên", cbDoiTuong, "doituonguutien", true, null, errorLabels);
        row += 2;
        
        CandidateFormSupport.addField(wrapper, c, row, 0, "Số điện thoại", tfSoDienThoai, "sdt", false, null, errorLabels);
        CandidateFormSupport.addField(wrapper, c, row, 2, "Khu vực", cbKhuVuc, "khuvuc", true, null, errorLabels);
        row += 2;
        
        CandidateFormSupport.addField(wrapper, c, row, 0, "Email", tfEmail, "email", false, null, errorLabels);
        row += 2;

        GridBagConstraints spacer = new GridBagConstraints();
        spacer.gridx = 0;
        spacer.gridy = row;
        spacer.gridwidth = 4;
        spacer.weighty = 1.0;
        spacer.fill = GridBagConstraints.BOTH;
        wrapper.add(new JLabel(""), spacer);

        return wrapper;
    }

    private JPanel createFooterPanel(Consumer<CandidateDTO> onSave, Runnable onCancel) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(new Color(238, 240, 243));
        footer.setBorder(new EmptyBorder(14, 18, 14, 18));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> onCancel.run());

        JButton btnSave = new JButton("Lưu");
        btnSave.setPreferredSize(new Dimension(120, 36));
        btnSave.setBackground(Color.decode("#0066CC"));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            if (!validateForm()) {
                return;
            }
            updateDTO();
            try {
                candidateBUS.updateCandidate(dto);
                if (onSave != null) {
                    onSave.accept(dto);
                }
            } catch (CandidateValidationException ex) {
                JOptionPane.showMessageDialog(
                        CandidateEditPanel.this,
                        ex.getMessageText(),
                        "Lỗi dữ liệu",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        CandidateEditPanel.this,
                        ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        for (JLabel lbl : errorLabels.values()) {
            lbl.setText(" "); 
        }

        // CCCD 
        String cccd = tfCccd.getText().trim();
        if (cccd.isEmpty()) {
            setError("cccd", "CCCD không được để trống");
            isValid = false;
        } 

        // Tên 
        if (tfTen.getText().trim().isEmpty()) {
            setError("ten", "Tên không được để trống");
            isValid = false;
        }

        // Số điện thoại (nếu có nhập)
        String phone = tfSoDienThoai.getText().trim();
        if (!phone.isEmpty() && !phone.matches("^0\\d{9}$")) {
            setError("sdt", "SĐT phải có 10 số và bắt đầu bằng số 0");
            isValid = false;
        }

        // Email (nếu có nhập)
        String email = tfEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            setError("email", "Email không đúng định dạng (VD: abc@gmail.com)");
            isValid = false;
        }

        // Ngày sinh
        String dateStr = tfNgaySinh.getText().replace("_", "").trim();
        if (dateStr.equals("//") || dateStr.isEmpty()) {
            setError("ngaysinh", "Ngày sinh không được để trống");
            isValid = false;
        } else if (CandidateFormSupport.parseDate(dateStr) == null) {
            setError("ngaysinh", "Ngày sinh không hợp lệ hoặc sai định dạng dd/MM/yyyy");
            isValid = false;
        } else {
            java.util.Date birthDate = CandidateFormSupport.parseDate(dateStr);
            if (birthDate != null) {
                if (!CandidateFormSupport.isValidBirthYearRange(birthDate)) {
                    setError("ngaysinh", "Năm sinh phải trong khoảng "
                            + CandidateFormSupport.MIN_BIRTH_YEAR
                            + " - "
                            + CandidateFormSupport.MAX_BIRTH_YEAR);
                    isValid = false;
                }
            }
        }

        if (tfNoiSinh.getText().trim().isEmpty()) {
            setError("noisinh", "Nơi sinh không được để trống");
            isValid = false;
        }

        if (cbKhuVuc.getSelectedIndex() == 0) {
            setError("khuvuc", "Vui lòng chọn khu vực");
            isValid = false;
        }

        if (cbGioiTinh.getSelectedIndex() == 0) {
            setError("gioitinh", "Vui lòng chọn giới tính");
            isValid = false;
        }

        return isValid;
    }

    private void setError(String key, String message) {
        JLabel errorLabel = errorLabels.get(key);
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    private void updateDTO() {
        dto.setCccd(tfCccd.getText().trim());
        dto.setSoBaoDanh(tfSoBaoDanh.getText().trim());
        dto.setHo(tfHo.getText().trim());
        dto.setTen(tfTen.getText().trim());
        dto.setNgaySinh(CandidateFormSupport.formatDateForStorage(tfNgaySinh.getText().trim()));
        dto.setNoiSinh(tfNoiSinh.getText().trim());
        dto.setSoDienThoai(tfSoDienThoai.getText().trim());
        dto.setEmail(tfEmail.getText().trim());
        dto.setGioiTinh((String) cbGioiTinh.getSelectedItem());
        String dt = (String) cbDoiTuong.getSelectedItem();
        if (dt == null || dt.trim().isEmpty() || "Không có".equals(dt)) {
            dto.setDoiTuong(null);
        } else {
            dto.setDoiTuong(dt);
        }
        dto.setKhuVuc((String) cbKhuVuc.getSelectedItem());
        dto.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
    }

}
