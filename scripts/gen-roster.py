#!/usr/bin/env python3
"""Generate the roster tables inside AGENTS.md from agents/registry.tsv.

The registry is the single source of truth. Everything between
<!-- BEGIN:ROSTER --> and <!-- END:ROSTER --> in AGENTS.md is generated;
everything outside it is hand-written prose and is left untouched.

Usage:
    python3 scripts/gen-roster.py            # rewrite the block in place
    python3 scripts/gen-roster.py --check     # exit 1 if the block is stale
    python3 scripts/gen-roster.py --markdown  # print the block to stdout
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REGISTRY = ROOT / "agents" / "registry.tsv"
AGENTS_DOC = ROOT / "AGENTS.md"

BEGIN = "<!-- BEGIN:ROSTER -->"
END = "<!-- END:ROSTER -->"

DIVISION_TITLES = {
    "executive": "اجرایی (Executive)",
    "engineering": "مهندسی (Engineering)",
    "design": "طراحی و محصول (Design)",
    "security": "امنیت (Security)",
    "quality": "کیفیت (Quality)",
    "marketing": "بازاریابی و رشد (Marketing)",
}

DIVISION_ORDER = ["executive", "engineering", "design", "security", "quality", "marketing"]

PERSIAN_DIGITS = str.maketrans("0123456789", "۰۱۲۳۴۵۶۷۸۹")


def fa(number: int) -> str:
    """Persian digits for prose; identifiers keep Latin digits."""
    return str(number).translate(PERSIAN_DIGITS)


def read_registry(path: Path = REGISTRY) -> list[dict[str, str]]:
    if not path.is_file():
        sys.exit(f"registry not found: {path}")
    rows: list[dict[str, str]] = []
    header: list[str] | None = None
    for lineno, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not raw.strip() or raw.lstrip().startswith("#"):
            continue
        cols = raw.split("\t")
        if header is None:
            header = cols
            missing = {"division", "slug", "upstream", "name", "emoji", "when"} - set(cols)
            if missing:
                sys.exit(f"{path}:{lineno}: registry header missing {sorted(missing)}")
            continue
        if len(cols) != len(header):
            sys.exit(
                f"{path}:{lineno}: expected {len(header)} columns, got {len(cols)} "
                f"(tab-separated; did a field contain a literal tab?)"
            )
        rows.append(dict(zip(header, cols)))
    if not rows:
        sys.exit(f"{path}: no agent rows found")
    return rows


def agent_file(row: dict[str, str]) -> Path:
    return ROOT / "agents" / row["division"] / f"{row['slug']}.md"


def render(rows: list[dict[str, str]]) -> str:
    by_division: dict[str, list[dict[str, str]]] = {}
    for row in rows:
        by_division.setdefault(row["division"], []).append(row)

    unknown = sorted(set(by_division) - set(DIVISION_ORDER))
    if unknown:
        sys.exit(f"registry uses divisions with no title: {unknown}")

    out: list[str] = []
    total = len(rows)
    out.append(
        f"مجموع: **{fa(total)}** عامل در **{fa(len(by_division))}** بخش. "
        "همهٔ متن‌های عاملی از پروژهٔ بالادست آمده‌اند و بازنویسی نشده‌اند."
    )
    out.append("")
    out.append("| بخش | عامل | شناسهٔ نصب | کی فعالش کنیم |")
    out.append("| --- | --- | --- | --- |")
    for division in DIVISION_ORDER:
        if division not in by_division:
            continue
        members = sorted(by_division[division], key=lambda r: r["slug"])
        anchor = f"#{division}"
        first = members[0]
        out.append(
            f"| [{DIVISION_TITLES[division]}]({anchor}) | "
            f"{first['emoji']} {first['name']} | `{first['slug']}` | {first['when']} |"
        )
        for row in members[1:]:
            out.append(
                f"|  | {row['emoji']} {row['name']} | `{row['slug']}` | {row['when']} |"
            )
    out.append("")

    for division in DIVISION_ORDER:
        if division not in by_division:
            continue
        members = sorted(by_division[division], key=lambda r: r["slug"])
        out.append(f'<a id="{division}"></a>')
        out.append("")
        out.append(f"### {DIVISION_TITLES[division]} — {fa(len(members))} عامل")
        out.append("")
        out.append("| عامل | فایل منبع | شناسهٔ Claude Code |")
        out.append("| --- | --- | --- |")
        for row in members:
            src = f"`{row['upstream']}.md`"
            out.append(
                f"| {row['emoji']} **{row['name']}** | {src} | `{row['upstream']}` |"
            )
        out.append("")
    return "\n".join(out).rstrip() + "\n"


def replace_block(doc: str, block: str) -> str:
    start = doc.find(BEGIN)
    end = doc.find(END)
    if start == -1 or end == -1 or end < start:
        sys.exit(f"{AGENTS_DOC}: missing {BEGIN} / {END} markers")
    return doc[: start + len(BEGIN)] + "\n" + block + "\n" + doc[end:]


def stale_problems(rows: list[dict[str, str]]) -> list[str]:
    problems = []
    for row in rows:
        path = agent_file(row)
        if not path.is_file():
            problems.append(f"missing agent file: {path.relative_to(ROOT)}")
            continue
        head = path.read_text(encoding="utf-8")
        if not head.startswith("---"):
            problems.append(f"{path.relative_to(ROOT)}: no frontmatter")
        if f"name: {row['name']}" not in head:
            problems.append(
                f"{path.relative_to(ROOT)}: frontmatter name does not match registry ({row['name']})"
            )
    return problems


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--check", action="store_true", help="fail if AGENTS.md is stale")
    group.add_argument("--markdown", action="store_true", help="print the block only")
    args = parser.parse_args(argv)

    rows = read_registry()
    problems = stale_problems(rows)
    if problems:
        for p in problems:
            print(f"[ERR] {p}", file=sys.stderr)
        return 1

    block = render(rows)
    if args.markdown:
        print(block, end="")
        return 0

    current = AGENTS_DOC.read_text(encoding="utf-8") if AGENTS_DOC.is_file() else ""
    updated = replace_block(current, block)

    if args.check:
        if updated != current:
            print(
                "[ERR] AGENTS.md roster is stale — run: python3 scripts/gen-roster.py",
                file=sys.stderr,
            )
            return 1
        print(f"[OK]  AGENTS.md roster is current ({len(rows)} agents)")
        return 0

    if updated != current:
        AGENTS_DOC.write_text(updated, encoding="utf-8")
        print(f"[OK]  updated {AGENTS_DOC.relative_to(ROOT)} ({len(rows)} agents)")
    else:
        print(f"[OK]  {AGENTS_DOC.relative_to(ROOT)} already current ({len(rows)} agents)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
