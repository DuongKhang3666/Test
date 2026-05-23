
---

# HỆ THỐNG QUẢN LÝ TUYỂN SINH ĐẠI HỌC 2026

### 1. Giới thiệu đề tài
Đề tài tập trung nghiên cứu và xây dựng một phần mềm quản lý công tác xét tuyển đại học trên nền tảng Desktop (Java Swing). Mục tiêu của dự án là giúp sinh viên nắm vững mô hình kiến trúc phân lớp (3-Tier Architecture), ứng dụng công nghệ ORM (Hibernate) để tương tác với cơ sở dữ liệu MySQL, và xử lý khối lượng dữ liệu lớn (Batch Processing) từ các file CSV/Excel lên đến hàng chục ngàn dòng. Đồng thời, dự án cũng áp dụng các thuật toán nội suy bách phân vị và logic sắp xếp nguyện vọng phức tạp.

### 2. Thành viên nhóm (Nhóm 5 người)
* **[TV1] (System & UI/UX):** Xây dựng bộ khung giao diện `MainLayout`, thiết kế các UI Components tái sử dụng. Lập trình module Quản lý Tài khoản, Quản lý Bảng quy đổi, xử lý luồng Đăng nhập và phân quyền (Admin/User).
* **[TV2] (Data Processing):** Phụ trách module Quản lý Thí sinh và Quản lý Điểm cộng. Lập trình thuật toán đọc/ghi file CSV tốc độ cao để import tự động dữ liệu 40.000+ thí sinh vào Database mà không gây tràn bộ nhớ.
* **[TV3] (Score & Conversion Logic):** Lập trình module Quản lý Điểm thi (THPT, V-SAT, ĐGNL). Xây dựng các hàm ánh xạ và quy đổi điểm chứng chỉ ngoại ngữ (IELTS, TOEFL) sang thang điểm 10.
* **[TV4] (Algorithm & Major Management):** Phụ trách Quản lý Ngành và Tổ hợp. Đảm nhiệm xử lý các luồng tính toán cốt lõi: viết code cho các công thức nội suy tương đương, tính độ lệch điểm, và xử lý thuật toán cộng điểm ưu tiên giảm dần.
* **[TV5] (Admission Execution):** Phụ trách Quản lý Nguyện vọng. Lập trình vòng lặp thuật toán xét tuyển tổng thể: duyệt danh sách nguyện vọng, gọi hàm tính điểm và so sánh với chỉ tiêu ngành để quyết định trạng thái trúng tuyển (Đậu/Rớt).

### 3. Kiến trúc hệ thống


Dự án được cấu trúc chặt chẽ theo chuẩn Maven và mô hình 3 Lớp (3-Tier Architecture), tách biệt hoàn toàn giữa dữ liệu, logic và giao diện:
* **`dto` (Data Transfer Object):** Chứa các class đối tượng (Entity) ánh xạ trực tiếp 1-1 với các bảng trong MySQL:
  * `AccountDTO`, `CandidateDTO`, `ExamScoreDTO`, `BonusScoreDTO`
  * `MajorDTO`, `MajorGroupDTO`, `SubjectGroupDTO`, `AspirationDTO`, `ConversionTableDTO`
* **`dao` (Data Access Object):** Tầng giao tiếp cơ sở dữ liệu. Hiện có: `AccountDAO`, `CandidateDAO`, `ExamScoreDAO`, `BonusScoreDAO`, `ConversionTableDAO`. Sử dụng Hibernate để thực hiện các câu lệnh truy vấn CRUD (Thêm, Sửa, Xóa, Lấy danh sách).
* **`bus` (Business Logic):** Tầng xử lý nghiệp vụ "não bộ" của app. Hiện có: `AccountBUS` (quản lý tài khoản, đăng nhập). Các module BUS khác (Candidate, Score, Admission) đang trong quá trình phát triển.
* **`gui` (Graphical User Interface):** Tầng hiển thị sử dụng Java Swing. Được chia nhỏ thành:
  * `components`: Chứa các "linh kiện" UI dùng chung cho toàn app (Nút bấm, Bảng dữ liệu, Thanh tìm kiếm, Sidebar, TextBox, ComboBox, v.v...).
  * `pages`: Chứa các màn hình chức năng độc lập:
    * `account/`: Quản lý tài khoản hệ thống (AccountManagementPage) ✅
    * `student/`: Quản lý thí sinh (đang phát triển)
    * `score/`: Quản lý điểm (đang phát triển)
    * `wish/`: Quản lý nguyện vọng (đang phát triển)
    * `admission/`: Xét tuyển (đang phát triển)
* **`utils`:** Chứa các class tiện ích dùng chung toàn hệ thống (Cấu hình Hibernate, hàm đọc file CSV, hằng số cấu hình).

### 4. Luồng dữ liệu và Thuật toán (Data Flow)
Để đảm bảo tính nhất quán và không bị lỗi đụng độ, dữ liệu di chuyển theo luồng một chiều nghiêm ngặt như sau:
* **Nhập liệu (Input):** Người dùng thao tác trên `gui`, chọn file `.csv`. Tầng `bus` tiếp nhận file, bóc tách từng dòng văn bản và đóng gói thành các đối tượng `dto`.
* **Lưu trữ (Storage):** Tầng `bus` chuyển mảng `dto` cho tầng `dao`. Tầng `dao` dùng cấu hình Batch Processing của Hibernate để đẩy hàng loạt dữ liệu xuống MySQL XAMPP an toàn.
* **Xử lý Xét tuyển (Processing):** Khi có lệnh chạy xét tuyển, `bus` lấy danh sách nguyện vọng từ `dao`, gọi các hàm toán học để tính Tổng Điểm Xét Tuyển (ĐTHGXT + ĐC + ĐƯT), so sánh với cấu hình Chỉ tiêu ngành và trả về danh sách kết quả (Đậu/Rớt).
* **Trình chiếu (Output):** `gui` nhận lại danh sách đối tượng đã được cập nhật kết quả từ `bus` và vẽ lên các cấu kiện bảng (`MyTable`) để người dùng theo dõi.

### 5. Hướng dẫn cài đặt và Demo
**Môi trường yêu cầu:** Java 17+ (hoặc 21), Maven, XAMPP (chạy MySQL).

**Các bước chạy dự án:**
1. **Khởi tạo Database:** * Mở XAMPP, khởi động module MySQL.
   * Truy cập `localhost/phpmyadmin`.
   * Import file `database/xettuyen2026_empty.sql` để tạo cấu trúc bảng.
2. **Cấu hình kết nối:**
   * Clone dự án về máy, mở bằng VS Code, IntelliJ hoặc Eclipse.
   * Đợi IDE tự động tải các thư viện qua file `pom.xml`.
   * Truy cập file `src/main/resources/hibernate.cfg.xml`. Kiểm tra thông tin kết nối (mặc định: user `root`, password để trống).
3. **Khởi chạy ứng dụng:**
   * Chạy file `Main.java` hoặc dùng lệnh Maven: `mvn -Dexec.mainClass="com.example.Main" exec:java`
   * Ứng dụng sẽ hiển thị màn hình đăng nhập.
   * **Tài khoản test mặc định:** Username: `admin` | Password: `123456`.

### 6. Dữ liệu Test (Data Import)
* Nhóm đã chuẩn bị sẵn bộ dữ liệu test thực tế từ file Excel của giảng viên.
* Toàn bộ các file để test tính năng Import (Thí sinh, Điểm, Chỉ tiêu, Nguyện vọng...) nằm ở thư mục `data_import/` ngoài cùng dự án. Khi test chức năng chọn file trên giao diện, anh em trỏ đường dẫn vào thư mục này.

### 7. Trạng thái phát triển (Status)
| Module | Trạng thái | Ghi chú |
|--------|-----------|--------|
| **Account Management** | ✅ Hoàn thành | Đăng nhập, quản lý tài khoản, phân quyền Admin |
| **Candidate Management** | 🔄 DAO/DTO sẵn sàng | Chờ BUS và GUI Pages |
| **Exam Score** | 🔄 DAO/DTO sẵn sàng | Chờ BUS và GUI Pages |
| **Bonus Score** | 🔄 DAO/DTO sẵn sàng | Chờ BUS và GUI Pages |
| **Major & Subject Groups** | 🔄 DTO sẵn sàng | Chờ DAO, BUS, GUI |
| **Aspiration Management** | 🔄 DTO sẵn sàng | Chờ DAO, BUS, GUI |
| **Conversion Table** | 🔄 DAO/DTO sẵn sàng | Chờ BUS và GUI Pages |
| **Admission Algorithm** | 🔄 Lược đồ sẵn sàng | Chờ BUS logic và UI |

---