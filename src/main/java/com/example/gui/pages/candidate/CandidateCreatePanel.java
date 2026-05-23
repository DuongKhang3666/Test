package com.example.gui.pages.candidate;

import com.example.bus.CandidateBUS;
import com.example.bus.CandidateBUS.CandidateValidationException;
import com.example.dto.CandidateDTO;
import com.example.gui.components.ComboBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Form để tạo mới thí sinh theo giao diện hồ sơ.
 */
public class CandidateCreatePanel extends JPanel {

    private java.util.Map<String, JLabel> errorLabels = new java.util.HashMap<>();

    private JTextField tfCccd;
    private JTextField tfSoBaoDanh;
    private JTextField tfHo;
    private JTextField tfTen;
    private JTextField tfNgaySinh;
    private JTextField tfSoDienThoai;
    private JTextField tfEmail;
    private ComboBox<String> cbNoiSinh;
    private ComboBox<String> cbGioiTinh;
    private ComboBox<String> cbDoiTuong;
    private ComboBox<String> cbKhuVuc;

    CandidateBUS candidateBUS = new CandidateBUS();

    public CandidateCreatePanel(Consumer<CandidateDTO> onCreate, Runnable onCancel) {
        java.util.Objects.requireNonNull(onCreate, "Hành động onCreate không được rỗng!");
        java.util.Objects.requireNonNull(onCancel, "Hành động onCancel không được rỗng!");
        
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 248));
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));
        card.add(new JScrollPane(createFormPanel()), BorderLayout.CENTER);
        card.add(createFooterPanel(onCreate, onCancel), BorderLayout.SOUTH);

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

        // Init tất cả fields trước
        tfCccd = CandidateFormSupport.createNumericTextField(12);
        tfSoBaoDanh = CandidateFormSupport.createTextField();
        tfHo = CandidateFormSupport.createTextField();
        tfTen = CandidateFormSupport.createTextField();
        tfNgaySinh = CandidateFormSupport.createDateTextField();
        tfSoDienThoai = CandidateFormSupport.createNumericTextField(10);
        tfEmail = CandidateFormSupport.createTextField();
        cbNoiSinh = new ComboBox<>(new String[]{
                                    "-- Vui lòng chọn --",
                                    "An Giang", "Bắc Ninh", "Cà Mau", "Cao Bằng", 
                                    "Gia Lai", "Hà Tĩnh", "Hưng Yên", "Khánh Hòa", 
                                    "Lai Châu", "Lâm Đồng", "Lạng Sơn", "Lào Cai", 
                                    "Nghệ An", "Ninh Bình", "Phú Thọ", "Quảng Ngãi", 
                                    "Quảng Ninh", "Quảng Trị", "Sơn La", "TP. Cần Thơ", 
                                    "TP. Hà Nội", "TP. Hải Phòng", "TP. Hồ Chí Minh", 
                                    "TP. Huế", "TP. Đà Nẵng", "Tây Ninh", "Thái Nguyên", 
                                    "Thanh Hóa", "Tuyên Quang", "Vĩnh Long", "Đắk Lắk", 
                                    "Điện Biên", "Đồng Nai", "Đồng Tháp"});
        cbGioiTinh = new ComboBox<>(new String[]{"-- Vui lòng chọn --", "Nam", "Nữ"});
        cbDoiTuong = new ComboBox<>(new String[]{"Không có", "01", "02a", "02b", "03a", "03b", "03c", "03d", "03đ", "04a", "04b", "05a", "05b", "05c", "06a", "06b", "06c"});
        cbKhuVuc = new ComboBox<>(new String[]{"-- Vui lòng chọn --", "1", "2", "2NT", "3"});

        CandidateFormSupport.addField(wrapper, c, row, 0, "Họ", tfHo, "ho", true, null, errorLabels);
        CandidateFormSupport.addField(wrapper, c, row, 2, "Tên", tfTen, "ten", true, null, errorLabels);
        row += 2;
        
        CandidateFormSupport.addField(wrapper, c, row, 0, "CCCD", tfCccd, "cccd", true, null, errorLabels);
        CandidateFormSupport.addField(wrapper, c, row, 2, "Số báo danh", tfSoBaoDanh, "sbd", false, null, errorLabels);
        row += 2;
        
        // Date picker cho ngày sinh
        CandidateFormSupport.addField(wrapper, c, row, 0, "Ngày sinh", tfNgaySinh, "ngaysinh", true, "Định dạng: dd/MM/yyyy (ngày/tháng/năm)", errorLabels);
        CandidateFormSupport.addField(wrapper, c, row, 2, "Nơi sinh", cbNoiSinh, "noisinh", true, null, errorLabels);
        row += 2;
        
        CandidateFormSupport.addField(wrapper, c, row, 0, "Giới tính", cbGioiTinh, "gioitinh", true, null, errorLabels);
        CandidateFormSupport.addField(wrapper, c, row, 2, "Đối tượng ưu tiên", cbDoiTuong, "doituonguutien", true, null, errorLabels);
        row += 2;
        
        CandidateFormSupport.addField(wrapper, c, row, 0, "Số điện thoại", tfSoDienThoai, "sdt", true, null, errorLabels);
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

    private JPanel createFooterPanel(Consumer<CandidateDTO> onCreate, Runnable onCancel) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(new Color(238, 240, 243));
        footer.setBorder(new EmptyBorder(14, 18, 14, 18));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> onCancel.run());

        JButton btnSave = new JButton("OK");
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
            CandidateDTO dto = buildDTO();
            try {
                candidateBUS.saveSingleCandidate(dto);
                if (onCreate != null) {
                    onCreate.accept(dto);
                }
            } catch (CandidateValidationException ex) {
                JOptionPane.showMessageDialog(
                        CandidateCreatePanel.this,
                        ex.getMessageText(),
                        "Lỗi dữ liệu",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        CandidateCreatePanel.this,
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
        
        // Reset toàn bộ thông báo lỗi về khoảng trắng
        for (JLabel lbl : errorLabels.values()) {
            lbl.setText(" "); 
        }

        // 1. Validate CCCD (12 số)
        String cccd = tfCccd.getText().trim();
        if (cccd.isEmpty()) {
            setError("cccd", "CCCD không được để trống");
            isValid = false;
        } else if (cccd.length() != 12) {
            setError("cccd", "CCCD phải bao gồm đúng 12 chữ số");
            isValid = false;
        }

        // 2. Validate Họ và Tên
        if (tfHo.getText().trim().isEmpty()) {
            setError("ho", "Họ không được để trống");
            isValid = false;
        }
        if (tfTen.getText().trim().isEmpty()) {
            setError("ten", "Tên không được để trống");
            isValid = false;
        }

        // 3. Validate Số điện thoại (10 số, bắt đầu bằng 0)
        String phone = tfSoDienThoai.getText().trim();
        if (phone.isEmpty()) {
            setError("sdt", "Số điện thoại không được để trống");
            isValid = false;
        } else if (!phone.matches("^0\\d{9}$")) {
            setError("sdt", "SĐT phải có 10 số và bắt đầu bằng số 0");
            isValid = false;
        }

        // 4. Validate Email (Nếu có nhập thì mới check định dạng)
        String email = tfEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            setError("email", "Email không đúng định dạng (VD: abc@gmail.com)");
            isValid = false;
        }

        // 5. Validate Ngày sinh (Phải là ngày hợp lệ)
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

        if (cbNoiSinh.getSelectedIndex() == 0) {
            setError("noisinh", "Vui lòng chọn nơi sinh");
        }

        if (cbKhuVuc.getSelectedIndex() == 0) {
            setError("khuvuc", "Vui lòng chọn khu vực");
        }

        if (cbGioiTinh.getSelectedIndex() == 0) {
            setError("gioitinh", "Vui lòng chọn giới tính");
        }

        return isValid;
    }

    // Hàm phụ trợ để set chữ đỏ
    private void setError(String key, String message) {
        JLabel errorLabel = errorLabels.get(key);
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    private CandidateDTO buildDTO() {
        CandidateDTO dto = new CandidateDTO();
        dto.setCccd(tfCccd.getText().trim());
        dto.setSoBaoDanh(tfSoBaoDanh.getText().trim());
        dto.setHo(tfHo.getText().trim());
        dto.setTen(tfTen.getText().trim());
        dto.setNgaySinh(CandidateFormSupport.formatDateForStorage(tfNgaySinh.getText().trim()));
        dto.setNoiSinh((String) cbNoiSinh.getSelectedItem());
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
        return dto;
    }

}
