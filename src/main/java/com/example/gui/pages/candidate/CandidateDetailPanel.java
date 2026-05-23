package com.example.gui.pages.candidate;

import com.example.bus.BonusScoreBUS;
import com.example.dto.CandidateDTO;
import com.example.gui.pages.bonusscore.BonusScoreFormSupport;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * Panel để xem chi tiết thí sinh (read-only)
 * Sử dụng trong overlay modal
 */
public class CandidateDetailPanel extends JPanel {
    private final CandidateDTO dto;
    private final BonusScoreBUS bonusScoreBUS;

    private JTextField tfThptScore;
    private JTextField tfDgnlScore;
    private JTextField tfVsatScore;
    private JLabel lblThptHelper;
    private JButton btnReload;
    private SwingWorker<List<Object[]>, Void> admissionWorker;

    @Override
    public void removeNotify() {
        if (admissionWorker != null && !admissionWorker.isDone()) {
            admissionWorker.cancel(true);
        }
        super.removeNotify();
    }

    public CandidateDetailPanel(CandidateDTO candidate) {
        this(candidate, () -> {});
    }

    public CandidateDetailPanel(CandidateDTO candidate, Runnable onClose) {
        if (candidate == null) {
            throw new IllegalArgumentException("Dữ liệu thí sinh không được rỗng!");
        }
        this.dto = candidate;
        this.bonusScoreBUS = new BonusScoreBUS();

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 248));
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));
        card.add(createScrollContent(), BorderLayout.CENTER);
        card.add(createFooterPanel(onClose), BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private JComponent createScrollContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(245, 246, 248));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));

        content.add(createPersonalInfoCard());
        content.add(Box.createVerticalStrut(14));
        content.add(createAdmissionResultsCard());
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(245, 246, 248));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        loadAdmissionResultsAsync();
        return scroll;
    }

    private JPanel createPersonalInfoCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.add(wrapper, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;

        int row = 0;

        JTextField tfHo = createReadOnlyTextField(dto.getHo());
        JTextField tfTen = createReadOnlyTextField(dto.getTen());
        JTextField tfCccd = createReadOnlyTextField(dto.getCccd());
        JTextField tfSoBaoDanh = createReadOnlyTextField(dto.getSoBaoDanh());
        JTextField tfNoiSinh = createReadOnlyTextField(dto.getNoiSinh());
        JTextField tfNgaySinh = createReadOnlyTextField(CandidateFormSupport.formatDateForDisplay(dto.getNgaySinh()));
        JTextField tfGioiTinh = createReadOnlyTextField(dto.getGioiTinh());
        JTextField tfDoiTuong = createReadOnlyTextField(dto.getDoiTuong());
        JTextField tfKhuVuc = createReadOnlyTextField(dto.getKhuVuc());
        JTextField tfEmail = createReadOnlyTextField(dto.getEmail());
        JTextField tfSoDienThoai = createReadOnlyTextField(dto.getSoDienThoai());

        row = addReadOnlyRow(wrapper, c, row, "Họ", tfHo, "Tên", tfTen);
        row = addReadOnlyRow(wrapper, c, row, "CCCD", tfCccd, "Số báo danh", tfSoBaoDanh);
        row = addReadOnlyRow(wrapper, c, row, "Ngày sinh", tfNgaySinh, "Giới tính", tfGioiTinh);
        row = addReadOnlyRow(wrapper, c, row, "Nơi sinh", tfNoiSinh, "Đối tượng ưu tiên", tfDoiTuong);
        row = addReadOnlyRow(wrapper, c, row, "Khu vực ưu tiên", tfKhuVuc, "Số điện thoại", tfSoDienThoai);
        row = addReadOnlyRow(wrapper, c, row, "Email", tfEmail);

        GridBagConstraints spacer = new GridBagConstraints();
        spacer.gridx = 0;
        spacer.gridy = row;
        spacer.gridwidth = 4;
        spacer.weighty = 1.0;
        spacer.fill = GridBagConstraints.BOTH;
        wrapper.add(new JLabel(""), spacer);

        return card;
    }

    private JPanel createAdmissionResultsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, 12, 8, 12));

        JLabel title = new JLabel("Điểm thi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(40, 40, 40));
        header.add(title, BorderLayout.WEST);

        btnReload = createReloadButton();
        btnReload.addActionListener(e -> loadAdmissionResultsAsync());
        header.add(btnReload, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 12, 12, 12));

        tfThptScore = createReadOnlyTextField("");
        tfDgnlScore = createReadOnlyTextField("");
        tfVsatScore = createReadOnlyTextField("");

        lblThptHelper = new JLabel("Tổ hợp xét tuyển: --");
        lblThptHelper.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblThptHelper.setForeground(new Color(140, 140, 140));
        Dimension helperPref = lblThptHelper.getPreferredSize();
        lblThptHelper.setMinimumSize(new Dimension(0, helperPref.height));

        int row = 0;

        // Place 3 score fields on the same row: (label, field) x3
        addScoreCell(body, row, 0, "Điểm THPT", tfThptScore);
        addScoreCell(body, row, 1, "Điểm ĐGNL", tfDgnlScore);
        addScoreCell(body, row, 2, "Điểm VSAT", tfVsatScore);

        // helper label under THPT field (span the THPT cell)
        GridBagConstraints helperConstraints = new GridBagConstraints();
        helperConstraints.gridx = 0; // start at THPT cell column
        helperConstraints.gridy = row + 2; // below label(row) and field(row+1)
        helperConstraints.gridwidth = 2; // span the THPT cell width
        helperConstraints.weightx = 1.0;
        helperConstraints.fill = GridBagConstraints.HORIZONTAL;
        helperConstraints.anchor = GridBagConstraints.WEST;
        helperConstraints.insets = new Insets(0, 8, 8, 8);
        body.add(lblThptHelper, helperConstraints);

        row += 3;

        GridBagConstraints spacer = new GridBagConstraints();
        spacer.gridx = 0;
        spacer.gridy = row;
        spacer.gridwidth = 2;
        spacer.weighty = 1.0;
        spacer.fill = GridBagConstraints.BOTH;
        body.add(new JLabel(""), spacer);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private static void addScoreCell(JPanel panel, int baseRow, int cellIndex, String labelText, JComponent field) {
        int gridX = cellIndex * 2;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = gridX;
        labelConstraints.gridy = baseRow;
        labelConstraints.gridwidth = 2;
        labelConstraints.weightx = 0.0;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(8, 8, 4, 12);
        panel.add(label, labelConstraints);

        JPanel fieldBox = new JPanel(new BorderLayout());
        fieldBox.setOpaque(false);
        fieldBox.add(field, BorderLayout.CENTER);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = gridX;
        fieldConstraints.gridy = baseRow + 1;
        fieldConstraints.gridwidth = 2;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(4, 0, 8, 8);
        fieldConstraints.ipady = 8;
        panel.add(fieldBox, fieldConstraints);
    }

    private JTextField createReadOnlyTextField(String value) {
        JTextField field = CandidateFormSupport.createTextField();
        field.setText(CandidateFormSupport.formatDisplay(value));
        field.setEditable(false);
        field.setFocusable(false);
        field.setBackground(Color.WHITE);
        field.setDisabledTextColor(new Color(40, 40, 40));
        return field;
    }

    private void loadAdmissionResultsAsync() {
        String cccd = dto != null ? safeString(dto.getCccd()) : "";
        if (cccd.isBlank()) {
            setAdmissionResults(null);
            return;
        }

        if (admissionWorker != null && !admissionWorker.isDone()) {
            admissionWorker.cancel(true);
        }

        setReloadEnabled(false);
        admissionWorker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                return bonusScoreBUS.getNguyenVongByCccd(cccd);
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled() || !CandidateDetailPanel.this.isDisplayable()) {
                        return;
                    }
                    List<Object[]> raw = get();
                    setAdmissionResults(raw);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    setAdmissionResults(null);
                } finally {
                    if (CandidateDetailPanel.this.isDisplayable()) {
                        setReloadEnabled(true);
                    }
                }
            }
        };
        admissionWorker.execute();
    }

    private void setAdmissionResults(List<Object[]> raw) {
        Double maxThpt = null;
        Double maxDgnl = null;
        Double maxVsat = null;

        String thptToHop = null;

        if (raw != null && !raw.isEmpty()) {
            for (Object[] r : raw) {
                if (r == null || r.length < 4) {
                    continue;
                }

                String toHop = safeString(r[1]);
                String pt = safeString(r[2]).toUpperCase();

                Object scoreObj = r[3];
                Double score = null;
                if (scoreObj instanceof Number) {
                    score = ((Number) scoreObj).doubleValue();
                } else if (scoreObj != null) {
                    try {
                        score = Double.parseDouble(scoreObj.toString());
                    } catch (NumberFormatException ignored) {
                        score = null;
                    }
                }

                if (score == null) {
                    continue;
                }

                if (pt.contains("THPT") || pt.contains("XÉT ĐIỂM THI")) {
                    if (maxThpt == null || score > maxThpt) {
                        maxThpt = score;
                        thptToHop = toHop;
                    }
                } else if (pt.contains("DGNL") || pt.contains("ĐGNL")) {
                    if (maxDgnl == null || score > maxDgnl) {
                        maxDgnl = score;
                    }
                } else if (pt.contains("VSAT")) {
                    if (maxVsat == null || score > maxVsat) {
                        maxVsat = score;
                    }
                }
            }
        }

        if (tfThptScore != null) {
            tfThptScore.setText(formatScoreOrDash(maxThpt));
        }
        if (tfDgnlScore != null) {
            tfDgnlScore.setText(formatScoreOrDash(maxDgnl));
        }
        if (tfVsatScore != null) {
            tfVsatScore.setText(formatScoreOrDash(maxVsat));
        }

        if (lblThptHelper != null) {
            String thptToHopDisplay = maxThpt == null ? "--" : CandidateFormSupport.formatDisplay(thptToHop);
            lblThptHelper.setText("Tổ hợp xét tuyển: " + thptToHopDisplay);
        }
    }

    private void setReloadEnabled(boolean enabled) {
        if (btnReload == null) return;
        btnReload.setEnabled(enabled);
        btnReload.setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private static int addReadOnlyRow(JPanel panel, GridBagConstraints base, int row, String label1, JComponent field1) {
        BonusScoreFormSupport.addReadOnlyField(panel, base, row, 0, label1, field1);
        return row + 2;
    }
    private static int addReadOnlyRow(JPanel panel, GridBagConstraints base, int row, String label1, JComponent field1, String label2, JComponent field2) {
        BonusScoreFormSupport.addReadOnlyField(panel, base, row, 0, label1, field1);
        BonusScoreFormSupport.addReadOnlyField(panel, base, row, 2, label2, field2);
        return row + 2;
    }

    private JButton createReloadButton() {
        JButton btn = new JButton();
        btn.setToolTipText("Reload");
        btn.setPreferredSize(new Dimension(34, 34));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setIcon(loadAndTintIcon("/assets/images/refresh.png", new Color(0, 102, 204), 18, 18));
        return btn;
    }

    private Icon loadAndTintIcon(String resourcePath, Color tint, int width, int height) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                return null;
            }

            BufferedImage tinted = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int tr = tint.getRed();
            int tg = tint.getGreen();
            int tb = tint.getBlue();
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int argb = img.getRGB(x, y);
                    int a = (argb >> 24) & 0xFF;
                    if (a == 0) {
                        tinted.setRGB(x, y, 0);
                        continue;
                    }
                    int out = (a << 24) | (tr << 16) | (tg << 8) | tb;
                    tinted.setRGB(x, y, out);
                }
            }

            Image scaled = tinted.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException ex) {
            return null;
        }
    }

    private static String safeString(Object v) {
        return v == null ? "" : v.toString().trim();
    }

    private static String formatScoreOrDash(Double value) {
        if (value == null) {
            return "--";
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private JPanel createFooterPanel(Runnable onClose) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(new Color(238, 240, 243));
        footer.setBorder(new EmptyBorder(14, 18, 14, 18));

        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 36));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> onClose.run());

        footer.add(btnClose);
        return footer;
    }

}
