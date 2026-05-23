package com.example.bus;

import com.example.dao.AspirationDAO;
import com.example.dao.MajorDAO;
import com.example.dto.*;
import com.example.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AdmissionProcessBUS {
    private final AspirationBUS aspirationBUS;
    private final ExamScoreBUS examScoreBUS;
    private final CandidateBUS candidateBUS;
    private final BonusScoreBUS bonusScoreBUS;
    private final MajorGroupBUS majorGroupBUS;
    private final ScoreCalculationService scoreCalcService;

    public AdmissionProcessBUS() {
        this.aspirationBUS    = new AspirationBUS();
        this.examScoreBUS     = new ExamScoreBUS();
        this.candidateBUS     = new CandidateBUS();
        this.bonusScoreBUS    = new BonusScoreBUS();
        this.majorGroupBUS    = new MajorGroupBUS();
        this.scoreCalcService = new ScoreCalculationService();
    }

    // ========================================================================
    // TIẾN TRÌNH 1: TÍNH TOÁN TOÀN BỘ ĐIỂM SỐ VÀ ĐỒNG BỘ XUỐNG DB
    // ========================================================================
    public int calculateAndSyncAllAspirations() throws Exception {
        System.out.println("⏳ Đang tải dữ liệu gốc từ Database lên RAM để tính toán điểm số...");
        List<AspirationDTO> listAspirations = aspirationBUS.getAll();
        if (listAspirations == null || listAspirations.isEmpty()) {
            return 0;
        }

        int countProcessed = 0;
        int batchSize = 1000;

        // 1.1 Map Điểm Thi
        Map<String, ExamScoreDTO> mapDiemThi = new HashMap<>();
        try {
            for (ExamScoreDTO exam : examScoreBUS.layTatCa()) {
                if (exam.getCccd() != null) mapDiemThi.put(exam.getCccd().replaceAll("\\s+", ""), exam); 
            }
        } catch (Exception e) { System.out.println("Lỗi load Điểm thi: " + e.getMessage()); }

        // 1.2 Map Thí Sinh
        Map<String, CandidateDTO> mapThiSinh = new HashMap<>();
        try {
            for (CandidateDTO cand : candidateBUS.getAllWithPagination(0, 999999)) {
                if (cand.getCccd() != null) mapThiSinh.put(cand.getCccd().replaceAll("\\s+", ""), cand);
            }
        } catch (Exception e) { System.out.println("Lỗi load Thí sinh: " + e.getMessage()); }

        // 1.3 Map Tổ hợp môn
        Map<String, List<MajorGroupDTO>> mapToHop = new HashMap<>();
        try {
            for (MajorGroupDTO group : majorGroupBUS.getAll()) {
                if (group.getMaNganh() != null) {
                    mapToHop.computeIfAbsent(group.getMaNganh().trim(), k -> new ArrayList<>()).add(group);
                }
            }
        } catch (Exception e) { System.out.println("Lỗi load Tổ hợp môn: " + e.getMessage()); }

        // 1.4 Map Điểm Cộng 
        Map<String, List<BonusScoreDTO>> mapDiemCong = new HashMap<>();
        try {
            for (BonusScoreDTO bonus : bonusScoreBUS.getBonusScoresWithPagination(0, 999999)) {
                if (bonus.getTsCccd() != null) {
                    mapDiemCong.computeIfAbsent(bonus.getTsCccd().replaceAll("\\s+", ""), k -> new ArrayList<>()).add(bonus);
                }
            }
        } catch (Exception e) { System.out.println("Lỗi load Điểm cộng: " + e.getMessage()); }

        System.out.println("✅ Tải dữ liệu hoàn tất! Bắt đầu phân loại phương thức và lưu theo lô...");

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        Map<Integer, BonusScoreDTO> mapBonusToUpdate = new HashMap<>(); 

        try {
            tx = session.beginTransaction();

            for (AspirationDTO asp : listAspirations) {
                String cccd    = asp.getNnCccd() != null ? asp.getNnCccd().replaceAll("\\s+", "") : "";
                String maNganh = asp.getNvManganh() != null ? asp.getNvManganh().trim() : "";
                String originalToHop = asp.getTtThm() != null ? asp.getTtThm().trim() : ""; 

                if (cccd.isEmpty() || maNganh.isEmpty()) continue; 
                
                ExamScoreDTO examScore = mapDiemThi.get(cccd);
                CandidateDTO candidate = mapThiSinh.get(cccd);
                List<MajorGroupDTO> allowedGroups = mapToHop.get(maNganh);
                List<BonusScoreDTO> bonusList = mapDiemCong.get(cccd);

                boolean hasExam   = examScore != null;
                boolean hasCand   = candidate != null;
                boolean hasGroups = allowedGroups != null && !allowedGroups.isEmpty();

                // ====================================================================
                // SỬA LỖI: Ưu tiên lấy phương thức xét tuyển từ đối tượng ExamScoreDTO
                // ====================================================================
                String phuongThuc = "THPT"; // Mặc định
                
                if (hasExam && examScore.getPhuongThuc() != null && !examScore.getPhuongThuc().trim().isEmpty()) {
                    phuongThuc = examScore.getPhuongThuc().trim().toUpperCase();
                } else if (asp.getTtPhuongthuc() != null && !asp.getTtPhuongthuc().trim().isEmpty()) {
                    phuongThuc = asp.getTtPhuongthuc().trim().toUpperCase();
                }

                // Chuẩn hóa các tên phương thức để tránh lỗi gõ nhầm hoặc lưu nhãn không đồng nhất
                phuongThuc = normalizeAdmissionMethod(phuongThuc);

                // Ép phương thức xét tuyển ngược lại vào Aspiration để giao diện và thống kê bắt được
                asp.setTtPhuongthuc(phuongThuc);
                // ====================================================================

                double maxDiemThxt = 0.0;
                String bestToHop   = originalToHop;

                // --- BẮT ĐẦU PHÂN LUỒNG XỬ LÝ THEO PHƯƠNG THỨC XÉT TUYỂN ---
                if (hasExam) {
                    if ("DGNL".equals(phuongThuc) || "VSAT".equals(phuongThuc)) {
                        // Gọi hàm quy đổi điểm ĐGNL / VSAT về thang 30
                        maxDiemThxt = scoreCalcService.quyDoiDiemTheoPhuongThuc(examScore, phuongThuc);
                        if (bestToHop.isEmpty()) {
                            bestToHop = phuongThuc; // Lấy tên phương thức làm tên tổ hợp
                        }
                    } else {
                        // Phương thức THPT truyền thống
                        if (hasGroups) {
                            for (MajorGroupDTO group : allowedGroups) {
                                try {
                                    double current = scoreCalcService.tinhDiemToHop(examScore, group);
                                    if (current > maxDiemThxt) {
                                        maxDiemThxt = current;
                                        bestToHop   = group.getMaToHop() != null ? group.getMaToHop().trim() : "";
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }

                // TÍNH ĐIỂM CỘNG & ƯU TIÊN 
                double tongDiemCC = 0.0;
                double tongDiemUtxt = 0.0;

                if (bonusList != null) {
                    for (BonusScoreDTO b : bonusList) {
                        String bMaNganh = b.getMaNganh() != null ? b.getMaNganh().trim() : "";
                        String bMaToHop = b.getMaToHop() != null ? b.getMaToHop().trim() : "";
                        
                        boolean isUniversalNganh = bMaNganh.isEmpty() || bMaNganh.equals("0.00") || bMaNganh.equals("0") || bMaNganh.equalsIgnoreCase("ALL");
                        boolean isUniversalToHop = bMaToHop.isEmpty() || bMaToHop.equals("0.00") || bMaToHop.equals("0") || bMaToHop.equalsIgnoreCase("ALL");

                        boolean matchNganh = isUniversalNganh || maNganh.equalsIgnoreCase(bMaNganh);
                        boolean matchToHop = isUniversalToHop || bestToHop.equalsIgnoreCase(bMaToHop) || originalToHop.equalsIgnoreCase(bMaToHop);

                        if (matchNganh && matchToHop) {
                            double cc = b.getDiemCC() != null ? b.getDiemCC() : 0.0;
                            double utxt = b.getDiemUtxt() != null ? b.getDiemUtxt() : 0.0;
                            
                            tongDiemCC += cc;
                            tongDiemUtxt += utxt;
                            
                            b.setDiemTong(Math.min(cc + utxt, 3.0));
                            mapBonusToUpdate.put(b.getIdDiemCong(), b);
                        }
                    }
                }

                double diemCong = Math.min(tongDiemCC + tongDiemUtxt, 3.0);

                double diemUuTienGoc = 0.0;
                if (hasCand) {
                    diemUuTienGoc = quyDoiDiemKhuVuc(candidate.getKhuVuc())
                                  + quyDoiDiemDoiTuong(candidate.getDoiTuong());
                }
                
                double diemUtqd = scoreCalcService.tinhDiemUuTien(maxDiemThxt, diemCong, diemUuTienGoc);
                double diemXetTuyen = Math.min(maxDiemThxt + diemUtqd + diemCong, 30.0);

                // Gán toàn bộ kết quả tính toán vào thực thể Nguyện vọng
                if (!bestToHop.isEmpty()) asp.setTtThm(bestToHop);
                asp.setDiemThxt(maxDiemThxt);       
                asp.setDiemUtqd(diemUtqd);          
                asp.setDiemCong(diemCong);          
                asp.setDiemXettuyen(diemXetTuyen);  
                asp.setNvKetqua("Đã tính điểm");
                
                session.update(asp);
                countProcessed++;

                if (countProcessed % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }

            if (!mapBonusToUpdate.isEmpty()) {
                for (BonusScoreDTO b : mapBonusToUpdate.values()) {
                    session.update(b);
                }
            }

            session.flush();
            tx.commit();
            System.out.println("✅ Đã đồng bộ điểm số đa phương thức (THPT/DGNL/VSAT) thành công!");
            return countProcessed;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("❌ Lỗi sync điểm số: " + e.getMessage());
            throw e;
        } finally {
            session.close();
        }
    }
    
    private double quyDoiDiemKhuVuc(String khuVuc) {
        if (khuVuc == null) return 0.0;
        String normalized = khuVuc.trim().toUpperCase();

        return switch (normalized) {
            case "KV1", "1" -> 0.75;
            case "KV2-NT", "KV2NT", "2NT", "2-NT" -> 0.50;
            case "KV2", "2" -> 0.25;
            case "KV3", "3" -> 1.0;
            default -> 0.0;
        };
    }

    private double quyDoiDiemDoiTuong(String doiTuong) {
        if (doiTuong == null) return 0.0;
        String dt = doiTuong.trim();
        if (dt.matches("0?[1234]")) return 2.0;
        if (dt.matches("0?[567]"))  return 1.0;
        return 0.0;
    }

    // ========================================================================
    // TIẾN TRÌNH 2: THỰC HIỆN XÉT TUYỂN (THUẬT TOÁN LỌC ẢO TOÀN HỆ THỐNG)
    // ========================================================================
    public void thucHienXetTuyen() throws Exception {
        calculateAndSyncAllAspirations();
        
        System.out.println("⏳ Đang khởi tạo tiến trình lọc ảo xét tuyển từ dữ liệu đa phương thức...");
        
        aspirationBUS.resetAllResults(); 

        List<MajorDTO> allMajors = new MajorDAO().getAll();
        Map<String, Integer> mapChiTieu = new HashMap<>();
        Map<String, Integer> mapDaTuyen = new HashMap<>();
        Map<String, Double> mapDiemSan = new HashMap<>();
        
        for (MajorDTO major : allMajors) {
            if (major.getMaNganh() != null) {
                String maNganhChuan = major.getMaNganh().trim();
                mapChiTieu.put(maNganhChuan, major.getNChiTieu());
                mapDaTuyen.put(maNganhChuan, 0);
                mapDiemSan.put(maNganhChuan, major.getNDiemSan() != null ? major.getNDiemSan().doubleValue() : 0.0);
            }
        }

        Set<String> danhSachThiSinhDaTrungTuyen = new HashSet<>(); 

        long totalAspirations = aspirationBUS.countAllAspirations();
        int batchSize = 1000; 
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            int count = 0;

            for (int offset = 0; offset < totalAspirations; offset += batchSize) {
                List<AspirationDTO> batchAspirations = aspirationBUS.getAllSortedForAdmissionPaginated(offset, batchSize);
                
                if (batchAspirations == null || batchAspirations.isEmpty()) {
                    break;
                }

                for (AspirationDTO asp : batchAspirations) {
                    String cccd = asp.getNnCccd() != null ? asp.getNnCccd().trim() : "";
                    String maNganh = asp.getNvManganh() != null ? asp.getNvManganh().trim() : "";
                    
                    if (cccd.isEmpty() || maNganh.isEmpty()) continue;

                    double diemXetTuyen = asp.getDiemXettuyen() != null ? asp.getDiemXettuyen() : 0.0;
                    double diemSanNganh = mapDiemSan.getOrDefault(maNganh, 0.0);

                    if (diemXetTuyen < diemSanNganh) {
                        asp.setNvKetqua("RỚT (Dưới điểm sàn)");
                        session.update(asp);
                        count++;
                        if (count % batchSize == 0) {
                            session.flush();
                            session.clear();
                        }
                        continue;
                    }

                    if (danhSachThiSinhDaTrungTuyen.contains(cccd)) {
                        asp.setNvKetqua("RỚT (Đã đậu NV cao hơn)");
                    } else {
                        int chiTieuNganh = mapChiTieu.getOrDefault(maNganh, 0);
                        int soLuongDaTuyen = mapDaTuyen.getOrDefault(maNganh, 0);

                        if (soLuongDaTuyen < chiTieuNganh) {
                            asp.setNvKetqua("ĐẬU");
                            danhSachThiSinhDaTrungTuyen.add(cccd); 
                            mapDaTuyen.put(maNganh, soLuongDaTuyen + 1); 
                        } else {
                            asp.setNvKetqua("RỚT (Trượt điểm chuẩn)");
                        }
                    }

                    session.update(asp);
                    count++;

                    if (count % batchSize == 0) {
                        session.flush(); 
                        session.clear(); 
                    }
                }
                System.out.println("⚡ Đã xử lý lọc ảo: " + Math.min(offset + batchSize, totalAspirations) + " / " + totalAspirations);
            }

            session.flush();
            tx.commit();
            capNhatDiemTrungTuyenChoTatCaNganh();
            System.out.println("✅ Tiến trình xét tuyển gộp đa phương thức hoàn tất thành công!");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("❌ Thất bại trong tiến trình lọc ảo: " + e.getMessage());
            throw e;
        } finally {
            session.close();
        }
    }

    private void capNhatDiemTrungTuyenChoTatCaNganh() {
        MajorDAO majorDAO = new MajorDAO();
        AspirationDAO aspirationDAO = new AspirationDAO();

        List<MajorDTO> majors = majorDAO.getAll();
        if (majors == null || majors.isEmpty()) {
            return;
        }

        for (MajorDTO major : majors) {
            if (major == null || major.getMaNganh() == null || major.getMaNganh().isBlank()) {
                continue;
            }

            List<AspirationDTO> admitted = aspirationDAO.getAdmittedCandidatesByMaNganh(major.getMaNganh().trim());
            BigDecimal cutoff = null;

            if (admitted != null && !admitted.isEmpty()) {
                Double minAdmittedScore = admitted.stream()
                        .map(AspirationDTO::getDiemXettuyen)
                        .filter(Objects::nonNull)
                        .min(Double::compareTo)
                        .orElse(null);

                if (minAdmittedScore != null) {
                    cutoff = BigDecimal.valueOf(minAdmittedScore);
                }
            }

            if (cutoff != null || major.getNDiemTrungTuyen() != null) {
                major.setNDiemTrungTuyen(cutoff);
            }

            // --- Cập nhật số lượng trúng tuyển theo phương thức vào bảng xt_nganh ---
            Map<String, Long> stats = thongKeSoLuongTrungTuyenTheoPhuongThuc(major.getMaNganh());
            major.setSlThpt(String.valueOf(stats.getOrDefault("THPT", 0L))); // DTO của bạn đang để field này là String
            major.setSlDgnl(stats.getOrDefault("DGNL", 0L).intValue());
            major.setSlVsat(stats.getOrDefault("VSAT", 0L).intValue());
            
            // Lưu lại Điểm chuẩn và Số lượng
            majorDAO.update(major);
        }
    }

    private String normalizeAdmissionMethod(String rawMethod) {
        if (rawMethod == null || rawMethod.isBlank()) {
            return "THPT";
        }

        String method = rawMethod.trim().toUpperCase();
        if (method.contains("DGNL") || method.contains("ĐGNL") || method.contains("NL")) {
            return "DGNL";
        }
        if (method.contains("VSAT") || method.contains("V-SAT") || method.contains("SAT")) {
            return "VSAT";
        }
        if (method.contains("THPT")) {
            return "THPT";
        }

        return "THPT";
    }

    // ========================================================================
    // CHỨC NĂNG TRA CỨU KẾT QUẢ VÀ THỐNG KÊ
    // ========================================================================
    public List<AspirationDTO> layDanhSachTrTrungTuyenTheoNganh(String maNganh) {
        return new AspirationDAO().getAdmittedCandidatesByMaNganh(maNganh);
    }

    public Map<String, Long> thongKeSoLuongTrungTuyenTheoPhuongThuc(String maNganh) {
        List<Object[]> queryResults = new AspirationDAO().countAdmittedByMethod(maNganh);
        Map<String, Long> mapThongKe = new HashMap<>();
        
        for (Object[] row : queryResults) {
            String phuongThucXT = row[0] != null ? row[0].toString().trim() : "Khác/Chưa rõ";
            Long soLuong = (Long) row[1];
            mapThongKe.put(phuongThucXT, soLuong);
        }
        return mapThongKe;
    }
}
