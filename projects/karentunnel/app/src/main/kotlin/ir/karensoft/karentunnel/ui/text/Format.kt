package ir.karensoft.karentunnel.ui.text

import ir.karensoft.karentunnel.core.connection.ConnectionState
import ir.karensoft.karentunnel.core.connection.DegradedReason
import ir.karensoft.karentunnel.core.error.ConfigError
import ir.karensoft.karentunnel.core.error.ConnectionError
import java.util.Locale

/**
 * Persian formatting helpers.
 *
 * Policy (from the brief): Persian numerals for human-facing quantities
 * (duration, byte counts, latency); Latin digits for technical identifiers
 * (IP addresses, ports, public keys, versions) so they can be copied verbatim.
 */
object Fa {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun digits(input: String): String = buildString(input.length) {
        for (ch in input) append(if (ch in '0'..'9') persianDigits[ch - '0'] else ch)
    }

    fun number(value: Long): String = digits(value.toString())

    fun number(value: Int): String = number(value.toLong())

    /** "۱۲:۳۴" style duration, hours only when needed. */
    fun duration(millis: Long): String {
        if (millis < 0) return "—"
        val totalSeconds = millis / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) {
            digits("%d:%02d:%02d".format(Locale.US, h, m, s))
        } else {
            digits("%02d:%02d".format(Locale.US, m, s))
        }
    }

    /** Human byte counts with Persian digits and Latin unit suffixes (KiB/MiB). */
    fun bytes(value: Long): String {
        if (value < 0) return "—"
        val units = listOf("B", "KiB", "MiB", "GiB", "TiB")
        var size = value.toDouble()
        var unit = 0
        while (size >= 1024 && unit < units.lastIndex) {
            size /= 1024
            unit++
        }
        val rendered = if (unit == 0) size.toInt().toString() else String.format(Locale.US, "%.1f", size)
        return digits(rendered) + " " + units[unit]
    }

    fun latency(millis: Long?): String = millis?.let { "${digits(it.toString())} ms" } ?: "—"
}

/**
 * The single mapping from machine state to Persian copy.
 *
 * Keeping every string here (rather than scattered in composables) means the
 * "never claim connected" rule can be verified by reading one file — and tested.
 */
object StatusText {

    fun label(state: ConnectionState): String = when (state) {
        ConnectionState.Disconnected -> "قطع"
        ConnectionState.Preparing -> "در حال آماده‌سازی"
        ConnectionState.Connecting -> "در حال اتصال"
        is ConnectionState.Connected -> "متصل و تأییدشده"
        is ConnectionState.Degraded -> "متصل، بدون تأیید"
        is ConnectionState.Reconnecting -> "تلاش دوباره"
        ConnectionState.Disconnecting -> "در حال قطع"
        is ConnectionState.Error -> "خطا"
    }

    fun detail(state: ConnectionState): String? = when (state) {
        is ConnectionState.Connected -> "تونل برقرار است و دست‌دادن (handshake) تازه تأیید شد."
        is ConnectionState.Degraded -> when (state.reason) {
            DegradedReason.NoHandshake ->
                "تونل ساخته شد اما هنوز دست‌دادنی انجام نشده؛ ترافیک ممکن است عبور نکند."
            DegradedReason.StaleHandshake ->
                "آخرین دست‌دادن قدیمی است؛ ممکن است ترافیک متوقف شده باشد."
            DegradedReason.HealthProbeFailed ->
                "سلامت تونل تأیید نشد؛ وضعیت اتصال قابل اعتماد نیست."
        }
        is ConnectionState.Reconnecting ->
            "تلاش ${Fa.number(state.attempt)} از ${Fa.number(state.maxAttempts)} — " +
                "تلاش بعدی پس از ${Fa.duration(state.nextDelayMillis)}"
        is ConnectionState.Error -> connectionError(state.error)
        else -> null
    }

    fun connectionError(error: ConnectionError): String = when (error) {
        ConnectionError.PermissionRequired -> "مجوز VPN داده نشده است. برای اتصال، اجازه دهید."
        ConnectionError.PermissionRevoked -> "مجوز VPN توسط سیستم یا کاربر لغو شد."
        ConnectionError.NoNetwork -> "شبکه‌ای در دسترس نیست. Wi-Fi یا دادهٔ همراه را بررسی کنید."
        is ConnectionError.DnsResolutionFailed -> "نام سرور «${error.host}» قابل تبدیل نشد."
        ConnectionError.Timeout -> "زمان اتصال به سرور تمام شد."
        is ConnectionError.TunnelSetupFailed -> "ساخت تونل ناموفق بود (${error.reason})."
        is ConnectionError.BackendFailure -> "هستهٔ تونل خطا داد (${error.reason})."
        ConnectionError.HandshakeTimeout -> "دست‌دادن با سرور انجام نشد؛ اتصال تأیید نشد."
        ConnectionError.TunnelDegraded -> "اتصال ناپایدار شد و تأیید خود را از دست داد."
        is ConnectionError.Transient -> "خطای موقت رخ داد؛ تلاش دوباره ممکن است کمک کند."
    }

    /** Config errors are shown at import time, never as raw engine text. */
    fun configError(error: ConfigError): String = when (error) {
        ConfigError.MissingEndpoint -> "آدرس سرور (Endpoint) در پیکربندی نیست."
        ConfigError.MissingEndpointPort -> "پورت سرور مشخص نیست (مثال: vpn.example.com:51820)."
        ConfigError.MissingAddress -> "نشانی اینترفیس (Address) در پیکربندی نیست."
        ConfigError.MissingDns -> "سروری برای DNS تعیین نشده است؛ برای جلوگیری از نشتی DNS، پیکربندی رد شد."
        ConfigError.MissingAllowedIps -> "AllowedIPs خالی است؛ مسیرهای تونل مشخص نیست."
        ConfigError.InvalidPrivateKey -> "کلید خصوصی معتبر نیست (باید base64 استاندارد ۳۲ بایتی باشد)."
        ConfigError.InvalidPeerPublicKey -> "کلید عمومی سرور (PublicKey) معتبر نیست."
        ConfigError.InvalidPresharedKey -> "کلید مشترک (PresharedKey) معتبر نیست."
        ConfigError.KeyReuseNotAllowed -> "کلید خصوصی و کلید عمومی یکسان‌اند؛ احتمالاً بخش اشتباهی جای‌گذاری شده."
        ConfigError.Ipv6NotFullyTunneled ->
            "پیکربندی نیمه‌IPv6 است: نشانی IPv6 دارد اما مسیر ::/0 ندارد. برای جلوگیری از نشتی رد شد."
        ConfigError.Ipv6OnlyDnsUnsupported ->
            "همهٔ DNSهای تعیین‌شده IPv6 هستند و IPv6 از تونل عبور نمی‌کند."
        is ConfigError.InvalidEndpoint -> "آدرس سرور «${error.raw}» معتبر نیست."
        is ConfigError.InvalidPort -> "پورت «${Fa.digits(error.raw)}» معتبر نیست."
        is ConfigError.InvalidAddress -> "نشانی اینترفیس «${Fa.digits(error.raw)}» معتبر نیست."
        is ConfigError.InvalidDns -> "DNS «${Fa.digits(error.raw)}» معتبر نیست؛ فقط IP مجاز است."
        is ConfigError.InvalidMtu -> "MTU برابر ${Fa.number(error.value)} خارج از محدودهٔ مجاز است."
        is ConfigError.InvalidKeepalive -> "مقدار PersistentKeepalive معتبر نیست."
        is ConfigError.TooManyDnsServers -> "حداکثر ۴ سرور DNS مجاز است (${Fa.number(error.count)} داده شد)."
        is ConfigError.Malformed -> "فایل پیکربندی قابل خواندن نیست: ${error.detail}"
    }
}
