package ir.karensoft.karentunnel.diagnostics

import ir.karensoft.karentunnel.core.model.Ipv6Policy
import ir.karensoft.karentunnel.core.model.ServerId
import kotlinx.serialization.Serializable

/**
 * A local, user-readable diagnostic result. It never leaves the device unless
 * the user explicitly exports it (and export is opt-in, see settings).
 */
@Serializable
data class DiagnosticReport(
    val serverId: ServerId,
    val ipv6Policy: Ipv6Policy,
    val evidence: List<Evidence>,
    val diagnosis: Diagnosis,
) {
    /** One-line summary for the dashboard's error card. */
    fun headline(): String = when (val d = diagnosis) {
        is Diagnosis.Confirmed -> "خطای قطعی: ${d.failure}"
        is Diagnosis.Probable -> "احتمال: ${d.hypothesis} (اطمینان: ${d.confidence})"
        is Diagnosis.Unknown -> "علت نامشخص؛ ${d.symptoms.size} نشانه ثبت شد"
    }
}
