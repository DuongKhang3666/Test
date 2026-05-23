package com.example.gui.pages.bonusscore;

import com.example.bus.AdmissionProcessBUS;
import com.example.bus.BonusScoreBUS;
import com.example.dto.BonusScoreDTO;
import com.example.gui.components.ImportPanel;
import com.example.gui.components.OverlayUtil;

import javax.swing.*;
import java.awt.*;

public class BonusScoreManagement extends JPanel {
    private final BonusScoreListPanel bonusScoreListPanel;

    BonusScoreBUS bonusScoreBUS = new BonusScoreBUS();

    public BonusScoreManagement() {
        setLayout(new BorderLayout());

        bonusScoreListPanel = new BonusScoreListPanel(this);
        add(bonusScoreListPanel, BorderLayout.CENTER);
    }

    public void showCreateOverlay() {
        JFrame owner = getOwnerFrame();
        if (owner == null) {
            return;
        }

        BonusScoreCreatePanel createPanel = new BonusScoreCreatePanel(bonusScore -> {
            JOptionPane.showMessageDialog(this, "Đã thêm điểm cộng thành công cho CCCD: " + bonusScore.getTsCccd());
            bonusScoreListPanel.refreshData();
            OverlayUtil.hideOverlay(owner);
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Thêm điểm cộng", createPanel, new Dimension(900, 620));
    }

    public void showEditOverlay(BonusScoreDTO bonusScore) {
        JFrame owner = getOwnerFrame();
        if (owner == null || bonusScore == null) {
            return;
        }

        BonusScoreEditPanel editPanel = new BonusScoreEditPanel(bonusScore, updated -> {
            JOptionPane.showMessageDialog(this, "Đã cập nhật điểm cộng thành công cho CCCD: " + updated.getTsCccd());
            bonusScoreListPanel.refreshData();
            OverlayUtil.hideOverlay(owner);
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Chỉnh sửa điểm cộng thí sinh", editPanel, new Dimension(900, 620));
    }

    public void showDetailOverlay(BonusScoreDTO bonusScore) {
        JFrame owner = getOwnerFrame();
        if (owner == null || bonusScore == null) {
            return;
        }

        String bonusScoreCccd = bonusScore.getTsCccd();

        BonusScoreDetailPanel detailPanel = new BonusScoreDetailPanel(bonusScore, () -> OverlayUtil.hideOverlay(owner));
        OverlayUtil.showOverlay(owner, "Chi tiết điểm cộng thí sinh với CCCD " + bonusScoreCccd, detailPanel, new Dimension(900, 620));
    }

    public void showDeleteOverlay(BonusScoreDTO bonusScore) {
        JFrame owner = getOwnerFrame();
        if (owner == null || bonusScore == null) {
            return;
        }

        String bonusScoreCccd = bonusScore.getTsCccd();

        BonusScoreDeletePanel deletePanel = new BonusScoreDeletePanel(bonusScore, toDelete -> {
            try {
                bonusScoreBUS.deleteBonusScore(toDelete);
                JOptionPane.showMessageDialog(this, "Đã xóa điểm cộng thành công cho thí sinh có CCCD: " + bonusScoreCccd);
                bonusScoreListPanel.refreshData();
                OverlayUtil.hideOverlay(owner);
            } catch (BonusScoreBUS.BonusScoreValidationException vex) {
                JOptionPane.showMessageDialog(this, vex.getMessageText(), "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Xóa thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, deletePanel, new Dimension(900, 620));
    }

    public void showImportOverlay() {
        JFrame owner = getOwnerFrame();
        if (owner == null) {
            return;
        }

        ImportPanel importPanel = new ImportPanel(file -> {
            Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            owner.setCursor(waitCursor);
            owner.getRootPane().setCursor(waitCursor);
            owner.getRootPane().getGlassPane().setCursor(waitCursor);

            SwingWorker<BonusScoreBUS.ImportResult, Void> worker = new SwingWorker<>() {
                @Override
                protected BonusScoreBUS.ImportResult doInBackground() {
                    return bonusScoreBUS.importFromExcelFile(file);
                }

                @Override
                protected void done() {
                    Cursor defaultCursor = Cursor.getDefaultCursor();
                    owner.setCursor(defaultCursor);
                    owner.getRootPane().setCursor(defaultCursor);
                    owner.getRootPane().getGlassPane().setCursor(defaultCursor);
                    try {
                        BonusScoreBUS.ImportResult result = get();
                        bonusScoreListPanel.refreshData();

                        if (result.hasFatalError()) {
                            JOptionPane.showMessageDialog(
                                    BonusScoreManagement.this,
                                    result.getErrorMessage(),
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        } else {
                            String header = "Import hoàn tất.";
                            String counts = "Đã thêm: " + result.getInsertedRows() + "\n"
                                    + "Bỏ qua: " + result.getSkippedRows() + "\n"
                                    + "Lỗi: " + result.getFailedRows() + "\n\n";

                            boolean hasErrorDetails = result.getErrorDetails() != null && !result.getErrorDetails().isEmpty();
                            boolean hasSkippedDetails = result.getSkippedDetails() != null && !result.getSkippedDetails().isEmpty();

                            if (!hasErrorDetails && !hasSkippedDetails) {
                                JOptionPane.showMessageDialog(
                                        BonusScoreManagement.this,
                                        header + "\n" + counts,
                                        "Kết quả Import",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                            } else {

                            syncAdmissionResults(owner);
                                JTextArea textArea = new JTextArea(18, 60);
                                textArea.setEditable(false);
                                textArea.setLineWrap(true);
                                textArea.setWrapStyleWord(true);
                                textArea.append(header + "\n\n");
                                textArea.append(counts);

                                if (hasErrorDetails) {
                                    textArea.append("Chi tiết lỗi:\n");
                                    for (BonusScoreBUS.ImportErrorDetail error : result.getErrorDetails()) {
                                        textArea.append(String.format("• CCCD: %s, Mã ngành/đối tượng: %s - Lý do: %s\n",
                                                error.getCccd(),
                                                error.getHoTen(),
                                                error.getReason()));
                                    }
                                    textArea.append("\n");
                                }

                                if (hasSkippedDetails) {
                                    textArea.append("Chi tiết bị bỏ qua:\n");
                                    for (BonusScoreBUS.ImportErrorDetail sk : result.getSkippedDetails()) {
                                        textArea.append(String.format("• CCCD: %s, Mã ngành/đối tượng: %s - Lý do: %s\n",
                                                sk.getCccd(),
                                                sk.getHoTen(),
                                                sk.getReason()));
                                    }
                                }

                                JScrollPane scrollPane = new JScrollPane(textArea);
                                JOptionPane.showMessageDialog(
                                        BonusScoreManagement.this,
                                        scrollPane,
                                        "Kết quả Import",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                BonusScoreManagement.this,
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

        OverlayUtil.showOverlay(owner, "Nhập điểm cộng từ Excel", importPanel, new Dimension(750, 380));
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
                    bonusScoreListPanel.refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            BonusScoreManagement.this,
                            "Đã import điểm cộng nhưng đồng bộ xét tuyển thất bại: " + ex.getMessage(),
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        };
        syncWorker.execute();
    }

    public void showList() {
        revalidate();// 
        repaint();
    }

    private JFrame getOwnerFrame() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        return (window instanceof JFrame) ? (JFrame) window : null;
    }
}