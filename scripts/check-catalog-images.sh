#!/usr/bin/env bash
# Verifies every catalog photo URL returns an image, and builds a labelled contact sheet
# (out/catalog-contact-sheet.jpg) so a person can confirm each photo shows the right place.
# Needs: curl, jq, ImageMagick. Run from the repo root.
set -euo pipefail
catalog="backend/catalog/destinations.json"
mkdir -p out/thumbs
fail=0
while IFS=$'\t' read -r id name url; do
  thumb="out/thumbs/${id}.jpg"
  read -r code type < <(curl -sL -o "$thumb" -w '%{http_code} %{content_type}\n' "${url/w=1200/w=480}")
  if [[ "$code" == "200" && "$type" == image/* ]]; then
    echo "OK   $id"
    convert "$thumb" -resize 480x320^ -gravity center -extent 480x320 \
      -fill white -undercolor '#000000AA' -pointsize 26 -gravity south -annotate +0+8 " $name ($id) " "$thumb"
  else
    echo "FAIL $id ($code $type) $url"; fail=1; rm -f "$thumb"
  fi
done < <(jq -r '.destinations[] | [.id, .name, .images[0].url] | @tsv' "$catalog")
montage out/thumbs/*.jpg -tile 4x -geometry +6+6 -background '#0D1B2A' out/catalog-contact-sheet.jpg
exit $fail
