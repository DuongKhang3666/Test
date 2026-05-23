# Hướng dẫn dùng bộ utils chung

Trong quá trình làm GUI và BUS, nhiều chỗ sẽ lặp lại cùng một kiểu xử lý như:

- kiểm tra ai đang đăng nhập
- đọc file Excel
- import số lượng dữ liệu lớn
- mở kết nối Hibernate tới MySQL

Thay vì mỗi người tự viết lại từ đầu, thư mục `com.example.utils` đã được chuẩn bị sẵn để dùng chung. Tài liệu này tóm tắt nhanh chức năng của từng class và cách gọi cơ bản.

## 1. `SessionManager` - Quản lý phiên đăng nhập

**Tác dụng**

- Lưu thông tin người dùng đang đăng nhập
- Kiểm tra quyền `admin` / `user`
- Dùng để ẩn hoặc hiện các nút chức năng theo quyền

**Cách dùng**

```java
if (SessionManager.isAdmin()) {
    btnXoaThiSinh.setVisible(true);
} else {
    btnXoaThiSinh.setVisible(false);
}

String tenNguoiDung = SessionManager.getCurrentUser().getHoTen();
```

## 2. `ExcelHelper` - Đọc Excel an toàn

**Tác dụng**

- Đọc dữ liệu từ Excel bằng Apache POI
- Giảm lỗi `NullPointerException` khi ô trống
- Giảm lỗi ép kiểu khi ô chứa số nhưng đọc như chuỗi, hoặc ngược lại

**Cách dùng**

```java
Cell cellCccd = row.getCell(0);
Cell cellDiem = row.getCell(1);

String cccd = ExcelHelper.getCellValueAsString(cellCccd);
double diemToan = ExcelHelper.getCellValueAsDouble(cellDiem);
```

## 3. `HibernateBatchUtil` - Import dữ liệu lớn theo lô

**Tác dụng**

- Dùng khi import Excel số lượng lớn
- Tránh gọi `dao.save()` liên tục quá nhiều lần gây đầy RAM
- Chia dữ liệu thành từng lô nhỏ để lưu nhanh và ổn định hơn

**Cách dùng**

```java
List<CandidateDTO> listThiSinh = ...;

boolean ketQua = HibernateBatchUtil.importCandidatesInBatch(listThiSinh);
if (ketQua) {
    System.out.println("Import thành công!");
}
```

**Ghi chú**

- Hiện tại file mẫu đang minh họa cho chức năng import thí sinh
- Các bảng khác có thể làm theo cùng mẫu: đổi DTO, đổi DAO và giữ nguyên cách chia lô

## 4. `HibernateUtil` - Tạo Session kết nối CSDL

**Tác dụng**

- Khởi tạo `SessionFactory`
- Mở `Session` để thực hiện HQL, insert, update, delete
- Đây là class mà DAO sẽ dùng thường xuyên nhất

**Cách dùng**

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    // Viết HQL ở đây
}
```

## Lưu ý khi sử dụng

- Các class trong `utils` là công cụ dùng chung, không nên sửa lung tung ở nhiều nơi
- Nếu thêm DTO hoặc module mới, cần kiểm tra lại phần mapping của Hibernate
- Khi import dữ liệu lớn, nên ưu tiên dùng batch thay vì lưu từng dòng một

## Tóm tắt nhanh

- `SessionManager`: quản lý người đang đăng nhập
- `ExcelHelper`: đọc Excel an toàn
- `HibernateBatchUtil`: import dữ liệu lớn theo lô
- `HibernateUtil`: mở kết nối Hibernate tới MySQL