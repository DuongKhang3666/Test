package com.example.gui.pages.conversion;

import com.example.bus.ConversionTableBUS;
import com.example.dto.ConversionTableDTO;
import com.example.gui.components.AddAndConfirmButton;
import com.example.gui.components.ActionButton;
import com.example.gui.components.CustomTable;
import com.example.gui.components.Pagination;
import com.example.gui.components.TextBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class ConversionTableListPage extends JPanel {
    private final ConversionTableManagementPage parent;
    private static final int LIMIT = 20;
    private TextBox searchBox;
    private CustomTable table;
    private Pagination pagination;
    private DefaultTableModel tableModel;
    private ConversionTableBUS conversionTableBUS;
    private String currentKeyword = "";

    public ConversionTableListPage(ConversionTableManagementPage parent) {
        this.parent = parent;
        this.conversionTableBUS = new ConversionTableBUS();

        setLayout(new BorderLayout(0, 18));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F8F9FA"));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(createToolBarPanel(), BorderLayout.NORTH);
        content.add(createMainTablePanel(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        loadDataToTable();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý bảng quy đổi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.decode("#0F172A"));

        JLabel lblSubtitle = new JLabel("Cấu hình các tham số nội suy tuyến tính V-SAT, bách phân vị ĐGNL và chứng chỉ ngoại ngữ.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(Color.decode("#64748B"));

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblSubtitle);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.putClientProperty("FlatLaf.style", "arc: 12;");
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        searchBox = new TextBox("Tìm kiếm theo mã, phương thức, tổ hợp...");
        searchBox.setPreferredSize(new Dimension(420, 40));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnListReload = new ActionButton("Tải lại", "/assets/images/refresh.png", "TERTIARY");
        btnListReload.addActionListener(e -> refresh());

        JButton btnImport = new ActionButton("Nhập Excel", "/assets/images/upload_file.png", "PRIMARY");
        btnImport.addActionListener(e -> parent.showImportOverlay());

        JButton btnAdd = new AddAndConfirmButton("+ Thêm mức quy đổi");
        btnAdd.addActionListener(e -> parent.showCreate());

        right.add(btnListReload);
        right.add(btnImport);
        right.add(btnAdd);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        searchBox.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                handleSearchChanged();
            }
        });

        card.add(toolbar, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMainTablePanel() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(false);

        String[] cols = {"ID", "Mã quy đổi", "Phương thức", "Tổ hợp", "Môn", "Phân vị", "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Hành động"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10;
            }
        };

        table = new CustomTable(tableModel);
        table.setFillsViewportHeight(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(10).setPreferredWidth(140);
        TableCellRenderer defaultHeaderRenderer = table.getTableHeader().getDefaultRenderer();
        // Canh trái header cho cột bình thường, canh giữa cho cột Hành động
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            final int colIdx = i;
            table.getColumnModel().getColumn(i).setHeaderRenderer((headerTable, value, isSelected, hasFocus, row, column) -> {
                Component component = defaultHeaderRenderer.getTableCellRendererComponent(headerTable, value, isSelected, hasFocus, row, column);
                if (component instanceof JLabel) {
                    int alignment = (colIdx == 10) ? SwingConstants.CENTER : SwingConstants.LEFT;
                    ((JLabel) component).setHorizontalAlignment(alignment);
                }
                return component;
            });
        }

        installActionColumnCursor(table);
        table.getColumnModel().getColumn(10).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(10).setCellEditor(new ActionCellEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);

        pagination = new Pagination(0, LIMIT, 1);
        pagination.addPageChangeListener(pageIndex -> loadDataForPage(pageIndex));

        card.add(sp, BorderLayout.CENTER);
        card.add(pagination, BorderLayout.SOUTH);

        return card;
    }

    public void refresh() {
        loadDataForPage(pagination != null ? pagination.getCurrentPage() : 1);
    }

    private void loadDataToTable() {
        loadDataForPage(pagination != null ? pagination.getCurrentPage() : 1);
    }

    private void handleSearchChanged() {
        String newKeyword = searchBox.getText() != null ? searchBox.getText().trim() : "";
        if (newKeyword.equals(currentKeyword)) {
            return;
        }
        currentKeyword = newKeyword;
        if (pagination != null) {
            pagination.setCurrentPageSilently(1);
        }
        loadDataForPage(1);
    }

    private void loadDataForPage(int pageIndex) {
        tableModel.setRowCount(0);
        List<ConversionTableDTO> list;

        if (currentKeyword == null || currentKeyword.isBlank()) {
            int totalItems = (int) conversionTableBUS.count();
            if (pagination != null) {
                pagination.setTotalItems(totalItems, LIMIT);
                pagination.setCurrentPageSilently(pageIndex);
            }
            list = conversionTableBUS.getPage(Math.max(0, pageIndex - 1), LIMIT);
        } else {
            List<ConversionTableDTO> filtered = conversionTableBUS.search(currentKeyword);
            int totalItems = filtered.size();
            if (pagination != null) {
                pagination.setTotalItems(totalItems, LIMIT);
                pagination.setCurrentPageSilently(pageIndex);
            }

            int safePage = pagination != null ? pagination.getCurrentPage() : pageIndex;
            int start = Math.max(0, (safePage - 1) * LIMIT);
            int end = Math.min(start + LIMIT, totalItems);
            list = start >= end ? java.util.Collections.emptyList() : filtered.subList(start, end);
        }

        if (list != null) {
            for (ConversionTableDTO dto : list) {
                tableModel.addRow(new Object[]{
                        dto.getIdqd(),
                        dto.getMaQuydoi() != null ? dto.getMaQuydoi() : "N/A",
                        dto.getPhuongthuc(),
                        dto.getTohop() != null ? dto.getTohop() : "-",
                        dto.getMon() != null ? dto.getMon() : "-",
                        dto.getPhanvi() != null ? dto.getPhanvi() : "-",
                        dto.getDiemA(),
                        dto.getDiemB(),
                        dto.getDiemC(),
                        dto.getDiemD(),
                        dto
                });
            }
        }
    }

    
    private void installActionColumnCursor(JTable targetTable) {
        targetTable.setCursor(Cursor.getDefaultCursor());
        targetTable.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                updateActionColumnCursor(targetTable, e);
            }
        });
        targetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                targetTable.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void updateActionColumnCursor(JTable targetTable, java.awt.event.MouseEvent event) {
        int row = targetTable.rowAtPoint(event.getPoint());
        int column = targetTable.columnAtPoint(event.getPoint());

        if (row >= 0 && column == 10) {
            targetTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            targetTable.setCursor(Cursor.getDefaultCursor());
        }
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

    private JPanel createActionButtonPanel(JButton btnEdit, JButton btnDelete, Color background) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JPanel inner = new JPanel(new GridLayout(1, 2, 8, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(0, 4, 0, 4));
        inner.setPreferredSize(new Dimension(70, 26));
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

    private ImageIcon loadBlackIcon(String resourcePath, int width, int height) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return null;
        }

        ImageIcon baseIcon = new ImageIcon(resource);
        Image scaled = baseIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaled);

        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(scaledIcon.getImage(), 0, 0, null);
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, width, height);
        g2.dispose();
        return new ImageIcon(image);
    }

    private class ActionCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        public ActionCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) setBackground(table.getSelectionBackground()); else setBackground(table.getBackground());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel actionPanel = createActionButtonPanel(
                    createActionButton("Sửa", "/assets/images/edit.png"),
                    createActionButton("Xóa", "/assets/images/delete.png"),
                    getBackground());
            actionPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return actionPanel;
        }
    }

    private class ActionCellEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private JPanel panel;
        private JButton btnEdit;
        private JButton btnDelete;
        private ConversionTableDTO currentDTO;

        public ActionCellEditor() {
            panel = new JPanel(new BorderLayout());
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

            btnEdit = createActionButton("Sửa", "/assets/images/edit.png");
            btnEdit.addActionListener(e -> {
                if (currentDTO != null) parent.showEdit(currentDTO);
                stopCellEditing();
            });

            btnDelete = createActionButton("Xóa", "/assets/images/delete.png");
            btnDelete.addActionListener(e -> {
                if (currentDTO == null) {
                    stopCellEditing();
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        ConversionTableListPage.this,
                        "Bạn có chắc chắn muốn xóa mức quy đổi này không?",
                        "Xác nhận xóa",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        conversionTableBUS.delete(currentDTO.getIdqd());
                        loadDataToTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ConversionTableListPage.this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
                stopCellEditing();
            });

            panel.add(createActionButtonPanel(btnEdit, btnDelete, Color.WHITE), BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Object maybe = table.getValueAt(row, 10);
            if (maybe instanceof ConversionTableDTO) currentDTO = (ConversionTableDTO) maybe;
            else currentDTO = null;

            if (isSelected) panel.setBackground(table.getSelectionBackground()); else panel.setBackground(table.getBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentDTO;
        }
    }

}