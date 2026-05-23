package com.example.bus;

import com.example.dao.ExamScoreDAO;
import com.example.dto.BonusScoreDTO;
import com.example.dto.ExamScoreDTO;
import com.example.dto.MajorGroupDTO;
import com.example.utils.ExcelHelper;
import com.example.utils.HibernateUtil;
import org.apache.poi.ss.usermodel.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;

public class ExamScoreBUS {

    private final ExamScoreDAO examScoreDAO = new ExamScoreDAO();
    private final ScoreCalculationService calculationService = new ScoreCalculationService();

    // ================================================================
    // IMPORT RESULT
    // ================================================================
    public static class ImportErrorDetail {
        private final String cccd;
        private final String reason;

        public ImportErrorDetail(String cccd, String reason) {
            this.cccd = cccd;
            this.reason = reason;
        }

        public String getCccd() { return cccd; }
        public String getReason() { return reason; }
    }

    public static class ImportResult {
        private final int insertedRows;
        private final int skippedRows;
        private final int failedRows;
        private final String errorMessage;
        private final List<ImportErrorDetail> errorDetails;

        public ImportResult(int inserted, int skipped, int failed, String errorMsg) {
            this(inserted, skipped, failed, errorMsg, new ArrayList<>());
        }

        public ImportResult(int inserted, int skipped, int failed, String errorMsg, List<ImportErrorDetail> errors) {
            this.insertedRows = inserted;
            this.skippedRows  = skipped;
            this.failedRows   = failed;
            this.errorMessage = errorMsg;
            this.errorDetails = errors != null ? errors : new ArrayList<>();
        }

        public int getInsertedRows() { return insertedRows; }
        public int getSkippedRows() { return skippedRows; }
        public int getFailedRows() { return failedRows; }
        public String getErrorMessage() { return errorMessage; }
        public List<ImportErrorDetail> getErrorDetails() { return errorDetails; }
        public boolean hasFatalError() { return errorMessage != null && !errorMessage.isEmpty(); }
    }

    // ================================================================
    // CRUD
    // ================================================================
    public boolean them(ExamScoreDTO dto) { return examScoreDAO.insert(dto); }
    public boolean sua(ExamScoreDTO dto) { return examScoreDAO.update(dto); }
    public boolean xoa(String cccd, String phuongThuc) { return examScoreDAO.deleteByCccdAndMethod(cccd, phuongThuc); }
    public ExamScoreDTO timTheoCccd(String cccd) { return examScoreDAO.findByCccd(cccd); }
    public ExamScoreDTO timTheoCccdAndMethod(String cccd, String phuongThuc) { return examScoreDAO.findByCccdAndMethod(cccd, phuongThuc); }
    public boolean existsByCccd(String cccd) { return examScoreDAO.findByCccd(cccd) != null; }
    public List<ExamScoreDTO> layTatCa() { return examScoreDAO.findAll(); }
    public List<ExamScoreDTO> layTheoTrang(int trang, int soHang) { return examScoreDAO.findAllPaged(trang, soHang); }
    public long demTatCa() { return examScoreDAO.countAll(); }
    public List<ExamScoreDTO> layTheoPhuongThuc(String pt) { return examScoreDAO.findByPhuongThuc(pt); }
    public List<ExamScoreDTO> timKiemTheoCccd(String keyword) { return examScoreDAO.searchByCccdPartial(keyword); }

    // ================================================================
    // TỔ HỢP MÔN TỪ DATABASE
    // ================================================================
    public List<String> layDanhSachToHop() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery(
                "SELECT DISTINCT matohop FROM xt_tohop_monthi ORDER BY matohop ASC", String.class);
            List<String> list = query.list();
            return (list != null && !list.isEmpty()) ? list : List.of("A00", "A01", "B00", "C00", "D01");
        } catch (Exception e) {
            e.printStackTrace();
            return List.of("A00", "A01", "B00", "C00", "D01");
        }
    }

    public MajorGroupDTO layMajorGroupTheoToHop(String maToHop) {
        if (maToHop == null || maToHop.trim().isEmpty()) return null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(
                "SELECT thm.thMon1, thm.hsMon1, thm.thMon2, thm.hsMon2, " +
                "thm.thMon3, thm.hsMon3, thm.dolech " +
                "FROM xt_nganh_tohop thm WHERE thm.matohop = :maToHop", Object[].class);
            query.setParameter("maToHop", maToHop.trim().toUpperCase());
            
            List<Object[]> results = query.list();
            if (results == null || results.isEmpty()) return null;

            Object[] row = results.get(0);
            MajorGroupDTO mg = new MajorGroupDTO();
            mg.setMaToHop(maToHop);
            mg.setThMon1((String) row[0]);
            mg.setHsMon1(row[1] != null ? ((Number) row[1]).intValue() : 1);
            mg.setThMon2((String) row[2]);
            mg.setHsMon2(row[3] != null ? ((Number) row[3]).intValue() : 1);
            mg.setThMon3((String) row[4]);
            mg.setHsMon3(row[5] != null ? ((Number) row[5]).intValue() : 1);
            
            if (row[6] != null) {
                mg.setDoLech(new BigDecimal(row[6].toString()));
            }
            return mg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================================================================
    // ĐIỂM ƯU TIÊN + ĐIỂM CỘNG (ĐÃ SỬA)
    // ================================================================
    public double tinhDiemUuTien(String cccd, double diemThgXt, double diemCong) {
        double diemTuBangCong = layDiemCongTuBangDiemCong(cccd);
        double diemTuDoiTuong = layDiemUuTienTuDoiTuongKhuVuc(cccd);
        
        return diemTuBangCong + diemTuDoiTuong;
    }

    public double layDiemCongTuBangDiemCong(String cccd) {
        return fetchDiemCongTuBangDiemCong(cccd);
    }

    public double layDiemUuTienTuDoiTuongKhuVuc(String cccd) {
        return fetchDiemUuTienTuDoiTuongKhuVuc(cccd);
    }

    // Lấy điểm cộng từ bảng xt_diemcongxettuyen
    private double fetchDiemCongTuBangDiemCong(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT COALESCE(SUM(d.diemTong), 0.0) FROM BonusScoreDTO d WHERE d.tsCccd = :cccd", 
                Double.class);
            query.setParameter("cccd", cccd);
            Double result = query.uniqueResult();
            return result != null ? Math.min(result, 3.0) : 0.0; // Không vượt 3.0
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    // Lấy điểm ưu tiên từ bảng thí sinh
    private double fetchDiemUuTienTuDoiTuongKhuVuc(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(
                "SELECT ts.doiTuong, ts.khuVuc FROM CandidateDTO ts WHERE ts.cccd = :cccd", 
                Object[].class);
            query.setParameter("cccd", cccd);
            
            List<Object[]> list = query.list();
            if (list.isEmpty()) return 0.0;

            Object[] row = list.get(0);
            String doiTuong = row[0] != null ? ((String) row[0]).trim() : null;
            String khuVuc = row[1] != null ? ((String) row[1]).trim() : null;

            return calculateUuTienTuDoiTuongKhuVuc(doiTuong, khuVuc);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private double calculateUuTienTuDoiTuongKhuVuc(String doiTuong, String khuVuc) {
        double ut = 0.0;
        if (doiTuong != null && !doiTuong.isBlank()) {
            switch (doiTuong.trim()) {
                case "01", "02", "03", "04" -> ut += 2.0;
                case "05", "06", "07" -> ut += 1.0;
                default -> ut += 0.0;
            }
        }
        if (khuVuc != null && !khuVuc.isBlank()) {
            switch (khuVuc.trim().toUpperCase()) {
                case "KV1", "1" -> ut += 0.75;
                case "KV2-NT", "KV2NT", "2NT", "2-NT" -> ut += 0.5;
                case "KV2", "2" -> ut += 0.25;
                case "KV3", "3" -> ut += 1.0;
                default -> ut += 0.0;
            }
        }
        return ut;
    }
    // ================================================================
    // IMPORT TỪ EXCEL - XỬ LÝ ĐÚNG ĐỊNH DẠNG LONG FORMAT
    
    // =========================================================================
    // HÀM 1: IMPORT FILE "ds_thiSinh.xlsx" (Mặc định phương thức THPT)
    // =========================================================================
    public ImportResult importFromCandidateExcel(File file) {
        List<ExamScoreDTO> listToInsert = new ArrayList<>();
        List<ImportErrorDetail> errorDetails = new ArrayList<>();
        int skippedRows = 0;
        int failedRows = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                return new ImportResult(0, 0, 0, "File Excel trống không có dữ liệu tiêu đề.", null);
            }
            rowIterator.next(); // Bỏ qua dòng tiêu đề chính (Cột STT, CCCD, Họ Tên...)

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // Thay thế việc gọi ExcelHelper.isRowEmpty(row) bằng kiểm tra ô CCCD trực tiếp để tránh lỗi undefined
                if (row == null) {
                    skippedRows++;
                    continue;
                }

                String cccd = ExcelHelper.getCellValueAsString(row.getCell(1));
                if (cccd == null || cccd.trim().isEmpty()) {
                    skippedRows++; // Nếu dòng trống hoàn toàn hoặc thiếu định danh chính thì bỏ qua hoặc coi là dòng trống
                    continue;
                }
                cccd = cccd.trim();

                try {
                    ExamScoreDTO dto = new ExamScoreDTO();
                    dto.setCccd(cccd);
                    dto.setSoBaoDanh(ExcelHelper.getCellValueAsString(row.getCell(2))); 
                    dto.setPhuongThuc("THPT"); // Đặt mặc định phương thức là THPT
                    dto.setIsActive(true);

                    // Khớp điểm các môn học THPT theo cấu trúc cột (Index bắt đầu từ 0)
                    dto.setDiemToan(ExcelHelper.getCellValueAsDouble(row.getCell(7)));    // TO
                    dto.setDiemVan(ExcelHelper.getCellValueAsDouble(row.getCell(8)));     // VA
                    dto.setDiemLy(ExcelHelper.getCellValueAsDouble(row.getCell(9)));      // LI
                    dto.setDiemHoa(ExcelHelper.getCellValueAsDouble(row.getCell(10)));    // HO
                    dto.setDiemSinh(ExcelHelper.getCellValueAsDouble(row.getCell(11)));   // SI
                    dto.setDiemSu(ExcelHelper.getCellValueAsDouble(row.getCell(12)));     // SU
                    dto.setDiemDia(ExcelHelper.getCellValueAsDouble(row.getCell(13)));     // DI
                    
                    // SỬA LỖI: Chuyển sang dùng các hàm map trực tiếp tương ứng trong ExamScoreDTO của bạn
                    // Bạn có thể tùy biến đổi sang các hàm tương tự như setDiemKhxh() / setDiemNgoaiNgu() nếu trường dữ liệu khác biệt
                    // Tạm thời nếu không có, hãy để ẩn hoặc thay bằng các trường setter đúng trong DTO của bạn:
                    // dto.setDiemGdcd(...) -> Thay bằng trường tương ứng của bạn nếu có
                    
                    dto.setDiemKtpl(ExcelHelper.getCellValueAsDouble(row.getCell(17)));    // KTPL
                    dto.setDiemCncn(ExcelHelper.getCellValueAsDouble(row.getCell(19)));    // CNCN
                    dto.setDiemCnnn(ExcelHelper.getCellValueAsDouble(row.getCell(20)));    // CNNN

                    listToInsert.add(dto);
                } catch (Exception e) {
                    failedRows++;
                    errorDetails.add(new ImportErrorDetail(cccd, "Lỗi đọc dòng dữ liệu: " + e.getMessage()));
                }
            }

            if (!listToInsert.isEmpty()) {
                examScoreDAO.saveAllWithResult(listToInsert, 50);
            }

            return new ImportResult(listToInsert.size(), skippedRows, failedRows, null, errorDetails);

        } catch (Exception e) {
            e.printStackTrace();
            return new ImportResult(0, 0, 0, "Lỗi hệ thống khi import file Thí sinh: " + e.getMessage(), null);
        }
    }

    // =========================================================================
    // HÀM 2: IMPORT FILE "VSAT DGNL.xlsx" (Nhân bản dòng dựa trên so khớp CMND)
    // =========================================================================
    // =========================================================================
    // HÀM 2: IMPORT FILE "VSAT DGNL.xlsx" (TỰ TẠO MỚI DÒNG - CHÈN TRỰC TIẾP VÀO DB)
    // =========================================================================
    public ImportResult importFromVsatDgnlExcel(File file) {
        List<ExamScoreDTO> listToInsert = new ArrayList<>();
        List<ImportErrorDetail> errorDetails = new ArrayList<>();
        int skippedRows = 0;
        int failedRows = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName().trim().toUpperCase(); 

                // Chỉ xử lý các sheet có tên là VSAT hoặc DGNL
                if (!sheetName.equals("VSAT") && !sheetName.equals("DGNL")) {
                    continue; 
                }

                Iterator<Row> rowIterator = sheet.iterator();
                if (!rowIterator.hasNext()) continue;
                rowIterator.next(); // Bỏ qua dòng tiêu đề

                // Cấu trúc map: CMND -> Map(Mã_Môn_Thi -> Điểm) để gom các dòng môn học lẻ
                Map<String, Map<String, Double>> candidateScoresMap = new HashMap<>();

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    if (row == null) {
                        skippedRows++;
                        continue;
                    }

                    String cmndExcel = ExcelHelper.getCellValueAsString(row.getCell(1));
                    if (cmndExcel == null || cmndExcel.trim().isEmpty()) {
                        skippedRows++;
                        continue; 
                    }
                    cmndExcel = cmndExcel.trim();

                    String maMonThi = ExcelHelper.getCellValueAsString(row.getCell(6)); // Cột MAMONTHI (Index 6)
                    Double diemExcel = ExcelHelper.getCellValueAsDouble(row.getCell(8));  // Cột DIEM (Index 8)

                    if (maMonThi == null || diemExcel == null) {
                        continue; 
                    }
                    maMonThi = maMonThi.trim().toUpperCase();

                    candidateScoresMap.putIfAbsent(cmndExcel, new HashMap<>());
                    candidateScoresMap.get(cmndExcel).put(maMonThi, diemExcel);
                }

                // Tiến hành xử lý tạo mới thực thể và đưa dữ liệu vào
                for (Map.Entry<String, Map<String, Double>> entry : candidateScoresMap.entrySet()) {
                    String cmnd = entry.getKey();
                    Map<String, Double> scoresMap = entry.getValue();

                    try {
                        ExamScoreDTO newDto = new ExamScoreDTO();
                        newDto.setCccd(cmnd);
                        newDto.setSoBaoDanh(cmnd); 
                        newDto.setPhuongThuc(sheetName); 
                        newDto.setIsActive(true);

                        // Khởi tạo giá trị mặc định tránh lỗi Null cho nền điểm gốc
                        newDto.setDiemToan(0.0);
                        newDto.setDiemVan(0.0);
                        newDto.setDiemLy(0.0);
                        newDto.setDiemHoa(0.0);
                        newDto.setDiemSinh(0.0);
                        newDto.setDiemSu(0.0);
                        newDto.setDiemDia(0.0);
                        newDto.setDiemKtpl(0.0);
                        newDto.setDiemCncn(0.0);
                        newDto.setDiemCnnn(0.0);
                        newDto.setN1Thi(0.0);
                        newDto.setN1Cc(0.0);
                        newDto.setNl1(0.0);
                        newDto.setNk1(0.0);
                        newDto.setNk2(0.0);

                        // Đổ dữ liệu điểm chuyên biệt từ tệp excel vừa đọc vào các cột tương ứng
                        if (sheetName.equals("DGNL")) {
                            Double diemDgnl = scoresMap.get("DGNL");
                            if (diemDgnl != null) {
                                newDto.setNl1(diemDgnl); 
                            }
                        } else if (sheetName.equals("VSAT")) {
                            if (scoresMap.containsKey("TO_VS")) newDto.setNk1(scoresMap.get("TO_VS"));   
                            if (scoresMap.containsKey("VA_VS")) newDto.setNk2(scoresMap.get("VA_VS"));   
                            if (scoresMap.containsKey("N1_VS")) newDto.setN1Thi(scoresMap.get("N1_VS")); 
                            if (scoresMap.containsKey("LI_VS")) newDto.setDiemLy(scoresMap.get("LI_VS"));
                            if (scoresMap.containsKey("HO_VS")) newDto.setDiemHoa(scoresMap.get("HO_VS"));
                        }

                        listToInsert.add(newDto);
                    } catch (Exception e) {
                        failedRows++;
                        errorDetails.add(new ImportErrorDetail(cmnd, "Lỗi tạo cấu trúc DTO: " + e.getMessage()));
                    }
                }
            }

            // THAY ĐỔI QUAN TRỌNG: Gọi trực tiếp hàm insert() của DAO để ép ghi mới vào DB cứng
            int successCount = 0;
            for (ExamScoreDTO dto : listToInsert) {
                boolean isInserted = examScoreDAO.insert(dto);
                if (isInserted) {
                    successCount++;
                } else {
                    failedRows++;
                    errorDetails.add(new ImportErrorDetail(dto.getCccd(), "Lỗi trùng lặp hoặc ràng buộc dữ liệu tại tầng DAO"));
                }
            }

            return new ImportResult(successCount, skippedRows, failedRows, null, errorDetails);

        } catch (Exception e) {
            e.printStackTrace();
            return new ImportResult(0, 0, 0, "Lỗi hệ thống import: " + e.getMessage(), null);
        }
    }

    private ExamScoreDTO findOriginalScoreByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ExamScoreDTO> query = session.createQuery(
                "FROM ExamScoreDTO WHERE cccd = :cccd AND isActive = true ORDER BY idDiemThi ASC", 
                ExamScoreDTO.class
            );
            query.setParameter("cccd", cccd);
            List<ExamScoreDTO> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================================================================
    // ÁNH XẠ MÃ MÔN V-SAT → FIELD DTO
    //
    // Mã chuẩn (CTU): TO_VS, LI_VS, HO_VS, VA_VS, SI_VS, SU_VS, DI_VS, N1_VS
    // Mã NHS bank:    M1=Toán, M2=Vật lý, M3=Hoá, M8=Tiếng Anh
    // Điểm V-SAT thang 150 → lưu thẳng vào NK1/NK2 (raw score)
    // ================================================================
    private void applyVSATScores(ExamScoreDTO dto, Map<String, Double> monDiem) {
        for (Map.Entry<String, Double> e : monDiem.entrySet()) {
            double diem = e.getValue();
            switch (e.getKey()) {
                case "TO_VS", "M1" -> dto.setNk1(maxOf(dto.getNk1(), diem));   // Toán → NK1
                case "LI_VS", "M2" -> dto.setNk2(maxOf(dto.getNk2(), diem));   // Lý   → NK2
                case "HO_VS", "M3" -> dto.setDiemHoa(maxOf(dto.getDiemHoa(), diem));
                case "VA_VS"       -> dto.setDiemVan(maxOf(dto.getDiemVan(), diem));
                case "SI_VS"       -> dto.setDiemSinh(maxOf(dto.getDiemSinh(), diem));
                case "SU_VS"       -> dto.setDiemSu(maxOf(dto.getDiemSu(), diem));
                case "DI_VS"       -> dto.setDiemDia(maxOf(dto.getDiemDia(), diem));
                case "N1_VS", "M8" -> dto.setN1Thi(maxOf(dto.getN1Thi(), diem));
                // Mã không nhận dạng được → bỏ qua
            }
        }
    }

    // ================================================================
    // ÁNH XẠ MÃ MÔN ĐGNL → FIELD DTO
    // Điểm ĐGNL thang 1200 → lưu vào NL1
    // ================================================================
    private void applyDGNLScores(ExamScoreDTO dto, Map<String, Double> monDiem) {
        for (Map.Entry<String, Double> e : monDiem.entrySet()) {
            if (e.getKey().equals("DGNL")) {
                dto.setNl1(maxOf(dto.getNl1(), e.getValue()));
            }
        }
    }

    // Trả về max, coi null = 0
    private double maxOf(Double existing, double newVal) {
        return Math.max(existing != null ? existing : 0.0, newVal);
    }

    // ================================================================
    // HELPER ĐỌC CELL
    // ================================================================
    private String getCellString(Row row, int colIndex) {
        if (colIndex < 0) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        String val = ExcelHelper.getCellValueAsString(cell);
        return (val == null || val.isBlank()) ? null : val.trim();
    }

    private Double getCellDouble(Row row, int colIndex) {
        if (colIndex < 0) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING  -> {
                try {
                    yield Double.parseDouble(
                        cell.getStringCellValue().trim().replace(",", "."));
                } catch (Exception e) { yield null; }
            }
            default -> null;
        };
    }

    // ================================================================
    // LOGIC TÍNH ĐIỂM
    // ================================================================
    public double tinhVaLuuN1Cc(String cccd, String loaiChungChi, Double diemChungChi) {
        ExamScoreDTO dto = examScoreDAO.findByCccd(cccd);
        if (dto == null) return 0.0;

        double n1Thi = dto.getN1Thi() != null ? dto.getN1Thi() : 0.0;
        double n1Cc  = n1Thi;

        if (loaiChungChi != null && diemChungChi != null && diemChungChi > 0) {
            n1Cc = calculationService.quyDoiChungChi(loaiChungChi, diemChungChi);
            n1Cc = Math.max(n1Thi, n1Cc);
        }

        examScoreDAO.updateN1Cc(cccd, n1Cc);
        return n1Cc;
    }

    public double tinhDiemToHop(ExamScoreDTO dto, MajorGroupDTO mg) {
        return calculationService.tinhDiemToHop(dto, mg);
    }

    public double tinhDiemXetTuyen(ExamScoreDTO dto, MajorGroupDTO mg,
                                   BonusScoreDTO bonus, double diemUuTien) {
        return calculationService.tinhDiemXetTuyen(dto, mg, bonus, diemUuTien);
    }

    public ScoreCalculationService getCalculationService() {
        return calculationService;
    }

    // ================================================================
    // THỐNG KÊ
    // ================================================================
    public long thongKeSoLuongTheoPhuongThuc(String phuongThuc) {
        if (phuongThuc == null || phuongThuc.isEmpty()) return demTatCa();
        return examScoreDAO.countByPhuongThuc(phuongThuc);
    }

    public double thongKeDiemTrungBinh(String tenCot, String phuongThuc) {
        return examScoreDAO.tinhDiemTrungBinh(tenCot, phuongThuc);
    }
        // ================================================================
    // ĐƯT CHI TIẾT (Value + Lý do)
    // ================================================================
    public static class DiemUuTienDetail {
        public final double diemUuTien;
        public final String lyDo;

        public DiemUuTienDetail(double diem, String reason) {
            this.diemUuTien = diem;
            this.lyDo = reason;
        }
    }

    /**
     * Trả về ĐƯT kèm lý do chi tiết
     */
    public DiemUuTienDetail tinhDiemUuTienChiTiet(String cccd) {
        double diemTuBangCong = layDiemCongTuBangDiemCong(cccd);
        double diemTuDoiTuongKV = layDiemUuTienTuDoiTuongKhuVuc(cccd);
        double tongDiem = diemTuBangCong + diemTuDoiTuongKV;

        StringBuilder lyDo = new StringBuilder();

        // Lý do từ đối tượng + khu vực
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> q = session.createQuery(
                "SELECT ts.doiTuong, ts.khuVuc FROM CandidateDTO ts WHERE ts.cccd = :cccd", 
                Object[].class);
            q.setParameter("cccd", cccd);
            List<Object[]> list = q.list();
            
            if (!list.isEmpty()) {
                Object[] row = list.get(0);
                String doiTuong = row[0] != null ? ((String) row[0]).trim() : null;
                String khuVuc = row[1] != null ? ((String) row[1]).trim() : null;

                if (doiTuong != null && !doiTuong.isEmpty()) {
                    lyDo.append("ĐT: ").append(doiTuong);
                }
                if (khuVuc != null && !khuVuc.isEmpty()) {
                    if (lyDo.length() > 0) lyDo.append(" + ");
                    lyDo.append("KV: ").append(khuVuc);
                }
            }
        } catch (Exception ignored) {}

        // Điểm cộng từ bảng diemcong
        if (diemTuBangCong > 0) {
            if (lyDo.length() > 0) lyDo.append(" + ");
            lyDo.append("Điểm cộng: ").append(String.format("%.2f", diemTuBangCong));
        }

        if (lyDo.length() == 0) {
            lyDo.append("Không có ưu tiên");
        }

        return new DiemUuTienDetail(tongDiem, lyDo.toString());
    }
    public ExamScoreDTO getScoreByCccdAndMethod(String cccd, String phuongThuc) {
        return examScoreDAO.findByCccdAndMethod(cccd, phuongThuc);
    }

    public boolean deleteScoreByCccdAndMethod(String cccd, String phuongThuc) {
        return examScoreDAO.deleteByCccdAndMethod(cccd, phuongThuc);
    }
}