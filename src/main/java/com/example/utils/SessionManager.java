package com.example.utils;

import com.example.dto.AccountDTO;

public class SessionManager {
    // Biến lưu trữ người dùng đang đăng nhập
    private static AccountDTO currentUser;

    // Lưu user khi đăng nhập thành công
    public static void login(AccountDTO account) {
        currentUser = account;
    }

    // Lấy thông tin user hiện tại ra xài
    public static AccountDTO getCurrentUser() {
        return currentUser;
    }

    // Kiểm tra xem có phải Admin không
    public static boolean isAdmin() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }

    // Xóa session khi đăng xuất
    public static void logout() {
        currentUser = null;
    }
}