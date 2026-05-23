package com.example.gui.pages.score;

import com.example.bus.AdmissionProcessBUS;
import com.example.bus.CandidateBUS;
import com.example.bus.ExamScoreBUS;
import com.example.dto.CandidateDTO;
import com.example.dto.ExamScoreDTO;
import com.example.gui.components.ImportPanel;
import com.example.gui.components.OverlayUtil;

import javax.swing.*;
import java.awt.*;

public class ScoreManagement extends JPanel {
    private final ScoreListPanel scoreListPanel;
    private final ScoreStatisticPanel scoreStatisticPanel;
    private final ExamScoreBUS examScoreBUS = new ExamScoreBUS();
    private final CandidateBUS candidateBUS = new CandidateBUS();

    public ScoreManagement() {
        setLayout(new BorderLayout());

        scoreListPanel      = new ScoreListPanel(this);
        scoreStatisticPanel = new ScoreStatisticPanel();

        // Ghép 2 panel theo chiều dọc trong một JPanel duy nhất
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(Color.decode("#F8F9FA"));

        // ScoreListPanel chiếm phần trên
        scoreListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(scoreListPanel);

        // Đường kẻ ngăn cách
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(separator);

        // ScoreStatisticPanel bên dưới
        scoreStatisticPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(scoreStatisticPanel);

        // Bọc trong JScrollPane để cuộn xuống xem thống kê
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void showViewOverlay(ExamScoreDTO dto) {
        JFrame owner = getOwnerFrame();
        if (owner == null || dto == null) return;
        
        // Khởi tạo ScoreEditPanel với tham số isReadOnly = true ở cuối
        // Vì xem chi tiết nên hành động onSave (updated -> {}) không cần làm gì cả
        ScoreEditPanel viewPanel = new ScoreEditPanel(dto, updated -> {}, () -> OverlayUtil.hideOverlay(owner), true);
        
        // Hiển thị panel lên với kích thước chuẩn giống form Sửa của bạn
        OverlayUtil.showOverlay(owner, viewPanel, new Dimension(920, 580));
    }

    public void showEditOverlay(ExamScoreDTO dto) {
        JFrame owner = getOwnerFrame();
        if (owner == null || dto == null) return;

        ScoreEditPanel editPanel = new ScoreEditPanel(dto, updated -> {
            boolean ok = examScoreBUS.sua(updated);
            if (ok) {
                scoreListPanel.refreshData();
                scoreStatisticPanel.refresh();
                OverlayUtil.hideOverlay(owner);
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, editPanel, new Dimension(920, 580));
    }

    public void showCreateOverlay() {
        JFrame owner = getOwnerFrame();
        if (owner == null) return;

        ScoreCreatePanel createPanel = new ScoreCreatePanel(dto -> {
            if (examScoreBUS.existsByCccd(dto.getCccd())) {
                JOptionPane.showMessageDialog(this, "CCCD này đã tồn tại trong bảng điểm. Vui lòng kiểm tra lại.", "Lỗi trùng CCCD", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CandidateDTO candidate = candidateBUS.findByCccd(dto.getCccd());
            if (candidate == null) {
                JOptionPane.showMessageDialog(this, "Thí sinh chưa tồn tại trong danh sách thí sinh. Vui lòng thêm thí sinh trước khi nhập điểm.", "Lỗi thiếu thí sinh", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = examScoreBUS.them(dto);
            if (ok) {
                scoreListPanel.refreshData();
                OverlayUtil.hideOverlay(owner);
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, createPanel, new Dimension(920, 580));
    }

    public void showImportOverlay() {
        JFrame owner = getOwnerFrame();
        if (owner == null) return;

        ImportPanel importPanel = new ImportPanel(file -> {
            Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            owner.setCursor(waitCursor);

            SwingWorker<ExamScoreBUS.ImportResult, Void> worker = new SwingWorker<>() {
                @Override
                protected ExamScoreBUS.ImportResult doInBackground() {
                    // TỰ ĐỘNG NHẬN DIỆN HÀM IMPORT QUA TÊN FILE
                    String fileName = file.getName().toLowerCase();
                    if (fileName.contains("dgnl") || fileName.contains("vsat")) {
                        return examScoreBUS.importFromVsatDgnlExcel(file);
                    } else {
                        return examScoreBUS.importFromCandidateExcel(file);
                    }
                }

                @Override
                protected void done() {
                    owner.setCursor(Cursor.getDefaultCursor());
                    try {
                        ExamScoreBUS.ImportResult result = get();
                        scoreListPanel.refreshData();
                        scoreStatisticPanel.refresh();

                        // SỬA LỖI: Kiểm tra lỗi hệ thống (nếu errorMessage khác null)
                        if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) {
                            JOptionPane.showMessageDialog(ScoreManagement.this,
                                    "Lỗi hệ thống: " + result.getErrorMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        } else {
                            // Hiển thị kết quả import thành công kèm số dòng lỗi (nếu có)
                            String msg = "Import thành công!\n" +
                                         "- Đã thêm/cập nhật: " + result.getInsertedRows() + " dòng.\n" +
                                         "- Bỏ qua (Dòng trống): " + result.getSkippedRows() + " dòng.\n" +
                                         "- Thất bại (Lỗi dữ liệu): " + result.getFailedRows() + " dòng.";
                            
                            // Nếu có dòng thất bại, thông báo chi tiết
                            if (result.getFailedRows() > 0) {
                                msg += "\nVui lòng kiểm tra lại cấu trúc dữ liệu của các dòng lỗi.";
                            }
                            
                            JOptionPane.showMessageDialog(ScoreManagement.this, msg, "Kết quả Import", JOptionPane.INFORMATION_MESSAGE);
                        }

                        syncAdmissionResults(owner);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ScoreManagement.this,
                                "Lỗi import: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        OverlayUtil.hideOverlay(owner);
                    }
                }
            };
            worker.execute();
        }, () -> OverlayUtil.hideOverlay(owner));

        OverlayUtil.showOverlay(owner, "Import điểm từ Excel", importPanel, new Dimension(750, 380));
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
                    scoreListPanel.refreshData();
                    scoreStatisticPanel.refresh();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ScoreManagement.this,
                            "Đã import điểm thi nhưng đồng bộ xét tuyển thất bại: " + ex.getMessage(),
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        };
        syncWorker.execute();
    }

    private JFrame getOwnerFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return (w instanceof JFrame) ? (JFrame) w : null;
    }
}