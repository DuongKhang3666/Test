package com.example.gui.pages.candidate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.example.bus.CandidateBUS;
import com.example.dto.CandidateDTO;
import com.example.gui.components.ActionButton;
import com.example.gui.components.CustomTable;

public class CandidateStatsPage extends JPanel {
    private static final Color PAGE_BACKGROUND = Color.decode("#F8FAFC");
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TITLE_COLOR = Color.decode("#0F172A");
    private static final Color MUTED_COLOR = Color.decode("#64748B");
    private static final Color BORDER_COLOR = Color.decode("#CBD5E1");
    private static final Color BADGE_BACKGROUND = Color.decode("#DBEAFE");
    private static final Color BADGE_FOREGROUND = Color.decode("#1D4ED8");

    private final CandidateBUS candidateBUS;

    public CandidateStatsPage() {
        this.candidateBUS = new CandidateBUS();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        List<CandidateDTO> candidates = loadCandidates();

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        root.add(createHeaderPanel(() -> refreshStats(root)));
        root.add(Box.createVerticalStrut(16));
        root.add(createTotalCard(candidates));
        root.add(Box.createVerticalStrut(16));

        JPanel rowPanel = new JPanel(new java.awt.GridLayout(1, 2, 16, 0));
        rowPanel.setOpaque(false);
        rowPanel.add(createGroupedCard("Thống Kê Theo Đối Tượng", "ACTIVE CYCLE", buildGroupedTable(candidates, true)));
        rowPanel.add(createGroupedCard("Thống Kê Theo Khu Vực", "", buildGroupedTable(candidates, false)));
        root.add(rowPanel);
        root.add(Box.createVerticalStrut(16));
        root.add(createNotePanel());

        JScrollPane scrollPane = new JScrollPane(root);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshStats(JPanel root) {
        root.removeAll();
        
        List<CandidateDTO> candidates = loadCandidates();

        root.add(createHeaderPanel(() -> refreshStats(root)));
        root.add(Box.createVerticalStrut(16));
        root.add(createTotalCard(candidates));
        root.add(Box.createVerticalStrut(16));

        JPanel rowPanel = new JPanel(new java.awt.GridLayout(1, 2, 16, 0));
        rowPanel.setOpaque(false);
        rowPanel.add(createGroupedCard("Thống Kê Theo Đối Tượng", "ACTIVE CYCLE", buildGroupedTable(candidates, true)));
        rowPanel.add(createGroupedCard("Thống Kê Theo Khu Vực", "", buildGroupedTable(candidates, false)));
        root.add(rowPanel);
        root.add(Box.createVerticalStrut(16));
        root.add(createNotePanel());

        root.revalidate();
        root.repaint();
    }

    private JPanel createHeaderPanel(Runnable onRefresh) {
        JPanel panel = new JPanel(new BorderLayout(16, 6));
        panel.setOpaque(false);

        JPanel titleSection = new JPanel(new BorderLayout(0, 6));
        titleSection.setOpaque(false);

        JLabel title = new JLabel("Thống kê Thí sinh");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TITLE_COLOR);

        JLabel subtitle = new JLabel("Xem nhanh phân bố hồ sơ theo đối tượng và khu vực.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED_COLOR);

        titleSection.add(title, BorderLayout.NORTH);
        titleSection.add(subtitle, BorderLayout.SOUTH);
        panel.add(titleSection, BorderLayout.WEST);

        ActionButton refreshBtn = new ActionButton("Tải lại", "/assets/images/refresh.png", "TERTIARY");
        refreshBtn.addActionListener(e -> {
            if (onRefresh != null) onRefresh.run();
        });
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.EAST);

        return panel;
    }

    // Đã sửa đổi: Thay JTable bằng JLabel to đùng chuẩn Dashboard
    private JPanel createTotalCard(List<CandidateDTO> candidates) {
        JPanel card = createCardShell();
        card.add(createCardHeader("Tổng Thí Sinh Hệ Thống", ""), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 5, 25, 10));

        JLabel totalNumber = new JLabel(String.valueOf(candidates.size()));
        totalNumber.setFont(new Font("Segoe UI", Font.BOLD, 48));
        totalNumber.setForeground(Color.decode("#0F172A"));

        JLabel labelText = new JLabel("Hồ sơ đã được import");
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        labelText.setForeground(Color.decode("#64748B"));

        contentPanel.add(totalNumber);
        contentPanel.add(labelText);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createGroupedCard(String titleText, String badgeText, JTable table) {
        JPanel card = createCardShell();
        card.add(createCardHeader(titleText, badgeText), BorderLayout.NORTH);
        card.add(wrapTable(table, new Dimension(0, 260)), BorderLayout.CENTER);
        return card;
    }

    private JPanel createCardHeader(String titleText, String badgeText) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TITLE_COLOR);
        header.add(title, BorderLayout.WEST);

        if (badgeText != null && !badgeText.isBlank()) {
            JLabel badge = new JLabel(badgeText);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setForeground(BADGE_FOREGROUND);
            badge.setBorder(new EmptyBorder(6, 10, 6, 10));
            badge.setOpaque(true);
            badge.setBackground(BADGE_BACKGROUND);

            JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgeWrap.setOpaque(false);
            badgeWrap.add(badge);
            header.add(badgeWrap, BorderLayout.EAST);
        }

        return header;
    }

    private JPanel createNotePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(true);
        panel.setBackground(Color.decode("#E2E8F0"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel noteLabel = new JLabel("Data Integrity Note");
        noteLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        noteLabel.setForeground(TITLE_COLOR);

        JLabel noteText = new JLabel("Số liệu lấy từ hồ sơ thí sinh hiện có trong hệ thống.");
        noteText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteText.setForeground(MUTED_COLOR);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(noteLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(noteText);

        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCardShell() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)));
        return card;
    }

    // Đã sửa đổi: Vẽ viền ngang, loại bỏ ép size gây thanh cuộn
    private JScrollPane wrapTable(JTable table, Dimension preferredSize) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(36); 
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#E2E8F0")); 

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        scrollPane.setOpaque(false);
        
        scrollPane.setPreferredSize(new Dimension(0, table.getRowHeight() * (table.getRowCount() + 2)));
        
        return scrollPane;
    }

    private JTable createTable(Object[][] data, String[] columns) {
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new CustomTable(model);
        table.setBackground(CARD_BACKGROUND);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    private JTable buildGroupedTable(List<CandidateDTO> candidates, boolean byObject) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (CandidateDTO candidate : candidates) {
            String key = byObject ? candidate.getDoiTuong() : candidate.getKhuVuc();
            key = normalizeGroupValue(key);
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getKey));

        int total = candidates.size();
        Object[][] data = new Object[entries.size() + 1][3];
        int index = 0;
        for (Map.Entry<String, Integer> entry : entries) {
            data[index][0] = entry.getKey();
            data[index][1] = entry.getValue();
            data[index][2] = total == 0 ? "0%" : formatPercent(entry.getValue(), total);
            index++;
        }

        int sum = 0;
        for (Map.Entry<String, Integer> entry : entries) {
            sum += entry.getValue();
        }
        data[index][0] = "TOTAL";
        data[index][1] = sum;
        data[index][2] = total == 0 ? "0%" : "100%";

        String[] columns = byObject
                ? new String[] {"Đối tượng", "Count", "Percentage"}
                : new String[] {"Khu vực", "Count", "Percentage"};
        return createTable(data, columns);
    }

    private String normalizeGroupValue(String value) {
        if (value == null) {
            return "(Không xác định)";
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? "(Không xác định)" : trimmed;
    }

    private String formatPercent(int count, int total) {
        if (total <= 0) {
            return "0%";
        }

        double percent = (count * 100.0) / total;
        return String.format(java.util.Locale.US, "%.1f%%", percent);
    }

    private List<CandidateDTO> loadCandidates() {
        List<CandidateDTO> candidates = candidateBUS.getAllWithPagination(0, Integer.MAX_VALUE);
        return candidates != null ? candidates : List.of();
    }
}