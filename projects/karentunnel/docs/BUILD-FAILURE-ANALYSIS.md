<div dir="rtl">

# تحلیل شکست بیلد و اصلاحات دور دوم

> نسخه: ۲٫۰ — ۲۰۲۶-۱۱-۰۴ · شاخه: `arena/cb29260a-ktunnel`

## ۱. وضعیت واقعی

| مورد | وضعیت |
| --- | --- |
| اولین اجرای `android-ci` (PR #1) | **fail** در گام `testDebugUnitTest` |
| لاگ کامپایلر | در آن زمان قابل دریافت نبود (ریدایرکت به `*.blob.core.windows.net`) |
| کد | **قبل از این دور، هرگز یک بار هم کامپایل نشده بود** |
| اجرای نهایی | ✅ **سبز** — `41807834698`، هر پنج job موفق |
| APK دیباگ | ساخته شد (۲۱٬۹۵۸٬۶۶۵ بایت) |

### ۱.۱ راه‌حل دسترسی به لاگ

آرشیو لاگ و artifact هر دو به `productionresultssa*.blob.core.windows.net`
ریدایرکت می‌شدند و از sandbox توسعه قابل دریافت نبودند. راه‌حلی که کار کرد:
workflow در صورت شکست، خودِ لاگ را در `ci/last-failure.md` **کامیت** می‌کند.
از آن به بعد هر شکست با `git fetch` قابل خواندن بود. این تغییر بیشترین اثر را
در کل این دور داشت: هر ۱۱ شکست بعدی با پیام واقعی کامپایلر حل شد، نه با حدس.

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

- ~~نسخه‌های Compose/AndroidX~~ — **حل شد.** `41807834698` همه را resolve و
  کل app را کامپایل کرد.
- ~~امضای `Backend`/`Tunnel`~~ — **حل شد.** اجرای `41767804110` تایپ‌های واقعی
  AAR نسخهٔ `1.0.20260102` را استخراج کرد (artifact `wireguard-api-dump`):

  ```java
  public interface Backend {
      Set<String> getRunningTunnelNames();
      Tunnel.State getState(Tunnel) throws Exception;
      Statistics getStatistics(Tunnel) throws Exception;   // ← متد، نه پراپرتی
      String getVersion() throws Exception;                // ← بدون آرگومان
      boolean isAlwaysOn() throws Exception;
      boolean isLockdownEnabled() throws Exception;
      Tunnel.State setState(Tunnel, Tunnel.State, Config) throws Exception;
  }
  public interface Tunnel {
      String getName();
      void onStateChange(Tunnel.State);
  }
  public class Statistics {
      boolean isStale();
      PeerStats peer(Key);
      Key[] peers();
      long totalRx();
      long totalTx();
  }
  ```

  کد ما دقیقاً با همین امضاها فراخوانی می‌کند و کامپایل می‌شود؛ پس تطبیق با
  کتابخانهٔ واقعی تأیید شده است.
- **رفتار زمان اجرا** — هیچ‌چیز روی دستگاه اجرا نشده. `GoBackend`، `VpnService`،
  Keystore و Compose فقط کامپایل شده‌اند.

## ۵. درس فرایندی

دور قبل ~۲٬۵۰۰ خط Kotlin بدون یک بار کامپایل نوشته شد. برای این دور:

> اولین کامیت باید کوچک و **کامپایل‌شدنی** باشد؛ هر کامیت بعدی فقط پس از
> سبز شدن CI. تعداد خط تایپ‌شده معیار پیشرفت نیست؛ معیار، بیلد سبز است.

## ۶. آنچه اثبات شد

- [x] `testDebugUnitTest` سبز — اجرای `41807834698` (ثبت در `TEST-PLAN.md` §۰)
- [x] `assembleDebug` سبز — artifact `karentunnel-debug-apk`، ۲۱٫۹ مگابایت
- [x] ۲۱ تست واقعاً اجرا شدند، نه فقط کامپایل
- [x] `lintDebug` بدون خطای بازدارنده (۰ مسئله در `:core`، `:data`، `:diagnostics`)

## ۷. آنچه هنوز اثبات نشده

- [ ] اجرای اپ روی دستگاه اندروید
- [ ] یک تونل واقعی با یک سرور واقعی WireGuard
- [ ] نشتی IPv6/DNS روی شبکهٔ واقعی
- [ ] رفتار FGS با `specialUse` روی API 34+

## ۸. تاریخ شکست‌ها و علت هرکدام

| اجرا | خطای غالب | علت ریشه‌ای |
| --- | --- | --- |
| `41758468638` | `testDebugUnitTest` | نسخهٔ WireGuard وجود نداشت |
| `41758813139` | `Could not find com.wireguard.android:tunnel:1.0.20260315` | نسخه از tag گیت حدس زده شده بود، منتشر نشده بود |
| `41759304515` | `Unresolved reference 'finalAddress'` | بازماندهٔ refactor در `ConfigNormalizer` |
| `41759854884` | `Unclosed comment`، `Unresolved reference 'fill'` | `/*` داخل KDoc (کامنت تودرتو)، و `String.fill()` که وجود ندارد |
| `41760140254` | `Unresolved reference 'TestConfigs'` | تست یک ماژول به سورس تست ماژول دیگر دسترسی ندارد |
| `41760453710` | `Check for instance is always 'false'` | tautology در تست، با `-Werror` خطا شد |
| `41760692805` | `isActive`، `Json`، `stat_sys_vpn_ic`، `when` ناقص | چهار خطای مستقل در `:app` و تست `:vpn` |
| `41761123889` | ۷ تست در ۶۰ ثانیه timeout | حلقهٔ health بی‌پایان، `advanceUntilIdle` هرگز برنمی‌گشت |
| `41762203584` | ۵ تست timeout | همان، با بازهٔ تزریق‌شده هنوز بی‌پایان |
| `41763155529` | ۲۱ از ۲۱ سبز، lint شکست | حلقه محدود شد؛ lint هرگز قبلاً اجرا نشده بود |
| `41763881119` | `MissingPermission: ACCESS_NETWORK_STATE` | مجوز در `:app` بود، مصرف‌کننده `:vpn` بود |
| `41765886354` | `ForegroundServicePermission: systemExempted` | این type مجوز `SCHEDULE_EXACT_ALARM` می‌خواست؛ به `specialUse` تغییر کرد |
| `41766458041` | فقط job بیلدش سبز بود؛ سه job دیگر شکست | **قبلاً اشتباهاً «سبز» ثبت شده بود** |
| `41767804110` | — | ✅ **سبز — هر چهار job** |
| `41807834698` | — | ✅ **سبز — هر پنج job (شامل release)** |

</div>
