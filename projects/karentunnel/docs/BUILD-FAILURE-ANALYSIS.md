<div dir="rtl">

# تحلیل شکست بیلد و اصلاحات دور دوم

> نسخه: ۲٫۰ — ۲۰۲۶-۱۱-۰۴ · شاخه: `arena/cb29260a-ktunnel`

## ۱. وضعیت واقعی

| مورد | وضعیت |
| --- | --- |
| اولین اجرای `android-ci` (PR #1) | **fail** در گام `testDebugUnitTest` |
| لاگ کامپایلر | در آن زمان قابل دریافت نبود (ریدایرکت به `*.blob.core.windows.net`) |
| کد | **قبل از این دور، هرگز یک بار هم کامپایل نشده بود** |
| این دور | ۱۰ نقص قطعی اصلاح شد؛ منتظر اجرای CI |

## ۲. نقص‌های قطعی که اصلاح شدند

| # | نقص | فایل | چرا خطای کامپایل بود |
| --- | --- | --- | --- |
| A | `Result.failure(ConfigError.…)` — ۴۳ مورد | `core/.../Errors.kt` | `Result.failure` فقط `Throwable` می‌پذیرد؛ `sealed interface` این‌طور نیست |
| B | `backend.statistics` | `vpn/.../WireGuardTunnelController.kt` | `Statistics getStatistics(Tunnel)` پارامتر دارد ⇒ متد است، نه پراپرتی |
| C | `peer?.latestHandshakeEpochMillis()` | همان | `PeerStats` یک Java record است ⇒ دسترسی پراپرتی |
| D | `kotlin {}` داخل `android {}` | همهٔ ۵ ماژول | در AGP 8.13 + Kotlin 2.x این افزونهٔ سطح‌بالا است |
| E | `JvmTarget.JVM_17` به‌صورت nested | همهٔ ۵ ماژول | `JvmTarget` یک enum سطح‌بالا است؛ با `fromTarget("17")` جایگزین شد |
| F | `component1..4` دستی روی data class | `ConfigNormalizer.kt` | data class این‌ها را خودش می‌سازد؛ بازتعریف خطاست (و کل `Quad4` حذف شد) |
| G | `TestConfigs` در `core/src/test` | تست‌های `data` | تست یک ماژول به سورس تست ماژول دیگر دسترسی ندارد ⇒ `testFixtures` |
| H | `coroutineContext[CoroutineDispatcher]` | `ProbeEngineTest` | `CoroutineContext` کلید داخلی برای dispatcher ندارد |
| I | `AnimatedContent`/`togetherWith` | `DashboardScreen` + catalog | `androidx.compose.animation` هرگز اعلام نشده بود |
| J | `ConnectTrigger` خوانده نمی‌شد | `ConnectionManager` | با `allWarningsAsErrors` هشدار «unused parameter» خطاست |

## ۳. نقص‌های منطقی که کامپایل نمی‌گرفت اما رفتار را خراب می‌کرد

| # | مشکل | اصلاح |
| --- | --- | --- |
| K | `Reconnecting → Reconnecting` در حلقهٔ تلاش، که `check()` را می‌انداخت | فقط وقتی وضعیت فعلی `Connecting` نیست، `Connecting` منتشر می‌شود |
| L | تصمیم IPv6 از «وجود هر مقدار v6» گرفته می‌شد، نه از `::/0` | تصمیم فقط بر مبنای مسیر پیش‌فرض v6؛ مسیرهای v6 پرت حذف می‌شوند |
| M | DNS نسخه ۶ در حالت IPv4-only حذف می‌شد، حتی اگر هیچ DNS دیگری نبود (نتیجه: پیکربندی با DNS خالی) | بررسی `Ipv6OnlyDnsUnsupported` به لایهٔ نرمال‌سازی منتقل شد |
| N | رد کردن مجوز VPN، رابط را در حالت «قطع» رها می‌کرد | حالا `Error(PermissionRequired)` صریح نمایش داده می‌شود |
| O | `ConfImporter` نبود DNS را «خطای پارس» می‌خواند | حالا به `ConfigNormalizer` واگذار می‌شود تا پیام دقیق فارسی بدهد |

## ۴. ریسک‌های باقی‌مانده (تأییدنشده)

- **نسخه‌های Compose/AndroidX** (`composeBom 2025.06.01`, `lifecycle 2.9.1`,
  `activityCompose 1.10.1`) هرگز resolve نشده‌اند. اولین کاری که CI می‌کند
  resolve کردن آن‌هاست.
- **امضای `Tunnel`** (`getName`/`onStateChange`) از سورس ۱٫۰٫۲۰۲۶۰۳۱۵ خوانده
  شده، اما اگر نسخهٔ منتشرشده عضو انتزاعی دیگری داشته باشد، object-expression
  می‌شکند.
- **`GoBackend.version`** نوع `String?` برگرداند؛ ما `?: "unknown"` گذاشتیم.

## ۵. درس فرایندی

دور قبل ~۲٬۵۰۰ خط Kotlin بدون یک بار کامپایل نوشته شد. برای این دور:

> اولین کامیت باید کوچک و **کامپایل‌شدنی** باشد؛ هر کامیت بعدی فقط پس از
> سبز شدن CI. تعداد خط تایپ‌شده معیار پیشرفت نیست؛ معیار، بیلد سبز است.

## ۶. آنچه باید اثبات شود (نه ادعا)

- [ ] `testDebugUnitTest` سبز + **شناسهٔ اجرای CI** ثبت‌شده در `TEST-PLAN.md`
- [ ] `assembleDebug` سبز + artifact APK
- [ ] ۲۱ تست واقعاً اجرا (نه فقط کامپایل)
- [ ] `lintDebug` بدون خطای بازدارنده

</div>
