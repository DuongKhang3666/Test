package com.example.bus;

import com.example.dao.ConversionTableDAO;
import com.example.dto.ConversionTableDTO;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class ConversionTableBUS {
    private ConversionTableDAO conversionTableDAO;

    public ConversionTableBUS() {
        this.conversionTableDAO = new ConversionTableDAO();
    }

    public List<ConversionTableDTO> getAll() {
        return conversionTableDAO.getAll();
    }

    public ConversionTableDTO getById(int idqd) {
        return conversionTableDAO.getById(idqd);
    }

    public boolean save(ConversionTableDTO dto) {
        validateForSave(dto);
        return conversionTableDAO.save(dto);
    }

    public boolean update(ConversionTableDTO dto) {
        validateForUpdate(dto);
        return conversionTableDAO.update(dto);
    }

    public boolean delete(int idqd) {
        return conversionTableDAO.delete(idqd);
    }

    // Phân trang đơn giản (từ 0) - Gắn trực tiếp vào CustomTable + Pagination
    public List<ConversionTableDTO> getPage(int pageIndex, int pageSize) {
        List<ConversionTableDTO> page = conversionTableDAO.getPage(pageIndex, pageSize);
        return page == null ? Collections.emptyList() : page;
    }

    // Tìm kiếm nội suy theo từ khóa
    public List<ConversionTableDTO> search(String keyword) {
        List<ConversionTableDTO> result = conversionTableDAO.search(keyword);
        return result == null ? Collections.emptyList() : result;
    }

    // Import bulk (chuẩn bị cho tính năng Nhập Excel)
    public int importBulk(List<ConversionTableDTO> rows) {
        if (rows == null || rows.isEmpty()) return 0;
        int count = 0;
        for (ConversionTableDTO r : rows) {
            if (save(r)) count++;
        }
        return count;
    }

    public List<String> getDanhSachPhuongThuc() {
        List<String> result = conversionTableDAO.findDistinctPhuongThuc();
        return result == null ? Collections.emptyList() : result;
    }

    public List<String> getDanhSachToHop() {
        List<String> result = conversionTableDAO.findDistinctToHop();
        return result == null ? Collections.emptyList() : result;
    }

    public List<ConversionTableDTO> getQuyDoiTheoMa(String maQuydoi) {
        return conversionTableDAO.findByMaQuydoi(maQuydoi);
    }

    public long count() {
        return conversionTableDAO.count();
    }

    // Lấy quy định quy đổi dựa trên Phương thức, Tổ hợp và Môn
    public ConversionTableDTO getQuyDoiChinhXac(String phuongthuc, String tohop, String mon) {
        List<ConversionTableDTO> all = getAll();
        if (all == null) return null;
        
        for (ConversionTableDTO c : all) {
            boolean matchPt = c.getPhuongthuc() != null && c.getPhuongthuc().equalsIgnoreCase(phuongthuc);
            boolean matchTh = c.getTohop() != null && c.getTohop().equalsIgnoreCase(tohop);
            boolean matchMon = c.getMon() != null && c.getMon().equalsIgnoreCase(mon);
            
            if (matchPt && matchTh && matchMon) {
                return c;
            }
        }
        return null;
    }

    public boolean existsMaQuydoi(String maQuydoi) {
        List<ConversionTableDTO> rows = getQuyDoiTheoMa(maQuydoi);
        return rows != null && !rows.isEmpty();
    }

    private void validateForSave(ConversionTableDTO dto) {
        validateCommon(dto);
        String ma = dto.getMaQuydoi().trim();
        if (existsMaQuydoi(ma)) {
            throw new IllegalArgumentException("Mã quy đổi đã tồn tại.");
        }
    }

    private void validateForUpdate(ConversionTableDTO dto) {
        validateCommon(dto);
        if (dto.getIdqd() <= 0) {
            throw new IllegalArgumentException("ID quy đổi không hợp lệ.");
        }
    }

    private void validateCommon(ConversionTableDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu quy đổi không hợp lệ.");
        }

        if (dto.getMaQuydoi() == null || dto.getMaQuydoi().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã quy đổi không được để trống.");
        }
        if (dto.getPhuongthuc() == null || dto.getPhuongthuc().trim().isEmpty()) {
            throw new IllegalArgumentException("Phương thức không được để trống.");
        }

        BigDecimal a = dto.getDiemA() == null ? BigDecimal.ZERO : dto.getDiemA();
        BigDecimal b = dto.getDiemB() == null ? BigDecimal.ZERO : dto.getDiemB();
        BigDecimal c = dto.getDiemC() == null ? BigDecimal.ZERO : dto.getDiemC();
        BigDecimal d = dto.getDiemD() == null ? BigDecimal.ZERO : dto.getDiemD();

        // Chỉ kiểm tra A <= B nếu B được nhập (khác 0).
        if (b.compareTo(BigDecimal.ZERO) != 0 && a.compareTo(b) > 0) {
            throw new IllegalArgumentException("Điểm mốc A không được lớn hơn điểm mốc B.");
        }
        
        // Chỉ kiểm tra C <= D nếu D được nhập (khác 0).
        if (d.compareTo(BigDecimal.ZERO) != 0 && c.compareTo(d) > 0) {
            throw new IllegalArgumentException("Điểm quy đổi C không được lớn hơn điểm quy đổi D.");
        }
    }

    // Hàm này tương đương với CandidateBUS.importFromExcelFile
    public String importFromExcelFile(java.io.File file) throws Exception {
        java.util.List<ConversionTableDTO> importList = new java.util.ArrayList<>();
        int skipCount = 0;
        int successCount = 0;

        try (java.io.InputStream is = new java.io.FileInputStream(file);
             org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(is)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();

            for (int i = 1; i <= rowCount; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                // Tận dụng ExcelHelper của team nếu có, nếu không thì dùng hàm getCellStringValue cơ bản
                String maQuydoi = getCellString(row.getCell(0)); 
                String phuongthuc = getCellString(row.getCell(1));
                
                if (maQuydoi.isEmpty() || phuongthuc.isEmpty()) continue;

                // KIỂM TRA TRÙNG LẶP: Bỏ qua nếu mã đã tồn tại (Giống logic check CCCD của Candidate)
                if (existsMaQuydoi(maQuydoi)) {
                    skipCount++;
                    continue;
                }

                ConversionTableDTO dto = new ConversionTableDTO();
                dto.setMaQuydoi(maQuydoi);
                dto.setPhuongthuc(phuongthuc);
                
                String tohop = getCellString(row.getCell(2));
                dto.setTohop(tohop.isEmpty() ? null : tohop);
                
                String mon = getCellString(row.getCell(3));
                dto.setMon(mon.isEmpty() ? null : mon);
                
                String phanvi = getCellString(row.getCell(4));
                dto.setPhanvi(phanvi.isEmpty() ? null : phanvi);

                dto.setDiemA(getCellDecimal(row.getCell(5)));
                dto.setDiemB(getCellDecimal(row.getCell(6)));
                dto.setDiemC(getCellDecimal(row.getCell(7)));
                dto.setDiemD(getCellDecimal(row.getCell(8)));

                importList.add(dto);
            }

            if (importList.isEmpty()) {
                return "Không có dữ liệu mới để import. (Có thể file trống hoặc tất cả mã quy đổi đã tồn tại).";
            }

            // Hiện tại dùng vòng lặp save cơ bản
            for (ConversionTableDTO dto : importList) {
                if (save(dto)) successCount++;
            }

            return "Import hoàn tất!\n- Thành công: " + successCount + " dòng.\n- Bỏ qua (trùng mã): " + skipCount + " dòng.";

        } catch (Exception e) {
            throw new Exception("Lỗi khi đọc file Excel: " + e.getMessage());
        }
    }

    // Các hàm phụ trợ đọc Cell (Hoặc bạn thay bằng ExcelHelper.getCellValueAsString của team)
    private String getCellString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        try {
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                return new java.math.BigDecimal(cell.getNumericCellValue()).toPlainString();
            }
            return cell.getStringCellValue().trim();
        } catch (Exception e) { return ""; }
    }

    private BigDecimal getCellDecimal(org.apache.poi.ss.usermodel.Cell cell) {
        String val = getCellString(cell);
        if (val.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(val); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    // HÀM TÍNH ĐIỂM THỰC TẾ (Đã ráp công thức nội suy tuyến tính chuẩn của thầy)
    public BigDecimal convertScore(String maQuydoi, BigDecimal rawScore) {
        if (rawScore == null) return null;
        
        List<ConversionTableDTO> list = conversionTableDAO.search(maQuydoi); 
        if (list == null || list.isEmpty()) return rawScore; // Không có luật -> Giữ nguyên điểm gốc

        ConversionTableDTO rule = list.get(0);
        BigDecimal a = rule.getDiemA();
        BigDecimal b = rule.getDiemB();
        BigDecimal c = rule.getDiemC();
        BigDecimal d = rule.getDiemD();

        // 1. Dạng ĐỘ LỆCH THPT (Ví dụ C01 chênh -0.68)
        // b, c, d bằng 0, chỉ có điểm a
        if ((b == null || b.compareTo(BigDecimal.ZERO) == 0) && 
            (c == null || c.compareTo(BigDecimal.ZERO) == 0)) {
            return rawScore.add(a != null ? a : BigDecimal.ZERO);
        }

        // 2. Dạng ĐỔI THẲNG IELTS/TOEFL (Ví dụ IELTS 7.0 -> 10đ)
        // a, b bằng 0, chỉ lấy điểm c làm đích
        if ((a == null || a.compareTo(BigDecimal.ZERO) == 0) && 
            (b == null || b.compareTo(BigDecimal.ZERO) == 0)) {
            return c != null ? c : rawScore;
        }

        // 3. Dạng NỘI SUY V-SAT & ĐGNL (Cần nội suy tuyến tính đủ 4 mốc a, b, c, d)
        // Công thức: y = c + [(x - a) / (b - a)] * (d - c)
        if (a != null && b != null && c != null && d != null) {
            if (b.compareTo(a) == 0) return c; // Tránh lỗi chia cho 0

            BigDecimal xMinusA = rawScore.subtract(a);
            BigDecimal bMinusA = b.subtract(a);
            BigDecimal dMinusC = d.subtract(c);

            // Tỷ lệ [(x - a) / (b - a)]
            BigDecimal ratio = xMinusA.divide(bMinusA, 10, java.math.RoundingMode.HALF_UP);
            BigDecimal multiplied = ratio.multiply(dMinusC);

            // Làm tròn lấy 2 chữ số thập phân
            return c.add(multiplied).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return rawScore; // Fallback
    }
}