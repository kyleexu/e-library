#!/usr/bin/env bash
# 本地/VPS 启动：检查/初始化 H2 → 编译 → 杀旧进程 → 启动服务
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
JAR_FILE="$ROOT_DIR/target/e-library-0.0.1-SNAPSHOT.jar"

resolve_java_home() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    return
  fi
  if [[ -x /usr/libexec/java_home ]]; then
    JAVA_HOME="$(/usr/libexec/java_home -v 11 2>/dev/null || /usr/libexec/java_home)"
    export JAVA_HOME
    return
  fi
  for candidate in \
    /usr/lib/jvm/java-11-openjdk-amd64 \
    /usr/lib/jvm/java-17-openjdk-amd64 \
    /usr/lib/jvm/default-java
  do
    if [[ -x "$candidate/bin/java" ]]; then
      export JAVA_HOME="$candidate"
      return
    fi
  done
  if command -v java >/dev/null 2>&1; then
    local java_bin
    java_bin="$(readlink -f "$(command -v java)" 2>/dev/null || command -v java)"
    export JAVA_HOME
    JAVA_HOME="$(cd "$(dirname "$java_bin")/.." && pwd)"
    return
  fi
  echo "ERROR: Java not found. Install JDK 11+ or set JAVA_HOME." >&2
  exit 1
}

find_h2_jar() {
  local jar=""
  jar="$(find "$HOME/.m2/repository/com/h2database/h2" -name 'h2-*.jar' \
    ! -name '*-sources.jar' ! -name '*-javadoc.jar' 2>/dev/null | sort -V | tail -n 1 || true)"
  if [[ -n "$jar" && -f "$jar" ]]; then
    echo "$jar"
    return 0
  fi
  return 1
}

resolve_java_home
export PATH="${JAVA_HOME}/bin:${PATH}"

echo "==> JAVA_HOME=${JAVA_HOME}"
java -version

ensure_h2_db() {
  if [[ -f "$DB_FILE" ]]; then
    echo "==> H2 database exists: $DB_FILE"
    return
  fi

  echo "==> H2 database not found, creating from schema.sql + data.sql ..."
  mkdir -p "$DB_DIR"

  local H2_JAR=""
  if ! H2_JAR="$(find_h2_jar)"; then
    echo "==> h2 jar missing locally, running mvn dependency:resolve (may take a while) ..."
    mvn -q -DskipTests dependency:resolve
    H2_JAR="$(find_h2_jar || true)"
  fi
  if [[ -z "${H2_JAR}" || ! -f "${H2_JAR}" ]]; then
    echo "ERROR: cannot find h2 jar in local Maven repo" >&2
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
      # shellcheck disable=SC2086
      kill $PIDS 2>/dev/null || true
      sleep 1
      PIDS="$(lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null || true)"
      if [[ -n "$PIDS" ]]; then
        # shellcheck disable=SC2086
        kill -9 $PIDS 2>/dev/null || true
      fi
    fi
  elif command -v fuser >/dev/null 2>&1; then
    fuser -k "${PORT}/tcp" 2>/dev/null || true
  fi
}

ensure_h2_db

echo "==> building ..."
if [[ ! -f "$JAR_FILE" ]]; then
  mvn -q -DskipTests package
else
  mvn -q -DskipTests package -o || mvn -q -DskipTests package
fi

kill_old_service

echo "==> Starting Spring Boot on port $PORT ..."
mkdir -p "$DB_DIR"
nohup java -jar "$JAR_FILE" --server.port="${PORT}" >"$LOG_FILE" 2>&1 &
echo $! >"$PID_FILE"

echo "==> Started. pid=$(cat "$PID_FILE"), log=$LOG_FILE"
echo "==> API: http://localhost:${PORT}/api/books"
echo "==> H2:  http://localhost:${PORT}/h2-console  (JDBC: jdbc:h2:file:./data/elibrary)"
