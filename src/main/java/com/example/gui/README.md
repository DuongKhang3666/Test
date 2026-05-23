## 🛠️ HƯỚNG DẪN GHÉP GIAO DIỆN (GUI) DÀNH CHO TEAM

**LƯU Ý:** Tên chuỗi String (tên Menu) ở Bước 2 và Bước 3 phải **TRÙNG KHỚP** (kể cả viết hoa, thường và khoảng trắng). Nếu gõ sai, CardLayout sẽ không lật được trang!

### Bước 1: Tạo file Page (Giao diện)
Tạo file class của bạn trong thư mục `gui/pages/...` (bắt buộc phải `extends JPanel`).
> *Ví dụ:* `src/main/java/com/example/gui/pages/candidate/CandidatePage.java`

### Bước 2: Khai báo vào MainLayout.java
Mở file `MainLayout.java`, tìm đến đoạn code nhúng trang và thêm các dòng sau (nhớ đổi tên class và tên chuỗi cho đúng với module của bạn):

    // Ví dụ mẫu cho trang Candidate
    JPanel candidatePage = new CandidatePage();
    candidatePage.setName("Candidates & Scores"); // <--- Tên này phải trùng khớp với Sidebar
    contentPanel.add(candidatePage, "Candidates & Scores"); 

### Bước 3: Gắn nút bấm vào Sidebar.java (Quan trọng)
Mở file `Sidebar.java`, tìm đến khu vực `THÊM CÁC MENU ITEM` và thêm dòng code sau để Sidebar nhận diện được trang của bạn:

    // Ví dụ mẫu cho trang Candidate
    addMenuItem("Candidates & Scores", "/assets/icons/candidates.png"); // <--- Đổi tên và đường dẫn icon

---
