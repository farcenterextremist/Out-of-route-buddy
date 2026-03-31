package com.example.outofroutebuddy.presentation.ui.rankings

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.databinding.FragmentRankingsBinding
import com.example.outofroutebuddy.databinding.ItemRankingBreakdownRowBinding
import com.example.outofroutebuddy.domain.models.FleetLeaderboard
import com.example.outofroutebuddy.domain.models.RankingBreakdown
import com.example.outofroutebuddy.domain.models.RankingCohort
import com.example.outofroutebuddy.domain.models.RankingTier
import com.example.outofroutebuddy.presentation.viewmodel.RankingsUiState
import com.example.outofroutebuddy.presentation.viewmodel.RankingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RankingsFragment : Fragment() {

    private var _binding: FragmentRankingsBinding? = null
    private val binding: FragmentRankingsBinding
        get() = _binding ?: throw IllegalStateException(
            "Binding accessed before onCreateView or after onDestroyView",
        )

    private val viewModel: RankingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRankingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rankingsBackButton.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is RankingsUiState.Loading -> showLoading()
                    is RankingsUiState.Success -> showRankings(state.leaderboard)
                    is RankingsUiState.Error -> showError(state.message)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoading() {
        binding.rankingsLoading.visibility = View.VISIBLE
        binding.rankingsError.visibility = View.GONE
        binding.cardScoreHero.visibility = View.GONE
        binding.cardBreakdown.visibility = View.GONE
        binding.cardProgression.visibility = View.GONE
        binding.cardFleetInfo.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.rankingsLoading.visibility = View.GONE
        binding.rankingsError.visibility = View.VISIBLE
        binding.rankingsError.text = message
        binding.cardScoreHero.visibility = View.GONE
        binding.cardBreakdown.visibility = View.GONE
        binding.cardProgression.visibility = View.GONE
        binding.cardFleetInfo.visibility = View.GONE
    }

    private fun showRankings(leaderboard: FleetLeaderboard) {
        binding.rankingsLoading.visibility = View.GONE
        binding.rankingsError.visibility = View.GONE

        val userRanking = leaderboard.userRanking
        if (userRanking == null) {
            showError(getString(R.string.ranking_no_trips))
            return
        }

        bindScoreHero(leaderboard)
        bindBreakdown(userRanking.breakdown)
        bindProgression(leaderboard)
        bindFleetInfo(leaderboard)
    }

    private fun bindScoreHero(leaderboard: FleetLeaderboard) {
        val userRanking = leaderboard.userRanking ?: return
        binding.cardScoreHero.visibility = View.VISIBLE

        binding.cohortBadge.text = getString(
            R.string.ranking_cohort_badge_format,
            leaderboard.cohort.displayName.uppercase(),
            formatCohortRange(leaderboard.cohort),
        )

        binding.scoreNumber.text = userRanking.overallScore.toInt().toString()
        applyTierBadge(binding.tierBadge, userRanking.tier)
        binding.rankNumber.text = "#${userRanking.rank}"
        binding.rankTotal.text = getString(R.string.ranking_of_drivers, leaderboard.cohortSize)
    }

    private fun applyTierBadge(view: TextView, tier: RankingTier) {
        val (bgColor, textColor) = when (tier) {
            RankingTier.DIAMOND -> R.color.tier_diamond_bg to R.color.tier_diamond_text
            RankingTier.PLATINUM -> R.color.tier_platinum_bg to R.color.tier_platinum_text
            RankingTier.GOLD -> R.color.tier_gold_bg to R.color.tier_gold_text
            RankingTier.SILVER -> R.color.tier_silver_bg to R.color.tier_silver_text
            RankingTier.BRONZE -> R.color.tier_bronze_bg to R.color.tier_bronze_text
        }

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f * resources.displayMetrics.density
            setColor(ContextCompat.getColor(requireContext(), bgColor))
        }
        view.background = drawable
        view.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        view.text = "\u2666 ${tier.displayName}"
    }

    private fun bindBreakdown(breakdown: RankingBreakdown) {
        binding.cardBreakdown.visibility = View.VISIBLE

        bindBreakdownRow(
            ItemRankingBreakdownRowBinding.bind(binding.breakdownOor.root),
            getString(R.string.ranking_oor_efficiency),
            breakdown.oorEfficiencyScore,
            "40%",
        )
        bindBreakdownRow(
            ItemRankingBreakdownRowBinding.bind(binding.breakdownConsistency.root),
            getString(R.string.ranking_consistency),
            breakdown.consistencyScore,
            "25%",
        )
        bindBreakdownRow(
            ItemRankingBreakdownRowBinding.bind(binding.breakdownVolume.root),
            getString(R.string.ranking_trip_volume),
            breakdown.tripVolumeScore,
            "15%",
        )
        bindBreakdownRow(
            ItemRankingBreakdownRowBinding.bind(binding.breakdownGps.root),
            getString(R.string.ranking_gps_quality),
            breakdown.gpsQualityScore,
            "10%",
        )
        bindBreakdownRow(
            ItemRankingBreakdownRowBinding.bind(binding.breakdownDiscipline.root),
            getString(R.string.ranking_route_discipline),
            breakdown.routeDisciplineScore,
            "10%",
        )
    }

    private fun bindBreakdownRow(
        row: ItemRankingBreakdownRowBinding,
        label: String,
        score: Double,
        weight: String,
    ) {
        row.breakdownLabel.text = label
        row.breakdownValue.text = score.toInt().toString()
        row.breakdownWeight.text = weight

        val fill = row.breakdownFill
        fill.post {
            val parent = fill.parent as View
            val targetWidth = (parent.width * score / 100.0).toInt()
            val lp = fill.layoutParams
            lp.width = targetWidth
            fill.layoutParams = lp
        }
    }

    private fun bindProgression(leaderboard: FleetLeaderboard) {
        val nextCohort = leaderboard.nextCohort
        if (nextCohort == null) {
            binding.cardProgression.visibility = View.GONE
            return
        }
        binding.cardProgression.visibility = View.VISIBLE

        binding.progressionTarget.text =
            "${nextCohort.displayName} \u00B7 ${nextCohort.subtitle}"

        val currentTrips = leaderboard.userRanking?.totalTrips ?: 0
        val targetTrips = leaderboard.cohort.tripRange.last
        val remaining = leaderboard.tripsUntilNextCohort
        val progress = if (targetTrips > 0) {
            (currentTrips.toFloat() / targetTrips * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        binding.progressionBar.progress = progress
        binding.progressionTrips.text = getString(
            R.string.ranking_trips_progress,
            currentTrips,
            targetTrips,
            remaining,
        )
    }

    private fun bindFleetInfo(leaderboard: FleetLeaderboard) {
        binding.cardFleetInfo.visibility = View.VISIBLE
        binding.fleetCountLabel.text = getString(
            R.string.ranking_fleet_count,
            leaderboard.cohortSize,
        )
    }

    private fun formatCohortRange(cohort: RankingCohort): String {
        return if (cohort == RankingCohort.LEGEND) {
            "${cohort.tripRange.first}+ trips"
        } else {
            "${cohort.tripRange.first}-${cohort.tripRange.last} trips"
        }
    }
}
