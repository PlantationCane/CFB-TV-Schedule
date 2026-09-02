package com.dantonio.cfbschedule.data

import retrofit2.http.GET
import retrofit2.http.Query

interface CfbdApi {

    @GET("calendar")
    suspend fun getCalendar(@Query("year") year: Int): List<CalendarWeek>

    @GET("games/media")
    suspend fun getGameMedia(
        @Query("year") year: Int,
        @Query("week") week: Int,
        @Query("seasonType") seasonType: String,
        @Query("classification") classification: String = "fbs"
    ): List<GameMedia>

    @GET("rankings")
    suspend fun getRankings(
        @Query("year") year: Int,
        @Query("week") week: Int,
        @Query("seasonType") seasonType: String
    ): List<PollWeek>

    @GET("teams/fbs")
    suspend fun getFbsTeams(@Query("year") year: Int): List<Team>

    @GET("scoreboard")
    suspend fun getScoreboard(@Query("classification") classification: String = "fbs"): List<ScoreboardGame>
}
