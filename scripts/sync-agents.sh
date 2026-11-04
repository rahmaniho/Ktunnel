#!/usr/bin/env bash
#
# sync-agents.sh — refresh the curated roster in agents/ from the vendored
# upstream copy in upstream/agency-agents/, using agents/registry.tsv.
#
# The curated agent bodies are COPIES, never edits: this script overwrites them
# byte-for-byte from upstream. Put KarenSoft-specific guidance in AGENCY.md,
# departments/, workflows/, or templates/ — never inside a copied agent file.
#
# Usage:
#   ./scripts/sync-agents.sh            # refresh agents/ from vendored upstream
#   ./scripts/sync-agents.sh --check    # exit 1 if any copy drifted (CI-friendly)
#   ./scripts/sync-agents.sh --update-upstream
#                                       # re-download upstream/ at the pinned
#                                       # commit in upstream/VERSION, then sync
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REGISTRY="$ROOT/agents/registry.tsv"
UPSTREAM="$ROOT/upstream/agency-agents"
VERSION_FILE="$ROOT/upstream/VERSION"

C_GREEN=""; C_RED=""; C_DIM=""; C_RESET=""
if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
  C_GREEN=$'\033[0;32m'; C_RED=$'\033[0;31m'; C_DIM=$'\033[2m'; C_RESET=$'\033[0m'
fi
ok()   { printf "%s[OK]%s  %s\n"  "$C_GREEN" "$C_RESET" "$*"; }
err()  { printf "%s[ERR]%s %s\n"  "$C_RED"   "$C_RESET" "$*" >&2; }
dim()  { printf "%s%s%s\n"        "$C_DIM"   "$*"        "$C_RESET"; }

MODE="sync"
case "${1:-}" in
  --check)           MODE="check" ;;
  --update-upstream) MODE="update" ;;
  --help|-h)
    sed -n '3,20p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
    exit 0 ;;
  "") ;;
  *) err "unknown argument: $1"; exit 2 ;;
esac

[[ -f "$REGISTRY" ]] || { err "registry not found: ${REGISTRY#"$ROOT"/}"; exit 1; }

# ---------------------------------------------------------------- upstream pin
pinned_commit() {
  [[ -f "$VERSION_FILE" ]] || { err "missing ${VERSION_FILE#"$ROOT"/}"; return 1; }
  awk -F': *' '/^commit:/ {print $2; exit}' "$VERSION_FILE"
}

update_upstream() {
  local commit repo tmp
  commit="$(pinned_commit)" || exit 1
  repo="$(awk -F': *' '/^repo:/ {print $2; exit}' "$VERSION_FILE")"
  [[ -n "$commit" ]] || { err "no commit: line in ${VERSION_FILE#"$ROOT"/}"; exit 1; }
  [[ -n "$repo" ]]   || { err "no repo: line in ${VERSION_FILE#"$ROOT"/}"; exit 1; }

  tmp="$(mktemp -d)"
  trap 'rm -rf "$tmp"' EXIT
  dim "fetching $repo @ ${commit:0:12}"
  curl -fsSL "https://codeload.github.com/$repo/tar.gz/$commit" -o "$tmp/upstream.tgz" \
    || { err "download failed — check network access to codeload.github.com"; exit 1; }
  tar -C "$tmp" -xzf "$tmp/upstream.tgz"
  local extracted
  extracted="$(find "$tmp" -maxdepth 1 -mindepth 1 -type d ! -name 'upstream.tgz' | head -1)"
  [[ -n "$extracted" ]] || { err "unexpected archive layout"; exit 1; }

  # Replace vendored tree, keeping no upstream .git metadata.
  find "$UPSTREAM" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
  tar -C "$extracted" --exclude=.git -cf - . | tar -C "$UPSTREAM" -xf -
  ok "upstream refreshed to ${commit:0:12} ($(find "$UPSTREAM" -type f | wc -l | tr -d ' ') files)"
}

# ---------------------------------------------------------------- registry
# Emits: division <TAB> slug <TAB> upstream <TAB> name
registry_rows() {
  awk -F'\t' '
    /^[[:space:]]*#/ { next }
    NF == 0          { next }
    $1 == "division" { seen = 1; next }
    seen             { printf "%s\t%s\t%s\t%s\n", $1, $2, $3, $4 }
  ' "$REGISTRY"
}

check_local_consistency() {
  local bad=0 division slug upstream name dir listing
  while IFS=$'\t' read -r division slug upstream name; do
    [[ -f "$ROOT/agents/$division/$slug.md" ]] \
      || { err "registered but missing: agents/$division/$slug.md"; bad=1; }
  done < <(registry_rows)

  # Every file under agents/<division>/ must be registered (no strays).
  for dir in "$ROOT"/agents/*/; do
    [[ -d "$dir" ]] || continue
    division="$(basename "$dir")"
    for listing in "$dir"*.md; do
      [[ -e "$listing" ]] || continue
      slug="$(basename "$listing" .md)"
      # awk (not grep -P) so this works on macOS/BSD grep too.
      awk -F'\t' -v d="$division" -v s="$slug" '
        /^[[:space:]]*#/ { next }
        $1 == d && $2 == s { found = 1; exit }
        END { exit(found ? 0 : 1) }
      ' "$REGISTRY" || { err "unregistered agent file: agents/$division/$slug.md"; bad=1; }
    done
  done
  return "$bad"
}

do_sync() {
  local division slug upstream name src dst copied=0
  while IFS=$'\t' read -r division slug upstream name; do
    src="$UPSTREAM"
    # upstream stems live in exactly one division dir; resolve without guessing.
    src="$(find "$UPSTREAM" -maxdepth 2 -type f -name "$upstream.md" -not -path '*/.github/*' | head -1)"
    [[ -n "$src" && -f "$src" ]] \
      || { err "upstream source not found for '$slug': $upstream.md"; exit 1; }
    dst="$ROOT/agents/$division/$slug.md"
    mkdir -p "$(dirname "$dst")"
    if [[ -f "$dst" ]] && cmp -s "$src" "$dst"; then
      continue
    fi
    cp "$src" "$dst"
    dim "  updated agents/$division/$slug.md"
    copied=$((copied + 1))
  done < <(registry_rows)
  ok "sync complete: $copied file(s) changed, $(registry_rows | wc -l | tr -d ' ') registered"
}

do_check() {
  local division slug upstream name src dst bad=0 mismatched=0
  while IFS=$'\t' read -r division slug upstream name; do
    src="$(find "$UPSTREAM" -maxdepth 2 -type f -name "$upstream.md" -not -path '*/.github/*' | head -1)"
    dst="$ROOT/agents/$division/$slug.md"
    if [[ -z "$src" ]]; then
      err "upstream source not found: $upstream.md"; bad=1; continue
    fi
    if [[ ! -f "$dst" ]]; then
      err "missing curated copy: agents/$division/$slug.md"; bad=1; continue
    fi
    if ! cmp -s "$src" "$dst"; then
      err "drifted from upstream: agents/$division/$slug.md (run ./scripts/sync-agents.sh)"; bad=1; mismatched=$((mismatched + 1))
    fi
  done < <(registry_rows)

  check_local_consistency || bad=1
  if (( bad )); then
    return 1
  fi
  ok "agents/ matches registry and upstream ($(registry_rows | wc -l | tr -d ' ') agents)"
}

case "$MODE" in
  update) update_upstream; do_sync ;;
  sync)   do_sync ;;
  check)  do_check ;;
esac
