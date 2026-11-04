<div dir="rtl">

# ADR-0004 — نوع سرویس پیش‌زمینه: `specialUse` و نه `systemExempted`

```yaml
id: ADR-0004
date: 2026-11-04
status: پذیرفته
deciders: architect, platform
supersedes:
```

## زمینه

از API 34، هر سرویس پیش‌زمینه باید نوع اعلام‌شده و مجوز متناظر داشته باشد.
برای VPN دو نامزد وجود دارد: `systemExempted` و `specialUse`.

مستندات اندروید `systemExempted` را برای اپ‌های VPN «واجد شرایط» می‌داند، اما
اعلام آن بدون برخورداری از معیارها باعث `ForegroundServiceTypeNotAllowedException`
می‌شود و هیچ API عمومی‌ای وجود ندارد که ثابت کند اپ از راه Settings پیکربندی
شده است.

## تصمیم

**انتخاب کردیم که** `specialUse` با `FOREGROUND_SERVICE_SPECIAL_USE` و
`PROPERTY_SPECIAL_USE_FGS_SUBTYPE` اعلام شود.

## گزینه‌های ردشده

| گزینه | چرا رد شد |
| --- | --- |
| `systemExempted` | پیش‌شرط‌اش («از راه Settings پیکربندی شده») قابل اثبات نیست؛ ریسک کرش هنگام اتصال. |
| بدون نوع (شبیه نسخه‌های قدیمی) | روی API 34+ خطای زمان اجرا. |
| `connectedDevice` | برای انتقال داده به دستگاه خارجی است، نه VPN محلی. |
| سرویس کوتاه‌عمر | ~۳ دقیقه محدودیت + خطر ANR. VPN باید ساعت‌ها بماند. |

## پیامدها

- Lint مجوز `FOREGROUND_SERVICE_SPECIAL_USE` می‌خواهد؛ اضافه شده.
- `specialUse` هیچ پیش‌شرط زمان اجرا ندارد، پس اتصال نباید به‌خاطر نوع سرویس
  شکست بخورد.
- **تأیید نشده:** رفتار واقعی روی دستگاه API 34+ هنوز آزمون نشده.
</div>
