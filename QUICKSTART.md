<div dir="rtl">

# راه‌اندازی سریع (ده دقیقه)

> هدف: از صفر تا اولین عامل در حال کار، بدون خواندن کل مخزن.

## دقیقهٔ ۰ — پیش‌نیاز

| مورد | چرا |
| --- | --- |
| `bash` و `git` | اسکریپت‌ها |
| `python3` (۳٫۹+) | تولید فهرست و بررسی لینک‌ها |
| Claude Code | ابزار پیش‌فرضی که فهرست در آن نصب می‌شود |

```bash
git clone https://github.com/rahmaniho/Ktunnel.git
cd Ktunnel
```

## دقیقهٔ ۱ — بررسی سلامت

```bash
./scripts/check-agency.sh
```

باید در پایان ببینید: `agency repo is healthy`. اگر نه، به بخش «عیب‌یابی»
پایین بروید.

## دقیقهٔ ۲ — نصب فهرست

```bash
./scripts/install.sh
```

چه اتفاقی افتاد: ۴۸ فایل در `.claude/agents/` و یک نقشهٔ خوانا در
`.claude/AGENCY-ROSTER.md` نوشته شد.

```bash
ls .claude/agents | head
```

> این نصب **repo-local** است: فایل‌ها همراه مخزن می‌آیند و برای هر کسی که
> مخزن را clone کند یکسان‌اند. اگر می‌خواهید همین فهرست در همهٔ پروژه‌های این
> ماشین هم در دسترس باشد: `./scripts/install.sh --user`

## دقیقهٔ ۳ — دیدن فهرست

```bash
upstream/agency-agents/scripts/install.sh --list agents | head -30
```

این فهرست **۲۸۳** عامل بالادست است. ۴۸ تای منتخب در `AGENTS.md` با ستون
«کی فعالش کنیم» آمده‌اند. تفاوت را بدانید: بالا دست منبع است، آژانس انتخاب
می‌کند.

## دقیقهٔ ۴ — اولین فراخوانی

Claude Code را **داخل همین پوشه** اجرا کنید (تا `.claude/agents/` دیده شود):

```bash
claude
```

سپس بنویسید:

```
از Reality Checker بخواه مخزن فعلی را از منظر «آماده برای انتشار» بررسی کند.
پیش‌فرضت «نیاز به کار» باشد و برای هر ادعا شاهد بخواه.
```

انتظار: عامل با شواهد مخالفت می‌کند یا شاهد می‌خواهد — نه با تأیید سریع. این
دقیقاً کاری است که باید بکند.

## دقیقهٔ ۵ — فراخوانی دوم: یک عامل تخصصی

```
از Code Reviewer بخواه فایل scripts/install.sh را از منظر
«چه چیزی در محیط‌های مختلف می‌شکند» مرور کند. یافته‌ها را با مسیر خط بده.
```

## دقیقهٔ ۶ — یک جریان کامل (اختیاری)

اگر یک قابلیت واقعی برای ساختن دارید:

```
از Agents Orchestrator بخواه جریان feature-development را برای این قابلیت اجرا کند:
<توضیح یک‌خطی>
معیار پذیرش را از من بپرس، سپس G1 تا G5 را با شاهد اجرا کن.
```

جزئیات جریان: [`workflows/feature-development.md`](workflows/feature-development.md)

## دقیقهٔ ۷ — کار در یک پروژه

```bash
cp -r templates/project-skeleton projects/<نام-پروژه>
```

سپس `projects/<نام>/STATUS.md` را پر کنید. اگر پروژه از قبل پرونده دارد
(مثل `projects/karentunnel/`)، مستقیم همان را به‌روز کنید.

## دقیقهٔ ۸ — قالب‌ها

| می‌خواهید | قالب |
| --- | --- |
| تسک بسازید | [`templates/task-template.md`](templates/task-template.md) |
| دیف را بازبینی کنید | [`templates/review-template.md`](templates/review-template.md) |
| کار را بپذیرید | [`templates/acceptance-template.md`](templates/acceptance-template.md) |
| تصمیم معماری ثبت کنید | `projects/<نام>/decisions/0000-template.md` |

## دقیقهٔ ۹ — افزودن یک عامل تازه

```bash
# ۱. پیدا کردن شناسه در بالا دست
upstream/agency-agents/scripts/install.sh --list agents | grep -i <کلید>

# ۲. یک سطر به agents/registry.tsv اضافه کنید
#    (بخش، شناسه، فایل بالا دست، نام، ایموجی، «کی فعالش کنیم»)

# ۳. بازنشر، نصب، و بررسی
./scripts/sync-agents.sh && ./scripts/install.sh --force
python3 scripts/gen-roster.py
./scripts/check-agency.sh
```

## دقیقهٔ ۱۰ — قدم بعدی

- اگر تصمیم می‌گیرید چیزی ساخته شود: [`workflows/new-project.md`](workflows/new-project.md)
- اگر کد می‌نویسید: [`workflows/feature-development.md`](workflows/feature-development.md)
- اگر باگی هست: [`workflows/bug-investigation.md`](workflows/bug-investigation.md)
- اگر انتشار در پیش است: [`workflows/security-audit.md`](workflows/security-audit.md) و
  [`workflows/release-management.md`](workflows/release-management.md)

## عیب‌یابی

| نشانه | علت | اصلاح |
| --- | --- | --- |
| `upstream installer missing` | پوشهٔ بالا دست ناقص clone شده | `git checkout upstream/` یا `./scripts/sync-agents.sh --update-upstream` |
| `AGENTS.md roster is stale` | `registry.tsv` تغییر کرده | `python3 scripts/gen-roster.py` |
| `drifted from upstream` | فایل در `agents/` دستی ویرایش شده | `./scripts/sync-agents.sh` |
| `.claude/agents has N files, registry has M` | نصب قدیمی است | `./scripts/install.sh --force` |
| عامل در Claude Code دیده نمی‌شود | از پوشهٔ دیگر اجرا شده | `cd` به ریشهٔ مخزن و دوباره اجرا کنید |
| `broken link` | لینک نسبی به فایل ناموجود | مسیر را اصلاح کنید یا `python3 scripts/check-links.py --verbose` بزنید |

## چه چیزی این آژانس نیست

- **ابزار خودکار نیست.** هیچ حلقهٔ پس‌زمینه‌ای اجرا نمی‌شود؛ هر عامل باید
  صریح فراخوانی شود.
- **جایگزین قضاوت انسانی نیست.** دروازه‌ها و شاهدها به تصمیم انسانی شکل
  می‌دهند، جایگزینش نمی‌شوند.
- **تعریف محصول نیست.** `projects/karentunnel/STATUS.md` توضیح می‌دهد که
  پروژهٔ Ktunnel هنوز حتی اسپک ندارد.

</div>
