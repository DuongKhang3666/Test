package com.example.bus;

import com.example.dao.BonusScoreDAO;
import com.example.dto.BonusScoreDTO;
import com.example.utils.ExcelHelper;
import com.example.utils.HibernateBatchUtil;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BonusScoreBUS {
    private static final double MAX_BONUS_TOTAL = 3.0;

    public static class ImportErrorDetail {
        private final String cccd;
        private final String hoTen;
        private final String reason;
        private static final double MAX_BONUS_TOTAL = 3.0;
        private final BonusScoreDAO bonusScoreDAO = new BonusScoreDAO();

        /**
         * Hàm lấy điểm cộng theo CCCD (Xử lý an toàn từ List lấy ra phần tử đầu tiên)
         */
        public BonusScoreDTO layDiemCongTheoCccd(String cccd) {
            if (cccd == null || cccd.trim().isEmpty()) {
                return null;
            }
            // Gọi hàm DAO gốc trả về List
            List<BonusScoreDTO> list = bonusScoreDAO.getByCccd(cccd.trim());
            if (list != null && !list.isEmpty()) {
                return list.get(0); // Lấy bản ghi điểm cộng đầu tiên của thí sinh
            }
            return null;
        }
        public ImportErrorDetail(String cccd, String hoTen, String reason) {
            this.cccd = cccd;
            this.hoTen = hoTen;
            this.reason = reason;
        }

        public String getCccd() {
            return cccd;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getReason() {
            return reason;
        }
    }

    public static class ImportResult {
        private final int insertedRows;
        private final int skippedRows;
        private final int failedRows;
        private final String errorMessage;
        private final List<ImportErrorDetail> errorDetails;
        private final List<ImportErrorDetail> skippedDetails;

        public ImportResult(int insertedRows, int skippedRows, int failedRows, String errorMessage, List<ImportErrorDetail> errorDetails) {
            this(insertedRows, skippedRows, failedRows, errorMessage, errorDetails, new ArrayList<>());
        }

        public ImportResult(int insertedRows, int skippedRows, int failedRows, String errorMessage, List<ImportErrorDetail> errorDetails, List<ImportErrorDetail> skippedDetails) {
            this.insertedRows = insertedRows;
            this.skippedRows = skippedRows;
            this.failedRows = failedRows;
            this.errorMessage = errorMessage;
            this.errorDetails = errorDetails != null ? errorDetails : new ArrayList<>();
            this.skippedDetails = skippedDetails != null ? skippedDetails : new ArrayList<>();
        }

        public int getInsertedRows() {
            return insertedRows;
        }

        public int getSkippedRows() {
            return skippedRows;
        }

        public int getFailedRows() {
            return failedRows;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<ImportErrorDetail> getErrorDetails() {
            return errorDetails;
        }

        public List<ImportErrorDetail> getSkippedDetails() {
            return skippedDetails;
        }

        public boolean hasFatalError() {
            return errorMessage != null && !errorMessage.isEmpty();
        }
    }

    public static class BonusScoreValidationException extends RuntimeException {
        private final List<String> errors;

        public BonusScoreValidationException(List<String> errors) {
            super(errors == null || errors.isEmpty() ? "Bonus score validation failed" : errors.get(0));
            this.errors = errors == null ? new ArrayList<>() : new ArrayList<>(errors);
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public String getMessageText() {
            return String.join("\n", errors);
        }
    }

    public static class GiaiThuongTemp {
        public String cccd;
        public String maMon;
        public double diemCoMon;
        public double diemKhongMon;

        public GiaiThuongTemp() {
        }

        public GiaiThuongTemp(String cccd, String maMon, double diemCoMon, double diemKhongMon) {
            this.cccd = cccd;
            this.maMon = maMon;
            this.diemCoMon = diemCoMon;
            this.diemKhongMon = diemKhongMon;
        }
    }

    private final BonusScoreDAO bonusScoreDAO;

    public BonusScoreBUS() {
        this.bonusScoreDAO = new BonusScoreDAO();
    }

    public BonusScoreDTO layDiemCongTheoCccd(String cccd) {
        if (cccd == null || cccd.trim().isEmpty()) {
            return null;
        }
        List<BonusScoreDTO> list = bonusScoreDAO.getByCccd(cccd.trim());
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(b -> b.getDiemTong() != null)
                .max((a, b) -> Double.compare(a.getDiemTong(), b.getDiemTong()))
                .orElse(list.get(0));
    }

    public ImportResult importFromExcelFile(File file) {
        if (file == null) {
            return new ImportResult(0, 0, 0, "File import không hợp lệ", new ArrayList<>());
        }

        int skippedRows = 0;
        int parsedRows = 0;
        List<ImportErrorDetail> skippedDetails = new ArrayList<>();
        List<ImportErrorDetail> errorDetails = new ArrayList<>();

        try (InputStream is = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return new ImportResult(0, 0, 0, "File Excel không có sheet dữ liệu", new ArrayList<>());
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return new ImportResult(0, 0, 0, "File Excel không có dòng tiêu đề", new ArrayList<>());
            }

            Map<String, Integer> colIndex = buildColumnIndex(headerRow);

            boolean hasMaMon = colIndex.containsKey("ma_mon");
            boolean hasDiemCoMon = colIndex.containsKey("diem_co_mon");
            boolean hasDiemKhongMon = colIndex.containsKey("diem_khong_mon");
            boolean hasMaNganh = colIndex.containsKey("ma_nganh");
            boolean hasMaToHop = colIndex.containsKey("ma_to_hop");
            boolean hasDiemCC = colIndex.containsKey("diem_cc");
            boolean hasDiemUtxt = colIndex.containsKey("diem_utxt");

            // Mode A: Giải thưởng (phục vụ importDiemGiaiThuong)
            if (hasMaMon && (hasDiemCoMon || hasDiemKhongMon)) {
                List<GiaiThuongTemp> listGT = new ArrayList<>();
                int firstDataRow = headerRow.getRowNum() + 1;
                for (int r = firstDataRow; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                        if (row == null || isRowTrulyEmpty(row, colIndex)) {
                            continue;
                        }

                    String cccd = getCellString(row, colIndex.get("cccd"));
                    if (cccd != null) cccd = cccd.trim();
                    if (cccd == null || cccd.isEmpty()) {
                        skippedRows++;
                        skippedDetails.add(new ImportErrorDetail("", "", "CCCD không được để trống (dòng " + (r + 1) + ")"));
                        continue;
                    }

                    String maMon = getCellString(row, colIndex.get("ma_mon"));
                    Double diemCoMon = parseDouble(getCellString(row, colIndex.get("diem_co_mon")));
                    Double diemKhongMon = parseDouble(getCellString(row, colIndex.get("diem_khong_mon")));
                    if (maMon != null) maMon = maMon.trim();

                    if (maMon == null || maMon.isEmpty()) {
                        skippedRows++;
                        skippedDetails.add(new ImportErrorDetail(cccd, "", "Mã môn không được để trống (dòng " + (r + 1) + ")"));
                        continue;
                    }

                    GiaiThuongTemp gt = new GiaiThuongTemp(
                            cccd,
                            maMon,
                            diemCoMon != null ? diemCoMon : 0.0,
                            diemKhongMon != null ? diemKhongMon : 0.0
                    );
                    listGT.add(gt);
                    parsedRows++;
                }

                    HibernateBatchUtil.BatchImportResult result = importDiemGiaiThuong(listGT);
                    List<ImportErrorDetail> resultErrors = convertBatchErrors(result.getFailedDetails());
                    // merge skipped details from batch result into skippedDetails
                    List<ImportErrorDetail> batchSkipped = convertSkippedBatchErrors(result.getSkippedDetails());
                    skippedDetails.addAll(batchSkipped);
                    return new ImportResult(
                            result.getInserted(),
                            skippedRows + result.getSkipped(),
                            result.getFailed(),
                            null,
                            resultErrors,
                            skippedDetails
                    );
            }

            // Mode B: Điểm tiếng Anh / chứng chỉ theo CCCD (phục vụ importDiemTiengAnh)
            if (hasDiemCC && !hasMaNganh && !hasMaToHop) {
                Map<String, Double> mapTA = new HashMap<>();
                int firstDataRow = headerRow.getRowNum() + 1;
                for (int r = firstDataRow; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                        if (row == null || isRowTrulyEmpty(row, colIndex)) {
                            // Âm thầm bỏ qua các dòng rỗng (Phantom rows) của Excel
                            continue;
                        }

                    String cccd = getCellString(row, colIndex.get("cccd"));
                    if (cccd != null) cccd = cccd.trim();
                    if (cccd == null || cccd.isEmpty()) {
                        skippedRows++;
                        skippedDetails.add(new ImportErrorDetail("", "", "CCCD không được để trống (dòng " + (r + 1) + ")"));
                        continue;
                    }

                    Double diem = parseDouble(getCellString(row, colIndex.get("diem_cc")));
                    if (diem == null) {
                        skippedRows++;
                        skippedDetails.add(new ImportErrorDetail(cccd, "", "Điểm chứng chỉ/tiếng Anh không hợp lệ (dòng " + (r + 1) + ")"));
                        continue;
                    }

                    mapTA.put(cccd, diem);
                    parsedRows++;
                }

                HibernateBatchUtil.BatchImportResult result = importDiemTiengAnh(mapTA);
                List<ImportErrorDetail> resultErrors = convertBatchErrors(result.getFailedDetails());
                // merge skipped details from batch result into skippedDetails
                List<ImportErrorDetail> batchSkipped = convertSkippedBatchErrors(result.getSkippedDetails());
                skippedDetails.addAll(batchSkipped);
                return new ImportResult(
                        result.getInserted(),
                        skippedRows + result.getSkipped(),
                        result.getFailed(),
                        null,
                        resultErrors,
                        skippedDetails
                );
            }

            // Mode C: Full bảng điểm cộng (upsert trực tiếp)
            if (hasMaNganh && hasMaToHop) {
                List<BonusScoreDTO> list = new ArrayList<>();
                int firstDataRow = headerRow.getRowNum() + 1;
                for (int r = firstDataRow; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null || isRowTrulyEmpty(row, colIndex)) {
                        continue;
                    }

                    String cccd = safeString(getCellString(row, colIndex.get("cccd")));
                    String maNganh = safeString(getCellString(row, colIndex.get("ma_nganh")));
                    String maToHop = safeString(getCellString(row, colIndex.get("ma_to_hop")));
                    String phuongThuc = safeString(getCellString(row, colIndex.get("phuong_thuc")));
                    String ghiChu = safeString(getCellString(row, colIndex.get("ghi_chu")));

                    if (cccd.isBlank()) {
                        skippedRows++;
                        skippedDetails.add(new ImportErrorDetail("", "", "CCCD không được để trống (dòng " + (r + 1) + ")"));
                        continue;
                    }

                    List<String> rowErrors = new ArrayList<>();
                    if (maNganh.isBlank()) {
                        rowErrors.add("Mã ngành không được để trống");
                    }
                    if (maToHop.isBlank()) {
                        rowErrors.add("Mã tổ hợp không được để trống");
                    }
                    if (phuongThuc.isBlank()) {
                        rowErrors.add("Phương thức không được để trống");
                    }

                    Double diemCC = hasDiemCC ? parseDouble(getCellString(row, colIndex.get("diem_cc"))) : 0.0;
                    Double diemU = hasDiemUtxt ? parseDouble(getCellString(row, colIndex.get("diem_utxt"))) : 0.0;

                    if (hasDiemCC && getCellString(row, colIndex.get("diem_cc")) != null && diemCC == null) {
                        rowErrors.add("Điểm CC không hợp lệ");
                    }
                    if (hasDiemUtxt && getCellString(row, colIndex.get("diem_utxt")) != null && diemU == null) {
                        rowErrors.add("Điểm ƯT không hợp lệ");
                    }

                    if (!rowErrors.isEmpty()) {
                        skippedRows++;
                        skippedDetails.add(new ImportErrorDetail(cccd, buildDisplayName(cccd, maNganh), String.join("; ", rowErrors) + " (dòng " + (r + 1) + ")"));
                        continue;
                    }

                    BonusScoreDTO dto = new BonusScoreDTO();
                    dto.setTsCccd(cccd);
                    dto.setMaNganh(maNganh);
                    dto.setMaToHop(maToHop);
                    dto.setPhuongThuc(phuongThuc);
                    dto.setDiemCC(diemCC != null ? diemCC : 0.0);
                    dto.setDiemUtxt(diemU != null ? diemU : 0.0);
                    dto.setDiemTong(capTotal(dto.getDiemCC(), dto.getDiemUtxt()));
                    dto.setGhiChu(ghiChu);
                    dto.setDcKeys(buildDcKeys(cccd, maNganh, maToHop));

                    normalizeForPersist(dto);
                    // Validate minimally for required fields
                    try {
                        validateForSaveOrUpdate(dto);
                    } catch (BonusScoreValidationException vex) {
                        skippedRows++;
                        skippedDetails.add(new ImportErrorDetail(cccd, buildDisplayName(cccd, maNganh), vex.getMessageText()));
                        continue;
                    }

                    list.add(dto);
                    parsedRows++;
                }

                if (list.isEmpty()) {
                    return new ImportResult(0, skippedRows, 0, "Không có dòng hợp lệ để import", new ArrayList<>(), skippedDetails);
                }

                HibernateBatchUtil.BatchImportResult result = bonusScoreDAO.saveAllUpsert(list, 50);
                List<ImportErrorDetail> resultErrors = convertBatchErrors(result.getFailedDetails());
                List<ImportErrorDetail> batchSkipped = convertSkippedBatchErrors(result.getSkippedDetails());
                skippedDetails.addAll(batchSkipped);
                return new ImportResult(
                    result.getInserted(),
                    skippedRows + result.getSkipped(),
                    result.getFailed(),
                    null,
                    resultErrors,
                    skippedDetails
                );
            }

            return new ImportResult(0, skippedRows, 0,
                    "Không nhận dạng được định dạng file Excel.\n" +
                            "Gợi ý cột cần có: CCCD + (mã ngành, mã tổ hợp, ... ) hoặc CCCD + điểm CC hoặc CCCD + mã môn.",
                    new ArrayList<>(), skippedDetails);

        } catch (Exception e) {
            e.printStackTrace();
            return new ImportResult(0, skippedRows, 0, "Lỗi khi đọc/import file: " + e.getMessage(), new ArrayList<>(), skippedDetails);
        }
    }

    public List<Object[]> getNguyenVongByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return new ArrayList<>();
        return bonusScoreDAO.getNguyenVongByCccd(cccd.trim());
    }

    public boolean checkTonTaiDiemCong(String dcKeys) {
        if (dcKeys == null || dcKeys.isBlank()) return false;
        return bonusScoreDAO.checkTonTaiDiemCong(dcKeys.trim());
    }

    public double getDiemTiengAnhDaCo(String cccd) {
        if (cccd == null || cccd.isBlank()) return 0.0;
        Double diem = bonusScoreDAO.getDiemTiengAnhDaCo(cccd.trim());
        return diem != null ? diem : 0.0;
    }

    public void saveSingleBonusScoreAndSyncEnglishScore(BonusScoreDTO bonusScoreDTO) {
        saveSingleBonusScoreInternal(bonusScoreDTO, true);
    }

    public void updateBonusScore(BonusScoreDTO bonusScoreDTO) {
        try {
            if (bonusScoreDTO == null) {
                throw new BonusScoreValidationException(List.of("Dữ liệu điểm cộng không hợp lệ"));
            }
            normalizeForPersist(bonusScoreDTO);
            validateForSaveOrUpdate(bonusScoreDTO);
            bonusScoreDTO.setDiemTong(capTotal(
                    bonusScoreDTO.getDiemCC() != null ? bonusScoreDTO.getDiemCC() : 0.0,
                    bonusScoreDTO.getDiemUtxt() != null ? bonusScoreDTO.getDiemUtxt() : 0.0
            ));
            bonusScoreDAO.update(bonusScoreDTO);
            bonusScoreDAO.dongBoDiemTiengAnh(bonusScoreDTO.getTsCccd(), bonusScoreDTO.getDiemCC());
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof BonusScoreValidationException) {
                throw e;
            }
            throw new RuntimeException("Error updating bonus score data", e);
        }
    }

    public void deleteBonusScore(BonusScoreDTO bonusScoreDTO) {
        try {
            if (bonusScoreDTO == null) {
                throw new BonusScoreValidationException(List.of("Dữ liệu điểm cộng không hợp lệ"));
            }
            int id = bonusScoreDTO.getIdDiemCong();
            if (id <= 0) {
                throw new BonusScoreValidationException(List.of("Mã điểm cộng không hợp lệ"));
            }
            boolean deleted = bonusScoreDAO.deleteById(id);
            if (!deleted) {
                throw new BonusScoreValidationException(List.of("Không tìm thấy bản ghi điểm cộng cần xóa"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof BonusScoreValidationException) {
                throw e;
            }
            throw new RuntimeException("Error deleting bonus score data", e);
        }
    }

    public void saveSingleBonusScore(BonusScoreDTO bonusScoreDTO) {
        saveSingleBonusScoreInternal(bonusScoreDTO, false);
    }

    private void saveSingleBonusScoreInternal(BonusScoreDTO bonusScoreDTO, boolean syncEnglishScore) {
        try {
            if (bonusScoreDTO == null) {
                throw new BonusScoreValidationException(List.of("Dữ liệu điểm cộng không hợp lệ"));
            }

            normalizeForPersist(bonusScoreDTO);
            validateForSaveOrUpdate(bonusScoreDTO);
            bonusScoreDTO.setDiemTong(capTotal(
                    bonusScoreDTO.getDiemCC() != null ? bonusScoreDTO.getDiemCC() : 0.0,
                    bonusScoreDTO.getDiemUtxt() != null ? bonusScoreDTO.getDiemUtxt() : 0.0
            ));

            bonusScoreDAO.save(bonusScoreDTO);

            if (syncEnglishScore) {
                double diemCC = bonusScoreDTO.getDiemCC() != null ? bonusScoreDTO.getDiemCC() : 0.0;
                bonusScoreDAO.dongBoDiemTiengAnh(bonusScoreDTO.getTsCccd(), diemCC);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof BonusScoreValidationException) {
                throw e;
            }
            throw new RuntimeException("Error saving single bonus score data", e);
        }
    }

    private void validateForSaveOrUpdate(BonusScoreDTO bonusScoreDTO) {
        List<String> errors = new ArrayList<>();

        String cccd = safeString(bonusScoreDTO.getTsCccd());
        if (cccd.isBlank()) {
            errors.add("CCCD không được để trống");
        }

        String dcKeys = safeString(bonusScoreDTO.getDcKeys());
        if (dcKeys.isBlank()) {
            errors.add("dc_keys không được để trống");
        }

        Double diemCC = bonusScoreDTO.getDiemCC();
        Double diemUtxt = bonusScoreDTO.getDiemUtxt();
        Double diemTong = bonusScoreDTO.getDiemTong();

        if (diemCC != null && diemCC < 0) {
            errors.add("Điểm CC không được âm");
        }
        if (diemUtxt != null && diemUtxt < 0) {
            errors.add("Điểm ƯT không được âm");
        }
        if (diemTong != null && diemTong < 0) {
            errors.add("Tổng điểm cộng không được âm");
        }

        if (!errors.isEmpty()) {
            throw new BonusScoreValidationException(errors);
        }
    }

    public HibernateBatchUtil.BatchImportResult importDiemTiengAnh(Map<String, Double> mapTA) {
        Map<String, Double> safeMapTA = mapTA != null ? mapTA : new HashMap<>();
        int skippedRows = 0;
        List<HibernateBatchUtil.RowImportError> failedDetails = new ArrayList<>();
        List<HibernateBatchUtil.RowImportError> skippedDetails = new ArrayList<>();

        // Build index of existing bonus scores by dcKeys using pagination to avoid loading all at once
        Map<String, BonusScoreDTO> mapDaCoTheoKey = new LinkedHashMap<>();
        final int daoPage = 500;
        int off = 0;
        List<BonusScoreDTO> pageRows;
        do {
            pageRows = bonusScoreDAO.getAllWithPagination(off, daoPage);
            if (pageRows != null) {
                for (BonusScoreDTO r : pageRows) {
                    if (r == null) continue;
                    String key = buildDcKeys(r.getTsCccd(), r.getMaNganh(), r.getMaToHop());
                    if (!key.isBlank()) mapDaCoTheoKey.put(key, r);
                }
                off += pageRows.size();
            } else {
                break;
            }
        } while (pageRows.size() == daoPage);

        // Iterate aspirations by pages, compute and batch upsert
        List<BonusScoreDTO> listKetQua = new ArrayList<>();
        off = 0;
        final int saveBatch = 500;
        List<Object[]> aspPage;
        int totalInserted = 0;
        int totalFailed = 0;
        int totalSkipped = 0;
        List<String> totalFailedCccds = new ArrayList<>();
        List<HibernateBatchUtil.RowImportError> totalFailedDetails = new ArrayList<>();
        List<HibernateBatchUtil.RowImportError> totalSkippedDetails = new ArrayList<>();

        do {
            aspPage = bonusScoreDAO.getAllNguyenVongBasicWithPagination(off, daoPage);
            if (aspPage == null || aspPage.isEmpty()) break;

            for (Object[] row : aspPage) {
                if (row == null || row.length < 4) {
                    skippedRows++;
                    failedDetails.add(new HibernateBatchUtil.RowImportError("", "", "Dòng nguyện vọng không hợp lệ"));
                    continue;
                }
                String cccd = safeString(row[0]);
                String maNganh = safeString(row[1]);
                String maToHop = safeString(row[2]);
                String phuongThuc = safeString(row[3]);
                if (cccd.isBlank()) {
                    skippedRows++;
                    failedDetails.add(new HibernateBatchUtil.RowImportError("", "", "CCCD không được để trống"));
                    continue;
                }

                Double diemTA = safeMapTA.get(cccd);
                if (diemTA == null) {
                    // Thí sinh này trong DB không có trong file Excel Tiếng Anh vừa up.
                    // Việc này là bình thường, âm thầm lướt qua, không ghi log rác.
                    continue;
                }

                String dcKeys = buildDcKeys(cccd, maNganh, maToHop);
                BonusScoreDTO existing = mapDaCoTheoKey.get(dcKeys);

                BonusScoreDTO bonusScore = existing != null ? existing : new BonusScoreDTO();
                if (existing != null && existing.getIdDiemCong() > 0) {
                    bonusScore.setIdDiemCong(existing.getIdDiemCong());
                }

                bonusScore.setTsCccd(cccd);
                bonusScore.setMaNganh(maNganh);
                bonusScore.setMaToHop(maToHop);
                bonusScore.setPhuongThuc(phuongThuc);
                bonusScore.setDiemCC(diemTA);
                bonusScore.setDiemUtxt(existing != null && existing.getDiemUtxt() != null ? existing.getDiemUtxt() : 0.0);
                bonusScore.setDiemTong(capTotal(bonusScore.getDiemCC(), bonusScore.getDiemUtxt()));
                bonusScore.setDcKeys(dcKeys);

                normalizeForPersist(bonusScore);
                listKetQua.add(bonusScore);

                if (listKetQua.size() >= saveBatch) {
                    HibernateBatchUtil.BatchImportResult batchRes = bonusScoreDAO.saveAllUpsert(listKetQua);
                    totalInserted += batchRes.getInserted();
                    totalFailed += batchRes.getFailed();
                    totalSkipped += batchRes.getSkipped();
                    if (batchRes.getFailedCccds() != null) totalFailedCccds.addAll(batchRes.getFailedCccds());
                    if (batchRes.getFailedDetails() != null) totalFailedDetails.addAll(batchRes.getFailedDetails());
                    if (batchRes.getSkippedDetails() != null) totalSkippedDetails.addAll(batchRes.getSkippedDetails());
                    listKetQua.clear();
                }
            }

            off += aspPage.size();
        } while (aspPage.size() == daoPage);

        if (!listKetQua.isEmpty()) {
            HibernateBatchUtil.BatchImportResult result = bonusScoreDAO.saveAllUpsert(listKetQua);
            totalInserted += result.getInserted();
            totalFailed += result.getFailed();
            totalSkipped += result.getSkipped();
            if (result.getFailedCccds() != null) totalFailedCccds.addAll(result.getFailedCccds());
            if (result.getFailedDetails() != null) totalFailedDetails.addAll(result.getFailedDetails());
            if (result.getSkippedDetails() != null) totalSkippedDetails.addAll(result.getSkippedDetails());
        }

        // merge in-row failed/skipped details collected while building list from aspirations
        totalFailedDetails = mergeFailedDetails(failedDetails, totalFailedDetails);
        totalSkippedDetails = mergeFailedDetails(skippedDetails, totalSkippedDetails);

        return new HibernateBatchUtil.BatchImportResult(
            totalInserted,
            totalFailed,
            skippedRows + totalSkipped,
            totalFailedCccds,
            totalFailedDetails,
            totalSkippedDetails
        );
    }

    public HibernateBatchUtil.BatchImportResult importDiemGiaiThuong(List<GiaiThuongTemp> listGT) {
        List<GiaiThuongTemp> safeListGT = listGT != null ? listGT : new ArrayList<>();
        Map<String, GiaiThuongTemp> mapGiaiThuong = indexGiaiThuongByCccd(safeListGT);
        int skippedRows = 0;
        List<HibernateBatchUtil.RowImportError> failedDetails = new ArrayList<>();
        List<HibernateBatchUtil.RowImportError> skippedDetails = new ArrayList<>();

        // Build index of existing bonus scores
        Map<String, BonusScoreDTO> mapDaCoTheoKey = new LinkedHashMap<>();
        final int daoPage = 500;
        int off = 0;
        List<BonusScoreDTO> pageRows;
        do {
            pageRows = bonusScoreDAO.getAllWithPagination(off, daoPage);
            if (pageRows != null) {
                for (BonusScoreDTO r : pageRows) {
                    if (r == null) continue;
                    String key = buildDcKeys(r.getTsCccd(), r.getMaNganh(), r.getMaToHop());
                    if (!key.isBlank()) mapDaCoTheoKey.put(key, r);
                }
                off += pageRows.size();
            } else break;
        } while (pageRows.size() == daoPage);

        // Iterate aspirations by pages and compute diemUtxt
        List<BonusScoreDTO> listKetQua = new ArrayList<>();
        off = 0;
        final int saveBatch = 500;
        int totalInserted = 0;
        int totalFailed = 0;
        int totalSkipped = 0;
        List<String> totalFailedCccds = new ArrayList<>();
        List<HibernateBatchUtil.RowImportError> totalFailedDetails = new ArrayList<>();
        List<HibernateBatchUtil.RowImportError> totalSkippedDetails = new ArrayList<>();
        List<Object[]> aspPage;
        do {
            aspPage = bonusScoreDAO.getAllNguyenVongBasicWithPagination(off, daoPage);
            if (aspPage == null || aspPage.isEmpty()) break;

            for (Object[] row : aspPage) {
                if (row == null || row.length < 4) {
                    skippedRows++;
                    failedDetails.add(new HibernateBatchUtil.RowImportError("", "", "Dòng nguyện vọng không hợp lệ"));
                    continue;
                }
                String cccd = safeString(row[0]);
                String maNganh = safeString(row[1]);
                String maToHop = safeString(row[2]);
                String phuongThuc = safeString(row[3]);
                if (cccd.isBlank()) {
                    skippedRows++;
                    failedDetails.add(new HibernateBatchUtil.RowImportError("", "", "CCCD không được để trống"));
                    continue;
                }

                GiaiThuongTemp giaiThuong = mapGiaiThuong.get(cccd);
                if (giaiThuong == null) {
                    // Thí sinh này không có trong file Excel Giải Thưởng. Lướt qua!
                    continue;
                }

                double diemUtxtMoi;
                String ptUpper = phuongThuc != null ? phuongThuc.toUpperCase() : "";

                // Gom ĐGNL và VSAT vào chung 1 nhóm: KHÔNG xét môn, tự động lấy diemKhongMon
                if (ptUpper.contains("DGNL") || ptUpper.contains("ĐGNL") || ptUpper.contains("VSAT")) {
                    diemUtxtMoi = giaiThuong.diemKhongMon;
                    
                // Chỉ có THPT (Xét điểm thi) mới cần tra cứu tổ hợp môn xem có môn đạt giải hay không
                } else if (ptUpper.contains("THPT") || ptUpper.contains("XÉT ĐIỂM THI")) {
                    boolean chuaMon = bonusScoreDAO.checkToHopChuaMon(maToHop, giaiThuong.maMon);
                    diemUtxtMoi = chuaMon ? giaiThuong.diemCoMon : giaiThuong.diemKhongMon;
                    
                // Các phương thức khác (nếu có) mặc định lấy điểm không môn
                } else {
                    diemUtxtMoi = giaiThuong.diemKhongMon;
                }

                String dcKeys = buildDcKeys(cccd, maNganh, maToHop);
                BonusScoreDTO existing = mapDaCoTheoKey.get(dcKeys);

                BonusScoreDTO bonusScore = existing != null ? existing : new BonusScoreDTO();
                if (existing != null && existing.getIdDiemCong() > 0) {
                    bonusScore.setIdDiemCong(existing.getIdDiemCong());
                }

                bonusScore.setTsCccd(cccd);
                bonusScore.setMaNganh(maNganh);
                bonusScore.setMaToHop(maToHop);
                bonusScore.setPhuongThuc(phuongThuc);
                bonusScore.setDiemCC(existing != null && existing.getDiemCC() != null ? existing.getDiemCC() : 0.0);
                bonusScore.setDiemUtxt(diemUtxtMoi);
                bonusScore.setDiemTong(capTotal(bonusScore.getDiemCC(), bonusScore.getDiemUtxt()));
                bonusScore.setDcKeys(dcKeys);

                normalizeForPersist(bonusScore);
                listKetQua.add(bonusScore);

                if (listKetQua.size() >= saveBatch) {
                    HibernateBatchUtil.BatchImportResult batchRes = bonusScoreDAO.saveAllUpsert(listKetQua);
                    totalInserted += batchRes.getInserted();
                    totalFailed += batchRes.getFailed();
                    totalSkipped += batchRes.getSkipped();
                    if (batchRes.getFailedCccds() != null) totalFailedCccds.addAll(batchRes.getFailedCccds());
                    if (batchRes.getFailedDetails() != null) totalFailedDetails.addAll(batchRes.getFailedDetails());
                    if (batchRes.getSkippedDetails() != null) totalSkippedDetails.addAll(batchRes.getSkippedDetails());
                    listKetQua.clear();
                }
            }

            off += aspPage.size();
        } while (aspPage.size() == daoPage);

        if (!listKetQua.isEmpty()) {
            HibernateBatchUtil.BatchImportResult result = bonusScoreDAO.saveAllUpsert(listKetQua);
            totalInserted += result.getInserted();
            totalFailed += result.getFailed();
            totalSkipped += result.getSkipped();
            if (result.getFailedCccds() != null) totalFailedCccds.addAll(result.getFailedCccds());
            if (result.getFailedDetails() != null) totalFailedDetails.addAll(result.getFailedDetails());
            if (result.getSkippedDetails() != null) totalSkippedDetails.addAll(result.getSkippedDetails());
        }

        // merge in-row batch-level failed/skipped details
        totalFailedDetails = mergeFailedDetails(failedDetails, totalFailedDetails);
        totalSkippedDetails = mergeFailedDetails(skippedDetails, totalSkippedDetails);

        return new HibernateBatchUtil.BatchImportResult(
            totalInserted,
            totalFailed,
            skippedRows + totalSkipped,
            totalFailedCccds,
            totalFailedDetails,
            totalSkippedDetails
        );
    }

    public void processAndSaveBonusScores(Map<String, Double> mapTA, List<GiaiThuongTemp> listGT) {
        importDiemTiengAnh(mapTA);
        importDiemGiaiThuong(listGT);
    }

    public long getBonusScoreCount() {
        return bonusScoreDAO.getCount();
    }

    public long countBonusScoresByCccd(String cccdPattern) {
        return bonusScoreDAO.countSearchResults(cccdPattern);
    }

    public List<BonusScoreDTO> getBonusScoresWithPagination(int offset, int limit) {
        return bonusScoreDAO.getAllWithPagination(offset, limit);
    }

    public List<BonusScoreDTO> searchBonusScoresWithPagination(String cccdPattern, int offset, int limit) {
        return bonusScoreDAO.getSearchWithPagination(cccdPattern, offset, limit);
    }

    private Map<String, BonusScoreDTO> indexExistingByDcKeys(List<BonusScoreDTO> listDaCo) {
        Map<String, BonusScoreDTO> map = new LinkedHashMap<>();
        if (listDaCo == null) {
            return map;
        }
        for (BonusScoreDTO bonusScore : listDaCo) {
            if (bonusScore == null) {
                continue;
            }
            String key = buildDcKeys(bonusScore.getTsCccd(), bonusScore.getMaNganh(), bonusScore.getMaToHop());
            if (!key.isBlank()) {
                map.put(key, bonusScore);
            }
        }
        return map;
    }

    private Map<String, GiaiThuongTemp> indexGiaiThuongByCccd(List<GiaiThuongTemp> listGT) {
        Map<String, GiaiThuongTemp> map = new HashMap<>();
        for (GiaiThuongTemp gt : listGT) {
            if (gt == null || gt.cccd == null || gt.cccd.isBlank()) {
                continue;
            }
            map.put(gt.cccd.trim(), gt);
        }
        return map;
    }

    private void normalizeForPersist(BonusScoreDTO bonusScoreDTO) {
        if (bonusScoreDTO == null) {
            return;
        }
        bonusScoreDTO.setTsCccd(safeString(bonusScoreDTO.getTsCccd()));
        bonusScoreDTO.setMaNganh(safeString(bonusScoreDTO.getMaNganh()));
        bonusScoreDTO.setMaToHop(safeString(bonusScoreDTO.getMaToHop()));
        bonusScoreDTO.setPhuongThuc(safeString(bonusScoreDTO.getPhuongThuc()));
        bonusScoreDTO.setGhiChu(safeString(bonusScoreDTO.getGhiChu()));
        bonusScoreDTO.setDcKeys(safeString(bonusScoreDTO.getDcKeys()));
        bonusScoreDTO.setDiemCC(bonusScoreDTO.getDiemCC() != null ? bonusScoreDTO.getDiemCC() : 0.0);
        bonusScoreDTO.setDiemUtxt(bonusScoreDTO.getDiemUtxt() != null ? bonusScoreDTO.getDiemUtxt() : 0.0);
        bonusScoreDTO.setDiemTong(bonusScoreDTO.getDiemTong() != null ? bonusScoreDTO.getDiemTong() : 0.0);
    }

    private double capTotal(double diemCC, double diemUtxt) {
        double total = diemCC + diemUtxt;
        return Math.min(total, MAX_BONUS_TOTAL);
    }

    private String buildDcKeys(String cccd, String maNganh, String maToHop) {
        return safeString(cccd) + "_" + safeString(maNganh) + "_" + safeString(maToHop);
    }

    private Map<String, Integer> buildColumnIndex(Row headerRow) {
        Map<String, Integer> colIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = normalizeHeader(ExcelHelper.getCellValueAsString(cell));
            int idx = cell.getColumnIndex();

            if (matchesAny(header, "cccd", "ts cccd", "so cccd")) {
                colIndex.put("cccd", idx);
            } else if (matchesAny(header, "ma nganh", "manganh")) {
                colIndex.put("ma_nganh", idx);
            } else if (matchesAny(header, "ma to hop", "matohop", "to hop")) {
                colIndex.put("ma_to_hop", idx);
            } else if (matchesAny(header, "phuong thuc", "phuongthuc")) {
                colIndex.put("phuong_thuc", idx);
            } else if (matchesAny(header, "diem cc", "diem chung chi", "diem tieng anh", "diem ta", "diem cong")) {
                colIndex.put("diem_cc", idx);
            } else if (matchesAny(header, "diem ut", "diem uu tien", "diemuxt", "diem utxt")) {
                colIndex.put("diem_utxt", idx);
            } else if (matchesAny(header, "ghi chu", "ghichu", "note")) {
                colIndex.put("ghi_chu", idx);
            } else if (matchesAny(header, "ma mon", "mamon")) {
                colIndex.put("ma_mon", idx);
            } else if (matchesAny(header, "diem co mon", "diem comon", "diem comon", "diem cong cho mon dat giai")) {
                colIndex.put("diem_co_mon", idx);
            } else if (matchesAny(header, "diem khong mon", "diem khongmon", "diem khong mon", "diem cong cho thxt ko co mon dat giai")) {
                colIndex.put("diem_khong_mon", idx);
            }
        }
        return colIndex;
    }

    private String getCellString(Row row, Integer colIdx) {
        if (colIdx == null) return "";
        Cell cell = row.getCell(colIdx);
        return ExcelHelper.getCellValueAsString(cell);
    }

    private boolean isRowTrulyEmpty(Row row, Map<String, Integer> colIndex) {
        if (row == null) return true;
        if (colIndex != null && !colIndex.isEmpty()) {
            for (Integer idx : colIndex.values()) {
                if (idx == null) continue;
                String v = ExcelHelper.getCellValueAsString(row.getCell(idx));
                if (v != null && !v.trim().isEmpty()) return false;
            }
            return true;
        }
        // Fallback: check all cells in the row
        short first = row.getFirstCellNum();
        short last = row.getLastCellNum();
        if (first < 0 || last < 0) return true;
        for (int i = first; i < last; i++) {
            String v = ExcelHelper.getCellValueAsString(row.getCell(i));
            if (v != null && !v.trim().isEmpty()) return false;
        }
        return true;
    }

    private Double parseDouble(String input) {
        if (input == null) {
            return null;
        }
        String normalized = input.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        normalized = normalized.replace(',', '.');
        normalized = normalized.replaceAll("\\s+", "");
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<ImportErrorDetail> convertBatchErrors(List<HibernateBatchUtil.RowImportError> batchErrors) {
        List<ImportErrorDetail> details = new ArrayList<>();
        if (batchErrors == null) {
            return details;
        }
        for (HibernateBatchUtil.RowImportError err : batchErrors) {
            if (err == null) {
                continue;
            }
            details.add(new ImportErrorDetail(
                    err.getCccd() != null ? err.getCccd() : "",
                    err.getHoTen() != null ? err.getHoTen() : "",
                    err.getReason() != null ? err.getReason() : "Không thể lưu vào CSDL"
            ));
        }
        return details;
    }

    private List<ImportErrorDetail> convertSkippedBatchErrors(List<HibernateBatchUtil.RowImportError> batchErrors) {
        List<ImportErrorDetail> details = new ArrayList<>();
        if (batchErrors == null) return details;
        for (HibernateBatchUtil.RowImportError err : batchErrors) {
            if (err == null) continue;
            String reason = err.getReason() != null ? err.getReason() : "Bị bỏ qua";
            if (err.getRowNum() > 0) {
                reason = reason + " (dòng " + err.getRowNum() + ")";
            }
            details.add(new ImportErrorDetail(
                    err.getCccd() != null ? err.getCccd() : "",
                    err.getHoTen() != null ? err.getHoTen() : "",
                    reason
            ));
        }
        return details;
    }

    private List<HibernateBatchUtil.RowImportError> mergeFailedDetails(List<HibernateBatchUtil.RowImportError> first, List<HibernateBatchUtil.RowImportError> second) {
        List<HibernateBatchUtil.RowImportError> merged = new ArrayList<>();
        if (first != null) {
            merged.addAll(first);
        }
        if (second != null) {
            merged.addAll(second);
        }
        return merged;
    }

    private String buildDisplayName(String cccd, String maNganh) {
        String left = safeString(cccd);
        String right = safeString(maNganh);
        if (left.isBlank()) {
            return right;
        }
        if (right.isBlank()) {
            return left;
        }
        return left + " - " + right;
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        normalized = normalized.toLowerCase().trim();
        normalized = normalized.replace('đ', 'd');
        return normalized.replaceAll("\\s+", " ");
    }

    private boolean matchesAny(String normalizedHeader, String... candidates) {
        if (normalizedHeader == null) {
            return false;
        }
        for (String c : candidates) {
            if (c == null) continue;
            String cn = normalizeHeader(c);
            if (!cn.isEmpty() && normalizedHeader.equals(cn)) {
                return true;
            }
        }
        return false;
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }

    public int batchUpdateAll(List<BonusScoreDTO> list) {
        if (list == null || list.isEmpty()) return 0;
        return bonusScoreDAO.batchUpdateAll(list);
    }
}
