package com.example.gui.pages.majorcombination;

import com.example.bus.MajorBUS;
import com.example.bus.SubjectGroupBUS;
import com.example.dto.MajorDTO;
import com.example.dto.SubjectGroupDTO;
import com.example.bus.MajorGroupBUS;
import com.example.dto.MajorGroupDTO;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

public class MajorCreatePanel extends JPanel {
    private final MajorBUS majorBUS;
    private final SubjectGroupBUS subjectGroupBUS;
    private final MajorGroupBUS majorGroupBUS;
    private final Consumer<MajorDTO> onSuccess;
    private final Runnable onCancel;
    private JTextField txtOriginalCombo;
    private SubjectGroupDTO selectedOriginalCombo;

    public MajorCreatePanel(Consumer<MajorDTO> onSuccess, Runnable onCancel) {
        this.majorBUS = new MajorBUS();
        this.subjectGroupBUS = new SubjectGroupBUS();
        this.majorGroupBUS = new MajorGroupBUS();
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

        JTextField txtMajorCode = new JTextField();
        JTextField txtMajorName = new JTextField();
        JTextField txtChiTieu = new JTextField();
        JTextField txtDiemSan = new JTextField();

        txtMajorCode.setPreferredSize(new Dimension(420, 36));
        txtMajorName.setPreferredSize(new Dimension(420, 36));
        txtChiTieu.setPreferredSize(new Dimension(420, 36));
        txtDiemSan.setPreferredSize(new Dimension(420, 36));

        fields.add(createFieldGroup("Mã ngành", txtMajorCode), gbc);
        gbc.gridy = 1;
        fields.add(createFieldGroup("Tên ngành", txtMajorName), gbc);
        gbc.gridy = 2;
        fields.add(createFieldGroup("Tổ hợp gốc", createOriginalComboSelector()), gbc);
        gbc.gridy = 3;
        fields.add(createFieldGroup("Chỉ tiêu", txtChiTieu), gbc);
        gbc.gridy = 4;
        fields.add(createFieldGroup("Điểm sàn", txtDiemSan), gbc);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        footer.setBackground(Color.decode("#F9FAFB"));

        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Thêm ngành");

        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnSave.setPreferredSize(new Dimension(130, 36));
        btnSave.setBackground(Color.decode("#1D4ED8"));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);

        btnCancel.addActionListener(e -> onCancel.run());
        btnSave.addActionListener(e -> {
            String maNganh = txtMajorCode.getText().trim();
            String tenNganh = txtMajorName.getText().trim();
            String chiTieuRaw = txtChiTieu.getText().trim();
            String diemSanRaw = txtDiemSan.getText().trim();

            if (maNganh.isEmpty() || tenNganh.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ mã ngành và tên ngành.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // VALIDATION: Tên ngành chỉ chứa chữ cái và khoảng trắng (hỗ trợ Tiếng Việt)
            if (!tenNganh.matches("^[\\p{L}\\s()]+$")) {
                JOptionPane.showMessageDialog(this, "Tên ngành chỉ được chứa chữ cái ", "Lỗi xác thực", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MajorDTO existingByCode = majorBUS.findByMaNganh(maNganh);
            if (existingByCode != null) {
                JOptionPane.showMessageDialog(this, "Mã ngành đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int chiTieu = 0;
            if (!chiTieuRaw.isEmpty()) {
                try {
                    chiTieu = Integer.parseInt(chiTieuRaw.replaceAll("[,\\s]", ""));
                    if (chiTieu < 0) {
                        JOptionPane.showMessageDialog(this, "Chỉ tiêu không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Chỉ tiêu phải là số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            BigDecimal diemSan = null;
            if (!diemSanRaw.isEmpty()) {
                try {
                    String cleaned = diemSanRaw.replaceAll("[,\\s]", "");
                    diemSan = new BigDecimal(cleaned);
                    if (diemSan.compareTo(BigDecimal.ZERO) < 0) {
                        JOptionPane.showMessageDialog(this, "Điểm sàn không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Điểm sàn phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

           try {
                MajorDTO payload = new MajorDTO();
                payload.setMaNganh(maNganh);
                payload.setTenNganh(tenNganh);
                payload.setNToHopGoc(selectedOriginalCombo != null ? selectedOriginalCombo.getMaToHop() : null);
                payload.setNChiTieu(chiTieu);
                payload.setNDiemSan(diemSan);

                // 1. Lưu Ngành (Major) trước
                majorBUS.save(payload);
                
                // 2. Tự động tạo Tổ hợp & Hệ số (MajorGroup)
                if (selectedOriginalCombo != null) {
                    try {
                        MajorGroupDTO groupDTO = new MajorGroupDTO();
                        groupDTO.setMaNganh(maNganh);
                        groupDTO.setMaToHop(selectedOriginalCombo.getMaToHop());
                        // Khóa tb_keys ghép từ Mã Ngành và Mã Tổ Hợp
                        groupDTO.setTbKeys(maNganh + "_" + selectedOriginalCombo.getMaToHop()); 
                        
                        // Lấy 3 môn từ tổ hợp gốc
                        groupDTO.setThMon1(selectedOriginalCombo.getMon1());
                        groupDTO.setThMon2(selectedOriginalCombo.getMon2());
                        groupDTO.setThMon3(selectedOriginalCombo.getMon3());
                        
                        // Set mặc định hệ số là 1 và độ lệch là 0
                        groupDTO.setHsMon1(1);
                        groupDTO.setHsMon2(1);
                        groupDTO.setHsMon3(1);
                        groupDTO.setDoLech(BigDecimal.ZERO);

                        // Lưu vào DB
                        majorGroupBUS.save(groupDTO);
                    } catch (Exception ex) {
                        System.err.println("Lỗi khi tự động tạo Tổ hợp & Hệ số: " + ex.getMessage());
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Ngành " + maNganh + " đã được thêm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                onSuccess.accept(payload);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể lưu ngành: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);

        add(fields, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JComponent createOriginalComboSelector() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        txtOriginalCombo = new JTextField();
        txtOriginalCombo.setEditable(false);
        txtOriginalCombo.setBackground(Color.WHITE);
        txtOriginalCombo.setPreferredSize(new Dimension(300, 36));
        txtOriginalCombo.setText("Chưa chọn tổ hợp gốc");

        JButton btnChoose = new JButton("Chọn tổ hợp gốc");
        btnChoose.setPreferredSize(new Dimension(150, 36));
        btnChoose.setBackground(Color.decode("#1D4ED8"));
        btnChoose.setForeground(Color.WHITE);
        btnChoose.setOpaque(true);
        btnChoose.setBorderPainted(false);
        btnChoose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChoose.addActionListener(e -> chooseOriginalCombo());

        panel.add(txtOriginalCombo, BorderLayout.CENTER);
        panel.add(btnChoose, BorderLayout.EAST);
        return panel;
    }

    private void chooseOriginalCombo() {
        List<SubjectGroupDTO> combos = subjectGroupBUS.getAll();
        if (combos != null) {
            // SẮP XẾP A-Z THEO MÃ TỔ HỢP
            combos = combos.stream()
                    .sorted(java.util.Comparator.comparing(
                            SubjectGroupDTO::getMaToHop,
                            java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                    ))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        if (combos == null || combos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có tổ hợp môn nào để chọn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<SubjectGroupDTO> comboBox = new JComboBox<>(combos.toArray(new SubjectGroupDTO[0]));
        comboBox.setPreferredSize(new Dimension(380, 34));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SubjectGroupDTO sg) {
                    setText(formatSubjectGroupLabel(sg));
                }
                return this;
            }
        });

        if (selectedOriginalCombo != null) {
            comboBox.setSelectedItem(selectedOriginalCombo);
        }

        JPanel chooser = new JPanel(new BorderLayout(0, 8));
        chooser.add(new JLabel("Chọn một tổ hợp gốc cho ngành này:"), BorderLayout.NORTH);
        chooser.add(comboBox, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, chooser, "Chọn tổ hợp gốc", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            selectedOriginalCombo = (SubjectGroupDTO) comboBox.getSelectedItem();
            updateOriginalComboText();
        }
    }

    private void updateOriginalComboText() {
        if (txtOriginalCombo == null) {
            return;
        }
        txtOriginalCombo.setText(selectedOriginalCombo == null
                ? "Chưa chọn tổ hợp gốc"
                : formatSubjectGroupLabel(selectedOriginalCombo));
    }

    private String formatSubjectGroupLabel(SubjectGroupDTO sg) {
        if (sg == null) {
            return "";
        }
        String code = sg.getMaToHop() != null ? sg.getMaToHop().trim() : "";
        String name = sg.getTenToHop() != null ? sg.getTenToHop().trim() : "";
        if (code.isEmpty()) {
            return name;
        }
        return name.isEmpty() ? code : code + " - " + name;
    }

    private JPanel createFieldGroup(String labelText, JComponent component) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.decode("#4B5563"));

        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#D1D5DB"), 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        p.add(lbl, BorderLayout.NORTH);
        p.add(component, BorderLayout.CENTER);
        return p;
    }
}