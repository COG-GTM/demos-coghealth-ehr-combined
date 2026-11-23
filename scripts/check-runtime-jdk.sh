#!/usr/bin/env bash
# Fails if a Java-based image in the docker-compose files is not digest-pinned or
# bundles a JDK affected by CVE-2026-114938 (Java4Shell, java.util.Base64 decoder).
# Usage: scripts/check-runtime-jdk.sh [compose-file ...]
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [ "$#" -eq 0 ]; then
  set -- "$ROOT_DIR/demos-coghealth-ehr-api/docker-compose.yml" "$ROOT_DIR/demos-coghealth-ehr-data/docker-compose.yml"
fi

# Lowest fixed build per affected feature release; 8 and 11 are not affected.
min_fixed() {
  case "$1" in
    17) echo 17.0.21.1 ;;
    21) echo 21.0.13.1 ;;
    25) echo 25.0.5.1 ;;
    27) echo 27.0.1.1 ;;
    *) echo "" ;;
  esac
}

version_lt() {
  [ "$1" != "$2" ] && [ "$(printf '%s\n%s\n' "$1" "$2" | sort -V | head -n1)" = "$1" ]
}

JAVA_IMAGE_PATTERN='keycloak|openjdk|temurin|corretto|jdk|jre|java'
fail=0

for compose in "$@"; do
  images="$(grep -E '^[[:space:]]*image:' "$compose" | awk '{print $2}' | grep -Ei "$JAVA_IMAGE_PATTERN" || true)"
  for image in $images; do
    if [[ "$image" != *@sha256:* ]]; then
      echo "FAIL $compose: $image is not pinned by digest"
      fail=1
      continue
    fi
    version_output="$(docker run --rm --entrypoint java "$image" -version 2>&1)" || {
      echo "FAIL $compose: could not run 'java -version' in $image"
      fail=1
      continue
    }
    version="$(sed -nE 's/.*version "([0-9][0-9._]*)".*/\1/p' <<<"$version_output" | head -n1)"
    major="${version%%.*}"
    [ "$major" = "1" ] && major="$(cut -d. -f2 <<<"$version")"
    fixed="$(min_fixed "$major")"
    if [ -n "$fixed" ] && version_lt "$version" "$fixed"; then
      echo "FAIL $compose: $image bundles JDK $version, below fixed $fixed (CVE-2026-114938)"
      fail=1
    else
      echo "OK   $compose: $image bundles JDK $version"
    fi
  done
done

exit "$fail"
