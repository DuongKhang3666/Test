package com.example.gui.pages.aspiration;

import com.example.bus.AdmissionProcessBUS;
import com.example.bus.AspirationBUS;
import com.example.dto.AspirationDTO;
import com.example.gui.components.ImportPanel;
import com.example.gui.components.OverlayUtil;
import java.io.File;
import javax.swing.*;
import java.awt.*;

public class AspirationManagement extends JPanel {
    private final AspirationListPanel aspirationListPanel;

    public AspirationManagement() {
        setLayout(new BorderLayout());

        // Gắn danh sách danh mục cố định tại khu vực trung tâm
        aspirationListPanel = new AspirationListPanel(this);
        add(aspirationListPanel, BorderLayout.CENTER);
    }

    public void showCreateOverlay() {
        JFrame owner = getOwnerFrame();
        if (owner == null) {
            return;
        }

        AspirationCreatePanel createPanel = new AspirationCreatePanel(created -> {
            JOptionPane.showMessageDialog(this, "Đã thêm nguyện vọng thành công cho thí sinh CCCD: " + created.getNnCccd());
            aspirationListPanel.refreshData();
            OverlayUtil.hideOverlay(owner);
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Thêm nguyện vọng xét tuyển", createPanel, new Dimension(850, 520));
    }

    public void showEditOverlay(AspirationDTO aspiration) {
        JFrame owner = getOwnerFrame();
        if (owner == null || aspiration == null) {
            return;
        }
        int aspirationId = aspiration.getIdnv();

        AspirationEditPanel editPanel = new AspirationEditPanel(aspiration, updated -> {
            JOptionPane.showMessageDialog(this, "Đã cập nhật thành công nguyện vọng #" + aspirationId);
            aspirationListPanel.refreshData();
            OverlayUtil.hideOverlay(owner);
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Chỉnh sửa nguyện vọng #" + aspirationId, editPanel, new Dimension(850, 520));
    }

    public void showImportOverlay() {
        JFrame owner = getOwnerFrame();
        if (owner == null) return;

        // Khởi tạo ImportPanel với 2 hàm callback: 1 cho nút OK (onImport), 1 cho nút Hủy (onCancel)
        ImportPanel importPanel = new ImportPanel(
            (File selectedFile) -> {
                AspirationBUS bus = new AspirationBUS();
                
                // Vô hiệu hóa chuột và đổi sang biểu tượng loading
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Chạy ngầm tiến trình đọc file Excel để không làm đơ giao diện
                SwingWorker<AspirationBUS.ImportResult, Void> worker = new SwingWorker<>() {
                    @Override
                    protected AspirationBUS.ImportResult doInBackground() {
                        // Hàm này tự động lọc ra CCCD, Mã Ngành, Thứ tự và skip các cột khác
                        return bus.importAspirationsFromExcel(selectedFile);
                    }

                    @Override
                    protected void done() {
                        setCursor(Cursor.getDefaultCursor());
                        try {
                            AspirationBUS.ImportResult result = get();
                            String msg = String.format("Nhập dữ liệu thành công!\n- Đã thêm: %d nguyện vọng\n- Bỏ qua/Lỗi: %d dòng", 
                                    result.getSuccessCount(), result.getErrorCount());
                                    
                            if(result.getSuccessCount() > 0) {
                                JOptionPane.showMessageDialog(AspirationManagement.this, msg, "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(AspirationManagement.this, msg + "\n(Hãy kiểm tra lại cấu trúc file Excel)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                            }
                            
                            aspirationListPanel.refreshData();
                            syncAdmissionResults(owner);
                            OverlayUtil.hideOverlay(owner);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(AspirationManagement.this, "Lỗi đọc file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            },
            () -> OverlayUtil.hideOverlay(owner) // Nút Hủy
        );

        OverlayUtil.showOverlay(owner, "Nhập Nguyện Vọng Từ Excel", importPanel, new Dimension(700, 450));
    }

    private void syncAdmissionResults(JFrame owner) {
        SwingWorker<Void, Void> syncWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                new AdmissionProcessBUS().thucHienXetTuyen();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    aspirationListPanel.refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            AspirationManagement.this,
                            "Đã import nguyện vọng nhưng đồng bộ xét tuyển thất bại: " + ex.getMessage(),
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        };
        syncWorker.execute();
    }

    public void showList() {
        revalidate();
        repaint();
    }

    private JFrame getOwnerFrame() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        return (window instanceof JFrame) ? (JFrame) window : null;
    }
}