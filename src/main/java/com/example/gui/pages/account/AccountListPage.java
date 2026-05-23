package com.example.gui.pages.account;

import com.example.bus.AccountBUS;
import com.example.dto.AccountDTO;
import com.example.gui.components.AddAndConfirmButton;
import com.example.gui.components.ActionButton;
import com.example.gui.components.CustomTable;
import com.example.gui.components.Pagination;
import com.example.gui.components.TextBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.util.List;

public class AccountListPage extends JPanel {
    private final AccountManagementPage parent;
    private static final int LIMIT = 20;
    private TextBox searchBox;
    private CustomTable table;
    private Pagination pagination;
    private DefaultTableModel tableModel;
    private AccountBUS accountBUS;
    private boolean isSearching = false;
    private String currentSearchKeyword = "";

    public AccountListPage(AccountManagementPage parent) {
        this.parent = parent;
        this.accountBUS = new AccountBUS();

        setLayout(new BorderLayout(0, 18));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F8F9FA"));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(createToolBarPanel(), BorderLayout.NORTH);
        content.add(createMainTablePanel(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        panel.setBackground(Color.decode("#F8F9FA"));

        JLabel lblTitle = new JLabel("Quản lý người dùng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel lblSubtitle = new JLabel("Xem, sửa và quản lý tài khoản hệ thống.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(Color.GRAY);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblSubtitle);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.putClientProperty("FlatLaf.style", "arc: 12;");
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224)),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        searchBox = new TextBox("Tìm kiếm theo tên người dùng...");
        searchBox.setPreferredSize(new Dimension(420, 40));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnListReload = new ActionButton("Tải lại", "/assets/images/refresh.png", "TERTIARY");
        JButton btnAdd = new AddAndConfirmButton("+ Thêm user");

        btnAdd.addActionListener(e -> {
            if (parent != null) {
                parent.showCreate();
            }
        });

        searchBox.addActionListener(e -> searchData());

        btnListReload.addActionListener(e -> refreshData());
        right.add(btnListReload);
        right.add(btnAdd);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMainTablePanel() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(false);

        String[] cols = {"STT", "Username", "Họ tên", "Role", "Trạng thái", "Hành động"};
        Object[][] rows = {};

        tableModel = new DefaultTableModel(rows, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Cột 5 đang là cột hành động (button)
            }
        };

        table = new CustomTable(tableModel);
        table.setFillsViewportHeight(true);

        // Căn chỉnh độ rộng các cột cho đẹp
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setMinWidth(140);
        table.getColumnModel().getColumn(5).setMaxWidth(180);
        table.getColumnModel().getColumn(5).setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setBackground(Color.WHITE);
                setForeground(Color.decode("#475569"));
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#CBD5E1")));
                return this;
            }
        });

        table.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionCellEditor(this));
        installActionColumnCursor(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);

        long totalRecords = accountBUS.getCount();
        pagination = new Pagination((int) totalRecords, LIMIT, 1);
        pagination.addPageChangeListener(page -> {
            int offset = (page - 1) * LIMIT;
            if (isSearching) {
                loadSearchData(offset);
            } else {
                loadData(offset);
            }
        });

        card.add(sp, BorderLayout.CENTER);
        card.add(pagination, BorderLayout.SOUTH);

        loadData(0);

        return card;
    }

    private void loadData(int offset) {
        try {
            renderAccounts(accountBUS.getAllWithPagination(offset, LIMIT));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSearchData(int offset) {
        try {
            renderAccounts(accountBUS.searchByUsernameOrName(currentSearchKeyword, offset, LIMIT));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch(String keyword) {
        try {
            long searchResultCount = accountBUS.getSearchCount(keyword);
            pagination.setTotalItems((int) searchResultCount, LIMIT);
            pagination.setCurrentPageSilently(1);
            isSearching = true;
            currentSearchKeyword = keyword;
            loadSearchData(0);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshData() {
        long totalRecords = accountBUS.getCount();
        pagination.setTotalItems((int) totalRecords, LIMIT);
        pagination.setCurrentPageSilently(1);
        isSearching = false;
        currentSearchKeyword = "";
        loadData(0);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<AccountDTO> list = accountBUS.getAll();

        if (list != null) {
            for (AccountDTO acc : list) {
                Object[] rowData = new Object[]{
                        acc.getId(),
                        acc.getUsername(),
                        acc.getHoTen() != null ? acc.getHoTen() : "N/A",
                        acc.getRole(),
                        acc.isActive() ? "Hoạt động" : "Vô hiệu",
                        acc
                };
                tableModel.addRow(rowData);
            }
        }
    }

    private void searchData() {
        String keyword = searchBox.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshData();
            return;
        }

        performSearch(keyword.trim());
    }

    public void refresh() {
        refreshData();
    }

    private void renderAccounts(List<AccountDTO> accounts) {
        tableModel.setRowCount(0);

        if (accounts == null) {
            return;
        }

        for (AccountDTO acc : accounts) {
            Object[] rowData = new Object[]{
                    acc.getId(),
                    acc.getUsername(),
                    acc.getHoTen() != null ? acc.getHoTen() : "N/A",
                    acc.getRole(),
                    acc.isActive() ? "Hoạt động" : "Vô hiệu",
                    acc
            };
            tableModel.addRow(rowData);
        }
    }

    // Hàm tiện ích để tra cứu DTO từ username trên bảng
    private AccountDTO getAccountByUsername(String username) {
        List<AccountDTO> list = accountBUS.getAll();
        if (list != null) {
            for (AccountDTO acc : list) {
                if (acc.getUsername().equals(username)) {
                    return acc;
                }
            }
        }
        return null;
    }

    private void refreshAfterDelete() {
        refreshData();
    }

    private ImageIcon loadBlackIcon(String resourcePath, int width, int height) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return null;
        }

        ImageIcon baseIcon = new ImageIcon(resource);
        Image scaled = baseIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaled);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(scaledIcon.getImage(), 0, 0, null);
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
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setIconTextGap(0);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createActionButtonPanel(JButton btnView, JButton btnEdit, JButton btnDelete, Color background) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JPanel inner = new JPanel(new GridLayout(1, 3, 8, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(0, 4, 0, 4));
        inner.setPreferredSize(new Dimension(110, 26));
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

    private void installActionColumnCursor(JTable targetTable) {
        targetTable.setCursor(Cursor.getDefaultCursor());
        targetTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateActionColumnCursor(targetTable, e);
            }
        });
        targetTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                targetTable.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void updateActionColumnCursor(JTable targetTable, MouseEvent event) {
        int row = targetTable.rowAtPoint(event.getPoint());
        int column = targetTable.columnAtPoint(event.getPoint());

        if (row >= 0 && column == 5) {
            targetTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            targetTable.setCursor(Cursor.getDefaultCursor());
        }
    }

    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JPanel actionPanel;
        private final JButton btnView;
        private final JButton btnEdit;
        private final JButton btnDelete;

        public ActionCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            btnView = createActionButton("Xem", "/assets/images/info.png");
            btnEdit = createActionButton("Sửa", "/assets/images/edit.png");
            btnDelete = createActionButton("Xóa", "/assets/images/delete.png");
            actionPanel = createActionButtonPanel(btnView, btnEdit, btnDelete, Color.WHITE);
            add(actionPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            actionPanel.setBackground(getBackground());
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton btnView;
        private JButton btnEdit;
        private JButton btnDelete;
        private AccountDTO currentAccount;
        private final AccountListPage parentPanel;

        public ActionCellEditor(AccountListPage parentPanel) {
            this.parentPanel = parentPanel;

            panel = new JPanel(new BorderLayout());
            panel.setOpaque(true);

            btnView = createActionButton("Xem", "/assets/images/info.png");
            btnView.addActionListener(e -> {
                if (parent != null && currentAccount != null) {
                    parent.showDetail(currentAccount);
                }
                stopCellEditing();
            });

            btnEdit = createActionButton("Sửa", "/assets/images/edit.png");
            btnEdit.addActionListener(e -> {
                if (parent != null && currentAccount != null) {
                    parent.showEdit(currentAccount);
                }
                stopCellEditing();
            });

            btnDelete = createActionButton("Xóa", "/assets/images/delete.png");
            btnDelete.addActionListener(e -> {
                if (currentAccount == null) {
                    stopCellEditing();
                    return;
                }

                String username = currentAccount.getUsername();
                int confirm = JOptionPane.showConfirmDialog(
                        AccountListPage.this,
                        "Bạn có chắc muốn xóa tài khoản '" + username + "'?",
                        "Xác nhận xóa",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        accountBUS.delete(currentAccount);
                        parentPanel.refreshAfterDelete();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                AccountListPage.this,
                                "Không thể xóa tài khoản: " + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
                stopCellEditing();
            });

            panel.add(createActionButtonPanel(btnView, btnEdit, btnDelete, Color.WHITE), BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            Object maybe = table.getValueAt(row, 5);
            if (maybe instanceof AccountDTO) {
                currentAccount = (AccountDTO) maybe;
            } else {
                Object uname = table.getValueAt(row, 1);
                if (uname instanceof String) currentAccount = getAccountByUsername((String) uname);
                else currentAccount = null;
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
            return currentAccount;
        }
    }
}