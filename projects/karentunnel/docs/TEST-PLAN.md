<div dir="rtl">

# برنامهٔ تست — KarenTunnel

> **قاعدهٔ حاکم:** هیچ تستی «موفق» گزارش نمی‌شود مگر اجرا شده باشد و شناسهٔ
> اجرا (CI run id یا شمارهٔ دستگاه) ثبت شود. تستِ فکر‌شده، تست نیست.

## ۰. وضعیت فعلی شواهد

> این جدول فقط شامل چیزهایی است که **اجرا شده‌اند**. هر ردیف یک شناسهٔ اجرا دارد.

| مورد | وضعیت | شاهد |
| --- | --- | --- |
| بیلد و تست کامل (CI) | ✅ **سبز** | اجرای `41767804110` — **هر چهار job** موفق |
| کامپایل هر پنج ماژول | ✅ انجام شد | همان اجرا، گام‌های `Unit tests` و `Assemble debug APK` |
| تست‌های واحد (JVM) | ✅ **اجرا و سبز** | همان اجرا؛ `testDebugUnitTest` برای هر پنج ماژول |
| بیلد APK دیباگ | ✅ **ساخته شد** | artifact `karentunnel-debug-apk`، ۲۱٬۹۵۸٬۶۶۷ بایت |
| Lint | ✅ **بدون خطا** | همان اجرا؛ `:core`، `:data`، `:diagnostics` بدون هیچ مسئله |
| تطبیق با API واقعی کتابخانه | ✅ از روی AAR | job `dump the real WireGuard tunnel API`؛ خروجی در `ci/wireguard-api.md` |
| بیلد پس از merge روی `main` | ✅ **سبز** | اجرای `41771656021` — هر چهار job موفق |
| تست روی دستگاه فیزیکی | ❌ انجام نشده | نیازمند دستگاه + سرور واقعی |
| تست نشتی DNS/IPv6 روی شبکهٔ واقعی | ❌ انجام نشده | نیازمند محیط شبکه‌ای واقعی |
| تأیید RFD/always-on | ❌ انجام نشده | نیازمند دستگاه |

### ۰.۱ گام‌های سبز اجرای `41767804110`

در job `assemble + unit tests + lint`:

```
Set up JDK 17                          => success
Set up Android SDK                     => success
Set up Gradle                          => success
Verify pinned dependencies resolve     => success
Unit tests (all modules)               => success
Assemble debug APK                     => success
Lint (release-blocking)                => success
Publish test + lint reports            => success
Upload APK                             => success
```

### ۰.۲ آنچه این موفقیت **اثبات نمی‌کند**

- هیچ تستی روی دستگاه اندروید اجرا نشده است. `VpnService`، `GoBackend`،
  Keystore و رابط Compose فقط **کامپایل** شده‌اند، نه اجرا.
- APK امضا نشده است (debug keystore خودکار CI).
- هیچ تونل واقعی ساخته نشده: نه دست‌دادنی انجام شده، نه بسته‌ای عبور کرده.
- ~~ابزار `wireguard-api` شکست خورد~~ — **حل شد** در اجرای `41767804110`؛
  امضاهای `Backend`/`Tunnel` با `javap` از AAR واقعی تأیید شدند
  (`ci/wireguard-api.md`).

این جدول با هر اجرای CI یا تست دستگاه به‌روز می‌شود. تا آن زمان، هر ادعای
«کار می‌کند» در این مخزن باطل است.

## ۱. تست‌های واحد خودکار (بدون دستگاه)

| # | ماژول | آزمون | چه چیزی را تضمین می‌کند |
| --- | --- | --- | --- |
| U1 | core | `ConfigNormalizerTest.*` | پیکربندی نامعتبر رد می‌شود؛ DNS اجباری است |
| U2 | core | `ipv4 only config excludes IPv6` | نشتی IPv6 ممکن نیست (F1) |
| U3 | core | `ipv6 interface address without default route is refused` | پیکربندی نیمه‌v6 رد می‌شود |
| U4 | core | `never produces TUNNELED without a v6 default route` | تضمین ساختاری |
| U5 | core | `secret is redacted in toString` | کلید در لاگ/خطا ظاهر نمی‌شود |
| U6 | core | `ConnectionStateMachineTest.connected requires all three proofs` | «متصل» دروغین ممکن نیست (F3) |
| U7 | core | `failure never maps to connected` | هیچ خطایی به Connected تبدیل نمی‌شود |
| U8 | core | `retry stops at max attempts` | حلقهٔ بی‌نهایت وجود ندارد |
| U9 | core | `attempt cap cannot be configured into an infinite loop` | سقف سخت |
| U10 | core | `permission errors are never retried automatically` | رفتار درست در لغو مجوز |
| U11 | vpn | `ConnectionManagerTest.*` | مدیر اتصال با کنترلر جعلی |
| U12 | vpn | `tunnel up without handshake is degraded not connected` | F3 در عمل |
| U13 | vpn | `double connect does not start two tunnels` | ایمنی برابر دو ضربه |
| U14 | vpn | `disconnect tears the tunnel down and clears state` | پاک‌سازی منابع |
| U15 | data | `FileSecureConfigStoreTest.round trip` | پیکربندی سالم برمی‌گردد |
| U16 | data | `file on disk does not contain the private key` | رمزنگاری در حالت سکون |
| U17 | data | `server id with path characters cannot escape the directory` | path traversal |
| U18 | data | `tampered/broken key is failure not garbage` | شکست امن |
| U19 | diagnostics | `dns failure alone is only probable` | ادعای فیلترینگ ممنوع |
| U20 | diagnostics | `healthy probes never produce a filtering claim` | همان |
| U21 | app | `StatusText` mapping | هر حالت برچسب درست فارسی دارد |

## ۲. تست‌های ابزاری (دستگاه/شبیه‌ساز)

| # | آزمون | روش | معیار قبولی |
| --- | --- | --- | --- |
| I1 | باز شدن اپ و نمایش «قطع» | Espresso | بدون کرش، RTL درست |
| I2 | درخواست مجوز VPN | `VpnService.prepare` | دیالوگ سیستم نمایش داده می‌شود |
| I3 | رد کردن مجوز | شبیه‌سازی رد | وضعیت `Error(PermissionRequired)` و پیام فارسی |
| I4 | اتصال با پیکربندی نامعتبر | import فایل خراب | پیام فارسی دقیق، بدون کرش |
| I5 | چرخهٔ عمر Activity | چرخش صفحه در حالت متصل | وضعیت حفظ می‌شود، تونل قطع نمی‌شود |
| I6 | بازگشت از پس‌زمینه | `Home` و بازگشت | وضعیت با مدیر اتصال هم‌خوان است |

## ۳. تست روی دستگاه فیزیکی (شبکهٔ واقعی)

| # | سناریو | گام‌ها | معیار قبولی |
| --- | --- | --- | --- |
| D1 | اتصال پایه Wi-Fi | اتصال به سرور شخصی | `Connected` و تأیید handshake در لاگ |
| D2 | اتصال پایه دادهٔ همراه | خاموش‌کردن Wi-Fi | اتصال برقرار یا خطای صادقانه |
| D3 | جابه‌جایی Wi-Fi ↔ داده | قطع Wi-Fi در حالت متصل | `Reconnecting` سپس `Connected` یا `Error` |
| D4 | قطع سرور | خاموش‌کردن سرور | `Degraded` سپس `Error`؛ **هرگز `Connected` پایدار** |
| D5 | شبکهٔ ضعیف | محدودسازی پهنای‌باند | backoff نمایی، حداکثر ۵ تلاش |
| D6 | بازگشت از حالت هواپیما | روشن‌کردن مجدد | رفتار مشخص، بدون حلقه |
| D7 | ری‌استارت اپ | kill و بازگشایی | وضعیت واقعی، نه «متصل» باقی‌مانده |
| D8 | ری‌استارت دستگاه | reboot | فقط با always-on یا گزینهٔ restore |
| D9 | Doze | نگه‌داشتن اپ در پس‌زمینه ۳۰ دقیقه | تشخیص handshake کهنه → `Degraded` |
| D10 | محدودیت باتری | اعمال restriction | رفتار مستند |
| D11 | هم‌زمانی با VPN دیگر | فعال‌کردن VPN دوم | رفتار سیستم، خطای روشن |
| D12 | لغو مجوز از Settings | لغو در حین اتصال | قطع فوری و `Disconnected` |

## ۴. تست امنیتی

| # | آزمون | روش | معیار قبولی |
| --- | --- | --- | --- |
| S1 | نشتی DNS | در حالت متصل، DNS query به سرور لاگ‌دار | هیچ پرس‌وجویی از بیرون تونل نمی‌رود |
| S2 | نشتی IPv6 | شبکهٔ dual-stack، پیکربندی IPv4-only | `ipv6Policy=EXCLUDED` و بدون مسیر v6 |
| S3 | ترافیک محافظت‌نشده در قطع تونل | قطع ناگهانی سرور | اپ `Degraded/Error` نشان می‌دهد؛ ترافیک بی‌صدا عبور نمی‌کند |
| S4 | ذخیرهٔ اعتبارنامه | بررسی فایل‌های `files/tunnels/` | فقط ciphertext؛ بدون کلید |
| S5 | افشا در لاگ | `adb logcat` در چرخهٔ کامل | هیچ کلید خصوصی/PSK |
| S6 | import پیکربندی | فایل با کلید نامعتبر/فیلد ناشناس | رد با پیام مشخص |
| S7 | رفتار اتصال مجدد | قطع مکرر شبکه | حداکثر ۵ تلاش، سپس توقف |
| S8 | پشتیبان‌گیری | `adb backup` | پیکربندی‌ها خارج از backup |
| S9 | exported components | `aapt dump xmltree` | فقط `MainActivity` صادرشده است |
| S10 | کلید در APK | `strings app.apk \| grep -i privatekey` | هیچ یافت نمی‌شود |

## ۵. قالب گزارش نتیجه

هر اجرا در `docs/test-runs/<تاریخ>-<اجرا>.md` ثبت می‌شود:

```markdown
# اجرای تست <شناسه>
- تاریخ: YYYY-MM-DD
- محیط: CI run <id> | دستگاه <مدل>، Android <نسخه>، شبکه <اپراتور>
- commit: <sha>

| # | آزمون | نتیجه | شاهد |
| --- | --- | --- | --- |
| U2 | ipv6 excluded | PASS | test-results/core/... |
| D4 | server down | BLOCKED | نیازمند سرور |
```

## ۶. چیزهایی که تست نمی‌کنیم و ادعا هم نمی‌کنیم

- عبور از هر نوع فیلترینگ
- سرعت/پایداری بهتر از سایر کلاینت‌های WireGuard
- ایمنی روی دستگاه root‌شده (مدل تهدید شامل root نیست)
- مقاومت در برابر تحلیل ترافیک در سطح DPI

</div>
