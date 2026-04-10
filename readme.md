## POJO - Plan Old Java Object 
- Chứa các fields và getter/setter , không extends hay implements gì đặc biệt.
- Dùng để ánh xạ dữ liệu từ DB lên Java Object
## ContentValues
- Dùng để đóng gói dữ liệu cần insert/update vào DB, tương tự HashMap
## Cursor
- Con trỏ trỏ đến từng dòng kết quả truy vấn, phải gọi hàm MoveToNext() để duyệt
- Cần phải cursor.close() và db.close() để tránh tràn bộ nhớ và giải phóng tài nguyên
## Context
- Truy cập tài nguyên (Resources): Nếu bạn muốn lấy một chuỗi văn bản trong file strings.xml, một tấm ảnh trong thư mục drawable, hay một layout... bạn cần Context để "hỏi" hệ thống xem chúng ở đâu.
- Tương tác với hệ thống: Gửi thông báo (Notification), mở một màn hình mới (Activity), hay đăng ký một dịch vụ chạy ngầm.
- Thông tin về môi trường: Biết được ứng dụng đang chạy ở đâu, thư mục lưu trữ dữ liệu nằm ở chỗ nào (để bạn dùng SQLite như ví dụ trước đó).
## Toast
## SessionManager
## Intent
## SharedPreferences 
- SharedPreferences lưu các cặp key-value đơn giản (String, int, boolean) vào file XML, phù hợp cho setting và session. SQLite dùng cho dữ liệu có cấu trúc phức tạp, quan hệ nhiều bảng.
## FLAG_ACTIVITY_NEW_TASK và FLAG_ACTIVITY_CLEAR_TASK 
- Hai flag này kết hợp để xóa toàn bộ back stack và tạo task mới, đảm bảo người dùng không thể nhấn Back để quay lại màn hình trước khi đăng nhập/đăng xuất — bảo mật cho luồng xác thực.
## RecyclerView khác ListView thế nào?
- RecyclerView bắt buộc dùng ViewHolder pattern nên tái sử dụng view hiệu quả hơn, hỗ trợ nhiều LayoutManager (Grid, Linear, Staggered), animation thêm/xóa item mượt hơn. onCreateViewHolder chỉ gọi khi tạo view mới, onBindViewHolder gọi mỗi khi bind dữ liệu vào view.
## Tại sao dùng Singleton cho CartManager thay vì lưu giỏ hàng vào DB?
- Giỏ hàng là dữ liệu tạm thời trong phiên làm việc, không cần persist — dùng Singleton trong memory nhanh hơn, đơn giản hơn. Khi user đặt hàng thì mới ghi vào DB bảng orders và order_items.
## Khi đặt hàng, tại sao phải lưu giá vào bảng order_items thay vì lấy giá từ bảng products?
- vì giá sản phẩm có thể thay đổi theo thời gian — lưu giá tại thời điểm mua vào order_items.price đảm bảo lịch sử đơn hàng luôn chính xác, không bị ảnh hưởng khi admin cập nhật giá sau này.
## AndroidManifest.xml dùng để làm gì?
- khai báo tất cả thành phần của app (Activity, Service, BroadcastReceiver, Permission...). Android đọc file này khi cài app — Activity không khai báo ở đây thì hệ thống không biết đến sự tồn tại của nó.
## Tại sao không cho xóa danh mục khi còn sản phẩm?
- vì bảng products có khóa ngoại category_id tham chiếu đến bảng categories — nếu xóa danh mục khi còn sản phẩm thì dữ liệu bị mồ côi (orphan data), các sản phẩm sẽ trỏ đến category_id không tồn tại gây lỗi khi JOIN.
###

Những điểm quan trọng cần nắm
1. Tại sao dùng Spinner thay vì EditText cho danh mục?
   Vì danh mục là dữ liệu có sẵn trong DB — dùng Spinner buộc admin chỉ chọn từ danh sách có sẵn, tránh nhập sai tên hoặc nhập danh mục không tồn tại. ArrayAdapter nối danh sách Category vào Spinner, khi chọn thì lấy getId() của Category tương ứng theo vị trí.
2. Tại sao nút xóa ghi là "Ẩn" thay vì "Xóa"?
   Vì productDAO.delete() thực ra chỉ set is_active = 0 chứ không xóa khỏi DB. Lý do: nếu xóa hẳn thì các bảng order_items cũ đang tham chiếu đến product_id đó sẽ bị lỗi khóa ngoại — lịch sử đơn hàng bị hỏng.
3. Tại sao giá dùng (int) existing.getPrice() khi hiển thị?
   Để tránh hiển thị 3500000.0 — ép kiểu về int trước khi setText sẽ hiện 3500000 gọn hơn.
4. Spinner trạng thái trong từng item đơn hàng:Admin có thể đổi trạng thái đơn hàng ngay tại chỗ mà không cần mở màn hình khác. Khi chọn trạng thái mới, onItemSelected gọi orderDAO.updateStatus() lưu vào DB ngay lập tức. Dùng biến firstTime = true để tránh trigger lần đầu khi setSelection() — đây là kỹ thuật phổ biến với Spinner trong RecyclerView.
5. Logic lọc kết hợp:Khi chọn lọc theo danh mục thì ưu tiên danh mục trước, bỏ qua filter trạng thái — tránh phức tạp khi kết hợp 2 điều kiện cùng lúc. Đây là trade-off đơn giản phù hợp với scope bài tập.
6. Doanh thu chỉ tính đơn "delivered":getTotalRevenue() chỉ cộng các đơn có status='delivered' — vì đơn pending hay cancelled chưa hoàn thành không nên tính vào doanh thu thực tế.
7. Câu truy vấn lọc theo Danh mục dùng Distinct
- SELECT DISTINCT o.* FROM orders o
   JOIN order_items oi ON o.id = oi.order_id
   JOIN products p ON oi.product_id = p.id
   WHERE p.category_id = ?
-  (Một đơn hàng có thể chứa nhiều sản phẩm cùng danh mục → nếu không có DISTINCT thì đơn đó sẽ bị lặp lại nhiều lần trong kết quả)