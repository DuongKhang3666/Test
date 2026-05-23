package com.example.gui.pages.candidate;

import com.example.bus.CandidateBUS;
import com.example.gui.components.AddAndConfirmButton;
import com.example.gui.components.ActionButton;
import com.example.gui.components.CustomTable;
import com.example.gui.components.Pagination;
import com.example.gui.components.TextBox;
import com.example.dto.CandidateDTO;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class CandidateListPanel extends JPanel {
    private final CandidateManagement parent;
    private final CandidateBUS candidateBUS;

    private TextBox searchBox;
    private CustomTable table;
    private Pagination pagination;
    private DefaultTableModel tableModel;
    private JLabel lblShowing;
    private final int LIMIT = 20;
    
    private boolean isSearching = false;
    private String currentSearchKeyword = "";
    private List<CandidateDTO> currentCandidates = new ArrayList<>();

    public CandidateListPanel() {
        this(null);
    }

    public CandidateListPanel(CandidateManagement parent) {
        this.parent = parent;
        this.candidateBUS = new CandidateBUS();

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

        JLabel lblTitle = new JLabel("Quản lý thí sinh");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel lblSubtitle = new JLabel("Xem và quản lý hồ sơ thí sinh.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(Color.GRAY);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblSubtitle);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.decode("#F8F9FA"));
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        searchBox = new TextBox("Tìm kiếm theo CCCD hoặc họ tên...");
        searchBox.setPreferredSize(new Dimension(420, 40));
        
        // Thêm listener để xử lý tìm kiếm
        if (searchBox.getDocument() != null) {
            searchBox.getDocument().addDocumentListener(new DocumentListener() {
                private javax.swing.Timer debounceTimer;
                
                private void handleSearch() {
                    String keyword = searchBox.getText().trim();
                    if (keyword.isEmpty()) {
                        isSearching = false;
                        refreshData();
                    } else {
                        isSearching = true;
                        currentSearchKeyword = keyword;
                        performSearch(keyword);
                    }
                }
                
                @Override
                public void insertUpdate(DocumentEvent e) {
                    scheduleSearch();
                }
                
                @Override
                public void removeUpdate(DocumentEvent e) {
                    scheduleSearch();
                }
                
                @Override
                public void changedUpdate(DocumentEvent e) {
                    scheduleSearch();
                }
                
                private void scheduleSearch() {
                    if (debounceTimer != null && debounceTimer.isRunning()) {
                        debounceTimer.stop();
                    }
                    debounceTimer = new javax.swing.Timer(500, e -> handleSearch());
                    debounceTimer.setRepeats(false);
                    debounceTimer.start();
                }
            });
        }
        
        // Lắng nghe sự kiện Enter
        searchBox.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    String keyword = searchBox.getText().trim();
                    if (keyword.isEmpty()) {
                        isSearching = false;
                        refreshData();
                    } else {
                        isSearching = true;
                        currentSearchKeyword = keyword;
                        performSearch(keyword);
                    }
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {}
            
            @Override
            public void keyTyped(KeyEvent e) {}
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnListReload = new ActionButton("Tải lại", "/assets/images/refresh.png", "TERTIARY");
        // JButton btnFilter = new ActionButton("Bộ lọc", "/assets/images/filter_list.png", "TERTIARY");
        JButton btnImport = new ActionButton("Nhập Excel", "/assets/images/upload_file.png", "PRIMARY");
        JButton btnAdd = new AddAndConfirmButton("+ Thêm thí sinh");

        btnListReload.addActionListener(e -> {
            if (parent != null) {
                refreshData();
            }
        });
        
        btnImport.addActionListener(e -> {
            if (parent != null) {
                parent.showImportOverlay();
            }
        });

        btnAdd.addActionListener(e -> {
            if (parent != null) {
                parent.showCreateOverlay();
            }
        });

        right.add(btnListReload);
        // right.add(btnFilter);
        right.add(btnImport);
        right.add(btnAdd);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMainTablePanel() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(false);

        // Table model (cột tiếng Việt)
        String[] cols = {"CCCD", "Họ", "Tên", "Ngày sinh", "Giới tính", "Khu vực", "Hành động"};
        Object[][] rows = {};

        tableModel = new DefaultTableModel(rows, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        table = new CustomTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setShowVerticalLines(false);
        table.setGridColor(Color.decode("#E5E7EB"));
        table.setIntercellSpacing(new Dimension(0, 0));
        installActionColumnCursor(table);

        configureActionColumn();
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E7EB")));
        sp.getViewport().setBackground(Color.WHITE);

        // Lấy tổng số bản ghi từ database
        long totalRecords = candidateBUS.getCount();
        pagination = new Pagination((int) totalRecords, LIMIT, 1);
        pagination.addPageChangeListener((page) -> {
            int offset = (page - 1) * LIMIT;
            if (isSearching) {
                loadSearchData(offset);
            } else {
                loadData(offset);
            }
        });

        // Footer: showing label on left, pagination on right
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        lblShowing = new JLabel("0 - 0 trong tổng số 0");
        lblShowing.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblShowing.setForeground(Color.decode("#6B7280"));
        footer.add(lblShowing, BorderLayout.WEST);
        footer.add(pagination, BorderLayout.EAST);

        card.add(sp, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        
        // Load dữ liệu ban đầu
        loadData(0);

        return card;
    }

    private void loadData(int offset) {
        try {
            List<CandidateDTO> list = candidateBUS.getAllWithPagination(offset, LIMIT);
            renderCandidates(list);
            long total = candidateBUS.getCount();
            int start = list.isEmpty() ? 0 : offset + 1;
            int end = offset + list.size();
            lblShowing.setText(String.format("%d - %d trong tổng số %d", start, end, total));
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        long totalRecords = candidateBUS.getCount();
        pagination.setTotalItems((int) totalRecords, LIMIT);
        pagination.setCurrentPageSilently(1);
        isSearching = false;
        currentSearchKeyword = "";
        loadData(0);
    }

    private void performSearch(String keyword) {
        try {
            long searchResultCount = candidateBUS.getSearchCount(keyword);
            pagination.setTotalItems((int) searchResultCount, LIMIT);
            pagination.setCurrentPageSilently(1);
            loadSearchData(0);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSearchData(int offset) {
        try {
            List<CandidateDTO> list = candidateBUS.searchByCccdOrName(currentSearchKeyword, offset, LIMIT);
            renderCandidates(list);
            long total = candidateBUS.getSearchCount(currentSearchKeyword);
            int start = list.isEmpty() ? 0 : offset + 1;
            int end = offset + list.size();
            lblShowing.setText(String.format("Đang hiển thị %d - %d trong tổng số %d", start, end, total));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu tìm kiếm: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderCandidates(List<CandidateDTO> candidates) {
        currentCandidates = new ArrayList<>(candidates);
        tableModel.setRowCount(0);

        for (CandidateDTO candidate : candidates) {
            tableModel.addRow(toRow(candidate));
        }
    }

    private Object[] toRow(CandidateDTO candidate) {
        return new Object[] {
                candidate.getCccd(),
                candidate.getHo(),
                candidate.getTen(),
                formatDateForDisplay(candidate.getNgaySinh()),
                candidate.getGioiTinh(),
                candidate.getKhuVuc(),
                candidate
        };
    }

    private void configureActionColumn() {
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor(this));
        table.getColumnModel().getColumn(6).setPreferredWidth(96);
        table.getColumnModel().getColumn(6).setMinWidth(88);
        table.getColumnModel().getColumn(6).setMaxWidth(110);
    }

    private String formatDateForDisplay(String dateValue) {
        return CandidateFormSupport.formatDateForDisplay(dateValue);
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

        if (row >= 0 && column == 6) {
            targetTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            targetTable.setCursor(Cursor.getDefaultCursor());
        }
    }

    private JPanel createActionButtonPanel(JButton btnView, JButton btnEdit, Color background) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        inner.setOpaque(false);
        inner.add(btnView);
        inner.add(btnEdit);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(inner, gbc);
        return panel;
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

    // Custom Cell Renderer cho cột Action
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

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel actionPanel = createActionButtonPanel(
                    createActionButton("Xem", "/assets/images/info.png"),
                    createActionButton("Sửa", "/assets/images/edit.png"),
                    getBackground());
            actionPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return actionPanel;
        }
    }

    // Custom Cell Editor cho cột Action
    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton btnView;
        private JButton btnEdit;
        private CandidateDTO currentCandidate;
        private CandidateListPanel parentPanel;

        public ActionCellEditor(CandidateListPanel parentPanel) {
            this.parentPanel = parentPanel;
            
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

            JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            inner.setOpaque(false);

            btnView = createActionButton("Xem", "/assets/images/info.png");
            btnView.addActionListener(e -> {
                if (parent != null && currentCandidate != null) {
                    parent.showDetailOverlay(currentCandidate);
                }
                stopCellEditing();
            });

            btnEdit = createActionButton("Sửa", "/assets/images/edit.png");
            btnEdit.addActionListener(e -> {
                if (parent != null && currentCandidate != null) {
                    parent.showEditOverlay(currentCandidate);
                }
                stopCellEditing();
            });

            inner.add(btnView);
            inner.add(btnEdit);

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
            if (value instanceof CandidateDTO) {
                currentCandidate = (CandidateDTO) value;
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
            return currentCandidate;
        }
    }
}
