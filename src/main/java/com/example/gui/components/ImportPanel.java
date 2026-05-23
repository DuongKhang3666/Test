package com.example.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.net.URL;

/**
 * Panel để import file Excel (drag & drop hoặc chọn file)
 * Tối đa 1 file, toggle giữa upload mode và selected file mode
 */
public class ImportPanel extends JPanel {
    private File selectedFile = null;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JLabel lblSelectedFileName;
    JButton btnStart = new JButton("OK");

    public ImportPanel(Consumer<File> onImport, Runnable onCancel) {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F8F9FA"));

        // Center - Toggle between upload zone and file selected view
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);
        
        mainPanel.add(createUploadZone(), "UPLOAD");
        mainPanel.add(createFileSelectedView(), "SELECTED");
        
        add(mainPanel, BorderLayout.CENTER);

        // Footer - Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        
        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 36));
        btnCancel.setForeground(Color.BLUE);
        btnCancel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> onCancel.run());

        btnStart.setEnabled(false);
        btnStart.setPreferredSize(new Dimension(120, 36));
        btnStart.setBackground(Color.decode("#0066CC"));
        btnStart.setForeground(Color.WHITE);
        btnStart.setOpaque(true);
        btnStart.setBorderPainted(false);
        btnStart.setFocusPainted(false);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.addActionListener(e -> {
            if (selectedFile != null) {
                onImport.accept(selectedFile);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn file để tiếp tục.", 
                    "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnStart);
        add(footer, BorderLayout.SOUTH);
    }

    public ImportPanel(String string) {
        //TODO Auto-generated constructor stub
    }

    private JPanel createUploadZone() {
        JPanel zone = new JPanel(new GridBagLayout());
        zone.setBackground(new Color(250, 250, 250));
        zone.setBorder(BorderFactory.createDashedBorder(new Color(180, 180, 180), 2, 6, 2, true));
        zone.setPreferredSize(new Dimension(650, 280));

        // Content
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        JLabel icon = new JLabel();
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        
        // FIX LỖI: Check Null an toàn khi load Ảnh
        URL iconUrl = getClass().getResource("/assets/images/upload_file.png");
        if (iconUrl != null) {
            ImageIcon imgIcon = new ImageIcon(iconUrl);
            Image img = imgIcon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            icon.setIcon(new ImageIcon(img));
        } else {
            icon.setText("📊");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48)); // Đổi sang font Emoji
        }

        JLabel lblMain = new JLabel("<html><b>Kéo và thả file Excel tại đây</b></html>");
        lblMain.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMain.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblSub = new JLabel("hoặc nhấn để chọn từ máy tính");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(120, 120, 120));
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);

        content.add(icon, BorderLayout.NORTH);
        content.add(lblMain, BorderLayout.CENTER);
        content.add(lblSub, BorderLayout.SOUTH);

        zone.add(content);

        // Mouse click to browse
        zone.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectFileFromDialog();
            }
        });

        // Drag & drop
        new DropTarget(zone, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) dtde.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File file = files.get(0);
                        if (isValidExcelFile(file)) {
                            selectFile(file);
                        } else {
                            JOptionPane.showMessageDialog(zone, 
                                "Vui lòng upload file Excel (.xls, .xlsx)!", 
                                "Invalid File", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return zone;
    }

    private JPanel createFileSelectedView() {
        JPanel view = new JPanel(new GridBagLayout());
        view.setOpaque(false);

        // Container
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(true);
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        container.setPreferredSize(new Dimension(600, 220));

        JLabel lblTitle = new JLabel("File đã tải lên:");
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(new Color(120, 120, 120));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitle.setBorder(new EmptyBorder(12, 18, 6, 0));

        JPanel fileRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        fileRow.setOpaque(false);
        fileRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileRow.setBorder(new EmptyBorder(0, 18, 0, 18));

        // Icon (left)
        JLabel icon = new JLabel();
        try {
            ImageIcon imgIcon = new ImageIcon(getClass().getResource("/assets/images/files.png"));
            java.awt.Image img = imgIcon.getImage().getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
            icon.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            icon.setText("📄");
            icon.setFont(new Font("Arial", Font.BOLD, 36));
        }
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(50, 50));

        lblSelectedFileName = new JLabel("");
        lblSelectedFileName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectedFileName.setForeground(Color.decode("#333333"));

        // File row: icon and file name on the same line
        fileRow.add(icon);
        fileRow.add(lblSelectedFileName);

        JPanel fileSection = new JPanel();
        fileSection.setLayout(new BoxLayout(fileSection, BoxLayout.Y_AXIS));
        fileSection.setOpaque(false);
        fileSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileSection.add(lblTitle);
        fileSection.add(Box.createVerticalStrut(4));
        fileSection.add(fileRow);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JPanel buttonsWrapper = new JPanel(new BorderLayout());
        buttonsWrapper.setOpaque(false);
        buttonsWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonsWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        buttonsWrapper.setBorder(new EmptyBorder(16, 0, 0, 12));

        JButton btnRemove = new JButton("Xóa file");
        btnRemove.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnRemove.setBorder(BorderFactory.createLineBorder(Color.RED));
        btnRemove.setForeground(new Color(200, 60, 60));
        btnRemove.setPreferredSize(new Dimension(80, 32));
        btnRemove.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRemove.addActionListener(e -> clearFile());

        JButton btnChangeFile = new JButton("Chọn file khác");
        btnChangeFile.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnChangeFile.setPreferredSize(new Dimension(110, 32));
        btnChangeFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChangeFile.addActionListener(e -> selectFileFromDialog());

        buttons.add(btnRemove);
        buttons.add(btnChangeFile);
        buttonsWrapper.add(buttons, BorderLayout.EAST);

        container.add(Box.createVerticalStrut(6));
        container.add(fileSection);
        container.add(Box.createVerticalStrut(10));
        container.add(buttonsWrapper);
        container.add(Box.createVerticalStrut(4));

        view.add(container);
        return view;
    }

    private void selectFile(File file) {
        selectedFile = file;
        lblSelectedFileName.setText(file.getName());
        btnStart.setEnabled(true);
        cardLayout.show(mainPanel, "SELECTED");
    }

    private void selectFileFromDialog() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Excel files (*.xls, *.xlsx)", "xls", "xlsx"));
        fc.setAcceptAllFileFilterUsed(false);
        int ok = fc.showOpenDialog(this);
        if (ok == JFileChooser.APPROVE_OPTION) {
            selectFile(fc.getSelectedFile());
        }
    }

    private void clearFile() {
        btnStart.setEnabled(false);
        selectedFile = null;
        cardLayout.show(mainPanel, "UPLOAD");
    }

    private boolean isValidExcelFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String name = file.getName().toLowerCase();
        return name.endsWith(".xls") || name.endsWith(".xlsx");
    }

    public java.io.File getSelectedFile() {
        return this.selectedFile; // hoặc tên biến lưu trữ đối tượng File trong class ImportPanel của bạn
    }
}
