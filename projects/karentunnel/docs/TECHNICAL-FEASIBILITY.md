<div dir="rtl">

# گزارش امکان‌سنجی و معماری فنی — KarenTunnel

> **فاز ۱ (Milestone 1).** این سند پیش از هر خط کد نوشته شده است.
> نسخهٔ ۱٫۰ — ۲۰۲۶-۱۱-۰۴
> وضعیت: **پذیرفته‌شده به‌عنوان مبنای اجرا؛ مشروط به تأیید موارد §۹.**

---

## ۰. خلاصهٔ مدیریتی

ساخت یک کلاینت VPN شخصی برای اندروید روی `VpnService` و کتابخانهٔ رسمی
WireGuard **امکان‌پذیر و از نظر فنی ساده‌تر از آن است که به نظر می‌رسد** — اما
دو نکتهٔ غیرمنتظره در بررسی کد منبع کتابخانه پیدا شد که اگر نادیده گرفته شوند،
یا اپلیکیشن بی‌صدا از کار می‌افتد یا ادعای امنیتی‌اش نادرست می‌شود:

1. **منطق «کیل‌سوئیچ» کتابخانه برعکس چیزی است که در پروژه‌های مشابه دیده
   می‌شود.** اگر پیکربندی شامل مسیر پیش‌فرض IPv4 (`0.0.0.0/0`) باشد ولی IPv6
   هم فعال باشد، انتظار می‌رود IPv6 از تونل خارج شود. در `GoBackend`، اگر
   مسیر پیش‌فرض وجود **نداشته باشد**، هر دو خانوادهٔ آدرس صریحاً مجاز می‌شوند —
   و این یعنی نشتی. ما برای همین، **IPv6 را در لایهٔ پیکربندی حذف می‌کنیم**
   (§۵٫۲) — نه اینکه به پیش‌فرض کتابخانه تکیه کنیم.
2. **کتابخانه هیچ Foreground Service ندارد.** خود اپلیکیشن رسمی WireGuard هم
   ندارد؛ این در Android 12+ خطر «کشته‌شدن اپ زیر Doze» را ایجاد می‌کند که
   ظاهراً اتصال را «متصل» نگه می‌دارد ولی ترافیک را متوقف می‌کند.

همچنین یک محدودیت محیطی قطعی وجود دارد: **این محیط ساخت (sandbox) نه JDK
دارد، نه Android SDK، و نه دسترسی به `dl.google.com` و Maven Central.**
بنابراین «APK ساخته‌شده» فقط از طریق CI قابل تولید است و تا سبز نشدن CI، هیچ
ادعای «ساخته شد» در این سند یا جای دیگر پذیرفته نیست (§۱۰).

---

## ۱. دامنه و غیردامنه

### داخل دامنه (نسخهٔ ۱٫۰)

| # | قابلیت |
| --- | --- |
| ۱ | تونل واقعی WireGuard از طریق `VpnService` + `com.wireguard.android:tunnel` |
| ۲ | مدیریت چرخهٔ عمر سرویس، اتصال/قطع/اتصال اضطراری |
| ۳ | ماشین حالت صریح با جلوگیری از گزارش «متصلِ دروغین» |
| ۴ | مقاوم‌سازی در برابر تغییر شبکه و محدودیت پس‌زمینه |
| ۵ | محافظت از نشتی DNS و IPv6 (fail-closed) |
| ۶ | ذخیرهٔ امن کلید خصوصی با Android Keystore |
| ۷ | سرورهای دستی (بدون هیچ سرور ساختگی) |
| ۸ | تشخیص‌گر شبکه با تفکیک «قطعی / محتمل / نامعلوم» |
| ۹ | رابط فارسی RTL، تم روشن/تاریک |
| ۱۰ | تست واحد + تست ابزاری + مستند شواهد |

### خارج از دامنه (صریحاً)

| مورد | چرا |
| --- | --- |
| OpenVPN | ارزیابی در §۸؛ برای ۱٫۰ نه |
| پروتکل‌های مبهم‌ساز (obfuscation) | خارج از WireGuard استاندارد؛ نیازمند بازبینی امنیتی جدا |
| اشتراک اکانت، پرداخت، سرور عمومی | محصول شخصی است |
| تضمین عبور از فیلترینگ | **غیرممکن است و ادعا نمی‌شود** |
| دور زدن تشخیص ترافیک در سطح DPI | اپلیکیشن VPN با پروتکل استاندارد این را تضمین نمی‌کند |
| آنالیتیکس، تلمتری، حساب کاربری | عمداً صفر |

---

## ۲. یافته‌های کلیدی بررسی (منبع‌دار)

### F1 — منطق `allowFamily` در `GoBackend` نشتی IPv6 ایجاد می‌کند

منبع: کد منبع `wireguard-android` تگ `1.0.20260315`،
`tunnel/src/main/java/com/wireguard/android/backend/GoBackend.java` (خطوط
۲۹۷–۳۴۰):

```java
boolean sawDefaultRoute = false;
for (final Peer peer : config.getPeers())
    for (final InetNetwork addr : peer.getAllowedIps()) {
        if (addr.getMask() == 0) sawDefaultRoute = true;
        builder.addRoute(addr.getAddress(), addr.getMask());
    }

// "Kill-switch" semantics
if (!(sawDefaultRoute && config.getPeers().size() == 1)) {
    builder.allowFamily(OsConstants.AF_INET);
    builder.allowFamily(OsConstants.AF_INET6);
}
```

**تحلیل:** `allowFamily` خانوادهٔ آدرس را «مجاز» می‌کند، نه «از تونل خارج
شده». در حالت متعارف (یک peer با `0.0.0.0/0` **و** `::/0`) مسیر هر دو خانواده
به تونل می‌رود، پس `allowFamily` لازم نیست. اما اگر پیکربندی فقط IPv4 داشته
باشد، کتابخانه هر دو خانواده را مجاز می‌کند تا اتصال نشکند؛ در این حالت
بسته‌های IPv6 مسیر تونل ندارند و **از اینترفیس فیزیکی خارج می‌شوند** — یعنی
نشتی IPv6.

**تصمیم معماری (اجباری):**

- در نرمال‌سازی پیکربندی (`core/model`)، اگر endpoint یا DNS سرور IPv4 باشد و
  `::/0` در allowed-ips نباشد، **هیچ آدرس IPv6 به `VpnService.Builder` اضافه
  نمی‌شود** و `allowFamily(AF_INET6)` فراخوانی نمی‌شود.
- بهترین حالت: پیکربندی سرور شامل `::/0` باشد تا IPv6 کاملاً داخل تونل برود.
- تست رگرسیون: `VpnBuilderPlanTest` باید تضمین کند برای پیکربندی IPv4-only هرگز
  `allowFamily(AF_INET6)` صادر نمی‌شود.

**نکتهٔ صادقانه:** چون `GoBackend` خودش builder را می‌سازد و ما کنترل مستقیمی
بر `allowFamily` نداریم، کنترل واقعی از طریق **محتوای پیکربندی** اعمال می‌شود
(حذف IPv6 از `Interface.addresses` و `Peer.allowedIps`). این محدودیت در
`KNOWN-LIMITATIONS.md` ثبت می‌شود و به‌عنوان دلیل معماری برای لایهٔ
`vpn:plan` (که پیش از فراخوانی کتابخانه، پیکربندی را می‌سنجد) آورده می‌شود.

### F2 — هیچ Foreground Service در کتابخانه یا اپ رسمی نیست

منبع: `tunnel/src/main/AndroidManifest.xml` (تنها یک `<service>` برای
`GoBackend$VpnService`، بدون `foregroundServiceType`) و `ui/` (اپ رسمی) که
هیچ ارجاعی به `startForeground()` ندارد.

از Android 12 (API 31) به بعد، اپ در پس‌زمینه نمی‌تواند FGS جدید استارت کند
(`ForegroundServiceStartNotAllowedException`)؛ از Android 14 (API 34) هر FGS
باید type اعلام‌شده و مجوز متناظر داشته باشد.

**تصمیم:**

- `KarenTunnel` یک FGS **اختصاصی** می‌سازد (`TunnelForegroundService`) با
  `foregroundServiceType="specialUse"`.
- **چرا نه `systemExempted`:** مستند رسمی «اپ‌های VPN پیکربندی‌شده در
  Settings» را واجد `systemExempted` می‌داند، اما همان صفحه تصریح می‌کند اپی که
  شرط‌ها را نداشته باشد و این type را اعلام کند
  `ForegroundServiceTypeNotAllowedException` می‌گیرد. هیچ API عمومی‌ای وجود
  ندارد که اپ با آن اثبات کند «در Settings پیکربندی شده است». اعلام یک type که
  ممکن است در زمان اجرا رد شود، با اصل «ادعا فقط با اثبات» سازگار نیست.
- `specialUse` هیچ پیش‌نیاز زمان‌اجرایی ندارد و برای همین دسته ساخته شده است؛
  دلیل استفاده با `<property ... PROPERTY_SPECIAL_USE_FGS_SUBTYPE>` ثبت می‌شود.
- **هزینهٔ پذیرفته‌شده:** بازبینی Play Console در صورت انتشار عمومی. جزئیات در
  `KNOWN-LIMITATIONS` §۲ و آزمون T-24 در `TEST-PLAN`.
- **نکتهٔ معماری مهم:** `GoBackend` خودش `context.startService(new Intent(...,
  VpnService.class))` صدا می‌زند و FGS ما باید هم‌زمان بالا باشد؛ ترتیب
  «FGS اول، سپس `setState(UP)`» اجباری است.
- **نتیجهٔ قابل مشاهده برای کاربر:** اگر اتصال «UP» باشد ولی handshake تازه
  نباشد، این حالت به‌عنوان `CONNECTED_DEGRADED` گزارش می‌شود، نه `CONNECTED`.

### F3 — `GoBackend` فقط حالت کتابخانه را برمی‌گرداند، نه اثبات تونل

`Tunnel.State` در کتابخانه سه مقدار دارد: `DOWN`, `TOGGLE`, `UP`
(`backend/Tunnel.java`). `getState()` وضعیت درون‌حافظه‌ای هندل Go است.

**تصمیم:** `CONNECTED` فقط وقتی اعلام می‌شود که **هر سه** شرط برقرار باشند:

1. `backend.getState(tunnel) == UP`
2. یک `Statistics.PeerStats` با `latestHandshakeEpochMillis` تازه (کمتر از
   آستانهٔ ۱۸۰ ثانیه) وجود داشته باشد
3. `VpnService` سیستم فعال باشد (`ConnectivityManager` یک شبکهٔ `VPN` ببیند)

هیچ‌کدام از این سه به‌تنهایی کافی نیست. این تعریف در `ConnectionStateMachine`
به‌صورت صریح کد می‌شود و تست واحد دارد.

### F4 — کتابخانه همیشه `allowBypass()` را صدا نمی‌زند، پس مدل کاربردی روی
همان مدل رسمی WireGuard است

در مسیر `UP`، فیلدهای `IncludedApplications`/`ExcludedApplications` از
پیکربندی خوانده می‌شوند. ما در ۱٫۰ از per-app استفاده نمی‌کنیم (همهٔ ترافیک
داخل تونل).

### F5 — نیاز به desugaring

منبع: README رسمی، بخش «Embedding»: کتابخانه از ویژگی‌های Java 8 استفاده
می‌کند و `coreLibraryDesugaringEnabled = true` لازم است. بدون آن، بیلد یا
اجرا در API پایین می‌شکند.

### F6 — DNS و IPv6: مسئولیت ما، نه کتابخانه

`VpnService.Builder.addDnsServer()` هر DNS داده‌شده را «تنظیم» می‌کند ولی
**جلوی DNS خارج از تونل را نمی‌گیرد**؛ کیل‌سوئیچ واقعی از مسیر و
`allowFamily` می‌آید. بنابراین:

- `dns` در پیکربندی **اجباری** است و اگر فقدان داشته باشد، اتصال با خطای
  `ConfigurationError.MissingDns` رد می‌شود (fail-closed).
- گزینهٔ «DNS سفارشی» در تنظیمات، پیکربندی را بازنویسی می‌کند، نه اینکه به
  سیستم اجازه دهد DNS اپراتور را استفاده کند.

---

## ۳. معماری پیشنهادی

```
┌──────────────────────────────────────────────────────────────┐
│ :app  (Compose UI, RTL, ViewModel, DI wiring)                │
│   DashboardScreen · ServersScreen · SettingsScreen           │
│   DiagnosticsScreen · ConnectionViewModel                    │
└───────────────▲──────────────────────────────┬───────────────┘
                │ StateFlow<ConnectionUiState> │ Intents
┌───────────────┴──────────────────────────────▼───────────────┐
│ :vpn                                                          │
│   TunnelController (interface)   ← قابل تعویض در تست          │
│   WireGuardTunnelController      ← پیاده‌سازی واقعی            │
│   ConnectionManager (ماشین حالت + backoff + health)           │
│   NetworkMonitor (ConnectivityManager callback)               │
│   TunnelForegroundService (FGS + notification)                │
│   VpnServiceLifecycle (prepare/onRevoke/شروع/توقف)            │
└───────────────▲──────────────────────────────┬───────────────┘
                │                              │
┌───────────────┴────────────┐   ┌─────────────▼───────────────┐
│ :data                      │   │ :diagnostics                │
│   SecureConfigStore        │   │   ProbeEngine               │
│   (Keystore + AES-GCM)     │   │   DnsProbe · TcpProbe       │
│   ServerRepository         │   │   HandshakeProbe · UdpProbe │
│   SettingsStore (DataStore)│   │   LeakInspector             │
└───────────────▲────────────┘   └─────────────▲───────────────┘
                │                              │
          ┌─────┴──────────────────────────────┴─────┐
          │ :core  (مدل‌ها، خطاها، لاگ ایمن، زمان)     │
          └──────────────────────────────────────────┘
```

### چرا این مرزها

| مرز | دلیل |
| --- | --- |
| `:vpn` جدا از `:app` | سرویس و ماشین حالت باید بدون UI و بدون Compose تست شوند |
| `TunnelController` به‌عنوان interface | تست ماشین حالت بدون تونل واقعی؛ و امکان افزودن پروتکل دوم بدون لمس UI |
| `:data` جدا | کلید و پیکربندی هرگز وارد ماژول UI نمی‌شود (قاعدهٔ «کمترین دسترسی») |
| `:diagnostics` جدا | تست‌های شبکه آرام و قابل‌قطع، مستقل از تونل |
| `:core` بدون اندروید | تست سریع JVM برای مدل و نرمال‌سازی |

### جریان اتصال (توالی)

```
UI: Connect
 └─> ConnectionManager.connect(serverId)
      1. بارگذاری پیکربندی از SecureConfigStore (رمزگشایی با Keystore)
      2. ConfigNormalizer: اعتبارسنجی + حذف IPv6 در صورت نیاز (F1) + DNS اجباری (F6)
      3. VpnService.prepare() → اگر Intent برگشت، UI مجوز بگیرد
      4. startForeground(TunnelForegroundService)   ← پیش از کتابخانه (F2)
      5. controller.up(config)  →  GoBackend.setState(UP)
      6. انتظار برای اثبات: state==UP && handshake تازه (F3)
      7. اعلام CONNECTED یا CONNECTED_DEGRADED یا ERROR
```

---

## ۴. الزامات سازگاری

| مورد | انتخاب | دلیل |
| --- | --- | --- |
| `minSdk` | **۲۶ (Android 8.0)** | NotificationChannel + FGS + always-on پایدار + Keystore |
| `targetSdk` | **۳۵ (Android 15)** | FGS type الزامی است؛ از رفتارهای تأییدنشدهٔ ۳۶ پرهیز می‌کنیم |
| `compileSdk` | **۳۶** | هم‌راستا با کتابخانهٔ WireGuard (compileSdk 36) |
| Java/Kotlin | Java 17 + Kotlin 2.x | نیاز AGP 8.13 و desugaring |
| ABI | `arm64-v8a`, `armeabi-v7a`, `x86_64` | pkgهای WireGuard در Android 15+ الزام 16KB page دارند |
| 16KB page | `ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON` | الزام Android 15 برای کتابخانهٔ native |
| Theme | Material 3، دینامیک خاموش | پیش‌بینی‌پذیری بصری |

### رفتارهای سیستمی که باید مدیریت شوند

| رفتار | مدیریت |
| --- | --- |
| Doze / App Standby | FGS + راهنمای always-on + تشخیص handshake کهنه |
| Battery optimization | راهنما + `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` فقط با رضایت صریح (نه خودکار) |
| `onRevoke()` (کاربر VPN را از Settings قطع کرد) | خاموش‌کردن فوری، به‌روزرسانی وضعیت، بدون تلاش بازگشت بی‌صدا |
| Always-on + lockdown | تشخیص با flag در Intent (`extras`)، غیرفعال‌کردن دکمهٔ قطع |
| تغییر Wi-Fi ↔ داده | `NetworkMonitor` + بازسازی تونل با backoff |
| راه‌اندازی مجدد دستگاه | `BOOT_COMPLETED` فقط اگر always-on یا گزینهٔ «بازیابی» روشن باشد |
| چند پروفایل کاری | خارج از دامنه (یک تونل در هر پروفایل، توسط سیستم) |

---

## ۵. ملاحظات امنیتی

### ۵٫۱ مدیریت کلید

| مرحله | طرح |
| --- | --- |
| تولید کلید | `Curve25519` از کتابخانهٔ WireGuard (تنها منبع مجاز) |
| ذخیره | Android Keystore: کلید AES-256-GCM با `setUserAuthenticationRequired(false)`، `StrongBox` در صورت وجود |
| رمزنگاری | پیکربندی کامل (شامل `PrivateKey`) با AES-GCM رمز می‌شود؛ IV تصادفی per-write |
| ورود دستی | کاربر فایل `.conf` را import می‌کند → اعتبارسنجی → فوراً رمزنگاری → حذف منبع خام |
| نمایش | کلید خصوصی **هرگز** در UI، لاگ، گزارش خطا، یا backup ظاهر نمی‌شود |
| حذف | حذف سرور = حذف ciphertext + کلید داده (به‌علاوهٔ `keys` مستقل) |

**ممنوع مطلق:** کلید در `BuildConfig`، در `strings.xml`، در فایل نمونه، در
تست، یا در `README`.

### ۵٫۲ نشتی IPv6 (پاسخ به F1)

سه لایه:

1. **لایهٔ مدل:** `ConfigNormalizer` اگر `::/0` نیست، آدرس IPv6 را حذف می‌کند
   و `ipv6Policy = EXCLUDED` را در مدل تنظیم می‌کند.
2. **لایهٔ پلن:** `VpnPlan` (ساختار قابل‌بازرسی) پیش از فراخوانی کتابخانه
   ساخته و در تست بررسی می‌شود.
3. **لایهٔ تشخیص:** `LeakInspector` پس از اتصال بررسی می‌کند آیا نشانی IPv6
   عمومی از مسیر غیرتونل قابل دستیابی است (فقط با رضایت کاربر و فقط مقصد
   پیکربندی‌شده — §۶).

### ۵٫۳ نشتی DNS

| خطر | کنترل |
| --- | --- |
| DNS اپراتور وقتی سرور DNS ندارد | رد پیکربندی (fail-closed) |
| DNS اپراتور وقتی کاربر «DNS خودکار» انتخاب کرده | گزینه وجود ندارد؛ همیشه DNS صریح از پیکربندی یا DNS سفارشی کاربر |
| DNS در حالت `CONNECTED_DEGRADED` | هشدار صریح در UI + پیشنهاد قطع |

### ۵٫۴ Fail-closed

| سناریو | رفتار پیش‌فرض |
| --- | --- |
| خطای ساخت تونل | ترافیک از تونل خارج نمی‌شود چون تونل ساخته نشده؛ UI = `ERROR` |
| قطع تونل ناخواسته | تلاش بازگشت با backoff؛ اگر ناموفق، `ERROR` + پیشنهاد always-on |
| از دست رفتن مجوز VPN | قطع فوری + `ERROR.PermissionRevoked` |
| پیکربندی نامعتبر | اتصال هرگز شروع نمی‌شود |

**توجه صادقانه:** «fail-closed» در سطح اپلیکیشن به‌معنای مسدودسازی کامل
ترافیک بدون همکاری سیستم نیست. مسدودسازی واقعی «بدون VPN هیچ چیز نرود» توسط
**Lockdown** در تنظیمات اندروید انجام می‌شود؛ ما فقط راهنمایی می‌کنیم (§۹).

### ۵٫۵ لاگ ایمن

- `SafeLogger` با یک `Redactor` که الگوهای `PrivateKey`, `PresharedKey`,
  `BEGIN … PRIVATE KEY`, توکن‌های شبیه کلید را حذف می‌کند.
- در release: سطح `WARN`؛ لاگ دیباگ فقط با build type `debug`.
- لاگ‌ها هرگز به شبکه نمی‌روند.

### ۵٫۶ سطح حملهٔ خودمان

| سطح | کنترل |
| --- | --- |
| `android:exported` | همهٔ اجزای ما `false` جز Activity اصلی |
| Intent از بیرون | `TunnelForegroundService` با action امضاشده و `exported=false` |
| فایل `.conf` واردشده | محدودیت حجم (۲۵۶ KB)، پارس با کتابخانه، رد فیلدهای ناشناس |
| Backup | `allowBackup=false` + `dataExtractionRules` که کلید را حذف می‌کند |
| Screenshot | روی صفحهٔ نمایش کلید، `FLAG_SECURE` |

---

## ۶. ماژول تشخیص شبکه (طرح)

اصل: **آرام، محدود، و با رضایت.** هیچ اسکن پورت، هیچ ارسال به دامنهٔ ثالث.

| آزمون | روش | حداکثر ترافیک |
| --- | --- | --- |
| DNS | `InetAddress.getAllByName` روی دامنهٔ خودِ endpoint | ۱ پرس‌وجو، هر ۳۰ ثانیه |
| دسترسی‌پذیری TCP | `Socket.connect(endpoint, 3s)` | ۱ اتصال |
| UDP/Handshake | از `Statistics.latestHandshake` کتابخانه | صفر ترافیک اضافی |
| تأخیر | RTT اتصال TCP + handshake | همان بالا |
| از دست رفتن بسته | نسبت retransmit قابل‌اندازه‌گیری نیست؛ **ادعا نمی‌کنیم**؛ به‌جای آن «نرخ handshake کهنه» گزارش می‌شود | — |
| IPv4/IPv6 | آزمون اتصال به endpoint نسخهٔ متناظر | ۱ اتصال هر خانواده |
| نشتی | بررسی مسیر خروج از طریق `ConnectivityManager` و `ip`-less API | بدون ترافیک اضافی |

### تفکیک سه‌سطحی علت

```kotlin
sealed interface Diagnosis {
    data class Confirmed(val failure: FailureKind, val evidence: List<Evidence>) : Diagnosis
    data class Probable(val hypothesis: String, val confidence: Confidence,
                        val evidence: List<Evidence>) : Diagnosis
    data class Unknown(val observedSymptoms: List<Evidence>) : Diagnosis
}
```

**قاعدهٔ سخت:** هیچ‌گاه `Confirmed(FilteringDetected)` تولید نمی‌شود. حداکثر
`Probable("الگوی رفتاری با قطع شبکه در لایهٔ … سازگار است")` با ذکر شاهد.
این همان چیزی است که در متن پرامپت «ادعای شناسایی مکانیزم فیلترینگ بدون شاهد»
خوانده شده و ما آن را ممنوع می‌کنیم.

---

## ۷. ارزیابی وابستگی‌ها و مجوزها

| وابستگی | نسخهٔ هدف | مجوز | لازم؟ | یادداشت |
| --- | --- | --- | --- | --- |
| `com.wireguard.android:tunnel` | `1.0.20260315` (هم‌راستا با upstream) | **Apache-2.0** (POM و `COPYING`) | بله | تنها منبع مجاز رمزنگاری و تونل |
| Kotlin stdlib / coroutines | 2.x / 1.10.x | Apache-2.0 | بله | |
| Compose BOM + Material3 | BOM 2025.x | Apache-2.0 | بله | UI |
| androidx core-ktx / activity-compose / lifecycle | آخرین پایدار | Apache-2.0 | بله | |
| DataStore (Preferences) | 1.1.x | Apache-2.0 | بله | تنظیمات (Room لازم نیست) |
| androidx.security-crypto | **استفاده نمی‌شود** | Apache-2.0 | نه | منسوخ؛ Keystore مستقیم + AES-GCM |
| JUnit4 / Kotlin-test | 4.13.2 | EPL-1.0 | بله (test) | |
| Truth + Turbine | پایدار | Apache-2.0 | بله (test) | |
| Robolectric | 4.x | MIT | اختیاری | تست لایهٔ اندرویدی بدون دستگاه |
| MockK | 1.13.x | Apache-2.0 | بله (test) | |
| AGP / Gradle | 8.13.x / 8.13 | Apache-2.0 | بله | |

### یادداشت مجوز مهم (صادقانه)

`wireguard-tools` بالادست تحت **GPLv2** است و کتابخانهٔ اندروید فایل
`libwg.so` را از آن بیلد می‌کند. خودِ پروژهٔ WireGuard نسخهٔ قابل‌جاسازی
اندروید را با POM **Apache-2.0** منتشر می‌کند. برای **استفادهٔ شخصی** این
تفاوت عملی ندارد؛ برای **توزیع عمومی** باید حقوقدان بررسی کند. این مورد در
`SECURITY-REPORT.md` و `KNOWN-LIMITATIONS.md` ثبت می‌شود.

**هیچ وابستگی GPL دیگری** در پروژه نیست. هیچ SDK تبلیغاتی/آنالیتیکس وجود ندارد.

---

## ۸. ارزیابی OpenVPN (آینده)

| معیار | وضعیت |
| --- | --- |
| کتابخانهٔ رسمی اندروید | `ics-openvpn` (GPLv2) — **ناسازگار با توزیع بسته** و سنگین |
| کتابخانهٔ جایگزین | `openvpn3` (AGPL) — همان مشکل |
| نتیجه | برای ۱٫۰ **نه**. اگر لازم شد، به‌صورت ماژول جدا و با بازبینی حقوقی، شاید به شکل «اپ جدا» |
| جایگزین عملی | WireGuard با پیکربندی‌های متفاوت (UDP/TCP-like از طریق سرور شخصی) |

**کلام آخر:** افزودن پروتکل دوم یک تصمیم حقوقی است، نه فقط فنی.

---

## ۹. ریسک‌های پیاده‌سازی

| # | ریسک | اثر | احتمال | کاهش |
| --- | --- | --- | --- | --- |
| R1 | FGS با `specialUse` روی دستگاه واقعی پذیرفته نشود | FGS شروع نشود | کم | تست T-24؛ `specialUse` پیش‌نیاز زمان‌اجرایی ندارد؛ راهنمای always-on |
| R2 | Doze ترافیک را متوقف کند ولی state=UP | **ادعای دروغین اتصال** | زیاد در ایران | تشخیص handshake کهنه → `CONNECTED_DEGRADED` |
| R3 | نشتی IPv6 به‌خاطر F1 | نقض حریم خصوصی | زیاد اگر نادیده گرفته شود | سه لایهٔ §۵٫۲ + تست رگرسیون |
| R4 | نبود JDK/SDK/Maven در محیط ساخت | نتوانم APK بسازم | **قطعیت** | CI با GitHub Actions؛ APK فقط از artifact سبز |
| R5 | ناسازگاری نسخهٔ artifact با upstream | بیلد شکست | متوسط | CI اولین چیزی که resolve می‌کند؛ نسخه در `libs.versions.toml` یک‌جا |
| R6 | قطع مکرر روی شبکهٔ موبایل ایران | تجربهٔ بد | زیاد | backoff نمایی + سقف + پیام روشن؛ **بدون حلقهٔ بی‌نهایت** |
| R7 | کاربر پیکربندی نادرست وارد کند | خطای مبهم | زیاد | اعتبارسنجی مرحله‌به‌مرحله با پیام فارسی دقیق |
| R8 | افشای کلید در backup ابری | نقض بحرانی | کم | `allowBackup=false` + قواعد استخراج |
| R9 | ۱۶KB page alignment روی Android 15 | کرش نصب | متوسط | flag کتابخانه + بررسی ABI در CI |
| R10 | محدودیت ۳ چرخهٔ بازگشت | گیر کردن کاربر | کم | پیام «اتصال ناموفق» + دکمهٔ تلاش دستی |

---

## ۱۰. محدودیت محیط ساخت (صادقانه و قطعی)

بررسی محیط این اجرا:

```
java, javac, kotlin, gradle, adb, aapt2, sdkmanager : موجود نیست
ANDROID_HOME / ANDROID_SDK_ROOT                     : تعریف نشده
dl.google.com (Maven AndroidX/AGP)                  : غیرقابل دسترسی (000)
repo1.maven.org (Maven Central)                     : غیرقابل دسترسی (000)
services.gradle.org                                 : غیرقابل دسترسی (000)
github.com / api.github.com                         : در دسترس
```

**پیامدها:**

1. کامپایل محلی **ممکن نیست**. بیلد و تست فقط با GitHub Actions (که به
   Maven/Google دسترسی دارد) قابل اجراست.
2. هر گزارشی از «بیلد موفق» یا «تست سبز» باید به یک اجرای CI با شناسهٔ مشخص
   ارجاع دهد. بدون آن، ادعا **باطل** است.
3. APK از artifact همان اجرا استخراج می‌شود؛ هرگز از محیط محلی ادعا نمی‌شود.
4. طراحی به‌گونه‌ای است که بیلد **قابل بازتولید** باشد: نسخه‌های pin‌شده در
   `gradle/libs.versions.toml`، بدون dependency پویا، بدون `+`.

---

## ۱۱. تصمیم‌های معماری (خلاصه، قابل بازبینی)

| ADR | تصمیم | جایگزین ردشده |
| --- | --- | --- |
| ADR-001 | کتابخانهٔ رسمی WireGuard، بدون لمس رمزنگاری | پیاده‌سازی userspace مستقل |
| ADR-002 | `VpnService` + یک FGS اختصاصی با `specialUse` | تکیه بر کتابخانه (F2) |
| ADR-003 | `CONNECTED` فقط با اثبات سه‌گانه (F3) | اعتماد به `getState()` |
| ADR-004 | حذف IPv6 در سطح پیکربندی | اتکا به `allowFamily` (F1) |
| ADR-005 | Keystore + AES-GCM دستی | `security-crypto` (منسوخ) |
| ADR-006 | DataStore، بدون Room | Room (نیاز واقعی نیست) |
| ADR-007 | ۵ ماژول Gradle، نه ۲۰ | ریزسرویسی‌کردن اپ موبایل |
| ADR-008 | متریک «نرخ handshake کهنه» به‌جای «packet loss» | ادعای دقت غیرقابل اثبات |

---

## ۱۲. معیار خروج از فاز ۱

- [x] مستند معماری نوشته شد.
- [x] نسخه‌ها و مجوزها فهرست شدند.
- [x] دو یافتهٔ بحرانی کد منبع (F1, F2) ثبت و به تصمیم تبدیل شدند.
- [x] محدودیت محیط ساخت صریح اعلام شد.
- [ ] **تأیید صاحب محصول** روی سه تصمیم زیر:
  - minSdk 26 / targetSdk 35
  - FGS با `specialUse`
  - حذف IPv6 در پیکربندی‌های IPv4-only (سخت‌گیرانه، ممکن است برخی سرورها را رد کند)

پس از تأیید، فاز ۲ (اسکلت اپ و رابط فارسی) بدون تغییر معماری آغاز می‌شود.

---

## ۱۳. منابع

| منبع | کاربرد |
| --- | --- |
| `developer.android.com/develop/connectivity/vpn` | چرخهٔ `prepare`/`establish`/`onRevoke`، always-on، per-app |
| `developer.android.com/reference/android/net/VpnService.Builder` | `addAddress`, `addRoute`, `allowFamily`, `setUnderlyingNetworks`, `setBlocking` |
| `developer.android.com/develop/background-work/services/fgs/service-types` | الزام type از API 34؛ `systemExempted` شرط‌دار و می‌تواند در زمان اجرا رد شود؛ `specialUse` بدون پیش‌نیاز |
| `github.com/WireGuard/wireguard-android` @ `1.0.20260315` | کد منبع `GoBackend`, `Tunnel`, `Config`, `Statistics` |
| `wireguard.com/embedding/` | روش رسمی جاسازی: `com.wireguard.android:tunnel` |
| README رسمی wireguard-android | الزام desugaring و `com.wireguard.android:tunnel:VERSION` |

</div>
