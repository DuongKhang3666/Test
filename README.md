# ỨNG DỤNG DỰ BÁO THỜI TIẾT (WEATHER APP) - MOBILE NÂNG CAO 2026

## 1. Giới thiệu đề tài
Đề tài tập trung nghiên cứu và triển khai cơ chế lấy dữ liệu thời tiết thời gian thực thông qua API và hiển thị trực quan trên nền tảng Android. Mục tiêu là giúp sinh viên nắm vững kiến thức về **Retrofit**, **WorkManager**, **Google Maps SDK** và kiến trúc phân lớp trong phát triển ứng dụng di động.

## 2. Thành viên nhóm (Nhóm 2 người)
* **Phước Hưng (Backend/Data):** Thiết lập cấu trúc dự án, lập trình logic lấy tọa độ (`FusedLocation`), xây dựng hệ thống gọi API và xử lý thông báo chạy ngầm (`Worker`).
* **Dương Khang (Frontend/UI):** Thiết kế giao diện Dashboard theo chuẩn hiện đại, xử lý `RecyclerView` dự báo, tích hợp lớp phủ radar trên Google Maps và logic chuyển đổi đơn vị.

## 3. Kiến trúc hệ thống
Dự án được tổ chức theo mô hình phân lớp (Package-based) để đảm bảo tính đóng gói và dễ bảo trì:

| Package  | Chức năng chính |
|:---------| :--- |
| `data`   | Chứa Retrofit Client, API Service và các Model (`WeatherResponse`, `ForecastResponse`) để xử lý dữ liệu JSON. |
| `ui`     | Chứa các Activity, Adapter hiển thị giao diện Dashboard và Bản đồ thời tiết. |
| `worker` | Xử lý tác vụ chạy ngầm định kỳ để gửi cảnh báo thời tiết xấu. |
| `utils`  | Chứa các hàm tiện ích như `LocationHelper` và các bộ chuyển đổi đơn vị. |

## 4. Giao thức & Luồng dữ liệu (Data Flow)
Để đảm bảo đồng bộ, nhóm thống nhất luồng xử lý dữ liệu như sau:
* **Vị trí:** Sử dụng `FusedLocationProviderClient` để lấy tọa độ thực tế của thiết bị.
* **Thời tiết:** Dữ liệu được trả về dưới dạng JSON, sau đó được parse vào các đối tượng Java tương ứng để hiển thị lên UI.
* **Bản đồ:** Tích hợp Google Maps SDK để hiển thị vị trí và tình trạng mây/mưa theo thời gian thực.
* **Thiết kế UI tham khảo:** [Figma Project Link](https://stitch.withgoogle.com/projects/1171664857264878422)

## 5. Hướng dẫn cài đặt và Demo
1. **Phía môi trường:**
   * Clone dự án về máy tính và mở bằng Android Studio.
   * Đảm bảo thiết bị test (máy thật hoặc máy ảo) có kết nối Internet và đã bật GPS.
2. **Cấu hình API Key:**
   * Truy cập vào file chứa hằng số (`utils/Constants.java` hoặc `local.properties`).
   * Thay thế các chuỗi `<YOUR_API_KEY>` bằng key thực tế của nhóm.
3. **Chạy ứng dụng:**
   * Cấp quyền **Vị trí (Location)** và **Thông báo (Notification)** trong lần đầu mở app để ứng dụng hoạt động chính xác.