package com.example.bus;

import com.example.dao.MajorDAO;
import com.example.dto.MajorDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MajorBUS {
    private final MajorDAO majorDAO;

    public MajorBUS() {
        this.majorDAO = new MajorDAO();
    }

    public List<MajorDTO> getAll() {
        return majorDAO.getAll();
    }

    public MajorDTO findById(int idNganh) {
        return majorDAO.findById(idNganh);
    }

    public MajorDTO findByMaNganh(String maNganh) {
        return majorDAO.findByMaNganh(maNganh);
    }

    public void save(MajorDTO major) {
        majorDAO.save(major);
    }

    public void update(MajorDTO major) {
        majorDAO.update(major);
    }

    public void delete(MajorDTO major) {
        majorDAO.delete(major);
    }

    public List<MajorDTO> importFromExcelFile(File file) throws IOException {
        List<MajorDTO> imported = new ArrayList<>();
        // 1. TẠO BỘ NHỚ LƯU CÁC NGÀNH ĐÃ ĐỌC ĐỂ CHỐNG TRÙNG LẶP (Chỉ lấy đúng số ngành gốc)
        Set<String> processedMajors = new HashSet<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                return imported;
            }

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int headerRowIndex = detectHeaderRowIndex(sheet, formatter, evaluator);
            Row header = sheet.getRow(headerRowIndex);
            Map<String, Integer> headerMap = buildHeaderMap(header, formatter, evaluator);

            // 2. CHỈ ĐỊNH ĐÍCH DANH CỘT CẦN LẤY
            int maNganhCol = resolveFirstColumn(headerMap, 1, "MANGANH", "MACTDT", "MAXETTUYEN");
            int tenNganhCol = resolveFirstColumn(headerMap, 2, "TENNGANHCHUAN", "TENCTDT", "TENNGANHCHUONGTRINHDAOTAO", "TENNGANH");
            int chiTieuCol = resolveFirstColumn(headerMap, -1, "CHITIEU", "CHITIEUCHOT");
                int diemSanCol = resolveFirstColumn(
                    headerMap,
                    3,
                    "DIEMSAN",
                    "NGUONGDAUVAO",
                    "NGUONGDAUVAO2025",
                    "NGUONGDAUVAONAM2025",
                    "DIEMCHUAN",
                    "DIEMXETTUYEN",
                    "DIEMXETTUYEN2025",
                    "DIEM_TRUNG_TUYEN",
                    "DIEMTRUNGTUYEN"
                );

            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String maNganh = getCellStringValue(row.getCell(maNganhCol), formatter, evaluator);
                maNganh = normalizeCode(maNganh);
                if (maNganh == null || maNganh.isEmpty()) {
                    continue;
                }

                // KIỂM TRA TRÙNG LẶP: Nếu mã ngành này đã được xử lý ở dòng trên rồi -> BỎ QUA NGAY
                if (processedMajors.contains(maNganh)) {
                    continue;
                }
                // Ghi nhớ mã ngành này lại
                processedMajors.add(maNganh);

                String tenNganh = tenNganhCol >= 0 ? getCellStringValue(row.getCell(tenNganhCol), formatter, evaluator) : null;
                if (tenNganh == null || tenNganh.isBlank()) {
                    continue;
                }

                // Lấy chỉ tiêu
                Integer chiTieu = null;
                if (chiTieuCol >= 0) {
                    String chiTieuRaw = getCellStringValue(row.getCell(chiTieuCol), formatter, evaluator);
                    if (chiTieuRaw != null && !chiTieuRaw.isBlank()) {
                        try {
                            String cleaned = chiTieuRaw.replaceAll("[^0-9.,-]", "").replaceAll(",", "");
                            chiTieu = (int) Math.round(Double.parseDouble(cleaned));
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    }
                }

                // Lấy điểm sàn
                BigDecimal diemSan = null;
                if (diemSanCol >= 0) {
                    String diemSanRaw = getCellStringValue(row.getCell(diemSanCol), formatter, evaluator);
                    if (diemSanRaw != null && !diemSanRaw.isBlank()) {
                        try {
                            diemSan = parseBigDecimal(diemSanRaw);
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    }
                }

                // 3. XỬ LÝ LƯU VÀO DATABASE
                MajorDTO existing = majorDAO.findByMaNganh(maNganh);

                if (existing == null) {
                    MajorDTO dto = new MajorDTO();
                    dto.setMaNganh(maNganh);
                    dto.setTenNganh(tenNganh);
                    dto.setNChiTieu(chiTieu != null ? chiTieu : 0);
                    if (diemSan != null) dto.setNDiemSan(diemSan);
                    
                    majorDAO.save(dto);
                    imported.add(dto);
                } else {
                    boolean changed = false;
                    if (!tenNganh.equals(existing.getTenNganh())) {
                        existing.setTenNganh(tenNganh);
                        changed = true;
                    }
                    if (chiTieu != null && existing.getNChiTieu() != chiTieu) {
                        existing.setNChiTieu(chiTieu);
                        changed = true;
                    }
                    if (diemSan != null && (existing.getNDiemSan() == null || existing.getNDiemSan().compareTo(diemSan) != 0)) {
                        existing.setNDiemSan(diemSan);
                        changed = true;
                    }
                    if (changed) {
                        majorDAO.update(existing);
                    }
                    imported.add(existing);
                }
            }
        }

        return imported; // Số lượng trả về lúc này sẽ chính xác (VD: 47)
    }

    private String normalizeCode(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replace("\u00A0", ""); 
        if (s.isEmpty()) return null;

        if (s.matches("^\\d+\\.0+$")) {
            s = s.substring(0, s.indexOf('.'));
        }

        if (s.matches("^[0-9]+(?:\\.[0-9]+)?[eE][+-]?\\d+$")) {
            try {
                s = new java.math.BigDecimal(s).toPlainString();
                if (s.endsWith(".0")) {
                    s = s.substring(0, s.length() - 2);
                }
            } catch (NumberFormatException ignore) {
            }
        }
        return s;
    }

    private int detectHeaderRowIndex(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (sheet == null) return 0;
        int maxScan = Math.min(10, sheet.getLastRowNum());
        for (int r = 0; r <= maxScan; r++) {
            Row candidate = sheet.getRow(r);
            Map<String, Integer> map = buildHeaderMap(candidate, formatter, evaluator);
            if (looksLikeMajorHeader(map)) return r;
        }
        return 0;
    }

    private boolean looksLikeMajorHeader(Map<String, Integer> headerMap) {
        if (headerMap == null || headerMap.isEmpty()) return false;
        boolean hasCode = headerMap.containsKey("MANGANH") 
                       || headerMap.containsKey("MACTDT") 
                       || headerMap.containsKey("MAXETTUYEN");
                       
        boolean hasName = headerMap.containsKey("TENNGANHCHUAN") 
                       || headerMap.containsKey("TENCTDT") 
                       || headerMap.containsKey("TENNGANHCHUONGTRINHDAOTAO") 
                       || headerMap.containsKey("TENNGANH");
                       
        return hasCode && hasName;
    }

    private Map<String, Integer> buildHeaderMap(Row header, DataFormatter formatter, FormulaEvaluator evaluator) {
        Map<String, Integer> map = new HashMap<>();
        if (header == null) return map;

        for (Cell cell : header) {
            String value = getCellStringValue(cell, formatter, evaluator);
            if (value == null) continue;
            String normalized = normalizeHeader(value);
            map.putIfAbsent(normalized, cell.getColumnIndex());
        }
        return map;
    }

    private int resolveFirstColumn(Map<String, Integer> headerMap, int fallback, String... keys) {
        if (headerMap == null || keys == null) return fallback;
        for (String key : keys) {
            if (key == null) continue;
            Integer col = headerMap.get(key);
            if (col != null) return col;
        }
        return fallback;
    }

    private String normalizeHeader(String value) {
        String noDiacritics = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('Đ', 'D')
            .replace('đ', 'd');
        return noDiacritics.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "");
    }

    private BigDecimal parseBigDecimal(String raw) {
        if (raw == null) return null;
        String cleaned = raw.trim().replace(" ", "").replaceAll("[^0-9,.-]", "");
        if (cleaned.contains(",") && !cleaned.contains(".")) {
            cleaned = cleaned.replace(",", ".");
        } else {
            cleaned = cleaned.replaceAll(",", "");
        }
        if (cleaned.isBlank()) return null;
        return new BigDecimal(cleaned);
    }

    private String getCellStringValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) return null;
        String raw = formatter.formatCellValue(cell, evaluator);
        if (raw == null) return null;
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}