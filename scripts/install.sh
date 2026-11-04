#!/usr/bin/env bash
#
# install.sh — install the curated KarenSoft roster into Claude Code.
#
# Two things happen:
#   1. .claude/agents/<upstream-slug>.md  is written repo-locally, so the roster
#      travels with the repo and every teammate gets the same set.
#   2. the per-agent map lives in .claude/AGENCY-ROSTER.md (generated), which
#      explains how upstream slugs map to the KarenSoft division folders.
#
# Rendering is delegated to upstream's own installer so the output stays
# byte-identical to what `upstream/agency-agents/scripts/install.sh --tool
# claude-code` would produce. We never re-implement its conversion.
#
# Usage:
#   ./scripts/install.sh                 # install into ./.claude/agents (repo-local)
#   ./scripts/install.sh --dry-run       # show what would be installed
#   ./scripts/install.sh --check         # verify .claude/agents matches the registry
#   ./scripts/install.sh --user          # also install globally (~/.claude/agents)
#   ./scripts/install.sh --tool <name>   # any upstream tool (cursor, codex, qwen, ...)
#   ./scripts/install.sh --path <dir>    # explicit destination directory
#   ./scripts/install.sh --force         # reinstall even if up to date
#
# Environment:
#   CLAUDE_CONFIG_DIR  honoured by upstream's installer for --user installs.
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REGISTRY="$ROOT/agents/registry.tsv"
UPSTREAM="$ROOT/upstream/agency-agents"
UPSTREAM_INSTALL="$UPSTREAM/scripts/install.sh"
DEST="$ROOT/.claude/agents"

C_GREEN=""; C_RED=""; C_DIM=""; C_YELLOW=""; C_RESET=""
if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
  C_GREEN=$'\033[0;32m'; C_RED=$'\033[0;31m'; C_YELLOW=$'\033[1;33m'
  C_DIM=$'\033[2m'; C_RESET=$'\033[0m'
fi
ok()   { printf "%s[OK]%s  %s\n"  "$C_GREEN"  "$C_RESET"  "$*"; }
warn() { printf "%s[!!]%s  %s\n"  "$C_YELLOW" "$C_RESET"  "$*"; }
err()  { printf "%s[ERR]%s %s\n"  "$C_RED"    "$C_RESET"  "$*" >&2; }
dim()  { printf "%s%s%s\n"        "$C_DIM"    "$*"        "$C_RESET"; }

TOOL="claude-code"
MODE="install"
USER_SCOPE=0
FORCE=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) MODE="dry-run"; shift ;;
    --check)   MODE="check"; shift ;;
    --force)   FORCE=1; shift ;;
    --user)    USER_SCOPE=1; shift ;;
    --tool)    TOOL="${2:?--tool needs a value}"; shift 2 ;;
    --path)    DEST="${2:?--path needs a value}"; shift 2 ;;
    --help|-h) sed -n '3,29p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) err "unknown argument: $1 (try --help)"; exit 2 ;;
  esac
done

[[ -x "$UPSTREAM_INSTALL" ]] \
  || { err "upstream installer missing: ${UPSTREAM_INSTALL#"$ROOT"/}"; \
       err "run: git submodule update --init  or re-run scripts/sync-agents.sh --update-upstream"; exit 1; }
[[ -f "$REGISTRY" ]] || { err "registry missing: agents/registry.tsv"; exit 1; }

# --------------------------------------------------------------- id extraction
# The upstream installer accepts source file stems (e.g. engineering-sre).
agent_ids() {
  awk -F'\t' '
    /^[[:space:]]*#/ { next }
    NF == 0          { next }
    $1 == "division" { seen = 1; next }
    seen             { print $3 }
  ' "$REGISTRY"
}

IDS_FILE="$(mktemp)"
agent_ids > "$IDS_FILE"
EXPECTED="$(wc -l < "$IDS_FILE" | tr -d ' ')"
[[ "$EXPECTED" -gt 0 ]] || { err "registry contains no agents"; exit 1; }

# Upstream reflects frontmatter names; ours are named after the file stem.
# The rendered extension depends on the tool: .md for markdown tools, .mdc for
# cursor rules, .toml for codex/vibe, or a per-agent directory for skills.
RENDER_EXT="md"
case "$TOOL" in
  cursor)          RENDER_EXT="mdc" ;;
  codex|vibe)      RENDER_EXT="toml" ;;
  openclaw|kimi|osaurus|antigravity|dsh) RENDER_EXT="dir" ;;
esac

installed_file() {
  local name
  name="$(rendered_name "$1")"
  if [[ "$RENDER_EXT" == "dir" ]]; then
    printf '%s/%s' "$DEST" "$name"
  else
    printf '%s/%s.%s' "$DEST" "$name" "$RENDER_EXT"
  fi
}

# Upstream derives the on-disk name per tool. Markdown tools keep the source
# stem; cursor/codex/qwen/vibe use the agent's frontmatter `name`, slugified.
# These match the rendered filenames we observed from upstream's installer.
rendered_name() {
  case "$TOOL" in
    cursor|codex|qwen|vibe|opencode|gemini-cli|zcode|kimi|openclaw|osaurus|antigravity|dsh)
      awk -F'\t' -v id="$1" '
        /^[[:space:]]*#/ { next }
        NF == 0          { next }
        $1 == "division" { seen = 1; next }
        seen && $3 == id { print $4; exit }
      ' "$REGISTRY" \
        | tr '[:upper:]' '[:lower:]' \
        | sed -e 's/[^a-z0-9]\{1,\}/-/g' -e 's/^-//' -e 's/-$//'
      ;;
    *) printf '%s' "$1" ;;
  esac
}

# Count rendered agent entries for the tool's layout.
count_installed() {
  local target="$1"
  [[ -d "$target" ]] || { echo 0; return; }
  if [[ "$RENDER_EXT" == "dir" ]]; then
    find "$target" -mindepth 1 -maxdepth 2 -type f \( -name 'SOUL.md' -o -name 'SKILL.md' -o -name 'agent.yaml' \) 2>/dev/null | wc -l | tr -d ' '
  else
    find "$target" -maxdepth 1 -name "*.$RENDER_EXT" 2>/dev/null | wc -l | tr -d ' '
  fi
}

# ------------------------------------------------------------------- modes

# Upstream's installer is allowed to regenerate integration files; it does that
# inside UPSTREAM_DIR. We point it at a throwaway staging copy so the vendored
# tree stays byte-identical to the pinned commit (check-agency.sh enforces this).
STAGE="$(mktemp -d)"
trap 'rm -rf "$STAGE"; rm -f "$IDS_FILE"' EXIT

stage_upstream() {
  STAGE_ROOT="$STAGE/agency-agents"
  mkdir -p "$STAGE_ROOT"
  tar -C "$UPSTREAM" --exclude=.git -cf - . | tar -C "$STAGE_ROOT" -xf -
}

run_upstream_install() {
  local dest="$1"; shift
  stage_upstream
  "$STAGE_ROOT/scripts/install.sh" --tool "$TOOL" --path "$dest" --no-interactive \
    --agents-file "$IDS_FILE" "$@"
}

do_check() {
  local id missing drifted=0
  while read -r id; do
    if [[ "$RENDER_EXT" == "dir" ]]; then
      [[ -d "$(installed_file "$id")" ]] || { err "not installed: $(installed_file "$id")"; drifted=1; continue; }
    elif [[ ! -f "$(installed_file "$id")" ]]; then
      err "not installed: $(installed_file "$id")"; missing=1; drifted=1; continue
    fi
    # Upstream renders identity for claude-code, so a correct install is a copy.
    if [[ "$TOOL" == "claude-code" ]]; then
      local src
      src="$(find "$UPSTREAM" -maxdepth 2 -type f -name "$id.md" -not -path '*/.github/*' | head -1)"
      if [[ -n "$src" ]] && ! cmp -s "$src" "$(installed_file "$id")"; then
        err "stale copy: $(installed_file "$id")"; drifted=1
      fi
    fi
  done < "$IDS_FILE"
  if (( drifted )); then
    err "roster is out of sync — run ./scripts/install.sh --force"
    return 1
  fi
  ok "$TOOL roster current in ${DEST#"$ROOT"/} ($EXPECTED agents)"
}

do_install() {
  local target="$1"
  if (( ! FORCE )) && [[ -d "$target" ]]; then
    local have
    have="$(find "$target" -maxdepth 1 -name '*.md' 2>/dev/null | wc -l | tr -d ' ')"
    if [[ "$have" == "$EXPECTED" ]]; then
      ok "$TOOL roster already present in ${target#"$ROOT"/} ($have agents) — use --force to rewrite"
      return 0
    fi
  fi
  dim "rendering $EXPECTED agents for $TOOL -> ${target#"$ROOT"/}"
  run_upstream_install "$target"
  ok "$TOOL: $(count_installed "$target") agents in ${target#"$ROOT"/}"
}

write_roster_map() {
  local out="$ROOT/.claude/AGENCY-ROSTER.md" division slug upstream name
  mkdir -p "$(dirname "$out")"
  {
    echo "# KarenSoft AI Agency — installed roster (generated)"
    echo
    echo "> Generated by \`scripts/install.sh\`. Do not edit by hand."
    echo "> Source of truth: \`agents/registry.tsv\`; agent bodies: \`upstream/agency-agents/\`."
    echo
    echo "Claude Code loads every file in \`.claude/agents/\`. Dispatch one by its"
    echo "\`name:\` frontmatter (right-hand column), not by the KarenSoft folder name."
    echo
    echo "| KarenSoft division | Curated file | Claude Code agent (\`name:\`) |"
    echo "| --- | --- | --- |"
    while IFS=$'\t' read -r division slug upstream name; do
      printf '| %s | `agents/%s/%s.md` | %s |\n' "$division" "$division" "$slug" "$name"
    done < <(awk -F'\t' '
      /^[[:space:]]*#/ { next }
      NF == 0          { next }
      $1 == "division" { seen = 1; next }
      seen             { printf "%s\t%s\t%s\t%s\n", $1, $2, $3, $4 }
    ' "$REGISTRY")
    echo
    echo "Workflows: see \`workflows/\`. Conventions: see \`AGENCY.md\`."
  } > "$out"
  ok "wrote ${out#"$ROOT"/}"
}

case "$MODE" in
  dry-run)
    "$UPSTREAM_INSTALL" --tool "$TOOL" --path "$DEST" --no-interactive --dry-run --agents-file "$IDS_FILE" ;;
  check)
    do_check ;;
  install)
    do_install "$DEST"
    [[ "$TOOL" == "claude-code" ]] && write_roster_map
    if (( USER_SCOPE )); then
      dim "also installing user-wide (this machine only)"
      "$UPSTREAM_INSTALL" --tool "$TOOL" --no-interactive --agents-file "$IDS_FILE"
      ok "user-wide install complete"
    fi
    ;;
esac
