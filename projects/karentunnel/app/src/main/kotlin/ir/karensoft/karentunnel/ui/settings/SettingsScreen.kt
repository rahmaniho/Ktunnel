package ir.karensoft.karentunnel.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.karensoft.karentunnel.ui.common.KeyValueRow
import ir.karensoft.karentunnel.ui.common.NoticeCard
import ir.karensoft.karentunnel.ui.common.NoticeTone
import ir.karensoft.karentunnel.ui.common.ScreenScaffold
import ir.karensoft.karentunnel.ui.common.SectionCard

/**
 * Settings.
 *
 * Two rules shape this screen:
 *  * every switch does something the user can verify, so no placeholder toggles;
 *  * the copy explains *why* a default is what it is, because a VPN whose
 *    behaviour is unexplained trains people to trust it blindly.
 */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onReconnectOnNetworkChange: (Boolean) -> Unit,
    onRestoreOnBoot: (Boolean) -> Unit,
    onPreferAlwaysOn: (Boolean) -> Unit,
    onConfirmDestructive: (Boolean) -> Unit,
    onRequestWipe: () -> Unit,
    onConfirmWipe: () -> Unit,
    onCancelWipe: () -> Unit,
    onDismissWipeResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "تنظیمات", onBack = onBack) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.wipeResult?.let { message ->
                NoticeCard("نتیجهٔ پاک‌سازی", message, NoticeTone.Warning)
                TextButton(onClick = onDismissWipeResult) { Text("بستن") }
            }

            SectionCard("رفتار اتصال") {
                SwitchRow(
                    title = "اتصال دوباره هنگام تغییر شبکه",
                    subtitle = "وقتی از Wi-Fi به دادهٔ همراه جابه‌جا می‌شوید، تونل با همان " +
                        "پیکربندی دوباره برپا می‌شود. بدون این گزینه، تونل پس از تغییر شبکه " +
                        "بی‌صدا از کار می‌افتد.",
                    checked = state.settings.reconnectOnNetworkChange,
                    onCheckedChange = onReconnectOnNetworkChange,
                )
                SwitchRow(
                    title = "بازگردانی خودکار پس از روشن‌شدن دستگاه",
                    subtitle = "فقط اگر آخرین وضعیت «متصل» بوده باشد. تونل بدون اطلاع شما " +
                        "برپا نمی‌شود.",
                    checked = state.settings.restoreOnBoot,
                    onCheckedChange = onRestoreOnBoot,
                )
                SwitchRow(
                    title = "ترجیح حالت always-on",
                    subtitle = "این گزینه فقط راهنمای نمایشی است؛ ثبت always-on در تنظیمات " +
                        "اندروید انجام می‌شود و برنامه نمی‌تواند آن را برای شما روشن کند.",
                    checked = state.settings.preferAlwaysOn,
                    onCheckedChange = onPreferAlwaysOn,
                )
            }

            SectionCard("ایمنی و داده") {
                SwitchRow(
                    title = "تأیید پیش از کارهای بازگشت‌ناپذیر",
                    subtitle = "حذف سرور کلید خصوصی را نابود می‌کند و قابل بازگردانی نیست.",
                    checked = state.settings.confirmDestructiveActions,
                    onCheckedChange = onConfirmDestructive,
                )
            }

            SectionCard("درباره") {
                KeyValueRow("نسخه", "۱٫۰ (دیباگ)")
                KeyValueRow("هستهٔ تونل", "WireGuard — کتابخانهٔ رسمی", showDivider = false)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "کلید خصوصی روی همین دستگاه ساخته یا وارد می‌شود، با Android " +
                        "Keystore رمزنگاری می‌شود و هرگز از دستگاه خارج نمی‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(12.dp))
            }

            SectionCard("پاک‌سازی") {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "همهٔ سرورها، کلیدهای رمزنگاری‌شده و انتخاب‌ها حذف می‌شوند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onRequestWipe) {
                        Text(
                            "پاک‌کردن همهٔ داده‌ها",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    if (state.confirmingWipe) {
        AlertDialog(
            onDismissRequest = onCancelWipe,
            title = { Text("همهٔ داده‌ها پاک شود؟") },
            text = {
                Text(
                    "تمام سرورها و کلیدهای خصوصی ذخیره‌شده حذف می‌شوند. این کار " +
                        "برگشت‌پذیر نیست و اگر نسخهٔ پشتیبان از فایل .conf ندارید، " +
                        "باید دوباره از ارائه‌دهندهٔ خود بگیرید.",
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmWipe) {
                    Text("پاک کن", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = onCancelWipe) { Text("انصراف") } },
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
