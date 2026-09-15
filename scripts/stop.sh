#!/usr/bin/env bash
# 停止后台运行的 e-library（按 PID 文件和端口）
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PORT="${PORT:-8080}"
PID_FILE="$ROOT_DIR/data/e-library.pid"

echo "==> Stopping e-library on port $PORT ..."

stopped=0

if [[ -f "$PID_FILE" ]]; then
  OLD_PID="$(cat "$PID_FILE" || true)"
  if [[ -n "${OLD_PID}" ]] && kill -0 "$OLD_PID" 2>/dev/null; then
    kill "$OLD_PID" 2>/dev/null || true
    sleep 1
    if kill -0 "$OLD_PID" 2>/dev/null; then
      kill -9 "$OLD_PID" 2>/dev/null || true
    fi
    echo "==> stopped pid=$OLD_PID"
    stopped=1
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
    echo "==> cleared listeners on port $PORT"
    stopped=1
  fi
elif command -v fuser >/dev/null 2>&1; then
  if fuser "${PORT}/tcp" >/dev/null 2>&1; then
    fuser -k "${PORT}/tcp" 2>/dev/null || true
    echo "==> cleared listeners on port $PORT"
    stopped=1
  fi
fi

if [[ "$stopped" -eq 0 ]]; then
  echo "==> nothing to stop"
fi
