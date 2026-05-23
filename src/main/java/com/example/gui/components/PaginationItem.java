package com.example.gui.components;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.net.URL;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class PaginationItem extends JButton {
    private static final String STYLE_BASE = "arc: 10; borderWidth: 0; focusWidth: 0;";
    // Trong class PaginationItem.java, sửa lại các biến hằng số này:
    private static final String STYLE_NORMAL =
            STYLE_BASE +
            "background: #FFFFFF; foreground: #212529; hoverBackground: #E9ECEF; pressedBackground: #DEE2E6;";
    private static final String STYLE_SELECTED =
            STYLE_BASE +
            "background: #0F4C81; foreground: #FFFFFF; pressedBackground: #3E5C76;";
    private static final String STYLE_DISABLED =
            STYLE_BASE +
            "background: #F3F4F6; foreground: #9CA3AF;";

    private static final int ITEM_H = 34;
    private static final int MIN_W  = 34;
    private static final int TEXT_MIN_W = 44;
    private static final int TEXT_PAD = 16;

    private boolean selectedItem;

    public PaginationItem(String label) {
        super(label);
        init();
        setSelectedItem(false);
        applyTextSizingSafe();
    }

    public PaginationItem(ImageIcon icon) {
        super();
        init();
        ImageIcon black = tintIcon(icon, Color.BLACK);
        setIcon(black);
        // Ensure disabled state uses same visible icon
        setDisabledIcon(black);
        setText("");        // icon-only
        applyIconSizing();  // vuông
        setSelectedItem(false);
    }

    public PaginationItem(ImageIcon icon, int iconSize) {
        super();
        init();
        if (icon != null && iconSize > 0) {
            ImageIcon scaled = new ImageIcon(icon.getImage().getScaledInstance(iconSize, iconSize, java.awt.Image.SCALE_SMOOTH));
            ImageIcon black = tintIcon(scaled, Color.BLACK);
            setIcon(black);
            setDisabledIcon(black);
        } else {
            ImageIcon black = tintIcon(icon, Color.BLACK);
            setIcon(black);
            setDisabledIcon(black);
        }
        setText("");        // icon-only
        applyIconSizing();  // vuông
        setSelectedItem(false);
    }

    public PaginationItem(String iconPath, int iconSize) {
        super();
        init();
        ImageIcon icon = loadIcon(iconPath);
        if (icon != null && iconSize > 0) {
            ImageIcon scaled = new ImageIcon(icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
            ImageIcon black = tintIcon(scaled, Color.BLACK);
            setIcon(black);
            setDisabledIcon(black);
        } else {
            ImageIcon black = tintIcon(icon, Color.BLACK);
            setIcon(black);
            setDisabledIcon(black);
        }
        setText("");        // icon-only
        applyIconSizing();  // vuông
        setSelectedItem(false);
    }

    private ImageIcon tintIcon(ImageIcon src, Color color) {
        if (src == null) return null;
        Image img = src.getImage();
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0) return src;
        BufferedImage colored = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = colored.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(color);
        g.fillRect(0, 0, w, h);
        g.setComposite(AlphaComposite.DstIn);
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return new ImageIcon(colored);
    }

    private void init() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setMargin(new Insets(0, 0, 0, 0));
    }

    private void applyIconSizing() {
        Dimension d = new Dimension(MIN_W, ITEM_H);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    private Font resolveFontSafe() {
        Font f = getFont();
        if (f == null) f = UIManager.getFont("Button.font");
        return f;
    }

    private void applyTextSizingSafe() {
        if (getIcon() != null) return; // icon-only

        Font f = resolveFontSafe();
        // Có thể chưa có font ở giai đoạn init sớm, nên thoát an toàn.
        if (f == null) return;

        String t = getText();
        if (t == null) t = "";

        FontMetrics fm = getFontMetrics(f);
        int textW = fm.stringWidth(t);
        int w = Math.max(TEXT_MIN_W, textW + TEXT_PAD);

        Dimension d = new Dimension(w, ITEM_H);
        setPreferredSize(d);
        setMinimumSize(d);
        // không setMaximumSize cho text button
    }

    @Override
    public void addNotify() {
        super.addNotify();
        applyTextSizingSafe();
    }

    @Override
    public void setText(String text) {
        super.setText(text);

        // Tránh lỗi khi setText được gọi trước lúc component có font hợp lệ.
        if (getIcon() == null) {
            if (resolveFontSafe() != null) {
                applyTextSizingSafe();
            } else {
                SwingUtilities.invokeLater(this::applyTextSizingSafe);
            }
        }
    }

    private ImageIcon loadIcon(String path) {
        if (path == null || path.isBlank()) return null;
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Không tìm thấy icon tại: " + path);
            return null;
        }
        return new ImageIcon(url);
    }

    public boolean isSelectedItem() {
        return selectedItem;
    }

    public static int getTextMinWidth() {
        return TEXT_MIN_W;
    }

    public static int getItemHeight() {
        return ITEM_H;
    }

    public void setSelectedItem(boolean selected) {
        this.selectedItem = selected;
        applyStyle();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        applyStyle();
    }

    private void applyStyle() {
        // Ưu tiên style disabled; nếu đang enabled thì dùng style selected/normal.
        if (!isEnabled()) {
            putClientProperty("FlatLaf.style", STYLE_DISABLED);
            return;
        }
        putClientProperty("FlatLaf.style", selectedItem ? STYLE_SELECTED : STYLE_NORMAL);
    }
}