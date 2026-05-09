# Micsu Music Streaming API

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8+-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Auth_State-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Cloudflare R2](https://img.shields.io/badge/Cloudflare_R2-Storage-F38020?logo=cloudflare&logoColor=white)](https://www.cloudflare.com/developer-platform/r2/)

Micsu là backend REST API cho nền tảng nghe nhạc và tương tác xã hội, xây dựng bằng Spring Boot. Dự án tập trung vào luồng tải lên, xử lý và phát nhạc qua HLS, đồng thời hỗ trợ các chức năng xã hội như theo dõi người dùng, thích, bình luận, repost, playlist và lịch sử nghe.

## Tổng quan

Hệ thống được thiết kế theo mô hình layered monolith, tách rõ các lớp controller, service, repository, entity, DTO và security. Dữ liệu chính được lưu trên MySQL, trạng thái auth được quản lý bằng JWT kết hợp Redis, còn file media được lưu trên Cloudflare R2 thông qua AWS SDK v2.

## Tính năng chính

### Xác thực và bảo mật

- Đăng ký, đăng nhập, làm mới token và đăng xuất bằng cơ chế access token + refresh token.
- JWT được kiểm tra bởi security filter; access token bị thu hồi sẽ được chặn thông qua Redis blacklist.
- Hỗ trợ phân quyền theo vai trò ở tầng bảo mật.

### Nhạc và phát trực tuyến

- Tải lên bài hát và lưu metadata phục vụ phát nhạc.
- Xử lý media theo luồng HLS để tối ưu việc streaming.
- Lưu trữ file âm thanh và segment HLS trên Cloudflare R2.

### Tính năng xã hội

- Thích, bình luận và repost bài hát.
- Theo dõi và bỏ theo dõi người dùng.
- Lưu lịch sử nghe nhạc.

### Tổ chức nội dung

- Quản lý playlist cá nhân.
- Quản lý hồ sơ người dùng.
- Tìm kiếm bài hát và duyệt theo thể loại.
- Ghi nhận lượt xem.

## Công nghệ sử dụng

- Java 21
- Spring Boot 3.4.3
- Spring Security, OAuth2 Resource Server, Nimbus JOSE + JWT
- Spring Data JPA, MySQL 8
- Spring Data Redis
- Cloudflare R2 qua AWS SDK v2
- FFmpeg cho xử lý HLS
- Lombok, MapStruct, Apache Commons
- Springdoc OpenAPI
- Micrometer Prometheus, Spring Boot Actuator
- Maven

## Kiến trúc dự án

```text
src/main/java/com/tunhan/micsu/
├── configuration/   # Cấu hình ứng dụng
├── controller/      # REST API
├── dto/             # Request/response models
├── entity/          # Domain entities
├── exception/       # Xử lý lỗi
├── repository/      # Truy cập dữ liệu
├── security/        # JWT, filters, auth
├── service/         # Business logic
└── utils/           # Tiện ích xử lý media, file, token
```

Các luồng chính của hệ thống:

1. Người dùng tải lên audio.
2. Ứng dụng xử lý media, tạo nội dung HLS.
3. Segment và playlist được lưu trên Cloudflare R2.
4. API trả về dữ liệu để client phát nhạc và hiển thị metadata.

## Yêu cầu hệ thống

- JDK 21
- Maven 3.9+ hoặc Maven Wrapper
- MySQL 8+
- Redis
- FFmpeg nếu bạn chạy luồng xử lý media trên máy local
- Cloudflare R2 credentials nếu dùng môi trường thật

## Cấu hình môi trường

Dự án đọc biến môi trường từ file `.env` hoặc môi trường hệ thống. Các biến quan trọng gồm:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `R2_ACCOUNT_ID`
- `R2_ACCESS_KEY`
- `R2_SECRET_KEY`
- `R2_BUCKET`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`

Ứng dụng mặc định chạy tại:

- Base URL: `http://localhost:8081/micsu`
- OpenAPI spec: `http://localhost:8081/micsu/docs/openapi.yml`
- Swagger UI: `http://localhost:8081/micsu/swagger-ui/index.html`
- Actuator health: `http://localhost:8081/micsu/actuator/health`
- Prometheus metrics: `http://localhost:8081/micsu/actuator/prometheus`

## Chạy ứng dụng local

### 1. Chuẩn bị `.env`

Tạo file `.env` ở thư mục gốc và khai báo các biến môi trường cần thiết.

### 2. Khởi động MySQL và Redis

Bạn có thể chạy trực tiếp bằng Docker Compose:

```bash
docker compose up -d mysql redis
```

Hoặc dùng full stack nếu bạn đã cấu hình đầy đủ image và reverse proxy:

```bash
docker compose up -d
```

### 3. Chạy ứng dụng Spring Boot

```bash
./mvnw spring-boot:run
```

Trên Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Chạy bằng Docker

File `docker-compose.yml` đã khai báo các dịch vụ chính:

- `micsu-app`
- `mysql`
- `redis`
- `nginx`

Nếu bạn muốn dùng image dựng sẵn, chỉ cần đảm bảo file `.env` đúng và chạy:

```bash
docker compose up -d
```

## Tài liệu API

Tài liệu OpenAPI được phục vụ trực tiếp từ ứng dụng và được mount vào thư mục static của Spring Boot. Bạn có thể mở Swagger UI để khám phá các endpoint theo từng nhóm như auth, songs, playlists, likes, comments, follows, reposts, search, history, genres và users.

## Ghi chú về Redis

Redis trong dự án chỉ được dùng cho trạng thái liên quan đến auth/security, không phải cache tổng quát cho toàn bộ ứng dụng.

- Refresh token được lưu theo khóa `refresh:<token> -> userId` với TTL.
- Access token đã thu hồi được blacklist theo `blacklist:<jti> -> 1` với TTL.
- Filter bảo mật kiểm tra blacklist trước khi cho phép JWT đi tiếp vào luồng xác thực.

## Cấu trúc tài nguyên

```text
docs/openapi/      # OpenAPI spec source
nginx/             # Cấu hình reverse proxy và SSL
monitoring/        # Prometheus và cấu hình giám sát
src/main/resources # Application config và static assets
```

## Đóng góp

Nếu bạn muốn mở rộng dự án, nên giữ nguyên cách tổ chức theo layer hiện tại, đồng thời cập nhật tài liệu OpenAPI và README khi thêm endpoint mới, thay đổi biến môi trường hoặc bổ sung dịch vụ hạ tầng.
