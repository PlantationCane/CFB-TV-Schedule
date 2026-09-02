package com.dantonio.cfbschedule.data

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ScheduleRepository(private val api: CfbdApi = NetworkModule.api) {

    private var cachedLogosYear: Int? = null
    private var cachedLogos: Map<String, String> = emptyMap()

    /** Team logo URLs keyed by school name, fetched once per season year and reused. */
    private suspend fun getTeamLogos(year: Int): Map<String, String> {
        if (cachedLogosYear == year) return cachedLogos
        val teams = runCatching { api.getFbsTeams(year) }.getOrDefault(emptyList())
        val logos = teams.mapNotNull { team -> team.logos?.firstOrNull()?.let { team.school to it } }.toMap()
        cachedLogosYear = year
        cachedLogos = logos
        return logos
    }

    /** Finds the week containing "now" (or the nearest upcoming one) and returns its schedule. */
    suspend fun getCurrentWeekSchedule(): WeekSchedule {
        val now = ZonedDateTime.now()
        val seasonYear = if (now.monthValue >= 7) now.year else now.year - 1
        val calendar = api.getCalendar(seasonYear)
        val nowInstant = now.toInstant()

        val currentWeek = calendar.firstOrNull { week ->
            val start = Instant.parse(week.startDate)
            val end = Instant.parse(week.endDate)
            !nowInstant.isBefore(start) && nowInstant.isBefore(end)
        } ?: calendar
            .filter { Instant.parse(it.startDate).isAfter(nowInstant) }
            .minByOrNull { Instant.parse(it.startDate) }
            ?: calendar.maxByOrNull { Instant.parse(it.startDate) }
            ?: error("No schedule data available for $seasonYear")

        return getWeekSchedule(currentWeek.season, currentWeek.week, currentWeek.seasonType)
            .trimToTodayOrLater()
    }

    suspend fun getWeekSchedule(year: Int, week: Int, seasonType: String): WeekSchedule {
        val games = api.getGameMedia(year, week, seasonType)
        val pollWeeks = runCatching { api.getRankings(year, week, seasonType) }.getOrDefault(emptyList())

        val apRanks = pollWeeks
            .flatMap { it.polls }
            .firstOrNull { it.poll.equals("AP Top 25", ignoreCase = true) }
            ?.ranks
            ?.associate { it.school to it.rank }
            ?: emptyMap()

        val teamLogos = getTeamLogos(year)

        // /scoreboard only covers a rolling window around "now" (no year/week params), so a game
        // outside that window simply won't be in the map and stays at the default SCHEDULED status.
        val scoreboardById = runCatching { api.getScoreboard() }
            .getOrDefault(emptyList())
            .associateBy { it.id }

        val zoneId = ZoneId.systemDefault()
        val scheduledGames = games.map { media ->
            val startTime = Instant.parse(media.startTime).atZone(zoneId)
            val score = scoreboardById[media.id]
            ScheduledGame(
                id = media.id,
                homeTeam = media.homeTeam,
                awayTeam = media.awayTeam,
                homeConference = media.homeConference,
                awayConference = media.awayConference,
                homeRank = apRanks[media.homeTeam],
                awayRank = apRanks[media.awayTeam],
                homeLogoUrl = teamLogos[media.homeTeam],
                awayLogoUrl = teamLogos[media.awayTeam],
                network = canonicalNetwork(media.outlet),
                startTime = startTime,
                isTimeTBD = media.isStartTimeTBD,
                liveStatus = parseLiveStatus(score?.status),
                homeScore = score?.homeTeam?.points,
                awayScore = score?.awayTeam?.points,
                period = score?.period,
                clock = score?.clock,
                situation = score?.situation,
                spread = score?.betting?.spread,
                overUnder = score?.betting?.overUnder
            )
        }
            // CFBD sometimes lists the exact same game+outlet more than once; a real multi-platform
            // simulcast (e.g. ESPN and Disney+ both carrying the same game) has a different network
            // per entry and is intentionally kept.
            .distinctBy { listOf(it.homeTeam, it.awayTeam, it.network, it.startTime.toInstant()) }

        val days = scheduledGames
            .groupBy { it.startTime.toLocalDate() }
            .toSortedMap()
            .map { (date, gamesOnDate) ->
                val timeSlots = gamesOnDate
                    .groupBy { if (it.isTimeTBD) null else it.startTime.toLocalTime() }
                    .toList()
                    // TBD (null) sorts after every kickoff time.
                    .sortedWith(compareBy({ it.first == null }, { it.first ?: LocalTime.MAX }))
                    .map { (time, gamesAtTime) ->
                        TimeSlot(time, gamesAtTime.sortedBy { networkPriority(it.network) })
                    }
                DaySchedule(date, timeSlots)
            }

        return WeekSchedule(year, week, seasonType, days)
    }

    private fun parseLiveStatus(status: String?): LiveStatus = when (status) {
        "in_progress" -> LiveStatus.IN_PROGRESS
        "completed" -> LiveStatus.COMPLETED
        else -> LiveStatus.SCHEDULED
    }
}
