# E-Library Service

简易电子图书馆后端：浏览 / 查询书籍、借阅、归还、查看当前借阅。

## Tech Stack

- Java 11
- Spring Boot 2.7
- MyBatis
- H2（本地文件库 `./data/elibrary`）
- Maven

## Assumptions

| 点 | 取舍 |
|---|---|
| 用户身份 | 不做登录；用请求头 `X-User-Id` 标识当前用户 |
| 借阅天数 | body 传 `loanDays`，仅允许 `14` 或 `30`；不传默认 `14` |
| 库存 | `Book.availableCopies`；借 -1、还 +1 |
| 重复借阅 | 同一用户对同一本书在未归还（`BORROWED`）时不能再借 |
| 内容类型 | `BookType`：COMPUTER / JOURNAL / COMIC / MAGAZINE / NOVEL / REFERENCE / HISTORY / SCIENCE / ART / BIOGRAPHY / KIDS |
| 认证 / 支付 / 预约 / 管理后台 | 刻意不做，控制作业范围 |

## Project Layout

```
controller/   REST API
service/      业务逻辑（借还带事务）
mapper/       MyBatis
model/        Book / Loan 等领域模型
common/       ApiResponse、全局异常处理
dto/          请求/响应 DTO
resources/schema.sql / data.sql
scripts/run.sh
```

## How to Run

### 推荐：启动脚本

脚本会：检查 H2（没有则用固定 SQL 建库）→ `mvn compile` → 杀掉旧进程 → 后台启动。

```bash
./scripts/run.sh
# 可选端口
PORT=8081 ./scripts/run.sh
```

- 日志：`data/e-library.log`
- PID：`data/e-library.pid`

### 手动启动

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 11)"
# 若还没有库文件，可先跑一次脚本，或自行用 H2 RunScript 执行 schema.sql + data.sql
mvn spring-boot:run
```

### H2 Console

- URL：http://localhost:8080/h2-console
- JDBC URL：`jdbc:h2:file:./data/elibrary`
- User：`sa`
- Password：（空）

### 简易测试页

启动服务后打开：http://localhost:8080/

可在页面上测：列表 / 详情 / 借阅 / 归还 / 我的借阅。

重建库（会清空借阅数据）：

```bash
rm -rf data/elibrary.mv.db data/elibrary.trace.db
./scripts/run.sh
```

## API

统一响应：

```json
{
  "code": 0,
  "status": "success",
  "message": "OK",
  "data": {}
}
```

失败时 `code` 非 0（如 404 / 409），`status` 为 `error`。

### 浏览书籍

```bash
curl "http://localhost:8080/api/books"
curl "http://localhost:8080/api/books?q=Clean"
curl "http://localhost:8080/api/books?type=COMPUTER"
```

### 书籍详情

```bash
curl "http://localhost:8080/api/books/1"
```

### 某书当前借出用户

```bash
curl "http://localhost:8080/api/books/1/borrowers"
```

### 借阅

```bash
curl -X POST "http://localhost:8080/api/loans" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: u1" \
  -d '{"bookId":1,"loanDays":14}'
```

### 归还

```bash
curl -X POST "http://localhost:8080/api/loans/1/return" \
  -H "X-User-Id: u1"
```

### 我的当前借阅

含借出时间、应还时间、已借天数等：

```bash
curl "http://localhost:8080/api/me/loans" \
  -H "X-User-Id: u1"
```

## Tests

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 11)"
mvn test
```

`LoanServiceTest` 覆盖：借书成功、重复借失败、书不存在、借阅单不存在、归还成功。

## Design Notes

- **分层**：Controller → Service → Mapper；借还写路径在 `LoanService` 内直接使用 `BookMapper` + `LoanMapper`，避免 Service 互相绕圈。
- **事务**：`borrowBook` / `returnBook` 使用 `@Transactional`，避免「写了借阅单但扣库存失败」。
- **错误**：业务异常统一为 `BusinessException`，经 `@RestControllerAdvice` 转为 `ApiResponse.fail`。
- **库存并发**：扣库存 SQL 带 `available_copies > 0` 条件，影响行数为 0 则失败回滚。
