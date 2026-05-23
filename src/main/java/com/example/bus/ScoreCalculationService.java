package com.example.bus;

import com.example.dto.BonusScoreDTO;
import com.example.dto.CandidateDTO;
import com.example.dto.ConversionTableDTO;
import com.example.dto.ExamScoreDTO;
import com.example.dto.MajorGroupDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ScoreCalculationService {

    private final ConversionTableBUS conversionBUS;

    public ScoreCalculationService() {
        this.conversionBUS = new ConversionTableBUS();
    }

    // 1. Quy đổi chứng chỉ
    public double quyDoiChungChi(String loaiChungChi, double diemChungChi) {
        if (loaiChungChi == null || loaiChungChi.trim().isEmpty() || diemChungChi <= 0) 
            return 0.0;

        var rules = conversionBUS.getQuyDoiTheoMa(loaiChungChi);
        if (rules == null || rules.isEmpty()) return 0.0;

        for (ConversionTableDTO row : rules) {
            if (isInRange(diemChungChi, row.getDiemA(), row.getDiemB())) {
                try {
                    return Double.parseDouble(row.getPhanvi().trim());
                } catch (Exception e) {
                    return 0.0;
                }
            }
        }
        return 0.0;
    }

    // 2. Quy đổi tổ hợp môn theo bảng độ lệch
    public double quyDoiToHopMon(double diemGoc, String toHopGoc, String toHopDich) {
        if (toHopGoc == null || toHopDich == null || toHopGoc.equalsIgnoreCase(toHopDich)) {
            return diemGoc;
        }

        ConversionTableDTO rule = conversionBUS.getQuyDoiChinhXac("THPT", toHopGoc, "");
        if (rule == null) return diemGoc;

        double delta = getDelta(rule, toHopDich);
        return round(diemGoc - delta, 3);
    }

    private double getDelta(ConversionTableDTO row, String toHop) {
        if (row == null) return 0.0;
        String t = toHop.toUpperCase().trim();
        switch (t) {
            case "A00": return safe(row.getDiemA());
            case "A01": return safe(row.getDiemB());
            case "B00": return safe(row.getDiemC());
            case "C00": return safe(row.getDiemD());
            default: return 0.0;
        }
    }

    // 3. QUY ĐỔI V-SAT & ĐGNL (Cập nhật theo đề bài)
    public double quyDoiDiemTheoPhuongThuc(ExamScoreDTO dto, String phuongThuc) {
        if (dto == null) return 0.0;
    
        if ("THPT".equalsIgnoreCase(phuongThuc)) {
            return tinhDiemToHopTHPT(dto);
        } 
        else if ("VSAT".equalsIgnoreCase(phuongThuc)) {
            // VSAT: NK1 + NK2 (tối đa 300) → thang 30 (tạm tuyến tính)
            double nk1 = safe(dto.getNk1());
            double nk2 = safe(dto.getNk2());
            double tong = nk1 + nk2;
            return round((tong / 300.0) * 30.0, 3);
        } 
        else if ("DGNL".equalsIgnoreCase(phuongThuc)) {
            // DGNL: NL1 (tối đa 1200) → thang 30 (tạm tuyến tính)
            double nl1 = safe(dto.getNl1());
            return round((nl1 / 1200.0) * 30.0, 3);
        }
        return 0.0;
    }
    private double tinhDiemToHopTHPT(ExamScoreDTO dto) {
        return (safe(dto.getDiemToan()) + safe(dto.getDiemLy()) + safe(dto.getDiemHoa())) / 3.0;
    }

    // Các hàm chính giữ nguyên
    public double tinhDiemToHop(ExamScoreDTO dto, MajorGroupDTO mg) {
        if (dto == null || mg == null) return 0.0;

        double d1 = layDiemMon(dto, mg.getThMon1());
        double d2 = layDiemMon(dto, mg.getThMon2());
        double d3 = layDiemMon(dto, mg.getThMon3());

        double w1 = mg.getHsMon1() != null ? mg.getHsMon1() : 1.0;
        double w2 = mg.getHsMon2() != null ? mg.getHsMon2() : 1.0;
        double w3 = mg.getHsMon3() != null ? mg.getHsMon3() : 1.0;

        double W = w1 + w2 + w3;
        if (W == 0) return 0.0;

        return round((d1 * w1 + d2 * w2 + d3 * w3) / W * 3.0, 3);
    }

    public double tinhDiemXetTuyen(ExamScoreDTO dto, MajorGroupDTO mg, BonusScoreDTO bonus, double diemUuTien) {
        double dthxt = tinhDiemToHop(dto, mg);
        return tinhDiemXetTuyen(dthxt, bonus, diemUuTien);
    }

    public double tinhDiemXetTuyen(double diemThgXt, BonusScoreDTO bonus, double diemUuTien) {
        double diemCong = (bonus != null && bonus.getDiemTong() != null)
                        ? Math.min(bonus.getDiemTong(), 3.0) : 0.0;
        return round(diemThgXt + diemCong + diemUuTien, 3);
    }

    public double tinhDiemXetTuyen(double diemThgXt, double diemCong, double diemUuTien) {
        double validCong = Math.min(diemCong, 3.0);
        return round(diemThgXt + validCong + diemUuTien, 3);
    }

    public double tinhDiemUuTien(double diemThgXt, double diemCong, double mucDiemUuTien) {
        double tong = diemThgXt + diemCong;
        if (tong < 22.5) {
            return mucDiemUuTien;
        } else {
            return mucDiemUuTien * (30.0 - tong) / (30.0 - 22.5);
        }
    }

    public double layDiemMon(ExamScoreDTO dto, String maMon) {
        if (dto == null || maMon == null) return 0.0;
        switch (maMon.toUpperCase()) {
            case "TO": return safe(dto.getDiemToan());
            case "LI": return safe(dto.getDiemLy());
            case "HO": return safe(dto.getDiemHoa());
            case "VA": return safe(dto.getDiemVan());
            case "N1": return safe(dto.getN1Cc());
            case "NL1": return safe(dto.getNl1());
            case "NK1": return safe(dto.getNk1());
            case "NK2": return safe(dto.getNk2());
            default: return 0.0;
        }
    }

    // Helper
    private boolean isInRange(double value, BigDecimal min, BigDecimal max) {
        if (min == null || max == null) return false;
        return value >= min.doubleValue() && value <= max.doubleValue();
    }

    private double safe(Double d) { return d != null ? d : 0.0; }
    private double safe(BigDecimal bd) { return bd != null ? bd.doubleValue() : 0.0; }

    private double round(double value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    // =========================================================================
    // HÀM TÍNH TỔNG ĐIỂM XÉT TUYỂN CHUẨN HÓA (ĐÃ FIX KHỚP THAM SỐ QUY DOI)
    // =========================================================================
    public double tinhTongDiemXetTuyen(ExamScoreDTO scoreDTO, MajorGroupDTO majorGroup) {
        if (scoreDTO == null || majorGroup == null) return 0.0;
        
        // 1. SỬA LỖI: Truyền đúng tham số (double, String, String) thay vì truyền cả hai đối tượng DTO
        double diemToHopGoc = 0.0;
        try {
            // Lấy mã tổ hợp đích cần xét từ MajorGroupDTO
            String maToHopMoi = majorGroup.getMaToHop(); 
            
            // Giả định tổ hợp gốc mặc định của thí sinh là "A00" hoặc bạn lấy từ thuộc tính phù hợp 
            // và lấy điểm thi của môn/tổ hợp đại diện để truyền vào làm tham số thứ nhất (ví dụ: scoreDTO.getDiemToan())
            double diemGocBanDau = scoreDTO.getDiemToan() != null ? scoreDTO.getDiemToan() : 0.0;
            
            // Gọi hàm quyDoiToHopMon theo đúng cấu trúc (double, String, String) đã định nghĩa trong hệ thống của bạn
            diemToHopGoc = quyDoiToHopMon(diemGocBanDau, maToHopMoi, "A00");
        } catch (Exception e) {
            diemToHopGoc = 0.0;
        }
        
        // 2. Lấy Điểm Cộng (ĐC) từ database dựa trên danh sách điểm cộng của CCCD thí sinh
        double diemCong = 0.0;
        com.example.dao.BonusScoreDAO bonusDAO = new com.example.dao.BonusScoreDAO();
        
        java.util.List<com.example.dto.BonusScoreDTO> bonusList = bonusDAO.getByCccd(scoreDTO.getCccd());
        if (bonusList != null && !bonusList.isEmpty()) {
            for (com.example.dto.BonusScoreDTO bonus : bonusList) {
                if (bonus.getDiemTong() != null && bonus.getDiemTong() > diemCong) {
                    diemCong = bonus.getDiemTong();
                }
            }
        }
        
        // Khống chế tổng điểm cộng tối đa không vượt quá 3.0 điểm theo quy chế thang 30
        if (diemCong > 3.0) {
            diemCong = 3.0;
        }
        
        // 3. Lấy Mức Điểm Ưu Tiên Gốc (MĐƯT) từ bảng thông tin thí sinh qua Hibernate trực tiếp
        double mucDiemUuTienGoc = 0.0; 
        try (org.hibernate.Session session = com.example.utils.HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.query.Query<Object[]> q = session.createQuery(
                "SELECT ts.doiTuong, ts.khuVuc FROM CandidateDTO ts WHERE ts.cccd = :cccd", Object[].class);
            q.setParameter("cccd", scoreDTO.getCccd());
            var list = q.list();
            if (!list.isEmpty()) {
                Object[] row = list.get(0);
                String doiTuong = row[0] != null ? row[0].toString().trim() : "";
                String khuVuc = row[1] != null ? row[1].toString().trim() : "";
                
                if (khuVuc.equalsIgnoreCase("KV1") || khuVuc.equals("1")) mucDiemUuTienGoc += 0.75;
                else if (khuVuc.equalsIgnoreCase("KV2-NT") || khuVuc.equalsIgnoreCase("KV2NT") || khuVuc.equalsIgnoreCase("2NT") || khuVuc.equals("2-NT")) mucDiemUuTienGoc += 0.5;
                else if (khuVuc.equalsIgnoreCase("KV2") || khuVuc.equals("2")) mucDiemUuTienGoc += 0.25;
                else if (khuVuc.equalsIgnoreCase("KV3") || khuVuc.equals("3")) mucDiemUuTienGoc += 1.0;
                
                if (doiTuong.equals("01") || doiTuong.equals("02") || doiTuong.equals("03") || doiTuong.equals("04")) {
                    mucDiemUuTienGoc += 2.0;
                } else if (doiTuong.equals("05") || doiTuong.equals("06") || doiTuong.equals("07")) {
                    mucDiemUuTienGoc += 1.0;
                }
            }
        } catch (Exception ignored) {}

        // 4. Áp dụng công thức giảm tải điểm ưu tiên từ mốc 22.5 điểm thực tế
        double tongDiemGocVaCong = diemToHopGoc + diemCong;
        double diemUuTienThucTe = 0.0;
        if (tongDiemGocVaCong < 22.5) {
            diemUuTienThucTe = mucDiemUuTienGoc;
        } else {
            diemUuTienThucTe = mucDiemUuTienGoc * (30.0 - tongDiemGocVaCong) / (30.0 - 22.5);
            if (diemUuTienThucTe < 0) {
                diemUuTienThucTe = 0.0;
            }
        }
        
        // 5. Tính Điểm Xét Tuyển Cuối Cùng (ĐXT = ĐTHXT + ĐC + ĐƯT)
        double diemXetTuyen = tongDiemGocVaCong + diemUuTienThucTe;
        
        return java.math.BigDecimal.valueOf(diemXetTuyen)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }
}