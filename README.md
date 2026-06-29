# Ứng Dụng Mobile: Đặt Vé Xem Phim

**Bài tập lớn môn học: Lập trình Mobile cơ bản**

**Giảng viên hướng dẫn:** BÙI ĐỨC THỌ

**Lớp:** 12523T.1


# Giới thiệu đề tài

## Bối cảnh

Hiện nay nhu cầu đặt vé xem phim ngày càng phổ biến. Tuy nhiên, việc phải đến trực tiếp quầy vé hoặc chờ đợi vào giờ cao điểm gây mất thời gian và tạo sự bất tiện cho người xem.

## Vấn đề hiện tại

* Người dùng phải đến trực tiếp để xem lịch chiếu và đặt vé.
* Khó quản lý số lượng ghế còn trống.
* Việc cập nhật lịch chiếu và thông tin phim còn thủ công.
* Chưa có hệ thống hỗ trợ quản lý vé và đặt vé hiệu quả.

## Mục tiêu / Giải pháp

Xây dựng ứng dụng Android giúp người dùng:

* Xem danh sách phim.
* Xem thông tin chi tiết phim.
* Chọn rạp và suất chiếu.
* Chọn ghế và đặt vé.
* Theo dõi lịch sử đặt vé.

Đồng thời cung cấp phân hệ Admin giúp quản lý phim, lịch chiếu, đơn đặt vé và người dùng bằng cơ sở dữ liệu SQLite.


# Thiết kế Cơ sở dữ liệu (Database)

Ứng dụng sử dụng SQLite để lưu trữ dữ liệu cục bộ nhằm đảm bảo tốc độ truy vấn nhanh và hoạt động ổn định.

## Các bảng dữ liệu chính

### NguoiDung

Quản lý tài khoản người dùng và quản trị viên.

Các trường:

* id
* username
* password
* fullname
* phone
* role

### Phim

Quản lý thông tin phim.

Các trường:

* id
* tenPhim
* moTa
* theLoai
* thoiLuong
* ngayKhoiChieu
* hinhAnh
* giaVe


### RapPhim

Quản lý thông tin rạp.

Các trường:

* id
* tenRap
* diaChi


### SuatChieu

Quản lý lịch chiếu phim.

Các trường:

* id
* idPhim
* idRap
* ngayChieu
* gioChieu
* phongChieu


### Ghe

Quản lý trạng thái ghế.

Các trường:

* id
* tenGhe
* trangThai
* idSuatChieu


### Ve

Quản lý thông tin đặt vé.

Các trường:

* id
* idNguoiDung
* idSuatChieu
* tongTien
* ngayDat
* trangThai

---

### ChiTietVe

Lưu chi tiết vé đã đặt.

Các trường:

* id
* idVe
* idGhe
* gia


# Luồng chức năng (Use Cases)

Hệ thống được xây dựng cho hai đối tượng chính.

## Người dùng (User)

### Đăng ký

Tạo tài khoản mới.

### Đăng nhập

Đăng nhập để sử dụng hệ thống.

### Xem danh sách phim

Hiển thị tất cả các bộ phim.

### Xem chi tiết phim

Hiển thị:

* Poster phim
* Thời lượng
* Mô tả
* Giá vé

### Tìm kiếm phim

Tìm theo tên phim.

### Đặt vé

* Chọn phim
* Chọn rạp
* Chọn suất chiếu
* Chọn ghế
* Xác nhận đặt vé

### Lịch sử đặt vé

Xem danh sách các vé đã đặt.

### Đăng xuất

Thoát khỏi hệ thống.


## Quản trị viên (Admin)

### Đăng nhập

Đăng nhập bằng tài khoản Admin.

### Quản lý phim

* Thêm phim
* Sửa phim
* Xóa phim
* Hiển thị danh sách phim

### Quản lý lịch chiếu

* Thêm lịch chiếu
* Sửa lịch chiếu
* Xóa lịch chiếu

### Quản lý ghế

* Cập nhật trạng thái ghế

### Quản lý vé

* Xem danh sách đặt vé
* Cập nhật trạng thái
* Xóa đơn

### Quản lý tài khoản

* Xem danh sách người dùng
* Đổi mật khẩu


# Công nghệ & Kiến trúc sử dụng

## Nền tảng

Android (API 24 trở lên)

## Ngôn ngữ

Java

## Môi trường phát triển

Android Studio

## Cơ sở dữ liệu

SQLite

## Thiết kế giao diện

* XML Layout
* RecyclerView
* CardView
* ViewBinding
* Material Design

## Kiến trúc

* Activity
* Adapter
* Model
* SQLiteOpenHelper
* Intent
* SharedPreferences


# Kết quả đạt được

* Ứng dụng hoạt động ổn định.
* Giao diện thân thiện và dễ sử dụng.
* Quản lý dữ liệu bằng SQLite.
* Hỗ trợ phân quyền User/Admin.
* Chức năng CRUD hoạt động chính xác.
* Đặt vé và quản lý ghế hoạt động tốt.


# Hạn chế

* Chỉ lưu dữ liệu trên thiết bị.
* Chưa hỗ trợ thanh toán trực tuyến.
* Chưa đồng bộ giữa nhiều thiết bị.


# Hướng phát triển

* Tích hợp Firebase.
* Thanh toán trực tuyến (VNPay, MoMo).
* Thông báo xác nhận vé.
* Đánh giá phim.
* Theo dõi trạng thái vé theo thời gian thực.


# Hướng dẫn chạy dự án

## Cài đặt môi trường

* Android Studio
* Android SDK
* Java JDK
* Máy ảo Android hoặc thiết bị thật

## Cách chạy

Clone project:

git clone [Link GitHub của bạn]

Sau đó:

1. Mở Android Studio
2. Chọn Open Project
3. Chờ Gradle đồng bộ
4. Nhấn Run App


# Tài khoản mặc định

## Admin

Tài khoản:
admin

Mật khẩu:
123

## User

Có thể đăng ký tài khoản mới trên ứng dụng.

# Cấu trúc thư mục dự án

├── app/
│ ├── src/main/java/com/example/appdatvephim/
│ │ ├── activities/
│ │ ├── adapters/
│ │ ├── database/
│ │ ├── models/
│ │ └── utils/
│ │
│ ├── src/main/res/
│ │ ├── layout/
│ │ ├── drawable/
│ │ ├── values/
│ │ └── mipmap/
│ │
│ └── AndroidManifest.xml
│
├── reports/
├── slides/
├── README.md
└── .gitignore


# Tác giả

Họ và tên: Lê Thanh Thảo & Nguyễn Thị Thùy

Lớp: 12523T.1

Đề tài: Xây dựng ứng dụng Mobile Đặt Vé Xem Phim

Môn học: Lập trình Mobile cơ bản
