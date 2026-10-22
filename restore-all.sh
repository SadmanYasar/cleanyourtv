#!/usr/bin/env bash
# Restore script for Xiaomi Mi Box 4 (MIBOX4)
set -euo pipefail

cd "$(dirname "$0")"
TV_HOST="${TV_HOST:-127.0.0.1}"
TV_PORT="${TV_PORT:-15555}"
PY=.venv/bin/python3

echo "Restoring all disabled packages on $TV_HOST:$TV_PORT..."

# Combine all debloated lists
for list in work/batch1.txt work/batch2.txt work/batch3.txt; do
  if [ -f "$list" ]; then
    echo "Restoring packages from $list..."
    TV_HOST="$TV_HOST" TV_PORT="$TV_PORT" ./tools/restore.sh "$list"
  fi
done

# Re-enable stock launcher
echo "Re-enabling stock launcher..."
TV_HOST="$TV_HOST" TV_PORT="$TV_PORT" "$PY" -I tools/tv.py "pm enable --user 0 com.google.android.tvlauncher"
TV_HOST="$TV_HOST" TV_PORT="$TV_PORT" "$PY" -I tools/tv.py "cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity" || true

# Reset private DNS if desired
echo "Resetting Private DNS to opportunistic..."
TV_HOST="$TV_HOST" TV_PORT="$TV_PORT" "$PY" -I tools/tv.py "settings put global private_dns_mode opportunistic"

echo "Restore complete! The stock launcher and all apps have been restored."
