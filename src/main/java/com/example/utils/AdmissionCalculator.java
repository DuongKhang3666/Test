package com.example.utils;

import com.example.dto.ExamScoreDTO;
import com.example.dto.MajorGroupDTO;

/**
 * Utility class cung cấp hàm quy đổi và tính toán điểm xét tuyển.
 *
 * CHÚ Ý: Một số quy ước (thang điểm nguồn) được giả định vì dữ liệu gốc không cung cấp rõ:
 * - ĐGNL (NL1) thang 1200 → quy về thang 10 bằng công thức: (nl1 / 1200) * 10
 * - VSAT (NK1 + NK2) được quy về thang 10 bằng cách cộng hai phần rồi chia cho 1200: (nk1+nk2)/1200*10
 *   (Nếu dự án có thang khác, chỉnh hàm `convertVsatToThpt` hoặc truyền `sourceMax` phù hợp.)
 */
public final class AdmissionCalculator {

    private AdmissionCalculator() {}

    // ----- Quy đổi các bài kiểm tra sang thang 10 THPT -----

    public static double convertDgnlToThpt(Double nl1) {
        if (nl1 == null) return 0.0;
        return (nl1 / 1200.0) * 10.0;
    }

    public static double convertVsatToThpt(Double nk1, Double nk2) {
        double a = (nk1 == null) ? 0.0 : nk1;
        double b = (nk2 == null) ? 0.0 : nk2;
        return ((a + b) / 1200.0) * 10.0;
    }

    /**
     * Chuyển giá trị từ thang bất kỳ về thang 10.
     * @param value giá trị nguồn
     * @param sourceMax giá trị lớn nhất của thang nguồn
     */
    public static double convertToThpt(double value, double sourceMax) {
        if (sourceMax <= 0) return 0.0;
        return (value / sourceMax) * 10.0;
    }

    // ----- Lấy điểm môn từ ExamScoreDTO theo mã môn (TO, LI, HO, SI, SU, DI, VA, TI, N1, NK1, NK2, etc.) -----
    public static double getSubjectScore(ExamScoreDTO exam, String subjectCode) {
        if (exam == null || subjectCode == null) return 0.0;
        switch (subjectCode.trim().toUpperCase()) {
            case "TO": return safe(exam.getDiemToan());
            case "LI": return safe(exam.getDiemLy());
            case "HO": return safe(exam.getDiemHoa());
            case "SI": return safe(exam.getDiemSinh());
            case "SU": return safe(exam.getDiemSu());
            case "DI": return safe(exam.getDiemDia());
            case "VA": return safe(exam.getDiemVan());
            case "TI": return safe(exam.getDiemTin());
            case "KTPL": return safe(exam.getDiemKtpl());
            case "CNCN": return safe(exam.getDiemCncn());
            case "CNNN": return safe(exam.getDiemCnnn());
            case "N1": return safe(exam.getN1Cc());
            case "N1_THI": return safe(exam.getN1Thi());
            case "NK1": return safe(exam.getNk1());
            case "NK2": return safe(exam.getNk2());
            case "NL1": return safe(exam.getNl1());
            default: return 0.0;
        }
    }

    private static double safe(Double v) { return v == null ? 0.0 : v; }

    /**
     * Tính điểm tổ hợp xét tuyển cho một `MajorGroupDTO` (một dòng tổ hợp của ngành).
     * Quy ước: sử dụng `thMon1`/`thMon2`/`thMon3` làm mã môn, và `hsMon1`..`hsMon3` làm hệ số (nếu null → 1).
     * Trả về tổng: score1*hs1 + score2*hs2 + score3*hs3
     */
    public static double computeCombinationScore(ExamScoreDTO exam, MajorGroupDTO mg) {
        if (exam == null || mg == null) return 0.0;
        String m1 = mg.getThMon1();
        String m2 = null; // Some DBs may store other thMon names; MajorGroupDTO currently only has thMon1.. th_mon1..3
        // Try reflectively: MajorGroupDTO uses getters getThMon1/getThMon2/getThMon3
        try {
            java.lang.reflect.Method g2 = mg.getClass().getMethod("getThMon2");
            java.lang.reflect.Method g3 = mg.getClass().getMethod("getThMon3");
            m2 = (String) g2.invoke(mg);
            m2 = (m2 == null) ? "" : m2;
            m2 = m2.isBlank() ? null : m2;
            String m3 = (String) g3.invoke(mg);
            m3 = (m3 == null) ? null : (m3.isBlank() ? null : m3);

            int hs1 = mg.getHsMon1() == null ? 1 : mg.getHsMon1();
            int hs2 = mg.getHsMon2() == null ? 1 : mg.getHsMon2();
            int hs3 = mg.getHsMon3() == null ? 1 : mg.getHsMon3();

            double s1 = getSubjectScore(exam, m1);
            double s2 = getSubjectScore(exam, m2);
            double s3 = getSubjectScore(exam, m3); 

            return s1 * hs1 + s2 * hs2 + s3 * hs3;
        } catch (Exception e) {
            // Nếu không có getter getThMon2/getThMon3, fallback dùng tbKeys hoặc tên khác
            double s1 = getSubjectScore(exam, m1);
            int hs1 = mg.getHsMon1() == null ? 1 : mg.getHsMon1();
            return s1 * hs1;
        }
    }

    /**
     * Tính điểm độ lệch giữa điểm tổ hợp và tham chiếu (nếu có). Nếu `doLech` trong `MajorGroupDTO` != null,
     * trả về |combinationScore - doLech|, ngược lại trả về 0.
     */
    public static double computeDoLech(double combinationScore, MajorGroupDTO mg) {
        if (mg == null || mg.getDoLech() == null) return 0.0;
        return Math.abs(combinationScore - mg.getDoLech().doubleValue());
    }

    /**
     * Tính điểm ưu tiên — hàm này đơn giản cộng các mức ưu tiên (region, quota, etc.).
     * @param extraPoints tổng điểm ưu tiên (do UI hoặc quy tắc khác tính trước)
     */
    public static double computePriorityPoints(double extraPoints) {
        return extraPoints;
    }

    /**
     * Tính tổng điểm xét tuyển theo phương thức THPT: combinationScore + priorityPoints - doLech
     */
    public static double computeTotalAdmissionScore(double combinationScore, double priorityPoints, double doLech) {
        return combinationScore + priorityPoints - doLech;
    }
}
