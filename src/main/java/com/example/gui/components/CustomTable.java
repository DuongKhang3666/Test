package com.example.gui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class CustomTable extends JTable {

    // BỘ VẼ DÒNG DỮ LIỆU: Hack thêm dấu cách trực tiếp vào chữ
    private final TableCellRenderer customCellRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Thủ thuật: Thêm 3 dấu cách vào trước chữ để thụt lề mà không bị Swing ghi đè
            String paddedValue = value == null ? "   " : "   " + value.toString();
            super.getTableCellRendererComponent(table, paddedValue, isSelected, hasFocus, row, column);
            return this;
        }
    };

    public CustomTable(TableModel model) {
        super(model);
        
        setRowHeight(45);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(Color.decode("#E2E8F0"));
        
        setBackground(Color.WHITE);
        setForeground(Color.decode("#111827"));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        setSelectionBackground(Color.decode("#F1F5F9"));
        setSelectionForeground(Color.decode("#111827"));
        setFocusable(false);
        setIntercellSpacing(new Dimension(0, 0));

        // Cấu hình Header
        getTableHeader().setPreferredSize(new Dimension(0, 45));
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Thủ thuật: Thêm 3 dấu cách vào trước chữ Header cho bằng với dòng bên dưới
                String paddedValue = value == null ? "   " : "   " + value.toString();
                super.getTableCellRendererComponent(table, paddedValue, isSelected, hasFocus, row, column);
                
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setBackground(Color.WHITE);
                setForeground(Color.decode("#475569"));
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#CBD5E1"))); 
                return this;
            }
        });
    }

    // Ép JTable xài bộ vẽ có dấu cách của mình
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        // Nếu cột đã có renderer riêng (ví dụ action buttons), trả về renderer đó.
        TableCellRenderer colRenderer = getColumnModel().getColumn(column).getCellRenderer();
        if (colRenderer != null) return colRenderer;
        return customCellRenderer;
    }
}