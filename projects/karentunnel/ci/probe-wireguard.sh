#!/usr/bin/env bash
# Asks Maven Central whether the WireGuard tunnel library is published anywhere
# under its group id, and probes the candidate coordinates directly.
# The development sandbox cannot reach Maven, so this runs in CI and its output
# is committed back to the branch.
set -uo pipefail

echo "# WireGuard artifact availability"
echo
echo "- date: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo

echo "## 1. Maven Central search API (group = com.wireguard.android)"
echo
echo '```json'
curl -s --max-time 30 \
  "https://search.maven.org/solrsearch/select?q=g:%22com.wireguard.android%22&rows=40&wt=json" \
  || echo "SEARCH API UNREACHABLE"
echo
echo '```'
echo

echo "## 2. Maven Central search API (name/artifact = tunnel, any group)"
echo
echo '```json'
curl -s --max-time 30 \
  "https://search.maven.org/solrsearch/select?q=a:%22tunnel%22&rows=20&wt=json" \
  || echo "SEARCH API UNREACHABLE"
echo
echo '```'
echo

echo "## 3. Direct coordinate probes"
echo
echo '| coordinate | HTTP |'
echo '| --- | --- |'
probe() {
  local group="$1" artifact="$2" version="$3"
  local path
  path=$(echo "$group" | tr '.' '/')
  local url="https://repo1.maven.org/maven2/${path}/${artifact}/${version}/${artifact}-${version}.pom"
  local code
  code=$(curl -s --max-time 20 -o /dev/null -w '%{http_code}' "$url")
  echo "| \`${group}:${artifact}:${version}\` | ${code} |"
}
probe com.wireguard.android tunnel 1.0.20260315
probe com.wireguard.android tunnel 1.0.20230706
probe com.wireguard.android tunnel 1.0.20210424
probe com.wireguard.android wireguard-android 1.0.20260315
echo

echo "## 4. Latest published version, if any"
echo
echo '```'
curl -s --max-time 20 \
  "https://repo1.maven.org/maven2/com/wireguard/android/tunnel/maven-metadata.xml" \
  || echo "MISSING (no such coordinate on Maven Central)"
echo '```'
echo

echo "## 5. Can Gradle resolve it in this exact project?"
echo
cd "$(dirname "$0")/.." || exit 0
./gradlew :app:dependencies --configuration debugRuntimeClasspath --console=plain 2>&1 \
  | grep -E 'wireguard|FAILED|Could not' | head -40 \
  || echo "no wireguard lines found in dependency report"
