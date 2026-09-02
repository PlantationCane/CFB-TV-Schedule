package com.dantonio.cfbschedule.data

import kotlinx.serialization.Serializable

@Serializable
data class GameMedia(
    val id: Int,
    val season: Int,
    val week: Int,
    val seasonType: String,
    val startTime: String,
    val isStartTimeTBD: Boolean,
    val homeTeam: String,
    val homeConference: String? = null,
    val awayTeam: String,
    val awayConference: String? = null,
    val mediaType: String,
    val outlet: String
)

@Serializable
data class CalendarWeek(
    val season: Int,
    val week: Int,
    val seasonType: String,
    val startDate: String,
    val endDate: String
)

@Serializable
data class PollWeek(
    val season: Int,
    val seasonType: String,
    val week: Int,
    val polls: List<Poll>
)

@Serializable
data class Poll(
    val poll: String,
    val isFinal: Boolean? = null,
    val ranks: List<PollRank>
)

@Serializable
data class PollRank(
    val rank: Int? = null,
    val teamId: Int,
    val school: String,
    val conference: String? = null,
    val firstPlaceVotes: Int? = null,
    val points: Int? = null
)

@Serializable
data class Team(
    val id: Int,
    val school: String,
    val logos: List<String>? = null
)

@Serializable
data class ScoreboardGame(
    val id: Int,
    val status: String,
    val period: Int? = null,
    val clock: String? = null,
    val situation: String? = null,
    val homeTeam: ScoreboardTeam,
    val awayTeam: ScoreboardTeam,
    val betting: ScoreboardBetting? = null
)

@Serializable
data class ScoreboardTeam(
    val points: Int? = null
)

@Serializable
data class ScoreboardBetting(
    val spread: Double? = null,
    val overUnder: Double? = null
)
