<div dir="rtl">

# راهنمای ساخت و نصب

## پیش‌نیازها

| مورد | نسخه | یادداشت |
| --- | --- | --- |
| JDK | ۱۷ | AGP 8.13 نیاز دارد |
| Android SDK | Platform 36 + Build-Tools | `ANDROID_HOME` را تنظیم کنید |
| Gradle | ۸٫۱۳ (wrapper) | نیازی به نصب دستی نیست |
| دستگاه/شبیه‌ساز | Android 8.0+ (API 26) | برای تست واقعی تونل، دستگاه فیزیکی لازم است |

## ساخت محلی

```bash
cd projects/karentunnel

# ۱. مسیر SDK را مشخص کنید (این فایل در گیت نیست)
cp local.properties.example local.properties
# sdk.dir=/path/to/Android/sdk  را ویرایش کنید

# ۲. تست‌های واحد (بدون شبکه، سریع)
./gradlew testDebugUnitTest

# ۳. ساخت APK دیباگ
./gradlew assembleDebug
# خروجی: app/build/outputs/apk/debug/app-debug.apk

# ۴. نصب روی دستگاه
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## ساخت Release

کلید امضا **هرگز** در مخزن نیست. یا از `~/.gradle/gradle.properties` استفاده
کنید یا در CI از Secrets:

```properties
# ~/.gradle/gradle.properties  (خارج از مخزن)
KARENTUNNEL_STORE_FILE=/absolute/path/to/release.jks
KARENTUNNEL_STORE_PASSWORD=***
KARENTUNNEL_KEY_ALIAS=karentunnel
KARENTUNNEL_KEY_PASSWORD=***
```

```bash
./gradlew assembleRelease    # پس از تنظیم signingConfig در app/build.gradle.kts
```

> **وضعیت فعلی:** بلوک `signingConfigs` عمداً در `app/build.gradle.kts` نیست،
> چون بدون کلید واقعی نمی‌توان آن را تست کرد و یک بلوک اشتباه، بیلد را می‌شکند.
> برای انتشار، بلوک زیر را با مقادیر بالا اضافه کنید:
>
> ```kotlin
> signingConfigs {
>     create("release") {
>         storeFile = file(System.getProperty("KARENTUNNEL_STORE_FILE")!!)
>         storePassword = System.getProperty("KARENTUNNEL_STORE_PASSWORD")
>         keyAlias = System.getProperty("KARENTUNNEL_KEY_ALIAS")
>         keyPassword = System.getProperty("KARENTUNNEL_KEY_PASSWORD")
>     }
> }
> ```

## ساخت در محیط بدون SDK (این محیط)

```bash
java, javac, gradle, adb, sdkmanager : موجود نیست
dl.google.com / repo1.maven.org      : غیرقابل دسترس
```

بنابراین ساخت محلی در این sandbox ممکن نیست. **منبع حقیقت، CI است:**

| منبع | مسیر |
| --- | --- |
| گردش کار | `.github/workflows/android-ci.yml` |
| خروجی‌ها | تب Actions → artifact `karentunnel-debug-apk` و `reports` |

CI این مراحل را اجرا می‌کند و هر کدام یک دروازه است:

1. `testDebugUnitTest` — همهٔ ماژول‌ها
2. `assembleDebug` — بیلد واقعی
3. `lintDebug` — بازدارنده
4. انتشار APK و گزارش‌ها به‌عنوان artifact

قاعده: **تا زمانی که یک اجرای CI با شناسهٔ مشخص سبز نشود، هیچ ادعای «ساخته
شد» یا «تست پاس شد» معتبر نیست.** شناسهٔ اجراها در `docs/TEST-PLAN.md` ثبت
می‌شود.

## بازتولید‌پذیری

| عامل | کنترل |
| --- | --- |
| نسخهٔ وابستگی‌ها | `gradle/libs.versions.toml` — همه pin شده، بدون `+` |
| Gradle wrapper | `gradle-wrapper.jar` (تأییدشده از مخزن رسمی Gradle) + `gradle-wrapper.properties` |
| JDK | temurin 17 در CI |
| SDK | `setup-android` با compileSdk 36 |
| تصویر CI | `ubuntu-latest` (تغییر آن، تاریخ بیلد را تغییر می‌دهد) |

## چرا `allWarningsAsErrors = true`

هر هشدار کامپایلر در همهٔ ماژول‌ها خطاست. دلیل: هشدارهای Kotlin معمولاً
«چیزی که فکر می‌کنید اجرا می‌شود، اجرا نمی‌شود» را نشان می‌دهند — و در کد
امنیتی، این تفاوت بین «قفل است» و «فکر می‌کنم قفل است» است.

## عیب‌یابی

| خطا | علت | راه‌حل |
| --- | --- | --- |
| `SDK location not found` | `local.properties` نیست | از فایل نمونه کپی کنید |
| `Could not resolve com.wireguard.android:tunnel` | نسخهٔ pin‌شده در Maven نیست | `libs.versions.toml` را به نسخهٔ منتشرشده تغییر دهید |
| `Unsupported class file major version` | JDK قدیمی | JDK 17 |
| `Desugaring` error | `isCoreLibraryDesugaringEnabled` خاموش | باید `true` باشد (نیاز کتابخانهٔ WireGuard) |
| `ForegroundServiceStartNotAllowedException` | type FGS پذیرفته نشد | `KNOWN-LIMITATIONS.md` §FGS |

</div>
