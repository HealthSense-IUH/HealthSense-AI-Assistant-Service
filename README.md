# HealthSense AI Assistant Service

Trợ lý AI của HealthSense, chạy trên **Claude qua Anthropic API** bằng Spring AI. Service này đứng cạnh Core Service: nhận lời nói hoặc chữ của người dùng, lấy dữ liệu thật qua API của Core Service (tool calling), và trả về lời diễn giải. Nó không chẩn đoán, không kê đơn; mọi kết luận về rung nhĩ thuộc về bác sĩ và điện tâm đồ.

## Tính năng dự kiến

| Tính năng | LLM làm gì | Số liệu từ đâu |
|---|---|---|
| Nhật ký yếu tố kích hoạt | tách câu nói tự do thành yếu tố có cấu trúc (rượu, cà phê, giấc ngủ, triệu chứng) | nhật ký + đợt cảnh báo AF; tương quan tính bằng SQL mỗi tuần |
| Báo cáo tuần | viết 5 câu từ bảng số | lịch sử đo, cảnh báo, bữa ăn |
| Tóm tắt tiền khám cho bác sĩ | gom hồ sơ thành một trang | Core Service |
| Giải thích từng cảnh báo | dịch SHAP của mô hình V6C ra lời | AI AFib Prediction Service |
| Chụp ảnh bữa ăn | nhận diện món, ước lượng khẩu phần, tra FNDDS qua tool | bảng FNDDS của Core Service |

## Công nghệ

Java 21, Spring Boot 4.1, Spring AI 2.0 (`spring-ai-starter-model-anthropic`, bộ nhớ hội thoại JDBC), Spring Data JPA + PostgreSQL (schema `assistant`), RabbitMQ (sự kiện từ Core Service), OpenFeign (gọi Core Service), Actuator.

Package gốc: `fit.iuh.se.hsassistant` (cùng quy ước `fit.iuh.se.hs*` của Core Service).

## Chạy tại chỗ

```bash
cp .env.example .env        # điền ANTHROPIC_API_KEY
docker compose up --build   # service ở cổng 8081 + Postgres dev
```

Hoặc chạy thẳng bằng Maven khi đã có Postgres:

```bash
./mvnw spring-boot:run
```

Kiểm tra:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8081/api/assistant/info
curl -X POST http://localhost:8081/api/assistant/chat -H "Content-Type: application/json" -d "{\"conversationId\":\"demo\",\"message\":\"Xin chào\"}"
```

## Biến môi trường

| Biến | Ý nghĩa | Mặc định |
|---|---|---|
| `ANTHROPIC_API_KEY` | API key tạo tại console.anthropic.com | bắt buộc |
| `ANTHROPIC_MODEL` | model Claude; Sonnet cho hội thoại, `claude-haiku-5-5` cho trích xuất rẻ, `claude-opus-5-5` cho báo cáo dài | `claude-sonnet-5-5` |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SCHEMA` | PostgreSQL dùng chung với Core Service (tên biến giống Core), schema `assistant` | bắt buộc, `DB_SCHEMA=assistant` |
| `RABBITMQ_HOST`, `_PORT`, `_USERNAME`, `_PASSWORD`, `_VHOST`, `_EXCHANGE`, `_QUEUE_ASSISTANT` | RabbitMQ của Core Service | bắt buộc |
| `RABBITMQ_HEALTH_ENABLED` | đưa RabbitMQ vào `/actuator/health` | `true` |
| `CORE_BASE_URL` | Core Service cho các tool | bắt buộc |
| `SERVER_PORT` | cổng HTTP | `8081` |

`application.properties` (chạy thật) chỉ đọc biến môi trường; `application-dev.properties` có sẵn giá trị dev và **không được commit** (`.gitignore`), giống Core Service. Schema `assistant` phải tồn tại trước trên Postgres (`CREATE SCHEMA assistant;`), sau đó Spring AI tự tạo bảng `SPRING_AI_CHAT_MEMORY` lần chạy đầu.

## CI/CD

`.github/workflows/ci-cd.yml`: Maven build + test (JDK 21), và khi push lên `main`: build image, quét CVE bằng Trivy cho cả hệ điều hành lẫn thư viện Java trong jar (chặn CRITICAL/HIGH đã có bản vá), đẩy lên Docker Hub `<DOCKERHUB_USERNAME>/ai-assistant-service` với tag `latest` và mã commit. Cần hai secret `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`.

Dockerfile build 2 giai đoạn, chép jar theo layer (dependencies tách khỏi application) nên lần build sau chỉ đổi vài MB; runtime là JRE 21, chạy user không phải root, healthcheck `/actuator/health/liveness`.

## Việc còn thiếu trước khi mở ra ngoài

- Xác thực: thêm `spring-boot-starter-oauth2-resource-server`, kiểm JWT do Core Service cấp, lấy `userId` từ token thay vì tin `conversationId` của client.
- Kiểm soát nội dung: lớp lọc riêng trước và sau mô hình (danh sách chủ đề cấm, che thông tin cá nhân trước khi gửi, dấu hiệu nguy hiểm → hướng dẫn 115), vì Anthropic API không có sẵn guardrails theo cấu hình.
- Che mật khẩu trong log kết nối RabbitMQ.
