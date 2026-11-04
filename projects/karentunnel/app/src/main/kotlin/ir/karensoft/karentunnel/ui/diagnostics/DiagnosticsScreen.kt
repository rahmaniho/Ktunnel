package ir.karensoft.karentunnel.ui.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.karensoft.karentunnel.diagnostics.Confidence
import ir.karensoft.karentunnel.diagnostics.Diagnosis
import ir.karensoft.karentunnel.diagnostics.DiagnosticReport
import ir.karensoft.karentunnel.diagnostics.Evidence
import ir.karensoft.karentunnel.diagnostics.EvidenceKind
import ir.karensoft.karentunnel.diagnostics.FailureKind
import ir.karensoft.karentunnel.diagnostics.Hypothesis
import ir.karensoft.karentunnel.ui.common.KeyValueRow
import ir.karensoft.karentunnel.ui.common.NoticeCard
import ir.karensoft.karentunnel.ui.common.NoticeTone
import ir.karensoft.karentunnel.ui.common.ScreenScaffold
import ir.karensoft.karentunnel.ui.common.SectionCard

/**
 * Diagnostics — the screen where the three-tier honesty rule becomes visible.
 *
 * Every result is rendered as exactly one of:
 *  * **تأیید‌شده** — a failure we reproduced and named;
 *  * **احتمالی** — a hypothesis with an explicit confidence and its evidence;
 *  * **نامشخص** — symptoms we observed but cannot attribute.
 *
 * There is no fourth rendering, and nothing here will ever say a filtering
 * mechanism was "detected": [Diagnosis.Probable] is the strongest claim the
 * engine can make, and the copy says so in plain language.
 */
@Composable
fun DiagnosticsScreen(
    state: DiagnosticsUiState,
    onBack: () -> Unit,
    onRun: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "عیب‌یابی", onBack = onBack) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NoticeCard(
                title = "این آزمون چه چیزی را ثابت می‌کند؟",
                body = "آزمون، مسیر شبکه را از دید همین دستگاه بررسی می‌کند: تبدیل نام سرور، " +
                    "دسترسی TCP به endpoint و وضعیت تونل. اگر همه‌چیز شکست بخورد، برنامه " +
                    "حداکثر می‌تواند بگوید «نشانه‌ها با یک فرضیه سازگارند» — هرگز نمی‌تواند " +
                    "بگوید فیلترینگ را «شناسایی» کرده است، چون از داخل دستگاه راهی برای اثبات " +
                    "آن وجود ندارد.",
                tone = NoticeTone.Neutral,
            )

            SectionCard("هدف") {
                KeyValueRow("سرور", state.serverLabel ?: "انتخاب نشده", showDivider = false)
            }

            when (val s = state.state) {
                is DiagnosticUi.Idle -> {
                    Button(
                        onClick = onRun,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.serverId != null,
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("اجرای آزمون")
                    }
                    if (state.serverId == null) {
                        Text(
                            text = "برای اجرای آزمون ابتدا یک سرور انتخاب کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is DiagnosticUi.Running -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.size(12.dp))
                        Text("در حال بررسی…", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                is DiagnosticUi.Failed -> {
                    NoticeCard("آزمون اجرا نشد", s.message, NoticeTone.Danger)
                    OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                        Text("بستن")
                    }
                }

                is DiagnosticUi.Done -> {
                    DiagnosisResult(s.report)
                    OutlinedButton(onClick = onRun, modifier = Modifier.fillMaxWidth()) {
                        Text("اجرای دوباره")
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosisResult(report: DiagnosticReport) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (val d = report.diagnosis) {
            is Diagnosis.Confirmed -> NoticeCard(
                title = "تأیید‌شده",
                body = describeFailure(d.failure),
                tone = NoticeTone.Danger,
            )
            is Diagnosis.Probable -> NoticeCard(
                title = "احتمالی — ${confidenceLabel(d.confidence)}",
                body = "${describeHypothesis(d.hypothesis)}\n\nاین یک فرضیه است، نه اثبات. " +
                    "برنامه نمی‌تواند از داخل دستگاه تشخیص دهد که علت واقعی همین است.",
                tone = NoticeTone.Warning,
            )
            is Diagnosis.Unknown -> NoticeCard(
                title = "نامشخص",
                body = "نشانه‌هایی ثبت شد اما به هیچ علتی نسبت داده نشد. " +
                    "این نتیجه صادقانه‌ترین حالت است، نه شکست آزمون.",
                tone = NoticeTone.Neutral,
            )
        }

        SectionCard("شواهد") {
            if (report.evidence.isEmpty()) {
                Text(
                    "شاهدی ثبت نشد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                report.evidence.forEachIndexed { index, evidence ->
                    EvidenceRow(
                        evidence = evidence,
                        showDivider = index != report.evidence.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun EvidenceRow(evidence: Evidence, showDivider: Boolean) {
    KeyValueRow(
        label = evidenceLabel(evidence.kind),
        value = evidence.detail,
        showDivider = showDivider,
    )
}

private fun describeFailure(kind: FailureKind): String = when (kind) {
    FailureKind.DNS_RESOLUTION_FAILED -> "نام سرور قابل تبدیل به نشانی نشد."
    FailureKind.ENDPOINT_UNREACHABLE -> "به endpoint سرور دسترسی برقرار نشد."
    FailureKind.CONNECT_TIMEOUT -> "اتصال در بازهٔ مجاز برقرار نشد."
    FailureKind.HANDSHAKE_TIMEOUT -> "دست‌دادن با سرور انجام نشد."
    FailureKind.NO_NETWORK -> "دستگاه هیچ شبکه‌ای ندارد."
    FailureKind.VPN_PERMISSION_MISSING -> "مجوز VPN به این برنامه داده نشده است."
    FailureKind.CONFIG_INVALID -> "پیکربندی ذخیره‌شده معتبر نیست."
    FailureKind.TUNNEL_DOWN_WHILE_EXPECTED -> "تونل باید برپا می‌بود اما برپا نیست."
    FailureKind.IPV6_PATH_EXPOSED -> "مسیر IPv6 بیرون از تونل باز است."
    FailureKind.DNS_PATH_NOT_TUNNELED -> "درخواست‌های DNS از تونل عبور نمی‌کنند."
}

private fun describeHypothesis(h: Hypothesis): String = when (h) {
    Hypothesis.ENDPOINT_BLOCKED_OR_DOWN ->
        "ممکن است endpoint مسدود باشد یا سرور از کار افتاده باشد. این دو از داخل دستگاه " +
            "قابل تفکیک نیستند."
    Hypothesis.HANDSHAKE_INTERFERENCE ->
        "دست‌دادن آغاز شد اما پاسخ معتبری برنگشت؛ ممکن است بسته‌ها در مسیر مختل شوند."
    Hypothesis.DNS_MEDIATION_OR_FAILURE ->
        "تبدیل نام سرور شکست می‌خورد در حالی که اتصال مستقیم به IP برقرار بود؛ ممکن است DNS " +
            "دستکاری شود."
    Hypothesis.CARRIER_NAT_MAPPING_LOSS ->
        "نگاشت NAT اپراتور ممکن است ارتباط UDP را قطع کند؛ این رفتار در شبکه‌های همراه رایج است."
    Hypothesis.SERVER_CONFIGURATION_MISMATCH ->
        "کلیدها یا AllowedIPs ممکن است با تنظیمات سرور هم‌خوان نباشند."
}

private fun confidenceLabel(c: Confidence): String = when (c) {
    Confidence.LOW -> "اطمینان کم"
    Confidence.MEDIUM -> "اطمینان متوسط"
    Confidence.HIGH -> "اطمینان بالا"
}

private fun evidenceLabel(kind: EvidenceKind): String = when (kind) {
    EvidenceKind.DNS_LOOKUP -> "تبدیل نام"
    EvidenceKind.TCP_CONNECT -> "اتصال TCP"
    EvidenceKind.HANDSHAKE_AGE -> "عمر دست‌دادن"
    EvidenceKind.INTERFACE_STATE -> "وضعیت اینترفیس"
    EvidenceKind.ROUTE_STATE -> "وضعیت مسیرها"
    EvidenceKind.CONFIG_CHECK -> "بررسی پیکربندی"
    EvidenceKind.NETWORK_CAPABILITY -> "توانایی شبکه"
}
