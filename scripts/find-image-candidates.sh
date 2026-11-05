#!/usr/bin/env bash
# Searches Wikimedia Commons for freely licensed photos of each destination in
# scripts/image-queries.json and writes out/candidates.json plus a labelled contact sheet
# (out/candidates-<id>.jpg) so a person can pick the photo that really shows the place.
# Needs: curl, jq, ImageMagick. Run from the repo root.
set -euo pipefail
UA="TripWise-catalog-curation/1.0 (https://github.com/ruksar-khatun/TripWise-v2)"
mkdir -p out/cand
echo '[]' > out/candidates.json
for id in $(jq -r '.queries | keys[]' scripts/image-queries.json); do
  q=$(jq -r --arg id "$id" '.queries[$id]' scripts/image-queries.json)
  url="https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search&gsrnamespace=6&gsrlimit=12"
  url+="&gsrsearch=$(jq -rn --arg q "$q filetype:bitmap" '$q|@uri')&prop=imageinfo&iiprop=url|size|extmetadata&iiurlwidth=1280"
  url+="&iiextmetadatafilter=Artist|LicenseShortName|LicenseUrl"
  curl -sS -A "$UA" "$url" | jq --arg id "$id" '
    [.query.pages // {} | to_entries[] | .value
     | select(.imageinfo[0].width > .imageinfo[0].height and .imageinfo[0].width >= 1600)
     | {id: $id, title, page: .imageinfo[0].descriptionurl, url: .imageinfo[0].thumburl,
        author: (.imageinfo[0].extmetadata.Artist.value // "" | gsub("<[^>]*>"; "") | gsub("\\s+"; " ") | ltrimstr(" ") | rtrimstr(" ")),
        license: (.imageinfo[0].extmetadata.LicenseShortName.value // ""),
        licenseUrl: (.imageinfo[0].extmetadata.LicenseUrl.value // "")}]
    | .[0:6] | to_entries | map(.value + {n: (.key + 1)})' > "out/cand/$id.json"
  jq -s '.[0] + .[1]' out/candidates.json "out/cand/$id.json" > out/tmp.json && mv out/tmp.json out/candidates.json
  thumbs=()
  while IFS=$'\t' read -r n turl; do
    f="out/cand/${id}-${n}.jpg"
    if curl -sSfL -A "$UA" -o "$f" "${turl/1280px-/480px-}"; then
      convert "$f" -resize 480x320^ -gravity center -extent 480x320 -fill white -undercolor '#000000AA' \
        -pointsize 28 -gravity south -annotate +0+8 " ${id} #${n} " "$f" && thumbs+=("$f")
    fi
  done < <(jq -r '.[] | [.n, .url] | @tsv' "out/cand/$id.json")
  if [ ${#thumbs[@]} -gt 0 ]; then
    montage "${thumbs[@]}" -tile 3x -geometry +4+4 -background '#0D1B2A' "out/candidates-${id}.jpg"
  fi
  echo "$id: ${#thumbs[@]} candidates"
  sleep 1
done
rm -rf out/cand
