package com.example.bus;

import com.example.dao.MajorDAO;
import com.example.dao.MajorGroupDAO;
import com.example.dto.MajorDTO;
import com.example.dto.MajorGroupDTO;
import com.example.dto.SubjectGroupDTO;
import com.example.bus.SubjectGroupBUS;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.text.Normalizer;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MajorGroupBUS {
    private static final Pattern COMBO_CODE_PATTERN = Pattern.compile("^[A-Z]\\d{2}$");
    private static final Set<String> BLOCKED_IMPORT_SUBJECT_CODES = Set.of("NK3", "NK4", "NK5", "NK6");
    private static final Set<String> FALLBACK_SUBJECT_CODES = Set.of(
            "TO", "LI", "HO", "SI", "SU", "DI", "VA", "TI",
            "KTPL", "CNCN", "CNNN", "N1", "NK1", "NK2", "NK3", "NK4", "NK5", "NK6"
    );
    private final MajorGroupDAO majorGroupDAO;
    private final SubjectGroupBUS subjectGroupBUS;
    private final MajorDAO majorDAO;

    public MajorGroupBUS() {
        this.majorGroupDAO = new MajorGroupDAO();
        this.subjectGroupBUS = new SubjectGroupBUS();
        this.majorDAO = new MajorDAO();
    }

    public List<MajorGroupDTO> getAll() {
        return majorGroupDAO.getAll();
    }

    public MajorGroupDTO findById(int id) {
        return majorGroupDAO.findById(id);
    }

    public List<MajorGroupDTO> findByMaNganh(String maNganh) {
        return majorGroupDAO.findByMaNganh(maNganh);
    }

    public MajorGroupDTO findByTbKeys(String tbKeys) {
        return majorGroupDAO.findByTbKeys(tbKeys);
    }

    public MajorGroupDTO findByMaNganhAndMaToHop(String maNganh, String maToHop) {
        return majorGroupDAO.findByMaNganhAndMaToHop(maNganh, maToHop);
    }

    public void save(MajorGroupDTO majorGroup) {
        majorGroupDAO.save(majorGroup);
    }

    public void update(MajorGroupDTO majorGroup) {
        majorGroupDAO.update(majorGroup);
    }

    public void delete(MajorGroupDTO majorGroup) {
        if (majorGroup == null || majorGroup.getMaToHop() == null || majorGroup.getMaToHop().isBlank()) {
            // Không có gì để làm nếu không có mã tổ hợp
            return;
        }

        String maToHopToDelete = majorGroup.getMaToHop();

        // 1. Xóa bản ghi trong xt_nganh_tohop
        majorGroupDAO.delete(majorGroup);

        // 2. Kiểm tra xem còn ngành nào khác sử dụng mã tổ hợp này không
        List<MajorGroupDTO> remainingCombinations = majorGroupDAO.findByMaToHop(maToHopToDelete);

        // 3. Nếu không còn ngành nào sử dụng, thì xóa luôn trong bảng xt_tohop_monthi
        if (remainingCombinations == null || remainingCombinations.isEmpty()) {
            SubjectGroupDTO subjectGroup = subjectGroupBUS.findByMaToHop(maToHopToDelete);
            if (subjectGroup != null) {
                try {
                    subjectGroupBUS.delete(subjectGroup);
                } catch (Exception e) {
                    System.err.println("Không thể xóa tổ hợp môn (có thể do bị ràng buộc dữ liệu): " + e.getMessage());
                }
            }
        }
    }

    /**
     * Update maNganh cho tất cả combinations của một ngành
     * Đã fix lỗi Duplicate Entry: Kiểm tra trước xem có bị trùng tổ hợp không
     */
    public void updateMajorCodeInCombinations(String oldMaNganh, String newMaNganh) {
        List<MajorGroupDTO> combinations = majorGroupDAO.findByMaNganh(oldMaNganh);
        for (MajorGroupDTO combo : combinations) {
            String newTbKeys = newMaNganh + "_" + combo.getMaToHop();
            
            // Kiểm tra xem tb_keys mới này đã tồn tại trong DB chưa
            MajorGroupDTO existingCombo = majorGroupDAO.findByTbKeys(newTbKeys);
            
            if (existingCombo != null) {
                // Nếu đã tồn tại rồi, xóa dòng cũ đi để tránh rác dữ liệu mồ côi
                majorGroupDAO.delete(combo);
            } else {
                // Chưa có thì update mã ngành và key mới
                combo.setMaNganh(newMaNganh);
                combo.setTbKeys(newTbKeys);
                majorGroupDAO.update(combo);
            }
        }
    }

    public List<MajorGroupDTO> importFromExcelFile(File file) throws IOException {
        List<MajorGroupDTO> importedRecords = new ArrayList<>();
        Set<String> validSubjects = loadValidSubjectCodes();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int headerRowIndex = detectHeaderRowIndex(sheet, dataFormatter, formulaEvaluator);
            Row headerRow = sheet.getRow(headerRowIndex);
            Map<String, Integer> headerMap = buildHeaderMap(headerRow, dataFormatter, formulaEvaluator);

            int maNganhCol = resolveFirstColumn(headerMap, 1, "MANGANH", "MA", "MACTDT", "MAXETTUYEN");
            int tenNganhCol = resolveFirstColumn(headerMap, 2, "TENNGANHCHUAN", "TENNGANH");
            int maToHopCol = resolveFirstColumn(headerMap, 3, "MATOHOP", "MATOHOPFULL", "MATOHOPDAYDU", "MA_TO_HOP");
            int mon1Col = resolveFirstColumn(headerMap, -1, "MON1", "THMON1", "TH_MON1", "MONTHI1", "MON_THI_1");
            int mon2Col = resolveFirstColumn(headerMap, -1, "MON2", "THMON2", "TH_MON2", "MONTHI2", "MON_THI_2");
            int mon3Col = resolveFirstColumn(headerMap, -1, "MON3", "THMON3", "TH_MON3", "MONTHI3", "MON_THI_3");
            int tbKeysCol = resolveFirstColumn(headerMap, -1, "TBKEYS", "TBKEY", "TB_KEYS");
            int gocCol = resolveFirstColumn(headerMap, -1, "GOC", "TOHOPGOC", "GOCCHOT");
            int doLechCol = resolveFirstColumn(headerMap, -1, "DOLECH", "DOLECHCHOT", "DOLECHTOHOP");

            for (Row row : sheet) {
                if (row.getRowNum() == headerRowIndex) continue;
                if (row.getRowNum() < headerRowIndex) continue;

                try {
                    String maNganh = getCellStringValue(row.getCell(maNganhCol), dataFormatter, formulaEvaluator);
                    maNganh = normalizeCode(maNganh);
                    String tenNganhChuan = tenNganhCol >= 0
                            ? getCellStringValue(row.getCell(tenNganhCol), dataFormatter, formulaEvaluator)
                            : null;
                    String maToHopFull = getCellStringValue(row.getCell(maToHopCol), dataFormatter, formulaEvaluator);

                    String tbKeys = tbKeysCol >= 0
                            ? getCellStringValue(row.getCell(tbKeysCol), dataFormatter, formulaEvaluator)
                            : null;

                    boolean isGoc = false;
                    if (gocCol >= 0) {
                        String gocRaw = getCellStringValue(row.getCell(gocCol), dataFormatter, formulaEvaluator);
                        isGoc = isTruthy(gocRaw);
                    }

                    BigDecimal doLech = null;
                    if (doLechCol >= 0) {
                        String doLechRaw = getCellStringValue(row.getCell(doLechCol), dataFormatter, formulaEvaluator);
                        doLech = parseBigDecimal(doLechRaw);
                    }

                    if (maNganh == null || maNganh.isEmpty() || maToHopFull == null || maToHopFull.isEmpty()) {
                        continue;
                    }

                    String[] subjects = parseSubjectsFromMaToHop(maToHopFull);
                    int[] weights = parseWeightsFromMaToHop(maToHopFull);
                    String maToHop = subjects[0]; 
                    String thMon1 = subjects.length > 1 ? subjects[1] : null;
                    String thMon2 = subjects.length > 2 ? subjects[2] : null;
                    String thMon3 = subjects.length > 3 ? subjects[3] : null;

                    if ((thMon1 == null || thMon1.isBlank()) && mon1Col >= 0) {
                        thMon1 = normalizeSubjectCode(getCellStringValue(row.getCell(mon1Col), dataFormatter, formulaEvaluator));
                    }
                    if ((thMon2 == null || thMon2.isBlank()) && mon2Col >= 0) {
                        thMon2 = normalizeSubjectCode(getCellStringValue(row.getCell(mon2Col), dataFormatter, formulaEvaluator));
                    }
                    if ((thMon3 == null || thMon3.isBlank()) && mon3Col >= 0) {
                        thMon3 = normalizeSubjectCode(getCellStringValue(row.getCell(mon3Col), dataFormatter, formulaEvaluator));
                    }

                    Integer hsMon1 = weights.length > 0 ? weights[0] : 1;
                    Integer hsMon2 = weights.length > 1 ? weights[1] : 1;
                    Integer hsMon3 = weights.length > 2 ? weights[2] : 1;

                    if (maToHop == null || !COMBO_CODE_PATTERN.matcher(maToHop).matches()) {
                        continue;
                    }

                    SubjectGroupDTO existingSubject = null;
                    try {
                        existingSubject = subjectGroupBUS.findByMaToHop(maToHop);
                    } catch (Exception ignore) {
                    }

                    if ((thMon1 == null || thMon1.isBlank()) && existingSubject != null) {
                        thMon1 = normalizeSubjectCode(existingSubject.getMon1());
                    }
                    if ((thMon2 == null || thMon2.isBlank()) && existingSubject != null) {
                        thMon2 = normalizeSubjectCode(existingSubject.getMon2());
                    }
                    if ((thMon3 == null || thMon3.isBlank()) && existingSubject != null) {
                        thMon3 = normalizeSubjectCode(existingSubject.getMon3());
                    }

                    if (isGoc) {
                        try {
                            MajorDTO major = majorDAO.findByMaNganh(maNganh);
                            if (major != null) {
                                String current = major.getNToHopGoc();
                                if (current == null || !current.equalsIgnoreCase(maToHop)) {
                                    major.setNToHopGoc(maToHop);
                                    majorDAO.update(major);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to update n_tohopgoc for maNganh=" + maNganh + ": " + e.getMessage());
                        }
                    }

                    if (!isValidImportedSubject(thMon1, validSubjects)
                            || !isValidImportedSubject(thMon2, validSubjects)
                            || !isValidImportedSubject(thMon3, validSubjects)) {
                        continue;
                    }

                    if (containsBlockedSubject(thMon1, thMon2, thMon3)) {
                        continue;
                    }

                    if (tbKeys == null || tbKeys.isBlank()) {
                        tbKeys = maNganh + "_" + maToHop;
                    }

                    MajorGroupDTO dto = new MajorGroupDTO();
                    dto.setMaNganh(maNganh);
                    dto.setMaToHop(maToHop);
                    dto.setThMon1(thMon1);
                    dto.setHsMon1(hsMon1);
                    dto.setThMon2(thMon2);
                    dto.setHsMon2(hsMon2);
                    dto.setThMon3(thMon3);
                    dto.setHsMon3(hsMon3);
                    dto.setTbKeys(tbKeys);
                    dto.setDoLech(doLech);
                    
                    try {
                        if (maToHop != null) {
                            if (existingSubject == null) {
                                com.example.dto.SubjectGroupDTO subjectDTO = new com.example.dto.SubjectGroupDTO(
                                        maToHop,
                                        thMon1 != null ? thMon1 : "-",
                                        thMon2 != null ? thMon2 : "-",
                                        thMon3 != null ? thMon3 : "-",
                                        tenNganhChuan
                                );
                                subjectGroupBUS.save(subjectDTO);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to persist SubjectGroup for maToHop=" + maToHop + ": " + e.getMessage());
                    }
                    
                    MajorGroupDTO existing = majorGroupDAO.findByMaNganhAndMaToHop(maNganh, maToHop);
                    if (existing == null) {
                        majorGroupDAO.save(dto);
                        importedRecords.add(dto);
                    } else {
                        boolean changed = false;
                        if (tbKeys != null && !tbKeys.isBlank() && (existing.getTbKeys() == null || !existing.getTbKeys().equals(tbKeys))) {
                            existing.setTbKeys(tbKeys);
                            changed = true;
                        }
                        if (doLech != null && (existing.getDoLech() == null || existing.getDoLech().compareTo(doLech) != 0)) {
                            existing.setDoLech(doLech);
                            changed = true;
                        }

                        if (hsMon1 != null && !hsMon1.equals(existing.getHsMon1())) {
                            existing.setHsMon1(hsMon1);
                            changed = true;
                        }
                        if (hsMon2 != null && !hsMon2.equals(existing.getHsMon2())) {
                            existing.setHsMon2(hsMon2);
                            changed = true;
                        }
                        if (hsMon3 != null && !hsMon3.equals(existing.getHsMon3())) {
                            existing.setHsMon3(hsMon3);
                            changed = true;
                        }

                        if (thMon1 != null && (existing.getThMon1() == null || !existing.getThMon1().equalsIgnoreCase(thMon1))) {
                            existing.setThMon1(thMon1);
                            changed = true;
                        }
                        if (thMon2 != null && (existing.getThMon2() == null || !existing.getThMon2().equalsIgnoreCase(thMon2))) {
                            existing.setThMon2(thMon2);
                            changed = true;
                        }
                        if (thMon3 != null && (existing.getThMon3() == null || !existing.getThMon3().equalsIgnoreCase(thMon3))) {
                            existing.setThMon3(thMon3);
                            changed = true;
                        }
                        if (changed) {
                            majorGroupDAO.update(existing);
                            importedRecords.add(existing);
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Error parsing row " + row.getRowNum() + ": " + e.getMessage());
                }
            }
        }

        return importedRecords;
    }

    private int detectHeaderRowIndex(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (sheet == null) {
            return 0;
        }
        int maxScan = Math.min(10, sheet.getLastRowNum());
        for (int r = 0; r <= maxScan; r++) {
            Row candidate = sheet.getRow(r);
            Map<String, Integer> map = buildHeaderMap(candidate, formatter, evaluator);
            if (looksLikeMajorGroupHeader(map)) {
                return r;
            }
        }
        return 0;
    }

    private boolean looksLikeMajorGroupHeader(Map<String, Integer> headerMap) {
        if (headerMap == null || headerMap.isEmpty()) {
            return false;
        }
        boolean hasCode = headerMap.containsKey("MANGANH")
                || headerMap.containsKey("MA");
        boolean hasCombo = headerMap.containsKey("MATOHOP")
                || headerMap.containsKey("MATOHOPFULL")
                || headerMap.containsKey("MA_TO_HOP");
        return hasCode && hasCombo;
    }

    private Map<String, Integer> buildHeaderMap(Row header, DataFormatter formatter, FormulaEvaluator evaluator) {
        Map<String, Integer> map = new HashMap<>();
        if (header == null) {
            return map;
        }

        for (Cell cell : header) {
            String value = getCellStringValue(cell, formatter, evaluator);
            if (value == null) {
                continue;
            }
            String normalized = normalizeHeader(value);
            map.putIfAbsent(normalized, cell.getColumnIndex());
        }

        return map;
    }

    private int resolveFirstColumn(Map<String, Integer> headerMap, int fallback, String... keys) {
        if (headerMap == null || keys == null) {
            return fallback;
        }
        for (String key : keys) {
            if (key == null) continue;
            Integer col = headerMap.get(key);
            if (col != null) {
                return col;
            }
        }
        return fallback;
    }

    private String normalizeHeader(String value) {
        String noDiacritics = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('Đ', 'D')
            .replace('đ', 'd');
        return noDiacritics.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
    }

    private BigDecimal parseBigDecimal(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim().replace(" ", "");
        cleaned = cleaned.replaceAll("[^0-9,.-]", "");
        if (cleaned.contains(",") && !cleaned.contains(".")) {
            cleaned = cleaned.replace(",", ".");
        } else {
            cleaned = cleaned.replaceAll(",", "");
        }
        if (cleaned.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isTruthy(String raw) {
        if (raw == null) {
            return false;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return false;
        }
        String upper = s.toUpperCase(Locale.ROOT);
        return !("0".equals(upper) || "FALSE".equals(upper) || "NO".equals(upper) || "KHONG".equals(upper));
    }

    private String normalizeCode(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().replace("\u00A0", "");
        if (s.isEmpty()) {
            return null;
        }

        if (s.matches("^\\d+\\.0+$")) {
            s = s.substring(0, s.indexOf('.'));
        }

        if (s.matches("^[0-9]+(?:\\.[0-9]+)?[eE][+-]?\\d+$")) {
            try {
                s = new BigDecimal(s).toPlainString();
                if (s.endsWith(".0")) {
                    s = s.substring(0, s.length() - 2);
                }
            } catch (NumberFormatException ignore) {
            }
        }

        return s;
    }

    private String[] parseSubjectsFromMaToHop(String maToHopFull) {
        String[] result = new String[4];
        String raw = maToHopFull == null ? "" : maToHopFull.trim();

        if (raw.length() >= 3) {
            result[0] = raw.substring(0, 3).toUpperCase(); 
        } else {
            result[0] = raw.toUpperCase();
        }

        Pattern pattern = Pattern.compile("\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(raw);

        if (matcher.find()) {
            String subjectsStr = matcher.group(1); 
            String[] parts = subjectsStr.split(",");

            for (int i = 0; i < parts.length && i < 3; i++) {
                String part = parts[i].trim(); 
                String subject = part.split("-")[0].trim().toUpperCase(); 
                result[i + 1] = subject;
            }
        }

        return result;
    }

    private int[] parseWeightsFromMaToHop(String maToHopFull) {
        int[] weights = new int[]{1, 1, 1};
        String raw = maToHopFull == null ? "" : maToHopFull.trim();

        Pattern pattern = Pattern.compile("\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.find()) {
            return weights;
        }

        String subjectsStr = matcher.group(1);
        String[] parts = subjectsStr.split(",");
        for (int i = 0; i < parts.length && i < 3; i++) {
            String part = parts[i].trim();
            String[] seg = part.split("-");
            if (seg.length >= 2) {
                String wRaw = seg[1].trim();
                try {
                    int w = Integer.parseInt(wRaw);
                    if (w > 0) {
                        weights[i] = w;
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        }
        return weights;
    }

    private Set<String> loadValidSubjectCodes() {
        Set<String> validSubjects = new HashSet<>();
        List<SubjectGroupDTO> subjectGroups = subjectGroupBUS.getAll();
        for (SubjectGroupDTO subjectGroup : subjectGroups) {
            addSubjectCode(validSubjects, subjectGroup.getMon1());
            addSubjectCode(validSubjects, subjectGroup.getMon2());
            addSubjectCode(validSubjects, subjectGroup.getMon3());
        }
        if (validSubjects.isEmpty()) {
            validSubjects.addAll(FALLBACK_SUBJECT_CODES);
        }
        return validSubjects;
    }

    private void addSubjectCode(Set<String> target, String value) {
        if (value != null && !value.trim().isEmpty()) {
            target.add(value.trim().toUpperCase());
        }
    }

    private String normalizeSubjectCode(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isValidImportedSubject(String subjectCode, Set<String> validSubjects) {
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            return false;
        }
        return validSubjects.contains(subjectCode.trim().toUpperCase());
    }

    private boolean containsBlockedSubject(String... subjects) {
        for (String subject : subjects) {
            if (subject != null && BLOCKED_IMPORT_SUBJECT_CODES.contains(subject.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private String getCellStringValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }

        String raw = formatter.formatCellValue(cell, evaluator);
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}