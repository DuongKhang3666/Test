package com.example.bus;

import com.example.dao.AspirationDAO;
import com.example.dto.AspirationDTO;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AspirationBUS {
    private final AspirationDAO aspirationDAO;

    // Các hằng số định nghĩa độ dài tối đa cấu trúc dữ liệu theo Database
    private static final int MAX_CCCD_LENGTH = 20;
    private static final int MAX_MANGANH_LENGTH = 45;
    private static final int MAX_KEYS_LENGTH = 100;
    private static final int MAX_PHUONGTHUC_LENGTH = 100;
    private static final int MAX_THM_LENGTH = 45;

    public AspirationBUS() {
        this.aspirationDAO = new AspirationDAO();
    }

    public AspirationBUS(AspirationDAO aspirationDAO) {
        this.aspirationDAO = aspirationDAO != null ? aspirationDAO : new AspirationDAO();
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG LẤY DỮ LIỆU & PHÂN TRANG & TÌM KIẾM
    // ----------------------------------------------------------------

    public List<AspirationDTO> getAll() {
        try {
            return aspirationDAO.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public AspirationDTO findById(int idnv) {
        if (idnv <= 0) return null;
        return aspirationDAO.findById(idnv);
    }

    public AspirationDTO findByNvKeys(String nvKeys) {
        if (nvKeys == null || nvKeys.isBlank()) return null;
        return aspirationDAO.findByNvKeys(nvKeys.trim());
    }

    public List<AspirationDTO> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return new ArrayList<>();
        return aspirationDAO.findByCccd(cccd.trim());
    }

    public List<AspirationDTO> findByMaNganh(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return new ArrayList<>();
        return aspirationDAO.findByMaNganh(maNganh.trim());
    }

    public List<AspirationDTO> getAllWithPagination(int offset, int limit) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 20;
        try {
            return aspirationDAO.getAllWithPagination(offset, limit);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách nguyện vọng phân trang", e);
        }
    }

    public List<AspirationDTO> searchByCccdOrMaNganh(String keyword, int offset, int limit) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 20;
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllWithPagination(offset, limit);
        }
        return aspirationDAO.searchByCccdOrMaNganh(keyword.trim(), offset, limit);
    }

    public long getCount() {
        return aspirationDAO.getCount();
    }

    public long countSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getCount();
        }
        return aspirationDAO.countSearch(keyword.trim());
    }

    // ----------------------------------------------------------------
    // THAO TÁC NGHIỆP VỤ (CHÈN - CẬP NHẬT - XÓA)
    // ----------------------------------------------------------------

    public void save(AspirationDTO aspiration) {
        try {
            normalizeAndTruncate(aspiration);
            validateAspiration(aspiration, true);
            aspirationDAO.save(aspiration);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof AspirationValidationException) {
                throw e;
            }
            throw new RuntimeException("Lỗi khi lưu nguyện vọng mới", e);
        }
    }

    public void update(AspirationDTO aspiration) {
        try {
            normalizeAndTruncate(aspiration);
            validateAspiration(aspiration, false);
            aspirationDAO.update(aspiration);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof AspirationValidationException) {
                throw e;
            }
            throw new RuntimeException("Lỗi khi cập nhật nguyện vọng", e);
        }
    }

    public void delete(AspirationDTO aspiration) {
        if (aspiration == null) {
            throw new IllegalArgumentException("Dữ liệu nguyện vọng cần xoá không hợp lệ");
        }
        try {
            aspirationDAO.delete(aspiration);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xoá nguyện vọng", e);
        }
    }

    // ----------------------------------------------------------------
    // PHỤC VỤ THUẬT TOÁN X T TUYỂN (ỦY QUYỀN TỪ DAO)
    // ----------------------------------------------------------------

    public List<AspirationDTO> getAllSortedForAdmission() {
        try {
            return aspirationDAO.getAllSortedForAdmission();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<AspirationDTO> getSortedByMaNganhForAdmission(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return new ArrayList<>();
        return aspirationDAO.getSortedByMaNganhForAdmission(maNganh.trim());
    }

    public List<AspirationDTO> getAllSortedForReview() {
        try {
            return aspirationDAO.getAllSortedForReview();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean updateResultStatus(int aspirationId, String status) {
        if (aspirationId <= 0) return false;
        String normalizedStatus = status != null ? status.trim() : null;
        return aspirationDAO.updateResultStatus(aspirationId, normalizedStatus);
    }

    public boolean updateResultFull(int aspirationId, String status,
                                    double diemXettuyen, double diemThxt,
                                    double diemCong, double diemUtqd) {
        if (aspirationId <= 0) return false;
        String normalizedStatus = status != null ? status.trim() : null;
        return aspirationDAO.updateResultFull(aspirationId, normalizedStatus, diemXettuyen, diemThxt, diemCong, diemUtqd);
    }

    public int batchUpdateStatus(List<Integer> ids, String status) {
        if (ids == null || ids.isEmpty()) return 0;
        String normalizedStatus = status != null ? status.trim() : null;
        return aspirationDAO.batchUpdateStatus(ids, normalizedStatus);
    }

    public int resetAllResults() {
        return aspirationDAO.resetAllResults();
    }

    public long countAdmittedByMaNganh(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return 0L;
        return aspirationDAO.countAdmittedByMaNganh(maNganh.trim());
    }

    public long countRegisteredByMaNganh(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return 0L;
        return findByMaNganh(maNganh.trim()).size();
    }

    public Map<String, Long> countAdmittedByMethod(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) {
            return new HashMap<>();
        }

        Map<String, Long> result = new HashMap<>();
        List<Object[]> rows = aspirationDAO.countAdmittedByMethod(maNganh.trim());
        if (rows == null) {
            return result;
        }

        for (Object[] row : rows) {
            String method = row != null && row.length > 0 && row[0] != null ? row[0].toString().trim() : "Khác";
            Long count = 0L;
            if (row != null && row.length > 1 && row[1] instanceof Number number) {
                count = number.longValue();
            }
            result.put(method, count);
        }
        return result;
    }

    public long countByStatus(String status) {
        String normalizedStatus = status != null ? status.trim() : null;
        return aspirationDAO.countByStatus(normalizedStatus);
    }

    // ----------------------------------------------------------------
    // LOGIC CHUẨN HOÁ VÀ KIỂM TRA DỮ LIỆU ĐẦU VÀO (VALIDATION)
    // ----------------------------------------------------------------

    private void normalizeAndTruncate(AspirationDTO aspiration) {
        if (aspiration == null) return;

        aspiration.setNnCccd(truncateString(aspiration.getNnCccd(), MAX_CCCD_LENGTH));
        aspiration.setNvManganh(truncateString(aspiration.getNvManganh(), MAX_MANGANH_LENGTH));
        aspiration.setNvKeys(truncateString(aspiration.getNvKeys(), MAX_KEYS_LENGTH));
        aspiration.setTtPhuongthuc(truncateString(aspiration.getTtPhuongthuc(), MAX_PHUONGTHUC_LENGTH));
        aspiration.setTtThm(truncateString(aspiration.getTtThm(), MAX_THM_LENGTH));
        
        if (aspiration.getNvKetqua() != null) {
            aspiration.setNvKetqua(aspiration.getNvKetqua().trim());
        }

        // Tự động sinh hoặc đồng bộ cột `nvKeys` theo cấu trúc: CCCD_MaNganh_ThuTu để đảm bảo tính duy nhất
        if ((aspiration.getNvKeys() == null || aspiration.getNvKeys().isBlank()) 
                && !isBlank(aspiration.getNnCccd()) && !isBlank(aspiration.getNvManganh())) {
            String generatedKey = aspiration.getNnCccd() + "_" + aspiration.getNvManganh() + "_" + aspiration.getNvTt();
            aspiration.setNvKeys(truncateString(generatedKey, MAX_KEYS_LENGTH));
        }
    }

    private void validateAspiration(AspirationDTO aspiration, boolean isCreate) {
        List<String> errors = new ArrayList<>();
        if (aspiration == null) {
            errors.add("Dữ liệu nguyện vọng không hợp lệ");
            throw new AspirationValidationException(errors);
        }

        String cccd = aspiration.getNnCccd();
        if (isBlank(cccd)) {
            errors.add("CCCD thí sinh không được bỏ trống");
        } else if (!cccd.matches("^(\\d{12}|TS_\\d+)$")) { 
            // ĐÃ SỬA: Chấp nhận cả CCCD 12 số thực tế HOẶC mã giả lập dạng TS_ kèm số
            errors.add("CCCD phải bao gồm chính xác 12 chữ số hoặc mã định danh dạng TS_");
        }

        String maNganh = aspiration.getNvManganh();
        if (isBlank(maNganh)) {
            errors.add("Mã ngành xét tuyển không được bỏ trống");
        }

        if (aspiration.getNvTt() <= 0) {
            errors.add("Thứ tự nguyện vọng phải là số nguyên dương lớn hơn 0");
        }

        // Kiểm tra ràng buộc điểm số không âm nếu được gán dữ liệu
        if (aspiration.getDiemThxt() != null && aspiration.getDiemThxt() < 0) {
            errors.add("Điểm tổ hợp xét tuyển không được nhỏ hơn 0");
        }
        if (aspiration.getDiemUtqd() != null && aspiration.getDiemUtqd() < 0) {
            errors.add("Điểm ưu tiên quy định không được nhỏ hơn 0");
        }
        if (aspiration.getDiemCong() != null && aspiration.getDiemCong() < 0) {
            errors.add("Điểm cộng chứng chỉ/giải thưởng không được nhỏ hơn 0");
        }
        if (aspiration.getDiemXettuyen() != null && aspiration.getDiemXettuyen() < 0) {
            errors.add("Tổng điểm xét tuyển không được nhỏ hơn 0");
        }

        // Kiểm tra trùng lặp logic của khóa duy nhất nvKeys
        String nvKeys = aspiration.getNvKeys();
        if (!isBlank(nvKeys)) {
            AspirationDTO existing = aspirationDAO.findByNvKeys(nvKeys);
            if (existing != null) {
                if (isCreate || existing.getIdnv() != aspiration.getIdnv()) {
                    errors.add("Nguyện vọng này đã tồn tại trong hệ thống (Trùng khoá định danh NV)");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new AspirationValidationException(errors);
        }
    }

    private String truncateString(String value, int maxLength) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength).trim();
        }
        return trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // ----------------------------------------------------------------
    // LỚP NGOẠI LỆ ĐỂ TRẢ VỀ LỖI CHO GIAO DIỆN (GUI UI)
    // ----------------------------------------------------------------

    public static class AspirationValidationException extends RuntimeException {
        private final List<String> errors;

        public AspirationValidationException(List<String> errors) {
            super(errors == null || errors.isEmpty() ? "Kiểm tra dữ liệu nguyện vọng thất bại" : errors.get(0));
            this.errors = errors == null ? new ArrayList<>() : new ArrayList<>(errors);
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public String getMessageText() {
            return String.join("\n", errors);
        }
    }

    public static class AspirationImportResult {
        private final int insertedRows;
        private final int skippedRows;
        private final String errorMessage;
        private final List<String> errorDetails;

        public AspirationImportResult(int insertedRows, int skippedRows, String errorMessage, List<String> errorDetails) {
            this.insertedRows = insertedRows;
            this.skippedRows = skippedRows;
            this.errorMessage = errorMessage;
            this.errorDetails = errorDetails != null ? errorDetails : new ArrayList<>();
        }

        public int getInsertedRows() { return insertedRows; }
        public int getSkippedRows() { return skippedRows; }
        public String getErrorMessage() { return errorMessage; }
        public List<String> getErrorDetails() { return errorDetails; }
    }

    public AspirationImportResult importFromCsvFile(File file) {
        if (file == null || !file.exists()) {
            return new AspirationImportResult(0, 0, "File import không tồn tại hoặc không hợp lệ", new ArrayList<>());
        }

        List<AspirationDTO> aspirationsToSave = new ArrayList<>();
        List<String> errorDetails = new ArrayList<>();
        int insertedRows = 0;
        int skippedRows = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                return new AspirationImportResult(0, 0, "File trống, không tìm thấy dòng tiêu đề", new ArrayList<>());
            }

            // Phân tách và lập chỉ mục tiêu đề cột (Hỗ trợ phân tách bằng dấu phẩy)
            String[] headers = headerLine.split(",");
            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colMap.put(headers[i].trim().toLowerCase(), i);
            }

            // Kiểm tra các cột bắt buộc tối thiểu
            if (!colMap.containsKey("nn_cccd") || !colMap.containsKey("nv_manganh") || !colMap.containsKey("nv_tt")) {
                return new AspirationImportResult(0, 0, "File không đúng cấu trúc mẫu (Thiếu cột nn_cccd, nv_manganh hoặc nv_tt)", new ArrayList<>());
            }

            String line;
            int rowNum = 1;
            while ((line = br.readLine()) != null) {
                rowNum++;
                if (line.trim().isEmpty()) {
                    skippedRows++;
                    continue;
                }

                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Phân tách chuỗi an toàn chống lỗi dấu phẩy lồng nhau
                try {
                    AspirationDTO asp = new AspirationDTO();
                    
                    // Đọc dữ liệu dựa trên chỉ mục cột
                    String cccd = getCellValue(tokens, colMap, "nn_cccd");
                    String maNganh = getCellValue(tokens, colMap, "nv_manganh");
                    String thuTuRaw = getCellValue(tokens, colMap, "nv_tt");
                    
                    if (cccd.isEmpty() || maNganh.isEmpty() || thuTuRaw.isEmpty()) {
                        skippedRows++;
                        errorDetails.add("Dòng " + rowNum + ": Bỏ qua do thiếu dữ liệu trường bắt buộc.");
                        continue;
                    }

                    asp.setNnCccd(cccd);
                    asp.setNvManganh(maNganh);
                    asp.setNvTt(Integer.parseInt(thuTuRaw));
                    
                    // Đọc các trường điểm số và thông tin phụ thuộc (nếu có trong file)
                    asp.setTtThm(getCellValue(tokens, colMap, "tt_thm"));
                    asp.setTtPhuongthuc(getCellValue(tokens, colMap, "tt_phuongthuc"));
                    
                    String diemThxt = getCellValue(tokens, colMap, "diem_thxt");
                    if (!diemThxt.isEmpty()) asp.setDiemThxt(Double.parseDouble(diemThxt));
                    
                    String diemUtqd = getCellValue(tokens, colMap, "diem_utqd");
                    if (!diemUtqd.isEmpty()) asp.setDiemUtqd(Double.parseDouble(diemUtqd));
                    
                    String diemCong = getCellValue(tokens, colMap, "diem_cong");
                    if (!diemCong.isEmpty()) asp.setDiemCong(Double.parseDouble(diemCong));
                    
                    String diemXt = getCellValue(tokens, colMap, "diem_xettuyen");
                    if (!diemXt.isEmpty()) asp.setDiemXettuyen(Double.parseDouble(diemXt));

                    String ketQua = getCellValue(tokens, colMap, "nv_ketqua");
                    asp.setNvKetqua(!ketQua.isEmpty() ? ketQua : "Chờ xét");

                    // Chuẩn hóa và validate nghiệp vụ
                    normalizeAndTruncate(asp);
                    
                    // Kiểm tra trùng lặp khóa định danh `nv_keys` trước khi nạp vào list nhằm tối ưu tốc độ kết nối DB
                    AspirationDTO existing = aspirationDAO.findByNvKeys(asp.getNvKeys());
                    if (existing != null) {
                        skippedRows++;
                        errorDetails.add("Dòng " + rowNum + ": Bỏ qua do nguyện vọng đã tồn tại trong hệ thống.");
                        continue;
                    }

                    aspirationsToSave.add(asp);
                } catch (Exception ex) {
                    skippedRows++;
                    errorDetails.add("Dòng " + rowNum + ": Lỗi định dạng dữ liệu số (" + ex.getMessage() + ").");
                }
            }

            // Tiến hành lưu hàng loạt dữ liệu thông qua Hibernate
            for (AspirationDTO dto : aspirationsToSave) {
                aspirationDAO.save(dto);
                insertedRows++;
            }

            return new AspirationImportResult(insertedRows, skippedRows, null, errorDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return new AspirationImportResult(0, skippedRows, "Lỗi đọc cấu trúc file: " + e.getMessage(), errorDetails);
        }
    }

    private String getCellValue(String[] tokens, Map<String, Integer> colMap, String columnName) {
        if (!colMap.containsKey(columnName)) return "";
        int index = colMap.get(columnName);
        if (index >= tokens.length) return "";
        return tokens[index].trim().replaceAll("^\"|\"$", ""); // Loại bỏ dấu nháy kép bọc chuỗi nếu có
    }

    // ----------------------------------------------------------------
    // CLASS IMPORT RESULT ĐỂ TRẢ VỀ TRẠNG THÁI
    // ----------------------------------------------------------------
    public static class ImportResult {
        private final int successCount;
        private final int errorCount;

        public ImportResult(int successCount, int errorCount) {
            this.successCount = successCount;
            this.errorCount = errorCount;
        }
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
    }


    // ----------------------------------------------------------------
    // HÀM ĐỌC VÀ LƯU NGUYỆN VỌNG THÔ TỪ EXCEL (TỐI ƯU TỐC ĐỘ CAO)
    // ----------------------------------------------------------------
    public ImportResult importAspirationsFromExcel(File file) {
        int errorCount = 0;
        if (file == null) return new ImportResult(0, 0);

        List<AspirationDTO> listToSave = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return new ImportResult(0, 0);

            // BƯỚC 1: LOAD TẤT CẢ KHÓA nvKeys LÊN RAM ĐỂ TÌM KIẾM TỐC ĐỘ O(1)
            java.util.Set<String> existingKeys = aspirationDAO.getAllNvKeys();

            int colCccd = -1, colMaNganh = -1, colThuTu = -1;
            int headerRowNum = -1;

            // Tìm kiếm dòng Header (quét 20 dòng đầu)
            int maxSearchRow = Math.min(sheet.getLastRowNum(), 20);
            for (int r = sheet.getFirstRowNum(); r <= maxSearchRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                colCccd = -1; colMaNganh = -1; colThuTu = -1;
                for (Cell cell : row) {
                    String header = com.example.utils.ExcelHelper.getCellValueAsString(cell).toLowerCase().trim();
                    if (header.contains("cccd") || header.contains("cmnd")) {
                        colCccd = cell.getColumnIndex();
                    } else if ((header.contains("mã xét tuyển") || header.contains("mã ngành") || header.contains("mã trường")) 
                            && !header.contains("tên")) {
                        colMaNganh = cell.getColumnIndex();
                    } else if (header.contains("thứ tự")) {
                        colThuTu = cell.getColumnIndex();
                    }
                }
                if (colCccd != -1 && colMaNganh != -1 && colThuTu != -1) {
                    headerRowNum = r;
                    break;
                }
            }

            if (headerRowNum == -1) return new ImportResult(0, 1);

            // BƯỚC 2: QUÉT DATA VÀ ĐƯA VÀO LIST TRÊN RAM
            for (int r = headerRowNum + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                try {
                    String cccd = com.example.utils.ExcelHelper.getCellValueAsString(row.getCell(colCccd)).trim();
                    String maNganh = com.example.utils.ExcelHelper.getCellValueAsString(row.getCell(colMaNganh)).trim();
                    String thuTuStr = com.example.utils.ExcelHelper.getCellValueAsString(row.getCell(colThuTu)).trim();

                    if (cccd.isEmpty() && maNganh.isEmpty() && thuTuStr.isEmpty()) continue;
                    if (cccd.isEmpty() || maNganh.isEmpty() || thuTuStr.isEmpty()) {
                        errorCount++;
                        continue;
                    }

                    int thuTu = (int) Double.parseDouble(thuTuStr);
                    String generatedKey = cccd + "_" + maNganh + "_" + thuTu;

                    // KIỂM TRA TRÙNG LẶP TRÊN RAM (Siêu nhanh, không cần gọi Database)
                    if (existingKeys.contains(generatedKey)) {
                        errorCount++;
                        continue;
                    }

                    AspirationDTO dto = new AspirationDTO();
                    dto.setNnCccd(cccd);
                    dto.setNvManganh(maNganh);
                    dto.setNvTt(thuTu);
                    dto.setNvKetqua("Chờ xét");
                    dto.setTtPhuongthuc("Xét điểm thi THPT");
                    dto.setNvKeys(generatedKey);

                    // Đưa vào danh sách lưu và cập nhật RAM để tránh trùng các dòng bên trong chính file Excel
                    existingKeys.add(generatedKey); 
                    listToSave.add(dto);

                } catch (Exception ex) {
                    errorCount++;
                }
            }

            // BƯỚC 3: LƯU HÀNG LOẠT XUỐNG DATABASE (1 Transaction duy nhất)
            int inserted = aspirationDAO.saveAllBatch(listToSave);
            return new ImportResult(inserted, errorCount);

        } catch (Exception e) {
            e.printStackTrace();
            return new ImportResult(0, errorCount);
        }
    }

    public void batchUpdateAll(List<AspirationDTO> list) {
        if (list == null || list.isEmpty()) return;
        aspirationDAO.batchUpdateAll(list);
    }


    public List<AspirationDTO> getAllSortedForAdmissionPaginated(int offset, int limit) {
        return aspirationDAO.getAllSortedForAdmissionPaginated(offset, limit);
    }

    public long countAllAspirations() {
        return aspirationDAO.countAllAspirations();
    }
}