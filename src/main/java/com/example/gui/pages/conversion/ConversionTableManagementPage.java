package com.example.gui.pages.conversion;

import com.example.bus.AdmissionProcessBUS;
import com.example.dto.ConversionTableDTO;
import com.example.gui.components.ImportPanel;
import com.example.gui.components.OverlayUtil;
import javax.swing.*;
import java.awt.*;

import com.example.bus.ConversionTableBUS;

public class ConversionTableManagementPage extends JPanel {
    private ConversionTableListPage listPage;
    private final ConversionTableBUS conversionTableBUS = new ConversionTableBUS();

    public ConversionTableManagementPage() {
        setLayout(new BorderLayout());
        setOpaque(false);

        listPage = new ConversionTableListPage(this);
        add(listPage, BorderLayout.CENTER);
    }

    private JFrame getOwnerFrame() {
        Window window = SwingUtilities.getWindowAncestor(this);
        return (window instanceof JFrame) ? (JFrame) window : null;
    }

    public void showList() {
        listPage.refresh();
    }

    public void showCreate() {
        JFrame owner = getOwnerFrame();
        if (owner == null) return;

        OverlayUtil.hideOverlay(owner);
        ConversionTableCreatePage createPage = new ConversionTableCreatePage(() -> {
            OverlayUtil.hideOverlay(owner);
            showList();
        });

        OverlayUtil.showOverlay(owner, "Thêm mức quy đổi mới", createPage, new Dimension(800, 550));
    }

    public void showEdit(ConversionTableDTO dto) {
        JFrame owner = getOwnerFrame();
        if (owner == null) return;

        OverlayUtil.hideOverlay(owner);
        ConversionTableEditPage editPage = new ConversionTableEditPage(dto, () -> {
            OverlayUtil.hideOverlay(owner);
            showList();
        });

        OverlayUtil.showOverlay(owner, "Chỉnh sửa mức quy đổi", editPage, new Dimension(800, 550));
    }

    public void showImportOverlay() {
        JFrame owner = getOwnerFrame();
        if (owner == null) return;

        ImportPanel importPanel = new ImportPanel(file -> {
            Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            owner.setCursor(waitCursor);
            owner.getRootPane().setCursor(waitCursor);
            owner.getRootPane().getGlassPane().setCursor(waitCursor);

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return conversionTableBUS.importFromExcelFile(file);
                }

                @Override
                protected void done() {
                    Cursor defaultCursor = Cursor.getDefaultCursor();
                    owner.setCursor(defaultCursor);
                    owner.getRootPane().setCursor(defaultCursor);
                    owner.getRootPane().getGlassPane().setCursor(defaultCursor);
                    try {
                        String resultMessage = get();
                        JOptionPane.showMessageDialog(
                                ConversionTableManagementPage.this,
                                resultMessage,
                                "Kết quả Import",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        showList();
                        syncAdmissionResults(owner);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ConversionTableManagementPage.this,
                                "Lỗi khi import file: " + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                    } finally {
                        OverlayUtil.hideOverlay(owner);
                    }
                }
            };
            worker.execute();
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Nhập bảng quy đổi từ Excel", importPanel, new Dimension(750, 380));
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
                    showList();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ConversionTableManagementPage.this,
                            "Đã import bảng quy đổi nhưng đồng bộ xét tuyển thất bại: " + ex.getMessage(),
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        };
        syncWorker.execute();
    }
}