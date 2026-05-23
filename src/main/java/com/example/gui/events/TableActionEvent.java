package com.example.gui.events;

public interface TableActionEvent {
    void onView(int row);
    void onEdit(int row);
    void onDelete(int row);
}