<div dir="rtl">

# بیلد و امضا

## پیش‌نیازها

| ابزار | نسخه |
| --- | --- |
| JDK | ۱۷ |
| Android SDK | ۳۶ (compileSdk)، build-tools ۳۵٫۰٫۰ |
| Gradle | از طریق wrapper |

## دستورها

```bash
cd projects/karentunnel
./gradlew testDebugUnitTest     # ۶۸ تست
./gradlew assembleDebug         # APK دیباگ
./gradlew assembleRelease       # APK release (نیازمند امضا)
./gradlew lintDebug             # lint
```

## امضای release

بیلد release **هیچ کلیدی در خود ندارد**. کلید از یک فایل خوانده می‌شود که
کامیت نمی‌شود:

`projects/karentunnel/keystore.properties`

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

ساخت کلید:

```bash
keytool -genkeypair -v \
  -keystore ~/keys/karentunnel-release.jks \
  -alias karentunnel \
  -keyalg RSA -keysize 4096 -validity 10000
```

**اگر این فایل نباشد، `assembleRelease` یک APK بدون امضا می‌سازد.** عمداً هیچ
بازگشتی به کلید debug وجود ندارد: یک artifact که با کلید debug امضا شده باشد
«قابل توزیع» به نظر می‌رسد در حالی که هیچ ارزش اعتمادی ندارد.

`keystore.properties`، `*.jks` و `*.keystore` در `.gitignore` هستند.

## امضای CI

job `release-apk` یک کلید موقت تولید می‌کند تا کل خط لوله (R8، shrink، امضا)
آزموده شود. artifact آن `karentunnel-release-apk-ci-signed` نام دارد و **برای
توزیع مناسب نیست**.

## چرا بیلد محلی اینجا ممکن نیست

این پروژه در محیطی توسعه داده شد که JDK و Android SDK ندارد و به
`dl.google.com` / `maven.google.com` دسترسی ندارد. بیلد فقط در CI اجرا شد.
هر نتیجهٔ بیلد در این مستندات از یک شمارهٔ اجرای واقعی می‌آید.

</div>
