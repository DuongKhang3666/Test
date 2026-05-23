// package com.example.GUI.components;
// import javax.swing.*;
// import  java.awt.*;

// import com.example.GUI.components.Details;
// import com.example.GUI.components.TextBox;
// import com.formdev.flatlaf.FlatClientProperties;
// import com.formdev.flatlaf.FlatClientProperties;
// import javax.swing.*;
// import javax.swing.border.EmptyBorder;
// import java.awt.*;

// public class UpdateUI extends JFrame {

//     public UpdateUI() {
//         initComp();
//     }

//     private void initComp() {
//         // 1. Cấu hình Frame chính
//         setTitle("Cập nhật tài khoản");
//         setSize(800, 600);
//         setLocationRelativeTo(null);
//         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//         setResizable(false);

//         // Loại bỏ thanh tiêu đề mặc định của OS (nếu muốn giống ảnh 100%)
//         // setUndecorated(true);

//         // 2. Tạo Panel chính với màu nền Xanh đậm (giống ảnh)
//         JPanel mainPanel = new JPanel();
//         mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//         mainPanel.setBackground(new Color(65, 80, 148)); // Màu xanh tím giống ảnh
//         mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Padding xung quanh

//         // --- TIÊU ĐỀ ---
//         JPanel pHeader = new JPanel(new BorderLayout());
//         pHeader.setOpaque(false);
//         JLabel lbTitle = new JLabel("Cập nhật tài khoản");
//         lbTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
//         lbTitle.setForeground(Color.WHITE);
//         pHeader.add(lbTitle, BorderLayout.WEST);

//         // Nút đóng giả lập (nếu dùng undecorated) hoặc trang trí
// //        JLabel lbClose = new JLabel("X");
// //        lbClose.setForeground(Color.RED);
// //        lbClose.setFont(new Font("SansSerif", Font.BOLD, 16));
// //        lbClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
// //        pHeader.add(lbClose, BorderLayout.EAST);

//         mainPanel.add(pHeader);
//         mainPanel.add(Box.createVerticalStrut(20)); // Khoảng cách



//         // 1. Mã tài khoản (Dùng textBox thường)
//         TextBox txtMaTK = new TextBox();
//         txtMaTK.setText("1");
//         txtMaTK.setEditable(false); // Không cho sửa
//         txtMaTK.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");
//         mainPanel.add(new Details("Mã tài khoản:", txtMaTK));

//         // 2. Chủ tài khoản
//         TextBox txtChuTK = new TextBox();
//         txtChuTK.setText("Nguyễn Văn A");
//         txtChuTK.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");
//         mainPanel.add(new Details("Chủ tài khoản:", txtChuTK));

//         // 3. Tên đăng nhập
//         TextBox txtUser = new TextBox();
//         txtUser.setText("0123456789");
//         txtUser.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");
//         mainPanel.add(new Details("Tên đăng nhập:", txtUser));

//         // 4. Vai trò (Dùng component 15 - textBox chứa ComboBox)
//         JComboBox<String> cbbRole = new JComboBox<>(new String[]{"Admin", "User", "Manager"});
//         // Truyền ComboBox vào textBox bằng constructor component 15 bạn đã viết
//         TextBox txtRole = new TextBox("Chọn vai trò", cbbRole);
//         txtRole.setText("Admin"); // Hiển thị text
//         txtRole.setEditable(false); // Để nó hành xử như dropdown
//         mainPanel.add(new Details("Vai trò:", txtRole));

//         // 5. Trạng thái (Dùng component 15)
//         JComboBox<String> cbbStatus = new JComboBox<>(new String[]{"Hoạt động", "Bị khóa"});
//         TextBox txtStatus = new TextBox("Trạng thái", cbbStatus);
//         txtStatus.setText("Hoạt động");
//         txtStatus.setEditable(false);
//         mainPanel.add(new Details("Trạng Thái:", txtStatus));

//         // 6. Ngày tạo
//         TextBox txtDate = new TextBox();
//         txtDate.setText("18/1/2026 14:39:00");
//         txtDate.setEditable(false);
//         txtDate.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");
//         mainPanel.add(new Details("Ngày tạo:", txtDate));

//         mainPanel.add(Box.createVerticalStrut(30)); // Khoảng cách xuống nút

//         // --- BUTTONS (Hủy - Cập nhật) ---
//         JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//         pBtn.setOpaque(false);

//         JButton btnCancel = new JButton("Hủy");
//         styleButton(btnCancel, new Color(220, 53, 69)); // Màu đỏ

//         JButton btnUpdate = new JButton("Cập nhật");
//         styleButton(btnUpdate, new Color(40, 167, 69)); // Màu xanh lá

//         pBtn.add(btnCancel);
//         pBtn.add(btnUpdate);
//         mainPanel.add(pBtn);

//         // Add mainPanel vào Frame
//         add(mainPanel);
//     }

//     // Hàm phụ trợ để style nút cho gọn
//     private void styleButton(JButton btn, Color color) {
//         btn.setFont(new Font("SansSerif", Font.BOLD, 14));
//         btn.setForeground(Color.WHITE);
//         btn.setBackground(color);
//         btn.setPreferredSize(new Dimension(100, 35));
//         btn.setFocusPainted(false);
//         btn.setBorderPainted(false);
//         // FlatLaf style cho nút bo tròn
//         btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");
//     }

//     public static void main(String[] args) {
//         try {
//             UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
//         } catch (Exception ex) {
//             System.err.println("Failed to initialize FlatLaf");
//         }

//         SwingUtilities.invokeLater(() -> {
//             new UpdateUI().setVisible(true);
//         });
//     }
// }

package com.example.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import com.formdev.flatlaf.FlatClientProperties;

public class UpdateUI extends JDialog {

    private JPanel mainPanel;
    private JPanel fieldsPanel;
    private JPanel buttonPanel;
    private Map<String, JComponent> componentsMap;

    public UpdateUI(Frame owner, String title) {
        super(owner, title, true);
        this.componentsMap = new LinkedHashMap<>();
        initComp(title);
    }

    private void initComp(String title) {
        setUndecorated(true); 
        
        // Bo góc Dialog
        getRootPane().putClientProperty(FlatClientProperties.STYLE, "arc: 60;");

        mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // --- HEADER ---
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(false);

        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbTitle.setForeground(Color.decode("#111827"));
        pHeader.add(lbTitle, BorderLayout.WEST);

        JButton btnClose = new JButton("X");
        btnClose.setForeground(Color.GRAY);
        btnClose.setFont(new Font("SansSerif", Font.PLAIN, 20));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        pHeader.add(btnClose, BorderLayout.EAST);

        mainPanel.add(pHeader, BorderLayout.NORTH);

        // --- FIELDS PANEL ---
        fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);
        
        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        // --- BUTTONS ---
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public void addField(String label, String key, JComponent component) {
        // Cố định chiều dài form qua PreferredSize của component
        component.setPreferredSize(new Dimension(450, 35)); 
        component.setMaximumSize(new Dimension(450, 35)); 
        component.putClientProperty(FlatClientProperties.STYLE, "arc: 15; borderWidth: 0; focusWidth: 0;");
        
        // if (component instanceof JComboBox || component instanceof JTextField || component instanceof TextBox) {
        //      component.setBackground(Color.WHITE);
        // }

        Details fieldRow = new Details(label, component);
        fieldRow.setPreferredSize(new Dimension(600, 40));
        
        JLabel lb = fieldRow.getLabel();
        lb.setForeground(Color.BLACK);
        lb.setFont(new Font("SansSerif", Font.PLAIN, 14));

        fieldsPanel.add(fieldRow);
        fieldsPanel.add(Box.createVerticalStrut(10)); 
        
        componentsMap.put(key, component);
    }

    public void addButton(String text, Color bgColor, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setPreferredSize(new Dimension(120, 38));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 15;"); 
        
        if (action != null) btn.addActionListener(action);

        // Thêm khoảng cách giữa các nút với nhau
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(10, 0));
        buttonPanel.add(spacer);

        buttonPanel.add(btn);
    }

    // Ghi đè phương thức setVisible để gọi pack() ngay trước khi hiện
    @Override
    public void setVisible(boolean b) {
        if (b) {
            pack(); 
            setLocationRelativeTo(getOwner());
        }
        super.setVisible(b);
    }

    public JComponent getComponentByKey(String key) {
        return componentsMap.get(key);
    }
}