package com.hughdevelopment.player.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.SovereignRank
import com.hughdevelopment.player.hermes.HermesVoiceManager

/**
 * RankPromotionVoiceDispatcher // Sovereign Rank Ascension Speech Triggers (R5).
 * Observes real-time rank progression and dispatches spoken celebratory announcements
 * via HermesVoiceManager when a user ascends into higher Sovereign Rank tiers.
 */
object RankPromotionVoiceDispatcher {

    private var lastRecordedRankLevel: Int = -1

    /**
     * Checks if current rank tier represents an ascension over previously recorded rank.
     * Dispatches TTS celebration if rank ascended and voice is enabled.
     */
    fun checkAndAnnouncePromotion(
        rank: SovereignRank,
        context: Context? = null
    ): Boolean {
        if (lastRecordedRankLevel == -1) {
            // First observation baseline on launch; establish baseline without celebrating
            lastRecordedRankLevel = rank.level
            return false
        }

        if (rank.level > lastRecordedRankLevel) {
            lastRecordedRankLevel = rank.level
            try {
                HermesVoiceManager.getInstance(context).speakRankPromotion(rank)
            } catch (_: Exception) {}
            return true
        }

        return false
    }

    /**
     * Checks if new level is an ascension over previous level.
     */
    fun isAscension(previousLevel: Int, newLevel: Int): Boolean {
        return newLevel > previousLevel && previousLevel > 0
    }

    /**
     * Resets the tracker baseline (useful for unit tests or account switches).
     */
    fun resetBaseline(level: Int = -1) {
        lastRecordedRankLevel = level
    }

    /**
     * Gets the currently recorded rank level.
     */
    fun getLastRecordedRankLevel(): Int = lastRecordedRankLevel
}

/**
 * Composable side-effect observing ListeningOverview rankTier and triggering Hermes Voice celebration.
 */
@Composable
fun RankPromotionVoiceEffect(overview: ListeningOverview) {
    val context = LocalContext.current
    LaunchedEffect(overview.rankTier.level) {
        RankPromotionVoiceDispatcher.checkAndAnnouncePromotion(overview.rankTier, context)
    }
}
