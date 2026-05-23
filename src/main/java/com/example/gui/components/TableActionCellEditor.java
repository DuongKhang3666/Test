package com.example.gui.components;

import com.example.gui.events.TableActionEvent;
import javax.swing.*;
import java.awt.*;

public class TableActionCellEditor extends DefaultCellEditor {
    private TableActionEvent event;

    public TableActionCellEditor(TableActionEvent event) {
        super(new JCheckBox()); // Trick của Java Swing
        this.event = event;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Lưu ý: Nếu bạn đang dùng tên class là PanelAction thì sửa lại cho khớp nhé
        TableActionPanel action = new TableActionPanel();
        // Wrap original event so we stop editing before dispatching the action.
        TableActionEvent wrapper = new TableActionEvent() {
            @Override
            public void onEdit(int r) {
                // ensure editor is stopped first to avoid leaving editor state
                fireEditingStopped();
                if (event != null) event.onEdit(r);
            }

            @Override
            public void onDelete(int r) {
                fireEditingStopped();
                if (event != null) event.onDelete(r);
            }
            @Override
            public void onView(int r) {
                fireEditingStopped();
                if (event != null) event.onView(r);
            }
        };
        action.initEvent(wrapper, row);
        action.setBackground(table.getSelectionBackground());
        action.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        return action;
    }

    @Override
    public Object getCellEditorValue() {
        return ""; // Trả về chuỗi rỗng thay vì false của JCheckBox
    }
}