// package com.example.GUI.components;

// import javax.swing.*;
// import java.awt.*;

// public class AppTitle extends JFrame {
//     private static final Color HEADER_COLOR = Color.decode("#00388D");
//     private static final Color BODY_COLOR = Color.decode("#202020");

//     public AppTitle() {
//         initUI();
//     }

//     private void initUI() {
//         // 1. Cấu hình cửa sổ
//         setTitle("Hệ thống quản lý học sinh");
//         setSize(900, 600);
//         setDefaultCloseOperation(EXIT_ON_CLOSE);
//         setLocationRelativeTo(null);

//         // 2. Layout & Nội dung
//         setLayout(new BorderLayout());

//         getContentPane().setBackground(BODY_COLOR);

//         // 3. Cấu hình flatlaf CHO TITLE BAR (QUAN TRỌNG)
//         JRootPane root = getRootPane();
//         root.putClientProperty("JRootPane.titleBarBackground", HEADER_COLOR);
//         root.putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

//         // Cấu hình UIManager để đồng bộ màu sắc cho các dialog con (nếu có)
//         UIManager.put("TitlePane.background", HEADER_COLOR);
//         UIManager.put("TitlePane.foreground", Color.WHITE);
//         UIManager.put("TitlePane.buttonHoverBackground", new Color(255, 255, 255, 30));
//         UIManager.put("TitlePane.closeHoverBackground", Color.decode("#E81123"));
//     }

//     // Hàm tiện ích: Để cho nội dung (dashboard, form...) vào cửa sổ này
//     public void setMainContent(JComponent component) {
//         getContentPane().removeAll();
//         add(component, BorderLayout.CENTER);
//         revalidate();
//         repaint();
//     }

//     public static void main(String[] args) {
//         SwingUtilities.invokeLater(() -> {
//             AppTitle appTitle = new AppTitle();
//             appTitle.setVisible(true);
//         });
//     }

// }

// LÀM MỚI

package com.example.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class AppTitle extends JPanel {
    private final Color HEADER_COLOR = Color.decode("#0F172A");
    private final Color HOVER_COLOR = Color.decode("#4d5aa0");
    private final Color HOVER_CLOSE_COLOR = Color.decode("#ED1D26");
    private JFrame frameParent; // Tham chiếu đến cửa sổ cha để thực hiện điều khiển (minimize, maximize, close)

    public AppTitle(JFrame frameParent) {
        this.frameParent = frameParent;
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(10, 0));
        this.setBackground(HEADER_COLOR);
        this.setPreferredSize(new Dimension(1440, 25));

        // Panel bên trái - Title
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new FlowLayout());
        leftPanel.setBackground(HEADER_COLOR);
        leftPanel.setBorder(new EmptyBorder(0, 30, 0, 30));
        
        JLabel lbTitle = new JLabel("Hệ thống quản lý tuyển sinh");
        lbTitle.setForeground(Color.decode("#FFFFFF"));
        lbTitle.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
        leftPanel.add(lbTitle, FlowLayout.LEFT);
        
        this.add(leftPanel, BorderLayout.WEST);

        // Panel bên phải - Buttons
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(HEADER_COLOR);

        // Các nút chức năng với icon
        String[] iconPaths = {
            "/assets/icons/minimize-sign.png",
            "/assets/icons/maximize.png",
            "/assets/icons/close.png"
        };
        String[] buttonNames = { "Minimize", "Maximize", "Close" };

        for(int i = 0; i < iconPaths.length; i++) {
            final int index = i;
            JButton btn = new JButton();
            btn.setName(buttonNames[i]);
            btn.setPreferredSize(new Dimension(35, this.getPreferredSize().height));
            revalidate();
            repaint();
            
            try {
                URL iconURL = getClass().getResource(iconPaths[i]);
                if (iconURL != null) {
                    ImageIcon icon = new ImageIcon(iconURL);
                    java.awt.Image scaledImage = icon.getImage().getScaledInstance(12, 12, java.awt.Image.SCALE_SMOOTH);
                    btn.setIcon(new ImageIcon(scaledImage));
                } else {
                    btn.setText(i == 0 ? "−" : (i == 1 ? "□" : "X"));
                    btn.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
                    btn.setForeground(Color.WHITE); 
                }
            } catch (Exception e) {
                btn.setText(i == 0 ? "−" : (i == 1 ? "□" : "X"));
                btn.setForeground(Color.WHITE);
            }
            
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Thêm hover effect
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (index == 2) {
                        btn.setBackground(HOVER_CLOSE_COLOR);
                    } else {
                        btn.setBackground(HOVER_COLOR);
                    }
                    btn.setContentAreaFilled(true); // BẬT vẽ nền khi di chuột vào
                    btn.setOpaque(true);
                    btn.repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setContentAreaFilled(false); // TẮT vẽ nền khi chuột rời đi
                    btn.setOpaque(false);
                    btn.repaint();
                }
            });
            
            // Thêm sự kiện click
            btn.addActionListener(e -> handleButtonClick(index));
            
            rightPanel.add(btn, i == 0 ? BorderLayout.WEST : (i == 1 ? BorderLayout.CENTER : BorderLayout.EAST));
        }
        
        this.add(rightPanel, BorderLayout.EAST);
    }

    private void handleButtonClick(int buttonIndex) {
        if (frameParent == null) return;
        
        switch(buttonIndex) {
            case 0: // Minimize
                frameParent.setState(JFrame.ICONIFIED);
                break;
            case 1: // Maximize
                if ((frameParent.getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
                    frameParent.setExtendedState(JFrame.MAXIMIZED_BOTH);
                } else {
                    frameParent.setExtendedState(JFrame.NORMAL);
                }
                break;
            case 2: // Close
                frameParent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frameParent.dispose();
                break;
        }
    }

}
