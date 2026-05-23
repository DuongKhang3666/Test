package com.example;

import com.example.bus.AccountBUS;
import com.example.gui.LoginFrame;
import com.example.utils.HibernateUtil;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::shutdown));

        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); 
        } catch (Exception ex) {
            System.err.println("Lỗi khởi tạo FlatLaf. App sẽ chạy với giao diện cũ: " + ex.getMessage());
        }

        AccountBUS accountBUS = new AccountBUS();
        accountBUS.ensureDefaultAdmin();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(accountBUS);
            loginFrame.setVisible(true);
        });
    }
}