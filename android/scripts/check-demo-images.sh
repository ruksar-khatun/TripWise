#!/usr/bin/env bash
# Verifies that every demo Unsplash photo URL in DemoContent.kt returns an image.
# Run from android/: ./scripts/check-demo-images.sh
set -euo pipefail
file="core/data/src/main/kotlin/com/tripwise/core/data/demo/DemoContent.kt"
ids=$(grep -oE '"[0-9]{10,13}-[0-9a-f]{12}"' "$file" | tr -d '"' | sort -u)
fail=0
for id in $ids; do
  url="https://images.unsplash.com/photo-$id?auto=format&fit=crop&w=200&q=50"
  read -r code type < <(curl -sL -o /dev/null -w '%{http_code} %{content_type}\n' "$url")
  if [[ "$code" == "200" && "$type" == image/* ]]; then echo "OK   $id"; else echo "FAIL $id ($code $type)"; fail=1; fi
done
exit $fail
