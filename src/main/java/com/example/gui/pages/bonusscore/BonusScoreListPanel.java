package com.example.gui.pages.bonusscore;

import com.example.gui.components.ActionButton;
import com.example.gui.components.AddAndConfirmButton;
import com.example.gui.components.CustomTable;
import com.example.gui.components.Pagination;
import com.example.gui.components.TextBox;
import com.example.bus.BonusScoreBUS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import com.example.dto.BonusScoreDTO;

public class BonusScoreListPanel extends JPanel {
    private final BonusScoreManagement parent;

    private static final int PAGE_SIZE = 20;

    private final BonusScoreBUS bonusScoreBUS = new BonusScoreBUS();

    private int currentTotalEntries = 0;

    private TextBox searchBox;
    private JLabel lblShowing;
    private JLabel lblTotalEntries;
    private CustomTable table;
    private DefaultTableModel tableModel;
    private Pagination pagination;

    public BonusScoreListPanel() {
        this(null);
    }

    public BonusScoreListPanel(BonusScoreManagement parent) {
        this.parent = parent;
        // use server-side pagination on init

        setLayout(new BorderLayout(0, 18));
        setBackground(Color.decode("#F5F7FA"));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        add(createHeaderSection(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(createTableSection(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        // initial load with pagination
        applyFilters();
    }

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        panel.setBackground(Color.decode("#F5F7FA"));

        JLabel title = new JLabel("Quản lý điểm cộng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.decode("#0F172A"));

        JLabel subtitle = new JLabel("Quản lý điểm cộng chứng chỉ và điểm cộng ưu tiên của thí sinh.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.decode("#64748B"));

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        return panel;
    }

    private JPanel createTableSection() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E5E7EB")),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnListReload = new ActionButton("Tải lại", "/assets/images/refresh.png", "TERTIARY");
        JButton btnImport = new ActionButton("Nhập Excel", "/assets/images/upload_file.png", "PRIMARY");
        JButton btnAdd = new AddAndConfirmButton("+ Thêm điểm cộng");
        btnAdd.setPreferredSize(new Dimension(200, 40));

        btnAdd.addActionListener(e -> {
            if (parent != null) {
                parent.showCreateOverlay();
            }
        });

        right.add(btnListReload);
        right.add(btnImport);
        right.add(btnAdd);

        btnListReload.addActionListener(e -> {
            refreshData();
        });

        btnImport.addActionListener(e -> {
            if (parent != null) {
                parent.showImportOverlay();
            }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        searchBox = new TextBox("Tìm kiếm theo CCCD...");
        searchBox.setPreferredSize(new Dimension(420, 38));
        searchBox.getDocument().addDocumentListener(new DocumentListener() {
            private final Timer debounceTimer = new Timer(250, e -> applyFilters());

            {
                debounceTimer.setRepeats(false);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                debounceTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                debounceTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                debounceTimer.restart();
            }
        });
        left.add(searchBox);

        // Place search box on the left and action buttons on the right
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        String[] columns = {
                "CCCD",
                "Mã ngành",
                "Tổ hợp",
                "Phương thức",
                "Điểm CC",
                "Điểm ƯT",
                "Tổng",
                "Hành động"
        };

        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        table = new CustomTable(tableModel);
        table.setRowHeight(42);
        table.setShowHorizontalLines(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        installActionColumnCursor(table);
        configureActionColumn();

        DefaultTableCellRenderer leftAligned = new DefaultTableCellRenderer();
        leftAligned.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(4).setCellRenderer(leftAligned);
        table.getColumnModel().getColumn(5).setCellRenderer(leftAligned);
        table.getColumnModel().getColumn(6).setCellRenderer(leftAligned);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E7EB")));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);

        pagination = new Pagination(1, PAGE_SIZE, 1);
        pagination.addPageChangeListener(this::refreshTable);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        lblShowing = new JLabel("0 - 0 trong tổng số 0");
        lblShowing.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblShowing.setForeground(Color.decode("#6B7280"));

        lblTotalEntries = new JLabel(" ");
        lblTotalEntries.setVisible(false);

        footer.add(lblShowing, BorderLayout.WEST);
        footer.add(pagination, BorderLayout.EAST);
        footer.add(lblTotalEntries, BorderLayout.CENTER);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }


    // private void seedHardcodedData() {
    //     allRecords.add(new BonusRecord("001203045678", "IT01", "A00", "HSGS", 1.5, 0.5, 2.0));
    //     allRecords.add(new BonusRecord("034201089342", "BA02", "D01", "IELTS", 2.0, 0.0, 2.0));
    //     allRecords.add(new BonusRecord("079204011234", "MD01", "B00", "N/A", 0.0, 1.25, 1.25));
    //     allRecords.add(new BonusRecord("001205099887", "IT01", "A01", "HSGS", 1.0, 0.5, 1.5));
    //     allRecords.add(new BonusRecord("082233445566", "CN11", "D01", "SAT", 1.75, 0.25, 2.0));
    //     allRecords.add(new BonusRecord("091122334455", "KT03", "B03", "TOEFL", 1.25, 0.25, 1.5));
    //     allRecords.add(new BonusRecord("094455667788", "IT02", "A02", "Xét học bạ", 1.5, 0.25, 1.75));
    //     allRecords.add(new BonusRecord("088877766655", "BA03", "D07", "Xét điểm thi", 1.0, 0.5, 1.5));
    //     allRecords.add(new BonusRecord("073344556677", "MD02", "B08", "Không áp dụng", 0.0, 1.0, 1.0));
    //     allRecords.add(new BonusRecord("065544332211", "CN12", "D05", "IELTS", 2.0, 0.25, 2.25));
    // }

    public void refreshData() {
        applyFilters();
    }

    private void applyFilters() {
        String search = searchBox == null ? "" : searchBox.getText().trim().toLowerCase(Locale.ROOT);

        // Use server-side pagination via DAO
        String searchText = search.trim();
        try {
            if (searchText.isEmpty()) {
                currentTotalEntries = (int) bonusScoreBUS.getBonusScoreCount();
            } else {
                String pattern = "%" + searchText + "%";
                currentTotalEntries = (int) bonusScoreBUS.countBonusScoresByCccd(pattern);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            currentTotalEntries = 0;
        }

        int totalPages = Math.max(1, (currentTotalEntries + PAGE_SIZE - 1) / PAGE_SIZE);
        pagination.setTotalPages(totalPages);
        pagination.setCurrentPageSilently(1);
        refreshTable(1);

        if (currentTotalEntries == 0) {
            lblShowing.setText("0 - 0 trong tổng số 0");
        }
    }

    private void refreshTable(int page) {
        tableModel.setRowCount(0);
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * PAGE_SIZE;

        String search = searchBox == null ? "" : searchBox.getText().trim();
        try {
            List<BonusScoreDTO> rows;
            if (search.isEmpty()) {
                rows = bonusScoreBUS.getBonusScoresWithPagination(offset, PAGE_SIZE);
            } else {
                String pattern = "%" + search + "%";
                rows = bonusScoreBUS.searchBonusScoresWithPagination(pattern, offset, PAGE_SIZE);
            }

            if (rows != null) {
                for (BonusScoreDTO r : rows) {
                        tableModel.addRow(new Object[]{
                            r.getTsCccd(),
                            r.getMaNganh(),
                            r.getMaToHop(),
                            r.getPhuongThuc(),
                            formatScore(r.getDiemCC()),
                            formatScore(r.getDiemUtxt(), 2),
                            formatScore(r.getDiemTong(), 2),
                            r
                        });
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        updateFooter(safePage, currentTotalEntries);
    }

    private void updateFooter(int page, int totalEntries) {
        if (totalEntries == 0) {
            lblShowing.setText("0 - 0 trong tổng số 0");
            return;
        }

        int start = (page - 1) * PAGE_SIZE + 1;
        int end = Math.min(page * PAGE_SIZE, totalEntries);
        lblShowing.setText(start + " - " + end + " trong tổng số " + String.format(Locale.ROOT, "%d", totalEntries));
    }

    private static String formatScore(Double value) {
        return formatScore(value, 1);
    }

    private static String formatScore(Double value, int decimals) {
        if (value == null) return "";
        String pattern = "%." + decimals + "f";
        return String.format(Locale.ROOT, pattern, value);
    }

    private void configureActionColumn() {
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionCellEditor());
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.getColumnModel().getColumn(7).setMinWidth(100);
        table.getColumnModel().getColumn(7).setMaxWidth(140);
    }

    private void installActionColumnCursor(JTable targetTable) {
        targetTable.setCursor(Cursor.getDefaultCursor());
        targetTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = targetTable.rowAtPoint(e.getPoint());
                int column = targetTable.columnAtPoint(e.getPoint());
                if (row >= 0 && column == 7) {
                    targetTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    targetTable.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        targetTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                targetTable.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private ImageIcon loadScaledIcon(String resourcePath, int width, int height) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return null;
        }

        ImageIcon icon = new ImageIcon(resource);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private ImageIcon loadBlackIcon(String resourcePath, int width, int height) {
        ImageIcon baseIcon = loadScaledIcon(resourcePath, width, height);
        if (baseIcon == null) {
            return null;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(baseIcon.getImage(), 0, 0, null);
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, width, height);
        g2.dispose();
        return new ImageIcon(image);
    }

    private JButton createActionButton(String text, String iconPath) {
        JButton button = new JButton();
        ImageIcon icon = loadBlackIcon(iconPath, 16, 16);
        if (icon != null) {
            button.setIcon(icon);
        } else {
            button.setText(text);
        }

        button.setPreferredSize(new Dimension(26, 26));
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.BLACK);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createActionButtonPanel(JButton btnView, JButton btnEdit, JButton btnDelete, Color background) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        inner.setOpaque(false);
        inner.add(btnView);
        inner.add(btnEdit);
        inner.add(btnDelete);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(inner, gbc);
        return panel;
    }

    private class ActionCellRenderer extends JPanel implements TableCellRenderer {

        public ActionCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            JPanel actionPanel = createActionButtonPanel(
                    createActionButton("Xem", "/assets/images/info.png"),
                    createActionButton("Sửa", "/assets/images/edit.png"),
                    createActionButton("Xóa", "/assets/images/delete.png"),
                    getBackground());
            actionPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return actionPanel;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton btnView;
        private final JButton btnEdit;
        private final JButton btnDelete;
        private BonusScoreDTO current;

        public ActionCellEditor() {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

            JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            inner.setOpaque(false);

            btnView = createActionButton("Xem", "/assets/images/info.png");
            btnView.addActionListener(e -> {
                if (parent != null && current != null) {
                    parent.showDetailOverlay(current);
                }
                stopCellEditing();
            });

            btnEdit = createActionButton("Sửa", "/assets/images/edit.png");
            btnEdit.addActionListener(e -> {
                if (parent != null && current != null) {
                    parent.showEditOverlay(current);
                }
                stopCellEditing();
            });

            btnDelete = createActionButton("Xóa", "/assets/images/delete.png");
            btnDelete.addActionListener(e -> {
                if (parent != null && current != null) {
                    parent.showDeleteOverlay(current);
                }
                stopCellEditing();
            });

            inner.add(btnView);
            inner.add(btnEdit);
            inner.add(btnDelete);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(inner, gbc);
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            if (value instanceof BonusScoreDTO) {
                current = (BonusScoreDTO) value;
            } else {
                current = null;
            }

            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return current;
        }
    }

    // Server-side pagination: data rows are fetched directly as BonusScoreDTO
}
