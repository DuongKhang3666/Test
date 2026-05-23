package com.example.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Utility để hiển thị overlay modal trên GlassPane
 * Nền mờ + card trắng ở giữa chứa nội dung form/dialog
 */
public class OverlayUtil {

    /**
     * Hiển thị overlay có title
     */
    public static void showOverlay(JFrame owner, String title, JComponent content, Dimension cardSize) {
        // Dùng JLayeredPane để tách dim layer và card layer
        JLayeredPane glass = new JLayeredPane();
        glass.setOpaque(false);

        // Nền mờ full screen
        JPanel dim = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dim.setOpaque(false);
        dim.setLayout(null);

        // Card trắng chứa nội dung
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        if (cardSize != null) {
            card.setPreferredSize(cardSize);
        }

        // Header với title và nút đóng
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)),
            BorderFactory.createEmptyBorder(0, 16, 0, 12)
        ));
        header.setPreferredSize(new Dimension(0, 40));

        // Title label
        if (title != null && !title.trim().isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(Color.decode("#333333"));
            header.add(titleLabel, BorderLayout.WEST);
        }

        // Close button
        JButton closeBtn = new JButton("x");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 16));
        closeBtn.setForeground(new Color(200, 60, 60));
        closeBtn.setBackground(Color.WHITE);
        closeBtn.setBorder(BorderFactory.createEmptyBorder());
        closeBtn.setFocusPainted(false);
        closeBtn.setPreferredSize(new Dimension(32, 32));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> hideOverlay(owner));
        header.add(closeBtn, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        // Add layers to glass (dim dưới, card trên top)
        glass.add(dim, Integer.valueOf(0));
        glass.add(card, Integer.valueOf(1));

        // Click vào dim (ngoài card) để đóng
        dim.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                hideOverlay(owner);
            }
        });

        // Consume click trên card để không bubble lên dim
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }
        });

        // ESC key để đóng
        glass.registerKeyboardAction(
            e -> hideOverlay(owner),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Layout handler khi frame resize
        ComponentAdapter layoutHandler = new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                dim.setBounds(0, 0, glass.getWidth(), glass.getHeight());
                
                // Center card
                if (cardSize != null) {
                    int x = (glass.getWidth() - cardSize.width) / 2;
                    int y = (glass.getHeight() - cardSize.height) / 2;
                    card.setBounds(x, y, cardSize.width, cardSize.height);
                } else {
                    int x = (glass.getWidth() - card.getPreferredSize().width) / 2;
                    int y = (glass.getHeight() - card.getPreferredSize().height) / 2;
                    card.setBounds(x, y, card.getPreferredSize().width, card.getPreferredSize().height);
                }
            }
        };
        glass.addComponentListener(layoutHandler);

        // Set GlassPane và hiển thị
        owner.getRootPane().setGlassPane(glass);
        glass.setVisible(true);

        // Trigger layout
        glass.setSize(owner.getWidth(), owner.getHeight());
        layoutHandler.componentResized(null);

        // Validate card để layout header/content đúng cách
        card.validate();
        card.repaint();
        glass.validate();
        glass.repaint();

        // Focus vào card để keyboard events hoạt động
        card.requestFocusInWindow();
    }

    /**
     * Hiển thị overlay với kích thước card tùy chỉnh
     * @param owner JFrame cha
     * @param content JComponent nội dung (form, panel,...)
     * @param cardSize kích thước card, nếu null thì auto-size
     */
    public static void showOverlay(JFrame owner, JComponent content, Dimension cardSize) {
        showOverlay(owner, null, content, cardSize);
    }

    /**
     * Hiển thị overlay với kích thước mặc định
     */
    public static void showOverlay(JFrame owner, JComponent content) {
        showOverlay(owner, content, null);
    }

    /**
     * Ẩn overlay
     */
    public static void hideOverlay(JFrame owner) {
        Component gp = owner.getRootPane().getGlassPane();
        gp.setVisible(false);
        owner.getRootPane().setGlassPane(new JPanel());
    }
}
