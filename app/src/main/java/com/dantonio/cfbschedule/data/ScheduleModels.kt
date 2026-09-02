package com.dantonio.cfbschedule.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

enum class LiveStatus { SCHEDULED, IN_PROGRESS, COMPLETED }

data class ScheduledGame(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeConference: String?,
    val awayConference: String?,
    val homeRank: Int?,
    val awayRank: Int?,
    val homeLogoUrl: String?,
    val awayLogoUrl: String?,
    val network: String,
    val startTime: ZonedDateTime,
    val isTimeTBD: Boolean,
    val liveStatus: LiveStatus = LiveStatus.SCHEDULED,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val period: Int? = null,
    val clock: String? = null,
    val situation: String? = null,
    val spread: Double? = null,
    val overUnder: Double? = null
)

/** One kickoff time within a day. [time] is null for the TBD bucket, which always sorts last. */
data class TimeSlot(
    val time: LocalTime?,
    val games: List<ScheduledGame>
)

data class DaySchedule(
    val date: LocalDate,
    val timeSlots: List<TimeSlot>
)

data class WeekSchedule(
    val year: Int,
    val week: Int,
    val seasonType: String,
    val days: List<DaySchedule>
)

data class ScheduleFilter(
    val topRankOnly: Boolean = false,
    val conference: String? = null,
    val showStreamingOnly: Boolean = false
) {
    /** Whether the empty-results message should say "no games match your filters" vs. "no games this week". */
    val isActive: Boolean get() = topRankOnly || conference != null || showStreamingOnly
}

private const val TOP_RANK_THRESHOLD = 20

/** FBS conferences, ordered power conferences first, then group of five. */
val FBS_CONFERENCES = listOf(
    "SEC", "Big Ten", "ACC", "Big 12",
    "American Athletic", "Conference USA", "Mid-American", "Mountain West", "Sun Belt",
    "Pac-12", "FBS Independents"
)

/**
 * CFBD doesn't return a stable network name — the same outlet shows up as "ACCN" one week and
 * "ACC Network" the next. Route everything through [canonicalNetwork] before display, sorting,
 * or comparing against [STREAMING_ONLY_NETWORKS] so those variants aren't treated as unknowns.
 */
private val NETWORK_ALIASES = mapOf(
    "ACC Network" to "ACCN",
    "SEC Network" to "SECN",
    "SEC Network+" to "SECN+",
    "ACC Network Extra" to "ACCN+",
    "Big Ten Network" to "BTN",
    "The CW Network" to "CW",
    "USA Net" to "USA",
    "ESPN Unlmtd" to "ESPN+",
    "ESPN Unlimited" to "ESPN+"
)

fun canonicalNetwork(rawOutlet: String): String = NETWORK_ALIASES[rawOutlet] ?: rawOutlet

val NETWORK_PRIORITY = listOf(
    "ABC", "CBS", "FOX", "NBC",
    "ESPN", "ESPN2", "ESPNU", "ABC/ESPN",
    "FS1", "FS2",
    "BTN", "SECN", "ACCN", "CBSSN", "Pac-12 Network", "CBS Sports Network",
    "USA", "CW", "TNT", "truTV",
    "Peacock", "HBO Max", "Disney+",
    "ESPN+", "SECN+", "ACCN+", "MW+", "UConn+"
)

/** Networks that require a separate streaming subscription rather than a normal cable/antenna hookup. */
val STREAMING_ONLY_NETWORKS = setOf(
    "ESPN+", "SECN+", "ACCN+", "MW+", "UConn+", "Peacock", "HBO Max", "Disney+"
)

fun networkPriority(network: String): Int {
    val index = NETWORK_PRIORITY.indexOf(network)
    return if (index == -1) NETWORK_PRIORITY.size else index
}

fun WeekSchedule.applyFilter(filter: ScheduleFilter): WeekSchedule {
    val filteredDays = days.mapNotNull { day ->
        val filteredSlots = day.timeSlots.mapNotNull { slot ->
            val filteredGames = slot.games.filter { it.matchesFilter(filter) }
            filteredGames.takeIf { it.isNotEmpty() }?.let { slot.copy(games = it) }
        }
        filteredSlots.takeIf { it.isNotEmpty() }?.let { day.copy(timeSlots = filteredSlots) }
    }
    return copy(days = filteredDays)
}

private fun ScheduledGame.matchesFilter(filter: ScheduleFilter): Boolean {
    val passesRank = !filter.topRankOnly ||
        (homeRank != null && homeRank <= TOP_RANK_THRESHOLD) ||
        (awayRank != null && awayRank <= TOP_RANK_THRESHOLD)
    val passesConference = filter.conference == null ||
        homeConference == filter.conference ||
        awayConference == filter.conference
    val passesStreaming = filter.showStreamingOnly || network !in STREAMING_ONLY_NETWORKS
    return passesRank && passesConference && passesStreaming
}

/** Drops days before [today] so the default view opens on today (or the next day with games). */
fun WeekSchedule.trimToTodayOrLater(today: LocalDate = LocalDate.now()): WeekSchedule =
    copy(days = days.filter { it.date >= today })

/** FBS conferences appearing in this (unfiltered) week, for building a filter menu. */
fun WeekSchedule.availableFbsConferences(): List<String> {
    val present = days.asSequence()
        .flatMap { it.timeSlots.asSequence() }
        .flatMap { it.games.asSequence() }
        .flatMap { sequenceOf(it.homeConference, it.awayConference) }
        .filterNotNull()
        .toSet()
    return FBS_CONFERENCES.filter { it in present }
}
