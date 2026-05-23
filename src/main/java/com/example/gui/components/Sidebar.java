package com.example.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.example.dto.AccountDTO;
import com.example.utils.SessionManager;

public class Sidebar extends JPanel {
    private final Color SELECTED_COLOR = Color.decode("#1E293B"); // Màu xanh nền khi được chọn
    private final Color BACKGROUND_COLOR = Color.decode("#0F172A"); // Màu nền Sidebar (Xanh đen)
    private static final String DEFAULT_AVATAR = "/assets/icons/user.png"; 
    
    private AccountDTO currentUser;
    private Map<String, NormalItem> mapNameToItem = new HashMap<>();
    private JPanel topPanel;
    private Consumer<String> onMenuSelected;

    public Sidebar(AccountDTO user) {
        this(user, null);
    }

    public Sidebar(AccountDTO user, Consumer<String> onMenuSelected) {
        this.currentUser = user;
        this.onMenuSelected = onMenuSelected;

        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(240, 0));

        // --- PHẦN TOP (CENTER) ---
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.setBorder(new EmptyBorder(15, 10, 10, 10));

        // THÊM CÁC MENU ITEM (Đã cập nhật mục Aspirations đồng bộ)
        addMenuItem("User Management", "/assets/icons/users.png");
        addMenuItem("Candidates", "/assets/icons/candidates.png");
        addMenuItem("Aspirations", "/assets/icons/files.png");
        addMenuItem("Candidate Stats", "/assets/icons/statistics.png");
        addMenuItem("Bonus Scores", "/assets/icons/bonusscores.png");
        addMenuItem("Score Management", "/assets/icons/scores.png"); 
        addMenuItem("Majors & Combinations", "/assets/icons/majors.png");
        addMenuItem("Conversion Tables", "/assets/icons/conversion.png");
        addMenuItem("Admission Process", "/assets/icons/process.png");

        // --- THÊM SCROLLBAR ---
        JScrollPane scrollPane = new JScrollPane(topPanel);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Tùy chỉnh thanh cuộn
        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        vBar.putClientProperty("ScrollBar.width", 6);
        vBar.putClientProperty("ScrollBar.thumbArc", 999);
        vBar.putClientProperty("ScrollBar.track", BACKGROUND_COLOR);
        vBar.putClientProperty("ScrollBar.showButtons", false);

        this.add(scrollPane, BorderLayout.CENTER);

        // --- PHẦN BOTTOM (USER & LOGOUT) ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.putClientProperty("FlatLaf.style", "background: #0F172A");
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(10, 8, 10, 8));

        // -- Ảnh và tên đăng nhập --
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        userPanel.setOpaque(false);
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // -- Ảnh (Dùng JLabel thường thay cho ImageAvatar bị lỗi) --
        JLabel avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(35, 35));
        avatar.setMinimumSize(new Dimension(35, 35));
        avatar.setMaximumSize(new Dimension(35, 35));
        avatar.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Tải avatar
        ImageIcon icon = tryLoadIcon(DEFAULT_AVATAR);
        if (icon != null) {
            java.awt.Image img = icon.getImage().getScaledInstance(35, 35, java.awt.Image.SCALE_SMOOTH);
            avatar.setIcon(new ImageIcon(img));
        } else {
            // Nếu không có ảnh, để biểu tượng mặc định
            avatar.setText("👤");
            avatar.setForeground(Color.WHITE);
            avatar.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        }
    
        // -- Tên đăng nhập --
        String displayName = (currentUser != null && currentUser.getUsername() != null) 
                                ? currentUser.getUsername() 
                                : "Admin Profile";

        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        userPanel.add(avatar);
        userPanel.add(nameLabel);

        // -- Đường kẻ --
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setBackground(new Color(255, 255, 255, 40)); // Kẻ mờ
        separator.setOpaque(true);
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        // -- Nút đăng xuất (Dùng NormalItem thay vì LogoutItem bị thiếu) --
        NormalItem logoutItem = new NormalItem("Logout", "/assets/icons/logout.png");
        logoutItem.setMargin(new java.awt.Insets(0, 0, 0, 0));
        logoutItem.setIconTextGap(10);
        logoutItem.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        // Ghi đè màu đỏ khi hover cho nút Logout
        logoutItem.putClientProperty("FlatLaf.style", 
            "arc: 15; background: null; foreground: #FFFFFF; " +
            "hoverBackground: #C92A2A; pressedBackground: #A61E1E; " + 
            "borderWidth: 0; focusWidth: 0; margin: 4,12,4,12;"
        );
        logoutItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, logoutItem.getPreferredSize().height));
        logoutItem.setMinimumSize(new Dimension(0, logoutItem.getPreferredSize().height));
        logoutItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutItem.setBorder(new EmptyBorder(0, 0, 0, 0));

        logoutItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Window currentWindow = SwingUtilities.getWindowAncestor(Sidebar.this);

                // 1. Hiện bảng hỏi xác nhận cho chuyên nghiệp
                int answer = JOptionPane.showConfirmDialog(
                        currentWindow,
                        "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?",
                        "Xác nhận đăng xuất",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                // 2. Nếu người dùng chọn YES
                if (answer == JOptionPane.YES_OPTION) {
                    SessionManager.logout();

                    // Lấy cái cửa sổ JFrame (MainLayout) đang chứa cái Sidebar này và tắt nó đi
                    if (currentWindow != null) {
                        currentWindow.dispose();
                    }

                    // 3. Khởi tạo lại AccountBUS và bật trang LoginFrame lên
                    com.example.bus.AccountBUS accountBUS = new com.example.bus.AccountBUS();
                    com.example.gui.LoginFrame loginFrame = new com.example.gui.LoginFrame(accountBUS);
                    loginFrame.setVisible(true);
                }
            }
        });

        // Lắp ghép các thành phần vào bottomPanel
        bottomPanel.add(userPanel);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(separator);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(logoutItem);

        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    // Hàm hỗ trợ thêm Menu Item bằng tay rất tiện lợi
    private void addMenuItem(String name, String iconPath) {
        NormalItem item = new NormalItem(name, iconPath);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, item.getPreferredSize().height));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        mapNameToItem.put(name, item);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                setSelectedMenu(name);
                if (onMenuSelected != null) {
                    onMenuSelected.accept(name);
                }
                System.out.println("Chuyển sang trang: " + name);
            }
        });
        
        topPanel.add(item);
        topPanel.add(Box.createVerticalStrut(5)); // Khoảng cách giữa các menu
    }

    public void setSelectedMenu(String menuName) {
        /* Duyệt và reset màu của các item khác */
        for (Map.Entry<String, NormalItem> entry : mapNameToItem.entrySet()) {
            NormalItem item = entry.getValue();
            item.setBackground(BACKGROUND_COLOR);
        }

        /* Set màu cho item được chọn */
        NormalItem item = mapNameToItem.get(menuName);
        if (item != null) {
            item.setBackground(SELECTED_COLOR);
        }
    }

    private ImageIcon tryLoadIcon(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) return null;
        String p = resourcePath.trim();

        if (p.startsWith("/")) {
            URL url = getClass().getResource(p);
            if (url == null) return null;
            return new ImageIcon(url);
        }

        File f = new File(p);
        if (f.exists() && f.isFile()) {
            return new ImageIcon(p);
        }

        return null;
    }
}