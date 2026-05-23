package com.example.gui.pages.candidate;

import com.example.bus.CandidateBUS;
import com.example.dto.CandidateDTO;
import com.example.gui.components.ImportPanel;
import com.example.gui.components.OverlayUtil;

import javax.swing.*;
import java.awt.*;

public class CandidateManagement extends JPanel {
    private final CandidateListPanel candidateListPanel;

    CandidateBUS candidateBUS = new CandidateBUS();

    public CandidateManagement() {
        setLayout(new BorderLayout());

        candidateListPanel = new CandidateListPanel(this);
        add(candidateListPanel, BorderLayout.CENTER);
    }

    public void showDetail(CandidateDTO candidate) {
        showDetailOverlay(candidate);
    }

    public void showDetailOverlay(CandidateDTO candidate) {
        JFrame owner = getOwnerFrame();
        if (owner == null || candidate == null) {
            return;
        }
        int candidateId = candidate.getId();

        CandidateDetailPanel detailPanel = new CandidateDetailPanel(candidate, () -> OverlayUtil.hideOverlay(owner));
        OverlayUtil.showOverlay(owner, "Hồ sơ thí sinh #" + candidateId, detailPanel, new Dimension(900, 600));
    }

    public void showCreateOverlay() {
        JFrame owner = getOwnerFrame();
        if (owner == null) {
            return;
        }

        CandidateCreatePanel createPanel = new CandidateCreatePanel(candidate -> {
            JOptionPane.showMessageDialog(this, "Đã tạo thí sinh thành công: " + candidate.getHo() + " " + candidate.getTen());
            candidateListPanel.refreshData();
            OverlayUtil.hideOverlay(owner);
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Thêm hồ sơ thí sinh", createPanel, new Dimension(900, 600));
    }

    public void showEditOverlay(CandidateDTO candidate) {
        JFrame owner = getOwnerFrame();
        if (owner == null || candidate == null) {
            return;
        }
        int candidateId = candidate.getId();

        CandidateEditPanel editPanel = new CandidateEditPanel(candidate, updated -> {
            String candidateHo = (!updated.getHo().isEmpty()) ? updated.getHo() + " " : "";
            JOptionPane.showMessageDialog(this, "Đã cập nhật hồ sơ thí sinh thành công: " + candidateHo + updated.getTen());
            candidateListPanel.refreshData();
            OverlayUtil.hideOverlay(owner);
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Chỉnh sửa thông tin thí sinh #" + candidateId, editPanel, new Dimension(900, 520));
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
            SwingWorker<CandidateBUS.ImportResult, Void> worker = new SwingWorker<>() {
                @Override
                protected CandidateBUS.ImportResult doInBackground() {
                    return candidateBUS.importFromExcelFile(file);
                }

                @Override
                protected void done() {
                    Cursor defaultCursor = Cursor.getDefaultCursor();
                    owner.setCursor(defaultCursor);
                    owner.getRootPane().setCursor(defaultCursor);
                    owner.getRootPane().getGlassPane().setCursor(defaultCursor);
                    try {
                        CandidateBUS.ImportResult result = get();
                        candidateListPanel.refreshData();

                        if (result.hasFatalError()) {
                            JOptionPane.showMessageDialog(
                                    CandidateManagement.this,
                                    "Loi khi import file: " + result.getErrorMessage(),
                                    "Loi",
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
                                        CandidateManagement.this,
                                        header + "\n" + counts,
                                        "Thông báo",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                            } else {
                                JTextArea textArea = new JTextArea(18, 60);
                                textArea.setEditable(false);
                                textArea.setLineWrap(true);
                                textArea.setWrapStyleWord(true);
                                textArea.append(header + "\n\n");
                                textArea.append(counts);

                                if (hasErrorDetails) {
                                    textArea.append("Chi tiết lỗi:\n");
                                    for (CandidateBUS.ImportErrorDetail error : result.getErrorDetails()) {
                                        textArea.append(String.format("• CCCD: %s, Họ tên: %s - Lý do: %s\n",
                                                error.getCccd(),
                                                error.getHoTen(),
                                                error.getReason()));
                                    }
                                    textArea.append("\n");
                                }

                                if (hasSkippedDetails) {
                                    textArea.append("Chi tiết bị bỏ qua:\n");
                                    for (CandidateBUS.ImportErrorDetail sk : result.getSkippedDetails()) {
                                        textArea.append(String.format("• CCCD: %s, Họ tên: %s - Lý do: %s\n",
                                                sk.getCccd(),
                                                sk.getHoTen(),
                                                sk.getReason()));
                                    }
                                }

                                JScrollPane scrollPane = new JScrollPane(textArea);
                                JOptionPane.showMessageDialog(
                                        CandidateManagement.this,
                                        scrollPane,
                                        "Kết quả import",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                CandidateManagement.this,
                                "Loi khi import file: " + ex.getMessage(),
                                "Loi",
                                JOptionPane.ERROR_MESSAGE
                        );
                    } finally {
                        OverlayUtil.hideOverlay(owner);
                    }
                }
            };
            worker.execute();
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Nhập dữ liệu thí sinh", importPanel, new Dimension(750, 380));
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
