package com.example.gui.pages.score;

import com.example.bus.ExamScoreBUS;
import com.example.dto.ExamScoreDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ScoreStatisticPanel extends JPanel {

    private final ExamScoreBUS bus = new ExamScoreBUS();

    // Số lượng
    private JLabel lblTotal, lblTHPT, lblVSAT, lblDGNL;
    private JLabel lblPercentTHPT, lblPercentVSAT, lblPercentDGNL;

    // Điểm trung bình theo phương thức
    private JLabel lblAvgToanTHPT, lblAvgVanTHPT, lblAvgN1THPT;
    private JLabel lblAvgVSAT, lblAvgDGNL;

    public ScoreStatisticPanel() {
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));
        setBackground(Color.decode("#F8F9FA"));

        add(createHeader(), BorderLayout.NORTH);
        add(createStatsPanel(), BorderLayout.CENTER);

        loadStatistics();
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("Thống kê điểm thi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.decode("#111827"));

        JButton btnRefresh = new JButton("↻ Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.addActionListener(e -> loadStatistics());

        p.add(title, BorderLayout.WEST);
        p.add(btnRefresh, BorderLayout.EAST);
        return p;
    }

    private JPanel createStatsPanel() {
        JPanel main = new JPanel(new GridLayout(2, 1, 0, 25));
        main.setOpaque(false);

        main.add(createCountPanel());
        main.add(createAveragePanel());

        return main;
    }

    private JPanel createCountPanel() {
        JPanel card = createCard("SỐ LƯỢNG THÍ SINH");

        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 15));
        grid.setOpaque(false);

        grid.add(createStatCard("Tổng thí sinh", lblTotal = new JLabel("0"), "#1E3A8A"));
        grid.add(createStatCard("THPT", lblTHPT = new JLabel("0"), "#1E40AF"));
        grid.add(createStatCard("V-SAT", lblVSAT = new JLabel("0"), "#166534"));
        grid.add(createStatCard("ĐGNL", lblDGNL = new JLabel("0"), "#4338CA"));

        grid.add(new JLabel("Tỷ lệ (%)", SwingConstants.CENTER));
        grid.add(lblPercentTHPT = new JLabel("0%"));
        grid.add(lblPercentVSAT = new JLabel("0%"));
        grid.add(lblPercentDGNL = new JLabel("0%"));

        card.add(grid);
        return card;
    }

    private JPanel createAveragePanel() {
        JPanel card = createCard("ĐIỂM TRUNG BÌNH");

        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 12));
        grid.setOpaque(false);

        // THPT
        grid.add(new JLabel("THPT", SwingConstants.CENTER));
        grid.add(createSmallStat("Toán", lblAvgToanTHPT = new JLabel("0.00")));
        grid.add(createSmallStat("Văn", lblAvgVanTHPT = new JLabel("0.00")));
        grid.add(createSmallStat("N1_CC", lblAvgN1THPT = new JLabel("0.00")));

        // VSAT & ĐGNL
        grid.add(new JLabel("V-SAT", SwingConstants.CENTER));
        grid.add(createSmallStat("Điểm TB", lblAvgVSAT = new JLabel("0.00")));
        grid.add(new JLabel(""));
        grid.add(new JLabel(""));

        grid.add(new JLabel("ĐGNL", SwingConstants.CENTER));
        grid.add(createSmallStat("Điểm TB", lblAvgDGNL = new JLabel("0.00")));
        grid.add(new JLabel(""));
        grid.add(new JLabel(""));

        card.add(grid);
        return card;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.decode("#1E40AF"));
        card.add(lblTitle, BorderLayout.NORTH);

        return card;
    }

    private JPanel createStatCard(String label, JLabel value, String color) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(Color.decode("#64748B"));

        value.setFont(new Font("Segoe UI", Font.BOLD, 24));
        value.setForeground(Color.decode(color));

        p.add(lbl, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JPanel createSmallStat(String label, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.decode("#64748B"));

        value.setFont(new Font("Segoe UI", Font.BOLD, 18));
        value.setForeground(Color.decode("#1E40AF"));

        p.add(lbl, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    public void loadStatistics() {
        long total = bus.demTatCa();
        long thpt = bus.thongKeSoLuongTheoPhuongThuc("THPT");
        long vsat = bus.thongKeSoLuongTheoPhuongThuc("VSAT");
        long dgnl = bus.thongKeSoLuongTheoPhuongThuc("DGNL");

        lblTotal.setText(String.valueOf(total));
        lblTHPT.setText(String.valueOf(thpt));
        lblVSAT.setText(String.valueOf(vsat));
        lblDGNL.setText(String.valueOf(dgnl));

        // Tính tỷ lệ phần trăm
        if (total > 0) {
            lblPercentTHPT.setText(String.format("%.1f%%", (thpt * 100.0) / total));
            lblPercentVSAT.setText(String.format("%.1f%%", (vsat * 100.0) / total));
            lblPercentDGNL.setText(String.format("%.1f%%", (dgnl * 100.0) / total));
        }

        // Điểm trung bình
        lblAvgToanTHPT.setText(String.format("%.2f", bus.thongKeDiemTrungBinh("diemToan", "THPT")));
        lblAvgVanTHPT.setText(String.format("%.2f", bus.thongKeDiemTrungBinh("diemVan", "THPT")));
        lblAvgN1THPT.setText(String.format("%.2f", bus.thongKeDiemTrungBinh("n1Cc", "THPT")));

        lblAvgVSAT.setText(String.format("%.2f", bus.thongKeDiemTrungBinh("nk1", "VSAT"))); // Có thể điều chỉnh
        lblAvgDGNL.setText(String.format("%.2f", bus.thongKeDiemTrungBinh("nl1", "DGNL")));
    }

    public void refresh() {
        loadStatistics();
    }
}