package com.example.bus;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.regex.Pattern;

import com.example.dao.CandidateDAO;
import com.example.dto.CandidateDTO;

import com.example.utils.ExcelHelper;
import com.example.utils.HibernateBatchUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class CandidateBUS {
    private final CandidateDAO candidateDAO;

    private static final int MAX_CCCD_LENGTH = 20;
    private static final int MAX_SBD_LENGTH = 45;
    private static final int MAX_HO_LENGTH = 100;
    private static final int MAX_TEN_LENGTH = 100;
    private static final int MAX_PHONE_LENGTH = 10;
    private static final int MAX_EMAIL_LENGTH = 100;
    private static final int MAX_NOI_SINH_LENGTH = 45;
    private static final int MAX_DOI_TUONG_LENGTH = 45;
    private static final int MAX_KHU_VUC_LENGTH = 45;
    private static final int MIN_BIRTH_YEAR = 1950;
    private static final int MAX_BIRTH_YEAR = 2008;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static class ImportErrorDetail {
        private final String cccd;
        private final String hoTen;
        private final String reason;

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

    public CandidateBUS() {
        this.candidateDAO = new CandidateDAO();
    }

    public CandidateBUS(CandidateDAO candidateDAO) {
        this.candidateDAO = candidateDAO != null ? candidateDAO : new CandidateDAO();
    }

    public List<CandidateDTO> getAllWithPagination(int offset, int limit) {
        if (offset < 0) {
            offset = 0;
        }
        if (limit <= 0) {
            limit = 20; 
        }
        try {
            return candidateDAO.getAllWithPagination(offset, limit);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching candidates with pagination", e);
        }
    }

    public void saveSingleCandidate(CandidateDTO candidate) {
        try {
            normalizeAndTruncateForPersist(candidate);
            validateForCreate(candidate);
            candidateDAO.save(candidate);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof CandidateValidationException) {
                throw e;
            }
            throw new RuntimeException("Error saving single candidate data", e);
        }
    }

    public void updateCandidate(CandidateDTO candidate) {
        try {
            normalizeAndTruncateForPersist(candidate);
            validateForUpdate(candidate);
            candidateDAO.update(candidate);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof CandidateValidationException) {
                throw e;
            }
            throw new RuntimeException("Error updating candidate data", e);
        }
    }

    public List<CandidateDTO> searchByCccdOrName(String keyword, int offset, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllWithPagination(offset, limit);
        }
        return candidateDAO.searchByCccdOrName(keyword.trim(), offset, limit);
    }

    public long getCount() {
        return candidateDAO.getCount();
    }

    public long getSearchCount(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getCount();
        }
        return candidateDAO.countSearchResults(keyword.trim());
    }

    public CandidateDTO findByCccd(String cccd) {
        if (cccd == null || cccd.trim().isEmpty()) {
            return null;
        }
        return candidateDAO.findByCccd(cccd.trim());
    }

    public ImportResult importFromExcelFile(File file) {
        if (file == null) {
            return new ImportResult(0, 0, 0, "File import không hợp lệ", new ArrayList<>());
        }

        List<CandidateDTO> candidates = new ArrayList<>();
        int skippedRows = 0;
        Set<String> seenInFile = new HashSet<>();
        List<ImportErrorDetail> skippedDetails = new ArrayList<>();

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

            int firstDataRow = headerRow.getRowNum() + 1;
            for (int r = firstDataRow; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    skippedRows++;
                    skippedDetails.add(new ImportErrorDetail("", "", "Dòng trống"));
                    continue;
                }

                String cccd = getCellString(row, colIndex.get("cccd"));
                if (cccd != null) {
                    cccd = cccd.trim();
                }
                if (cccd == null || cccd.isEmpty()) {
                    skippedRows++;
                    String hoTenEmpty = getCellString(row, colIndex.get("ho_ten"));
                    skippedDetails.add(new ImportErrorDetail("", hoTenEmpty != null ? hoTenEmpty : "", "CCCD không được để trống"));
                    continue;
                }

                // Trùng CCCD trong chính file -> bỏ qua để tránh save trùng và làm chậm.
                if (!seenInFile.add(cccd)) {
                    skippedRows++;
                    String hoTenDup = getCellString(row, colIndex.get("ho_ten"));
                    skippedDetails.add(new ImportErrorDetail(cccd, hoTenDup != null ? hoTenDup : "", "Trùng CCCD trong file"));
                    continue;
                }

                CandidateDTO candidate = new CandidateDTO();
                candidate.setCccd(cccd);

                String soBaoDanh = getCellString(row, colIndex.get("so_bao_danh"));
                String hoTen = getCellString(row, colIndex.get("ho_ten"));
                String ngaySinhRaw = getCellString(row, colIndex.get("ngay_sinh"));
                String gioiTinh = getCellString(row, colIndex.get("gioi_tinh"));
                String doiTuong = getCellString(row, colIndex.get("doi_tuong"));
                String khuVuc = getCellString(row, colIndex.get("khu_vuc"));
                String noiSinh = getCellString(row, colIndex.get("noi_sinh"));
                String dienThoai = getCellString(row, colIndex.get("dien_thoai"));
                String email = getCellString(row, colIndex.get("email"));

                String ngaySinh = normalizeDate(ngaySinhRaw);

                if (soBaoDanh != null && !soBaoDanh.isEmpty()) candidate.setSoBaoDanh(soBaoDanh);
                if (hoTen != null && !hoTen.isEmpty()) candidate.setTen(hoTen);
                if (ngaySinh != null && !ngaySinh.isEmpty()) candidate.setNgaySinh(ngaySinh);
                if (gioiTinh != null && !gioiTinh.isEmpty()) candidate.setGioiTinh(gioiTinh);
                if (doiTuong != null && !doiTuong.isEmpty()) candidate.setDoiTuong(doiTuong);
                if (khuVuc != null && !khuVuc.isEmpty()) candidate.setKhuVuc(khuVuc);
                if (noiSinh != null && !noiSinh.isEmpty()) candidate.setNoiSinh(noiSinh);
                if (dienThoai != null && !dienThoai.isEmpty()) candidate.setSoDienThoai(dienThoai);
                if (email != null && !email.isEmpty()) candidate.setEmail(email);
                candidate.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));

                normalizeAndTruncateForPersist(candidate);

                List<String> rowErrors = validateForImport(candidate);
                if (!rowErrors.isEmpty()) {
                    skippedRows++;
                    String hoTenForErr = candidate.getTen() != null ? candidate.getTen() : getCellString(row, colIndex.get("ho_ten"));
                    skippedDetails.add(new ImportErrorDetail(cccd, hoTenForErr != null ? hoTenForErr : "", String.join("; ", rowErrors)));
                    continue;
                }

                candidates.add(candidate);
            }

            if (candidates.isEmpty()) {
                return new ImportResult(0, skippedRows, 0, "Không có dòng hợp lệ để import", new ArrayList<>());
            }

            // Lọc CCCD đã có sẵn trong DB để import lại file không bị chậm + không báo lỗi hàng loạt.
            List<String> inputCccds = new ArrayList<>();
            for (CandidateDTO c : candidates) {
                if (c.getCccd() != null && !c.getCccd().isEmpty()) {
                    inputCccds.add(c.getCccd());
                }
            }
            Set<String> existingCccds = new HashSet<>(candidateDAO.findExistingCccds(inputCccds));
            if (!existingCccds.isEmpty()) {
                List<CandidateDTO> filtered = new ArrayList<>(candidates.size());
                for (CandidateDTO c : candidates) {
                    if (c.getCccd() != null && existingCccds.contains(c.getCccd())) {
                        skippedRows++;
                        String ho = c.getHo() != null ? c.getHo() + " " + (c.getTen() != null ? c.getTen() : "") : (c.getTen() != null ? c.getTen() : "");
                        skippedDetails.add(new ImportErrorDetail(c.getCccd(), ho.trim(), "Đã tồn tại trong CSDL"));
                        continue;
                    }
                    filtered.add(c);
                }
                candidates = filtered;
            }

            // Không có dòng mới (tất cả đã tồn tại) -> coi như OK, chỉ báo bỏ qua.
            if (candidates.isEmpty()) {
                return new ImportResult(0, skippedRows, 0, null, new ArrayList<>());
            }

            HibernateBatchUtil.BatchImportResult batchResult = candidateDAO.saveAllWithResult(candidates, 50);
            int insertedRows = batchResult.getInserted();
            int failedRows = batchResult.getFailed();
            int batchSkipped = batchResult.getSkipped();

            List<ImportErrorDetail> errorDetails = new ArrayList<>();
            if (batchResult.getFailedDetails() != null && !batchResult.getFailedDetails().isEmpty()) {
                for (HibernateBatchUtil.RowImportError err : batchResult.getFailedDetails()) {
                    if (err == null) continue;
                    String cccd = err.getCccd() != null ? err.getCccd() : "";
                    String hoTen = err.getHoTen() != null ? err.getHoTen() : "";
                    String reason = err.getReason() != null && !err.getReason().isEmpty()
                            ? err.getReason()
                            : "Không thể lưu vào CSDL";
                    errorDetails.add(new ImportErrorDetail(cccd, hoTen, reason));
                }
            } else {
                for (String failedCccd : batchResult.getFailedCccds()) {
                    errorDetails.add(new ImportErrorDetail(failedCccd, "", "Không thể lưu vào CSDL"));
                }
            }

            // Include skipped counted by batch util (e.g., duplicates detected during per-row fallback)
            skippedRows += batchSkipped;
            return new ImportResult(insertedRows, skippedRows, failedRows, null, errorDetails, skippedDetails);

        } catch (Exception e) {
            e.printStackTrace();
            return new ImportResult(0, skippedRows, 0, "Lỗi khi đọc/import file: " + e.getMessage(), new ArrayList<>());
        }
    }

    public boolean saveAllFromExcelFile(File file) {
        ImportResult result = importFromExcelFile(file);
        return !result.hasFatalError() && result.getInsertedRows() > 0;
    }

    private Map<String, Integer> buildColumnIndex(Row headerRow) {
        Map<String, Integer> colIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = normalizeHeader(ExcelHelper.getCellValueAsString(cell));
            int idx = cell.getColumnIndex();

            if (matchesAny(header, "cccd")) {
                colIndex.put("cccd", idx);
            } else if (matchesAny(header, "so bao danh", "sobaodanh", "sbd")) {
                colIndex.put("so_bao_danh", idx);
            } else if (matchesAny(header, "ho ten", "hoten")) {
                colIndex.put("ho_ten", idx);
            } else if (matchesAny(header, "ngay sinh", "ngaysinh")) {
                colIndex.put("ngay_sinh", idx);
            } else if (matchesAny(header, "gioi tinh", "gioitinh")) {
                colIndex.put("gioi_tinh", idx);
            } else if (matchesAny(header, "doi tuong uu tien", "doi tuong", "dtut")) {
                colIndex.put("doi_tuong", idx);
            } else if (matchesAny(header, "khu vuc uu tien", "khu vuc", "kvut")) {
                colIndex.put("khu_vuc", idx);
            } else if (matchesAny(header, "noi sinh", "noisinh")) {
                colIndex.put("noi_sinh", idx);
            } else if (matchesAny(header, "dien thoai", "dien thoai lien he", "phone", "sdt")) {
                colIndex.put("dien_thoai", idx);
            } else if (matchesAny(header, "email")) {
                colIndex.put("email", idx);
            }
        }
        return colIndex;
    }

    private String normalizeDate(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        Date parsed = parseStorageDate(input);
        if (parsed == null) {
            return input.trim();
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(parsed);
    }

    private String getCellString(Row row, Integer colIdx) {
        if (colIdx == null) return "";
        Cell cell = row.getCell(colIdx);
        return ExcelHelper.getCellValueAsString(cell);
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

    private void validateForCreate(CandidateDTO candidate) {
        List<String> errors = new ArrayList<>();
        if (candidate == null) {
            errors.add("Dữ liệu thí sinh không hợp lệ");
            throw new CandidateValidationException(errors);
        }

        String cccd = safeTrim(candidate.getCccd());
        if (isBlank(cccd)) {
            errors.add("CCCD không được để trống");
        } else {
            if (!cccd.matches("^\\d{12}$")) {
                errors.add("CCCD phải gồm đúng 12 chữ số");
            }
            if (candidateDAO.findByCccd(cccd) != null) {
                errors.add("CCCD đã tồn tại");
            }
        }

        String ho = safeTrim(candidate.getHo());
        if (isBlank(ho)) {
            errors.add("Họ không được để trống");
        }

        validateSharedRules(candidate, errors, true, true, true);

        if (!errors.isEmpty()) {
            throw new CandidateValidationException(errors);
        }
    }

    private void validateForUpdate(CandidateDTO candidate) {
        List<String> errors = new ArrayList<>();
        if (candidate == null) {
            errors.add("Dữ liệu thí sinh không hợp lệ");
            throw new CandidateValidationException(errors);
        }

        String cccd = safeTrim(candidate.getCccd());
        if (isBlank(cccd)) {
            errors.add("CCCD không được để trống");
        } else {
            CandidateDTO existing = candidateDAO.findByCccd(cccd);
            if (existing != null && existing.getId() != candidate.getId()) {
                errors.add("CCCD đã tồn tại");
            }
        }

        validateSharedRules(candidate, errors, false, false, true);

        if (!errors.isEmpty()) {
            throw new CandidateValidationException(errors);
        }
    }

    private List<String> validateForImport(CandidateDTO candidate) {
        if (candidate == null) {
            return Collections.singletonList("Dữ liệu thí sinh không hợp lệ");
        }
        List<String> errors = new ArrayList<>();

        String cccd = safeTrim(candidate.getCccd());
        if (isBlank(cccd)) {
            errors.add("CCCD không được để trống");
        }

        String ten = safeTrim(candidate.getTen());
        if (isBlank(ten)) {
            errors.add("Tên không được để trống");
        }

        // For imports, do not require 'khu_vuc' to be present (Excel may leave it empty)
        validateSharedRules(candidate, errors, false, false, false);
        return errors;
    }

    private void validateSharedRules(CandidateDTO candidate, List<String> errors, boolean requirePhone, boolean requireCccdDigits, boolean requireKhuVuc) {
        String ten = safeTrim(candidate.getTen());
        if (isBlank(ten)) {
            errors.add("Tên không được để trống");
        } else if (ten.length() > MAX_TEN_LENGTH) {
            errors.add("Tên không được quá " + MAX_TEN_LENGTH + " ký tự");
        }

        String ho = safeTrim(candidate.getHo());
        if (!isBlank(ho) && ho.length() > MAX_HO_LENGTH) {
            errors.add("Họ không được quá " + MAX_HO_LENGTH + " ký tự");
        }

        String sbd = safeTrim(candidate.getSoBaoDanh());
        if (!isBlank(sbd) && sbd.length() > MAX_SBD_LENGTH) {
            errors.add("Số báo danh không được quá " + MAX_SBD_LENGTH + " ký tự");
        }

        String phone = safeTrim(candidate.getSoDienThoai());
        if (requirePhone && isBlank(phone)) {
            errors.add("Số điện thoại không được để trống");
        }
        if (!isBlank(phone)) {
            if (phone.length() > MAX_PHONE_LENGTH) {
                errors.add("Số điện thoại không được quá " + MAX_PHONE_LENGTH + " số");
            }
            if (!phone.matches("^0\\d*$")) {
                errors.add("Số điện thoại phải bắt đầu bằng số 0 và chỉ chứa số");
            }
            if (phone.length() != MAX_PHONE_LENGTH && requirePhone) {
                errors.add("Số điện thoại phải có đúng " + MAX_PHONE_LENGTH + " số");
            }
        }

        String email = safeTrim(candidate.getEmail());
        if (!isBlank(email)) {
            if (email.length() > MAX_EMAIL_LENGTH) {
                errors.add("Email không được quá " + MAX_EMAIL_LENGTH + " ký tự");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.add("Email không đúng định dạng");
            }
        }

        String ngaySinh = safeTrim(candidate.getNgaySinh());
        if (isBlank(ngaySinh)) {
            errors.add("Ngày sinh không được để trống");
        } else {
            Date parsed = parseStorageDate(ngaySinh);
            if (parsed == null || !isValidBirthYearRange(parsed)) {
                errors.add("Ngày sinh không hợp lệ hoặc năm sinh ngoài khoảng cho phép");
            }
        }

        String gioiTinh = safeTrim(candidate.getGioiTinh());
        if (isBlank(gioiTinh) || "-- Vui lòng chọn --".equalsIgnoreCase(gioiTinh)) {
            errors.add("Giới tính không được để trống");
        }

        String doiTuong = safeTrim(candidate.getDoiTuong());
        if (!isBlank(doiTuong) && doiTuong.length() > MAX_DOI_TUONG_LENGTH) {
            errors.add("Đối tượng không được quá " + MAX_DOI_TUONG_LENGTH + " ký tự");
        }

        String khuVuc = safeTrim(candidate.getKhuVuc());
        if (requireKhuVuc) {
            if (isBlank(khuVuc) || "-- Vui lòng chọn --".equalsIgnoreCase(khuVuc)) {
                errors.add("Khu vực không được để trống");
            } else if (khuVuc.length() > MAX_KHU_VUC_LENGTH) {
                errors.add("Khu vực không được quá " + MAX_KHU_VUC_LENGTH + " ký tự");
            }
        } else {
            if (!isBlank(khuVuc) && khuVuc.length() > MAX_KHU_VUC_LENGTH) {
                errors.add("Khu vực không được quá " + MAX_KHU_VUC_LENGTH + " ký tự");
            }
        }

        String noiSinh = safeTrim(candidate.getNoiSinh());
        if (!isBlank(noiSinh) && noiSinh.length() > MAX_NOI_SINH_LENGTH) {
            errors.add("Nơi sinh không được quá " + MAX_NOI_SINH_LENGTH + " ký tự sau chuẩn hoá");
        }

        String cccd = safeTrim(candidate.getCccd());
        if (requireCccdDigits && !isBlank(cccd) && !cccd.matches("^\\d{12}$")) {
            errors.add("CCCD phải gồm đúng 12 chữ số");
        }
    }

    private void normalizeAndTruncateForPersist(CandidateDTO candidate) {
        if (candidate == null) {
            return;
        }

        candidate.setCccd(normalizeString(candidate.getCccd(), MAX_CCCD_LENGTH, false));
        candidate.setSoBaoDanh(normalizeString(candidate.getSoBaoDanh(), MAX_SBD_LENGTH, false));
        candidate.setHo(normalizeString(candidate.getHo(), MAX_HO_LENGTH, false));
        candidate.setTen(normalizeString(candidate.getTen(), MAX_TEN_LENGTH, false));
        candidate.setSoDienThoai(normalizePhone(candidate.getSoDienThoai()));
        candidate.setGioiTinh(normalizeString(candidate.getGioiTinh(), 10, false));
        candidate.setEmail(normalizeString(candidate.getEmail(), MAX_EMAIL_LENGTH, false));
        candidate.setNoiSinh(normalizeNoiSinh(candidate.getNoiSinh()));
        String normDoiTuong = normalizeString(candidate.getDoiTuong(), MAX_DOI_TUONG_LENGTH, false);
        if (isBlank(normDoiTuong)) {
            candidate.setDoiTuong(null);
        } else {
            candidate.setDoiTuong(normDoiTuong);
        }
        candidate.setKhuVuc(normalizeString(candidate.getKhuVuc(), MAX_KHU_VUC_LENGTH, false));

        String normalizedNgaySinh = normalizeDateStorage(candidate.getNgaySinh());
        candidate.setNgaySinh(normalizedNgaySinh);

        if (!isBlank(normalizedNgaySinh)) {
            java.util.Date parsed = parseStorageDate(normalizedNgaySinh);
            if (parsed != null) {
                java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("ddMMyyyy");
                candidate.setPassword(out.format(parsed));
            } else {
                candidate.setPassword(normalizeString(candidate.getPassword(), 100, false));
            }
        } else {
            candidate.setPassword(normalizeString(candidate.getPassword(), 100, false));
        }
    }

    private String normalizeNoiSinh(String value) {
        String normalized = safeTrim(value);
        if (isBlank(normalized)) {
            return normalized;
        }
        if (normalized.length() <= MAX_NOI_SINH_LENGTH) {
            return normalized;
        }
        int lastComma = normalized.lastIndexOf(',');
        if (lastComma >= 0 && lastComma < normalized.length() - 1) {
            normalized = normalized.substring(lastComma + 1).trim();
        }
        if (normalized.length() > MAX_NOI_SINH_LENGTH) {
            normalized = normalized.substring(0, MAX_NOI_SINH_LENGTH).trim();
        }
        return normalized;
    }

    private String normalizePhone(String value) {
        String normalized = safeTrim(value);
        if (isBlank(normalized)) {
            return normalized;
        }
        normalized = normalized.replaceAll("\\s+", "");
        if (normalized.length() > MAX_PHONE_LENGTH) {
            normalized = normalized.substring(0, MAX_PHONE_LENGTH);
        }
        return normalized;
    }

    private String normalizeString(String value, int maxLength, boolean collapseSpaces) {
        String normalized = safeTrim(value);
        if (isBlank(normalized)) {
            return normalized;
        }
        if (collapseSpaces) {
            normalized = normalized.replaceAll("\\s+", " ");
        }
        if (normalized.length() > maxLength) {
            normalized = normalized.substring(0, maxLength).trim();
        }
        return normalized;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Date parseStorageDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            SimpleDateFormat ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
            ddMMyyyy.setLenient(false);
            return ddMMyyyy.parse(value.trim());
        } catch (ParseException ex) {
            return null;
        }
    }

    private String normalizeDateStorage(String value) {
        Date parsed = parseStorageDate(value);
        if (parsed == null) {
            return safeTrim(value);
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(parsed);
    }

    private boolean isValidBirthYearRange(Date date) {
        if (date == null) {
            return false;
        }
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        int year = Integer.parseInt(yearFormat.format(date));
        return year >= MIN_BIRTH_YEAR && year <= MAX_BIRTH_YEAR;
    }

    private boolean matchesAny(String header, String... candidates) {
        for (String candidate : candidates) {
            if (header.equals(candidate) || header.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    public static class CandidateValidationException extends RuntimeException {
        private final List<String> errors;

        public CandidateValidationException(List<String> errors) {
            super(errors == null || errors.isEmpty() ? "Candidate validation failed" : errors.get(0));
            this.errors = errors == null ? new ArrayList<>() : new ArrayList<>(errors);
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public String getMessageText() {
            return String.join("\n", errors);
        }
    }
}
