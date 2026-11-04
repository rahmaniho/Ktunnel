#!/usr/bin/env bash
#
# check-agency.sh — single gate for the whole coordination repo.
#
# Verifies, without network access:
#   1. registry is parseable, ids are unique, required divisions exist
#   2. every registered agent exists and matches upstream byte-for-byte
#   3. no unregistered stray files under agents/<division>/
#   4. every agent listed in a department README frontmatter exists
#   5. AGENTS.md roster block is current (scripts/gen-roster.py --check)
#   6. .claude/agents matches the registry when it is present
#   7. every markdown link between repo docs resolves
#   8. workflow/README frontmatter is well formed (yaml fenced block)
#
# Exit code 0 = healthy, 1 = at least one problem (all are printed).
#
# Usage:
#   ./scripts/check-agency.sh          # full check
#   ./scripts/check-agency.sh --quiet  # only problems, no section headers
#
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REGISTRY="$ROOT/agents/registry.tsv"
UPSTREAM="$ROOT/upstream/agency-agents"

QUIET=0
[[ "${1:-}" == "--quiet" ]] && QUIET=1

C_GREEN=""; C_RED=""; C_DIM=""; C_RESET=""
if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
  C_GREEN=$'\033[0;32m'; C_RED=$'\033[0;31m'; C_DIM=$'\033[2m'; C_RESET=$'\033[0m'
fi
ok()   { (( QUIET )) || printf "%s[OK]%s  %s\n" "$C_GREEN" "$C_RESET" "$*"; }
err()  { printf "%s[ERR]%s %s\n" "$C_RED" "$C_RESET" "$*" >&2; }
note() { (( QUIET )) || printf "%s--  %s%s\n" "$C_DIM" "$*" "$C_RESET"; }

PROBLEMS=0
fail() { err "$*"; PROBLEMS=$((PROBLEMS + 1)); }

section() { (( QUIET )) || printf "\n%s== %s%s\n" "$C_DIM" "$*" "$C_RESET"; }

# --------------------------------------------------------------- 1. registry
section "registry"
if [[ ! -f "$REGISTRY" ]]; then
  fail "missing agents/registry.tsv"
else
  rows="$(awk -F'\t' '
    /^[[:space:]]*#/ { next }
    NF == 0          { next }
    $1 == "division" { seen = 1; next }
    seen             { printf "%s\t%s\t%s\n", $1, $2, $3 }
  ' "$REGISTRY")"
  TOTAL="$(printf '%s\n' "$rows" | grep -c . || true)"
  if (( TOTAL == 0 )); then
    fail "registry has no agent rows"
  else
    ok "$TOTAL registered agents"
    dupes="$(printf '%s\n' "$rows" | cut -f2 | sort | uniq -d)"
    if [[ -n "$dupes" ]]; then
      fail "duplicate slugs in registry: $(echo "$dupes" | tr '\n' ' ')"
    else
      ok "slugs unique"
    fi
    for d in executive engineering design security quality marketing; do
      n="$(printf '%s\n' "$rows" | awk -F'\t' -v d="$d" '$1 == d' | grep -c . || true)"
      if (( n == 0 )); then
        fail "division '$d' has no registered agents"
      else
        note "$d: $n"
      fi
    done
  fi
fi

# ------------------------------------------------- 2-3. agents/ vs upstream
section "agents/ integrity"
if [[ ! -d "$UPSTREAM" ]]; then
  fail "missing vendored upstream: upstream/agency-agents/"
else
  missing=0; drifted=0
  while IFS=$'\t' read -r division slug upstream name; do
    [[ -z "$division" ]] && continue
    file="$ROOT/agents/$division/$slug.md"
    if [[ ! -f "$file" ]]; then
      fail "registered but missing: agents/$division/$slug.md"; missing=$((missing + 1)); continue
    fi
    src="$(find "$UPSTREAM" -maxdepth 2 -type f -name "$upstream.md" -not -path '*/.github/*' | head -1)"
    if [[ -z "$src" ]]; then
      fail "upstream source not found: $upstream.md"; continue
    fi
    if ! cmp -s "$src" "$file"; then
      fail "drifted from upstream: agents/$division/$slug.md"; drifted=$((drifted + 1))
    fi
  done < <(printf '%s\n' "${rows:-}")
  (( missing == 0 && drifted == 0 )) && ok "all curated files match upstream byte-for-byte"

  strays=0
  for file in "$ROOT"/agents/*/*.md; do
    [[ -e "$file" ]] || continue
    division="$(basename "$(dirname "$file")")"
    slug="$(basename "$file" .md)"
    if ! awk -F'\t' -v d="$division" -v s="$slug" '
        /^[[:space:]]*#/ { next }
        $1 == d && $2 == s { found = 1; exit }
        END { exit(found ? 0 : 1) }
      ' "$REGISTRY"; then
      fail "unregistered agent file: agents/$division/$slug.md"; strays=$((strays + 1))
    fi
  done
  (( strays == 0 )) && ok "no stray files under agents/"
fi

# ------------------------------------------- 4. department frontmatter refs
section "departments"
for readme in "$ROOT"/departments/*/README.md; do
  [[ -e "$readme" ]] || continue
  dept="$(basename "$(dirname "$readme")")"
  listed="$(awk '/^---$/{n++; next} n==1 && /^agents:/{sub(/^agents:[ ]*/,""); gsub(/,/," "); print; exit}' "$readme")"
  if [[ -z "$listed" ]]; then
    fail "departments/$dept/README.md has no 'agents:' frontmatter"; continue
  fi
  count=0
  for a in $listed; do
    count=$((count + 1))
    if ! awk -F'\t' -v s="$a" '
        /^[[:space:]]*#/ { next }
        $2 == s { found = 1; exit }
        END { exit(found ? 0 : 1) }
      ' "$REGISTRY"; then
      fail "departments/$dept/README.md lists unknown agent: $a"
    fi
  done
  ok "departments/$dept: $count agents listed, all registered"
done

# ------------------------------------------------------- 5. AGENTS.md roster
section "AGENTS.md roster"
if [[ -f "$ROOT/AGENTS.md" ]]; then
  if python3 "$ROOT/scripts/gen-roster.py" --check >/dev/null 2>&1; then
    ok "roster block is current"
  else
    fail "AGENTS.md roster is stale — run: python3 scripts/gen-roster.py"
  fi
else
  fail "missing AGENTS.md"
fi

# ----------------------------------------------------- 6. installed roster
section ".claude/agents"
if [[ -d "$ROOT/.claude/agents" ]]; then
  have="$(find "$ROOT/.claude/agents" -maxdepth 1 -name '*.md' | wc -l | tr -d ' ')"
  if [[ -n "${TOTAL:-}" && "$have" == "$TOTAL" ]]; then
    ok ".claude/agents has $have files (matches registry)"
  else
    fail ".claude/agents has $have files, registry has ${TOTAL:-?} — run ./scripts/install.sh --force"
  fi
else
  note "not installed (optional, repo-local) — run ./scripts/install.sh"
fi

# ---------------------------------------------------------- 7. link checks
section "markdown links"
if command -v python3 >/dev/null 2>&1; then
  if python3 "$ROOT/scripts/check-links.py"; then
    ok "all relative links resolve"
  else
    fail "broken relative links (see above)"
  fi
else
  note "python3 not found, skipping link check"
fi

# ------------------------------------------------- 8. workflow frontmatter
section "workflows"
for wf in "$ROOT"/workflows/*.md; do
  [[ -e "$wf" ]] || continue
  base="$(basename "$wf")"
  head -1 "$wf" | grep -qx -- '---' || { fail "workflows/$base: missing frontmatter"; continue; }
  for key in workflow gates departments output; do
    grep -qE "^$key:" "$wf" || fail "workflows/$base: missing '$key:'"
  done
  ok "workflows/$base"
  # referenced agent ids must exist in the registry
  for a in $(awk '/^---$/{n++; next} n==1 && /^agents:/{sub(/^agents:[ ]*/,""); gsub(/,/," "); print; exit}' "$wf"); do
    awk -F'\t' -v s="$a" '
      /^[[:space:]]*#/ { next }
      $2 == s { found = 1; exit }
      END { exit(found ? 0 : 1) }
    ' "$REGISTRY" || fail "workflows/$base references unknown agent: $a"
  done
done

# ------------------------------------------------------------------ verdict
printf "\n"
if (( PROBLEMS )); then
  err "$PROBLEMS problem(s) found"
  exit 1
fi
ok "agency repo is healthy"
