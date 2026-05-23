package com.example.gui.pages.majorcombination;

import com.example.bus.SubjectGroupBUS;
import com.example.dto.SubjectGroupDTO;
import com.example.gui.components.AddAndConfirmButton;
import com.example.gui.components.CustomTable;
import com.example.gui.components.TextBox;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class SubjectGroupManagementPage extends JPanel {
    private final SubjectGroupBUS subjectGroupBUS;
    private DefaultTableModel tableModel;
    private CustomTable subjectGroupTable;
    private TextBox txtSearch;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private List<SubjectGroupDTO> allData;

    public SubjectGroupManagementPage() {
        this.subjectGroupBUS = new SubjectGroupBUS();
        
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);

        JLabel titleLabel = new JLabel("Subject Groups Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode("#0F172A"));

        JLabel subTitleLabel = new JLabel("Manage subject group combinations for entrance exam.");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitleLabel.setForeground(Color.decode("#64748B"));

        leftHeader.add(titleLabel);
        leftHeader.add(Box.createVerticalStrut(5));
        leftHeader.add(subTitleLabel);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightHeader.setOpaque(false);

        txtSearch = new TextBox("Search by Combo Code...", (javax.swing.Icon) null);
        AddAndConfirmButton btnImport = new AddAndConfirmButton("📥 Import Excel");
        btnImport.setBackground(Color.decode("#64748B"));
        AddAndConfirmButton btnAdd = new AddAndConfirmButton("+ Add New Group");

        btnImport.addActionListener(e -> onImportClick());
        btnAdd.addActionListener(e -> onAddNewClick());
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                onSearchChange();
            }
        });

        rightHeader.add(txtSearch);
        rightHeader.add(btnImport);
        rightHeader.add(btnAdd);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        // --- TABLE ---
        tableModel = new DefaultTableModel(
            new Object[]{"COMBO CODE", "SUBJECT 1", "SUBJECT 2", "SUBJECT 3", "GROUP NAME", "ACTIONS"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 5; }
        };

        subjectGroupTable = new CustomTable(tableModel);
        subjectGroupTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        subjectGroupTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        subjectGroupTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        subjectGroupTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        subjectGroupTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        subjectGroupTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        subjectGroupTable.getColumnModel().getColumn(5).setCellRenderer(new SubjectGroupActionCellRenderer());
        subjectGroupTable.getColumnModel().getColumn(5).setCellEditor(new SubjectGroupActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(subjectGroupTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0")));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- PAGINATION ---
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        paginationPanel.setOpaque(false);
        
        JButton btnPrev = new JButton("Previous");
        JButton btnNext = new JButton("Next");
        JLabel pageLabel = new JLabel("Page 1");
        
        btnPrev.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshTable();
                pageLabel.setText("Page " + (currentPage + 1));
            }
        });
        
        btnNext.addActionListener(e -> {
            currentPage++;
            refreshTable();
            pageLabel.setText("Page " + (currentPage + 1));
        });

        paginationPanel.add(btnPrev);
        paginationPanel.add(pageLabel);
        paginationPanel.add(btnNext);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(paginationPanel, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        allData = subjectGroupBUS.getAll();
        if (allData != null) {
            allData = allData.stream()
                    .sorted(java.util.Comparator.comparing(
                            SubjectGroupDTO::getMaToHop,
                            java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                    ))
                    .toList();
        }
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        if (allData == null || allData.isEmpty()) {
            return;
        }

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allData.size());

        for (int i = start; i < end; i++) {
            SubjectGroupDTO sg = allData.get(i);
            Object[] rowData = new Object[]{
                sg.getMaToHop(),
                sg.getMon1(),
                sg.getMon2(),
                sg.getMon3(),
                sg.getTenToHop() != null ? sg.getTenToHop() : "-",
                sg // store the DTO in the actions column so editor can access it
            };
            tableModel.addRow(rowData);
        }
    }

    private void onSearchChange() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        
        List<SubjectGroupDTO> all = subjectGroupBUS.getAll();
        allData = all.stream()
            .filter(sg -> (sg.getMaToHop() != null && sg.getMaToHop().toLowerCase().contains(keyword)) ||
                         (sg.getTenToHop() != null && sg.getTenToHop().toLowerCase().contains(keyword)))
            .toList();
        currentPage = 0;
        refreshTable();
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

    private JButton createActionButton(String tooltip, String iconPath, Color accentColor) {
        JButton button = new JButton();
        ImageIcon icon = loadScaledIcon(iconPath, 14, 14);
        if (icon != null) {
            button.setIcon(icon);
        } else {
            button.setText(tooltip);
        }

        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(26, 26));
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(accentColor);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private class SubjectGroupActionCellRenderer extends JPanel implements TableCellRenderer {
        public SubjectGroupActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
            setOpaque(true);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.decode("#E2E8F0")));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            removeAll();
            add(createActionButton("Sửa", "/assets/images/edit.png", Color.decode("#0066CC")));
            add(createActionButton("Xóa", "/assets/images/delete.png", Color.decode("#DC3545")));
            return this;
        }
    }

    private class SubjectGroupActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton btnEdit;
        private final JButton btnDelete;
        private SubjectGroupDTO currentItem;

        public SubjectGroupActionCellEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.decode("#E2E8F0")));

            btnEdit = createActionButton("Sửa", "/assets/images/edit.png", Color.decode("#0066CC"));
            btnDelete = createActionButton("Xóa", "/assets/images/delete.png", Color.decode("#DC3545"));

            btnEdit.addActionListener(e -> {
                if (currentItem != null) {
                    showAddEditDialog(currentItem);
                }
                stopCellEditing();
            });

            btnDelete.addActionListener(e -> {
                if (currentItem != null) {
                    int confirm = JOptionPane.showConfirmDialog(
                            SubjectGroupManagementPage.this,
                            "Xóa tổ hợp môn này?\nHành động sẽ xóa mọi mapping ngành liên quan.",
                            "Xác nhận",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            subjectGroupBUS.delete(currentItem);
                            loadData();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(SubjectGroupManagementPage.this, "Không thể xóa tổ hợp: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                stopCellEditing();
            });

            panel.add(btnEdit);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentItem = value instanceof SubjectGroupDTO ? (SubjectGroupDTO) value : null;
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentItem;
        }
    }

    private void onImportClick() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().endsWith(".xlsx") || f.getName().endsWith(".xls");
            }
            @Override
            public String getDescription() { return "Excel Files (*.xlsx, *.xls)"; }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "Import feature not yet implemented.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onAddNewClick() {
        showAddEditDialog(null);
    }

    private void showAddEditDialog(SubjectGroupDTO existingData) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add Subject Group", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Combo Code
        JPanel pCode = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblCode = new JLabel("Combo Code:");
        lblCode.setPreferredSize(new Dimension(120, 25));
        JTextField txtCode = new JTextField(20);
        if (existingData != null) txtCode.setText(existingData.getMaToHop());
        pCode.add(lblCode);
        pCode.add(txtCode);

        // Subject 1
        JPanel pSubj1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblSubj1 = new JLabel("Subject 1:");
        lblSubj1.setPreferredSize(new Dimension(120, 25));
        JTextField txtSubj1 = new JTextField(20);
        if (existingData != null) txtSubj1.setText(existingData.getMon1());
        pSubj1.add(lblSubj1);
        pSubj1.add(txtSubj1);

        // Subject 2
        JPanel pSubj2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblSubj2 = new JLabel("Subject 2:");
        lblSubj2.setPreferredSize(new Dimension(120, 25));
        JTextField txtSubj2 = new JTextField(20);
        if (existingData != null) txtSubj2.setText(existingData.getMon2());
        pSubj2.add(lblSubj2);
        pSubj2.add(txtSubj2);

        // Subject 3
        JPanel pSubj3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblSubj3 = new JLabel("Subject 3:");
        lblSubj3.setPreferredSize(new Dimension(120, 25));
        JTextField txtSubj3 = new JTextField(20);
        if (existingData != null) txtSubj3.setText(existingData.getMon3());
        pSubj3.add(lblSubj3);
        pSubj3.add(txtSubj3);

        // Group Name
        JPanel pName = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblName = new JLabel("Group Name:");
        lblName.setPreferredSize(new Dimension(120, 25));
        JTextField txtName = new JTextField(20);
        if (existingData != null) txtName.setText(existingData.getTenToHop());
        pName.add(lblName);
        pName.add(txtName);

        // Buttons
        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = new JButton("Save Group");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> {
            String maToHop = txtCode.getText().trim();
            String mon1 = txtSubj1.getText().trim();
            String mon2 = txtSubj2.getText().trim();
            String mon3 = txtSubj3.getText().trim();
            
            if (maToHop.isEmpty() || mon1.isEmpty() || mon2.isEmpty() || mon3.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            SubjectGroupDTO newSg = new SubjectGroupDTO(maToHop, mon1, mon2, mon3, txtName.getText().trim());

            if (existingData != null) {
                newSg.setIdToHop(existingData.getIdToHop());
                subjectGroupBUS.update(newSg);
            } else {
                subjectGroupBUS.save(newSg);
            }

            JOptionPane.showMessageDialog(dialog, "Saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            loadData();
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        pButtons.add(btnSave);
        pButtons.add(btnCancel);

        contentPane.add(pCode);
        contentPane.add(pSubj1);
        contentPane.add(pSubj2);
        contentPane.add(pSubj3);
        contentPane.add(pName);
        contentPane.add(Box.createVerticalStrut(20));
        contentPane.add(pButtons);

        dialog.add(new JScrollPane(contentPane));
        dialog.setVisible(true);
    }
}
