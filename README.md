# Nhà thuốc Bình An

Project demo gồm giao diện Vue 3, REST API Java Spring Boot và PostgreSQL.

## 1. Chuẩn bị PostgreSQL

Tạo database một lần trong pgAdmin hoặc `psql`, kết nối vào PostgreSQL bằng tài khoản quản trị:

```sql
CREATE DATABASE binh_an;
```

Backend mặc định kết nối `localhost:5432`, database `binh_an`, user `binh_an`.
Lệnh cài PostgreSQL riêng cho project tạo database, role ứng dụng và đặt `DB_URL`,
`DB_USER`, `DB_PASSWORD` trong biến môi trường Windows của tài khoản hiện tại. Mở terminal
mới sau khi cài, rồi tại thư mục `backend` chạy:

```powershell
mvn spring-boot:run
```

Không cần tạo bảng thủ công: Hibernate tạo/cập nhật bảng khi chạy. Khi database chưa có
sản phẩm, backend tự thêm dữ liệu thuốc demo. Có thể đổi kết nối bằng `DB_URL`, `DB_USER`,
`DB_PASSWORD` và cổng API bằng `SERVER_PORT`.

## 2. Chạy ứng dụng

Mở terminal thứ nhất tại `backend`:

```powershell
mvn spring-boot:run
```

Mở terminal thứ hai tại `frontend`:

```powershell
npm install
npm run dev
```

Mở địa chỉ Vite hiển thị, thường là `http://localhost:5173`. Vite chuyển tiếp `/api` sang
backend tại `http://localhost:8080`.

## API

- `GET /api/medicines` — danh sách thuốc và tồn kho.
- `GET /api/orders` — lịch sử đơn, không trả về thông tin liên hệ/địa chỉ khách hàng.
- `POST /api/orders` — tạo đơn, kiểm tra tồn kho và hạn dùng trong transaction, trừ kho,
  yêu cầu xác nhận khi có thuốc kê đơn.

Ví dụ body tạo đơn:

```json
{
  "customer": {
    "name": "Nguyen Van A",
    "phone": "0901234567",
    "address": "Quan 1, TP HCM"
  },
  "items": [
    { "medicineId": 1, "quantity": 2 }
  ],
  "prescriptionConfirmed": false
}
```

## Deploy miễn phí

Project có thể chạy với frontend trên Vercel, backend Spring Boot trên Render và PostgreSQL trên Neon.
Các gói miễn phí có giới hạn; Render có thể tạm dừng backend khi không có truy cập, nên request đầu
có thể khởi động chậm. Chỉ dùng dữ liệu demo, không lưu dữ liệu khách hàng thật.

1. Tạo database PostgreSQL trên Neon, lấy JDBC URL và thông tin user/password.
2. Đưa project lên một GitHub repository **private**. Tạo dịch vụ Render từ `render.yaml`,
   nhập `DB_URL` dạng `jdbc:postgresql://<host>/<database>?sslmode=require`, cùng `DB_USER`
   và `DB_PASSWORD`. Khi Render cấp URL dạng `https://<ten-dich-vu>.onrender.com`, ghi lại URL này.
3. Import cùng repository vào Vercel, đặt Root Directory là `frontend`, rồi thêm biến build
   `VITE_API_BASE_URL` bằng URL backend Render (không thêm dấu `/` cuối).
4. Sau khi Vercel cấp URL frontend, đặt biến Render `APP_CORS_ORIGINS` thành URL đó
   (ví dụ `https://<ten-site>.vercel.app`) rồi redeploy backend.

Trong môi trường production, database ngoài được cấu hình qua biến môi trường Render; không đưa
mật khẩu Neon vào source code. Cửa hàng demo hiện chưa có đăng nhập/phân quyền, vì vậy không
nên mở chức năng đặt hàng hoặc ghi dữ liệu khách hàng thật trên Internet.

Ứng dụng Java console cũ vẫn có thể chạy bằng JDK 11:

```powershell
javac -encoding UTF-8 -d out src\Main.java
java -cp out Main
```

> Đây là project demo chưa có đăng nhập/phân quyền quản trị. Không triển khai công khai
> hoặc lưu dữ liệu khách hàng thật trước khi bổ sung xác thực và bảo vệ dữ liệu phù hợp.
