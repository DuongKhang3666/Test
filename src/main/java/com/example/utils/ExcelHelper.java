package com.example.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import java.text.SimpleDateFormat;

public class ExcelHelper {

    /**
     * Hàm lấy giá trị của ô Excel dưới dạng Chuỗi (String) an toàn.
     * Dùng để đọc: CCCD, Họ tên, Mã Ngành...
     */
    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return ""; // Ô trống trả về chuỗi rỗng, không bị Null
        }

        CellType cellType = cell.getCellType();
        
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue().trim();
                
            case NUMERIC:
                // Trường hợp 1: Nếu ô đó được định dạng là Ngày tháng (Date)
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Chuẩn format MySQL
                    return dateFormat.format(cell.getDateCellValue());
                } 
                // Trường hợp 2: Nếu là số bình thường (ví dụ: số điện thoại, số nguyên)
                else {
                    double value = cell.getNumericCellValue();
                    // Nếu là số nguyên (không có phần thập phân), ép về long để mất số .0 ở đuôi
                    if (value == Math.floor(value)) {
                        return String.valueOf((long) value);
                    }
                    return String.valueOf(value);
                }
                
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
                
            case BLANK:
            case ERROR:
            default:
                return "";
        }
    }

    /**
     * Hàm lấy giá trị của ô Excel dưới dạng Số thực (Double).
     * Dùng để đọc: Điểm thi (Toán, Lý, Hóa...), Điểm cộng...
     */
    public static double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                // Đề phòng trường hợp Excel lưu điểm số dưới dạng Text (Chữ)
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}