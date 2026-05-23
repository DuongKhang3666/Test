package com.example.gui.components;

import java.awt.Color;
import java.awt.Font;

public class AppTheme {
    // Bảng màu chuẩn
    public static final Color PRIMARY = Color.decode("#0F4C81");    // Xanh dương đậm (Header, Nút chính)
    public static final Color SECONDARY = Color.decode("#3E5C76");  // Xanh xám (Nút phụ, Hover)
    public static final Color TERTIARY = Color.decode("#743B00");   // Nâu cam (Nút Edit/Warning)
    public static final Color NEUTRAL = Color.decode("#F8F9FA");    // Xám nhạt (Màu nền hệ thống)
    public static final Color DANGER = Color.decode("#C92A2A");     // Đỏ (Nút Delete - lấy chuẩn FlatLaf)
    public static final Color TEXT_DARK = Color.decode("#212529");  // Chữ màu tối
    
    // Font chữ chuẩn (Public Sans hoặc Segoe UI làm fallback)
    public static final Font FONT_HEADLINE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
}