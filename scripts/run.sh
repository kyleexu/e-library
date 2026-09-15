#!/usr/bin/env bash
# 本地启动脚本：检查/初始化 H2 → mvn compile → 杀旧进程 → 启动服务
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PORT="${PORT:-8080}"
DB_DIR="$ROOT_DIR/data"
DB_FILE="$DB_DIR/elibrary.mv.db"
SCHEMA_SQL="$ROOT_DIR/src/main/resources/schema.sql"
DATA_SQL="$ROOT_DIR/src/main/resources/data.sql"
PID_FILE="$ROOT_DIR/data/e-library.pid"
LOG_FILE="$ROOT_DIR/data/e-library.log"
JDBC_URL="jdbc:h2:file:${DB_DIR}/elibrary"

# --- JDK 11 ---
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 11)}"
fi
export PATH="$JAVA_HOME/bin:$PATH"

echo "==> JAVA_HOME=$JAVA_HOME"
java -version

# --- 1. 检查 H2；没有则用固定 SQL 建库 ---
ensure_h2_db() {
  if [[ -f "$DB_FILE" ]]; then
    echo "==> H2 database exists: $DB_FILE"
    return
  fi

  echo "==> H2 database not found, creating from schema.sql + data.sql ..."
  mkdir -p "$DB_DIR"

  # 确保依赖已下载，拿到可运行的 h2 jar（排除 sources/javadoc）
  mvn -q -DskipTests dependency:resolve
  H2_JAR="$(find "$HOME/.m2/repository/com/h2database/h2" -name 'h2-*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' 2>/dev/null | sort -V | tail -n 1 || true)"
  if [[ -z "$H2_JAR" || ! -f "$H2_JAR" ]]; then
    echo "ERROR: cannot find h2 jar in local Maven repo" >&2
    exit 1
  fi
  # 再确认 jar 内含 RunScript
  if ! jar tf "$H2_JAR" 2>/dev/null | grep -q 'org/h2/tools/RunScript.class'; then
    echo "ERROR: selected jar has no RunScript: $H2_JAR" >&2
    exit 1
  fi
  echo "==> using H2 jar: $H2_JAR"
  java -cp "$H2_JAR" org.h2.tools.RunScript \
    -url "$JDBC_URL" \
    -user sa \
    -password "" \
    -script "$SCHEMA_SQL"

  java -cp "$H2_JAR" org.h2.tools.RunScript \
    -url "$JDBC_URL" \
    -user sa \
    -password "" \
    -script "$DATA_SQL"

  echo "==> H2 database created: $DB_FILE"
}

# --- 3. 杀死占用端口的老服务 ---
kill_old_service() {
  echo "==> Stopping old process on port $PORT (if any) ..."
  if [[ -f "$PID_FILE" ]]; then
    OLD_PID="$(cat "$PID_FILE" || true)"
    if [[ -n "${OLD_PID}" ]] && kill -0 "$OLD_PID" 2>/dev/null; then
      kill "$OLD_PID" 2>/dev/null || true
      sleep 1
      kill -9 "$OLD_PID" 2>/dev/null || true
    fi
    rm -f "$PID_FILE"
  fi

  if command -v lsof >/dev/null 2>&1; then
    PIDS="$(lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null || true)"
    if [[ -n "$PIDS" ]]; then
      echo "$PIDS" | xargs kill 2>/dev/null || true
      sleep 1
      PIDS="$(lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null || true)"
      if [[ -n "$PIDS" ]]; then
        echo "$PIDS" | xargs kill -9 2>/dev/null || true
      fi
    fi
  fi
}

ensure_h2_db

# --- 2. mvn 编译 ---
echo "==> mvn compile ..."
mvn -q -DskipTests compile

kill_old_service

# --- 启动新服务 ---
echo "==> Starting Spring Boot on port $PORT ..."
mkdir -p "$DB_DIR"
nohup mvn -q spring-boot:run -Dspring-boot.run.arguments="--server.port=${PORT}" \
  >"$LOG_FILE" 2>&1 &
echo $! >"$PID_FILE"

echo "==> Started. pid=$(cat "$PID_FILE"), log=$LOG_FILE"
echo "==> API: http://localhost:${PORT}/api/books"
echo "==> H2:  http://localhost:${PORT}/h2-console  (JDBC: jdbc:h2:file:./data/elibrary)"
