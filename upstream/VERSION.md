# upstream/ — پروژهٔ بالادست (vendored)

این پوشه یک **کپی کامل و بدون تغییر** از مخزن بالادست است. عامل‌ها از اینجا
خوانده و در `agents/` بازنشر می‌شوند؛ هیچ‌جا به نسخهٔ زندهٔ GitHub وابسته نیستیم.

| فیلد | مقدار |
| --- | --- |
| مخزن | `msitarzewski/agency-agents` |
| نسخه | `b37b5bb33b78f2c1d6567d98e25ca818e3259219` |
| تاریخ همگام‌سازی | 2026-11-04 |
| تعداد فایل | 384 فایل (بدون `.git`) |
| حجم | حدود ۵٫۶ مگابایت |
| مجوز | MIT — فایل `upstream/agency-agents/LICENSE` |

## چیدمان

```
upstream/agency-agents/
├── academic/ design/ engineering/ ...   # ۱۸ بخش عامل (۲۸۳ فایل)
├── integrations/                        # خروجی تبدیل‌شده به ازای هر ابزار
├── strategy/                            # NEXUS: playbooks, runbooks, coordination
├── scripts/                             # install.sh, convert.sh, lint-agents.sh, ...
├── examples/                            # جریان‌های نمونه
├── divisions.json tools.json            # کاتالوگ رسمی
└── README.md
```

## قوانین نگه‌داری

1. **این پوشه را دستی ویرایش نکنید.** هر تغییر در `upstream/` با همگام‌سازی بعدی
   از بین می‌رود. سفارشی‌سازی‌های کارن‌سافت جای دیگری است:
   `AGENCY.md`، `AGENTS.md`، `departments/`، `workflows/`، `templates/`.
2. **ارتقا فقط از طریق اسکریپت:**
   ```bash
   # ۱. کامیت هدف را در upstream/VERSION بنویسید
   # ۲. میکس‌پین را به‌روز کنید و بازنشر کنید:
   ./scripts/sync-agents.sh --update-upstream
   # ۳. خروجی را دوباره نصب و بررسی کنید:
   ./scripts/install.sh --force && ./scripts/check-agency.sh
   ```
3. **بازنویسی محلی مجاز نیست.** `agents/` همیشه با `upstream/` بایت‌به‌بایت برابر
   است؛ `scripts/sync-agents.sh --check` هر انحرافی را رد می‌کند.

## چرا کپی کامل و نه submodule؟

- کار بدون شبکه و بدون وابستگی به دسترسی GitHub.
- `git grep` و مرور یکپارچه روی کل اکوسیستم.
- تبدیل و نصب با اسکریپت‌های خودِ بالادست انجام می‌شود؛ چیزی دوباره‌نویسی نشده.
- هزینهٔ حجم: حدود ۵٫۶ مگابایت — قابل قبول برای یک مخزن دانش/هماهنگی.

## مجوز و انتساب

پروژهٔ بالادست تحت MIT منتشر شده است. متن مجوز در
`upstream/agency-agents/LICENSE` نگه داشته شده و باید در توزیع‌های بعدی هم بماند.
محتوای عامل‌ها و نام‌های عاملی متعلق به نویسندگان اصلی پروژهٔ
`msitarzewski/agency-agents` است؛ کارن‌سافت فقط تقسیم‌بندی، فیلتر، و
جریان‌های کاری خودش را اضافه کرده است.
