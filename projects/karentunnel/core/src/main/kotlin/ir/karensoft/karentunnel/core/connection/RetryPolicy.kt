package ir.karensoft.karentunnel.core.connection

import ir.karensoft.karentunnel.core.error.ConnectionError
import ir.karensoft.karentunnel.core.error.isRetryable
import kotlin.math.pow

/**
 * Exponential backoff with a hard ceiling and a hard attempt cap.
 *
 * The prompt for this project forbids infinite retry loops, so the cap is not
 * configurable upward beyond [MAX_ATTEMPTS_HARD_LIMIT]: the UI may offer
 * "retry now", which starts a *new* run, but the manager itself stops.
 *
 * [jitterFraction] spreads retries of many devices after a network outage;
 * jitter is applied with a deterministic seed in tests via [delayFor]'s
 * `random` parameter so behaviour stays reproducible.
 */
data class RetryPolicy(
    val initialDelayMillis: Long = 1_000,
    val maxDelayMillis: Long = 60_000,
    val multiplier: Double = 2.0,
    val maxAttempts: Int = 5,
    val jitterFraction: Double = 0.20,
) {
    init {
        require(initialDelayMillis > 0) { "initialDelayMillis must be > 0" }
        require(maxDelayMillis >= initialDelayMillis) { "maxDelayMillis < initialDelayMillis" }
        require(multiplier >= 1.0) { "multiplier must be >= 1" }
        require(maxAttempts in 1..MAX_ATTEMPTS_HARD_LIMIT) {
            "maxAttempts must be in 1..$MAX_ATTEMPTS_HARD_LIMIT"
        }
        require(jitterFraction in 0.0..0.5) { "jitterFraction must be in 0..0.5" }
    }

    /**
     * Delay before attempt [attempt] (1-based: the delay *after* the first
     * failure is for attempt 2).
     *
     * @param random source of jitter in [0,1); injected in tests.
     */
    fun delayFor(attempt: Int, random: Double = 0.5): Long {
        if (attempt <= 1) return 0
        val exponent = attempt - 2
        val raw = initialDelayMillis * multiplier.pow(exponent)
        val capped = raw.coerceAtMost(maxDelayMillis.toDouble())
        val jitter = 1.0 + jitterFraction * (2.0 * random.coerceIn(0.0, 1.0) - 1.0)
        return (capped * jitter).toLong().coerceIn(1, maxDelayMillis)
    }

    /** Should we try again after [failure] on [attempt]? */
    fun shouldRetry(attempt: Int, failure: ConnectionError): Boolean =
        attempt < maxAttempts && failure.isRetryable

    companion object {
        const val MAX_ATTEMPTS_HARD_LIMIT = 8

        /** Default for 1.0.0: 5 attempts, ~1s→60s, capped. */
        val DEFAULT = RetryPolicy()
    }
}
