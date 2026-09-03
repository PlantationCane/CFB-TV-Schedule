package com.dantonio.cfbschedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.dantonio.cfbschedule.data.DaySchedule
import com.dantonio.cfbschedule.data.LiveStatus
import com.dantonio.cfbschedule.data.ScheduleFilter
import com.dantonio.cfbschedule.data.ScheduledGame
import com.dantonio.cfbschedule.data.TimeSlot
import com.dantonio.cfbschedule.data.WeekSchedule
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// A brighter, more saturated green than the app's primary brand color, used only for the day
// header banner so the date pops instead of blending into a dark background.
private val DAY_HEADER_GREEN = androidx.compose.ui.graphics.Color(0xFF2FA85A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val displayState by viewModel.displayState.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val availableConferences by viewModel.availableConferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CFB TV Schedule", fontWeight = FontWeight.Bold)
                        if (uiState is ScheduleUiState.Success) {
                            val schedule = (uiState as ScheduleUiState.Success).schedule
                            Text(
                                text = "Week ${schedule.week} • ${schedule.seasonType.replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadCurrentWeek() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState is ScheduleUiState.Success) {
                FilterBar(
                    filter = filter,
                    availableConferences = availableConferences,
                    onTopRankOnlyChange = viewModel::setTopRankOnly,
                    onConferenceChange = viewModel::setConferenceFilter,
                    onShowStreamingOnlyChange = viewModel::setShowStreamingOnly
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = displayState) {
                    is ScheduleUiState.Loading -> LoadingView()
                    is ScheduleUiState.Error -> ErrorView(state.message, onRetry = { viewModel.loadCurrentWeek() })
                    is ScheduleUiState.Success -> WeekScheduleList(state.schedule, filtersActive = filter.isActive)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBar(
    filter: ScheduleFilter,
    availableConferences: List<String>,
    onTopRankOnlyChange: (Boolean) -> Unit,
    onConferenceChange: (String?) -> Unit,
    onShowStreamingOnlyChange: (Boolean) -> Unit
) {
    var conferenceMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = filter.topRankOnly,
            onClick = { onTopRankOnlyChange(!filter.topRankOnly) },
            label = { Text("Top 20") }
        )

        FilterChip(
            selected = filter.showStreamingOnly,
            onClick = { onShowStreamingOnlyChange(!filter.showStreamingOnly) },
            label = { Text("Streaming games") }
        )

        Box {
            FilterChip(
                selected = filter.conference != null,
                onClick = { conferenceMenuExpanded = true },
                label = { Text(filter.conference ?: "Conference") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
            )
            DropdownMenu(
                expanded = conferenceMenuExpanded,
                onDismissRequest = { conferenceMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All conferences") },
                    onClick = {
                        onConferenceChange(null)
                        conferenceMenuExpanded = false
                    }
                )
                availableConferences.forEach { conference ->
                    DropdownMenuItem(
                        text = { Text(conference) },
                        onClick = {
                            onConferenceChange(conference)
                            conferenceMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Couldn't load the schedule", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Surface(
                onClick = onRetry,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    "Retry",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun WeekScheduleList(schedule: WeekSchedule, filtersActive: Boolean) {
    if (schedule.days.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (filtersActive) "No games match these filters." else "No games found for this week.")
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(schedule.days) { day ->
            DaySection(day)
        }
    }
}

@Composable
private fun DaySection(day: DaySchedule) {
    val dayLabel = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dateLabel = day.date.format(DateTimeFormatter.ofPattern("MMMM d"))
    val gameCount = day.timeSlots.sumOf { it.games.size }

    Column {
        Surface(color = DAY_HEADER_GREEN) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "$dayLabel, $dateLabel",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "$gameCount ${if (gameCount == 1) "game" else "games"}",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        day.timeSlots.forEach { timeSlot ->
            TimeSlotSection(timeSlot)
        }
    }
}

@Composable
private fun TimeSlotSection(timeSlot: TimeSlot) {
    val label = timeSlot.time?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "TBD"
    Column {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                label,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
        timeSlot.games.forEach { game ->
            GameRow(game)
        }
    }
}

@Composable
private fun GameRow(game: ScheduledGame) {
    Column {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Network badge and time share a header row; team names get the full row width
                // below so they don't get squeezed at large system font scales.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NetworkBadge(game.network)
                    Text(
                        text = gameStatusLabel(game),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (game.liveStatus == LiveStatus.IN_PROGRESS) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TeamLine(
                        rank = game.awayRank,
                        team = game.awayTeam,
                        logoUrl = game.awayLogoUrl,
                        score = game.awayScore,
                        isWinning = game.liveStatus != LiveStatus.SCHEDULED &&
                            game.awayScore != null && game.homeScore != null && game.awayScore > game.homeScore
                    )
                    Text("@", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TeamLine(
                        rank = game.homeRank,
                        team = game.homeTeam,
                        logoUrl = game.homeLogoUrl,
                        score = game.homeScore,
                        isWinning = game.liveStatus != LiveStatus.SCHEDULED &&
                            game.homeScore != null && game.awayScore != null && game.homeScore > game.awayScore
                    )
                }
                if (game.liveStatus == LiveStatus.SCHEDULED) {
                    bettingLineLabel(game)?.let { label ->
                        Spacer(Modifier.height(6.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

private fun bettingLineLabel(game: ScheduledGame): String? {
    val spreadText = game.spread?.let { spread ->
        val favorite = if (spread <= 0) game.homeTeam else game.awayTeam
        "$favorite ${formatLineNumber(-kotlin.math.abs(spread))}"
    }
    val overUnderText = game.overUnder?.let { "O/U ${formatLineNumber(it)}" }
    return listOfNotNull(spreadText, overUnderText).takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

private fun formatLineNumber(value: Double): String {
    val rounded = kotlin.math.round(value * 10) / 10
    return if (rounded == rounded.toInt().toDouble()) rounded.toInt().toString() else rounded.toString()
}

private fun gameStatusLabel(game: ScheduledGame): String = when (game.liveStatus) {
    LiveStatus.COMPLETED -> "FINAL"
    LiveStatus.IN_PROGRESS -> game.situation ?: buildString {
        if (game.period != null) append("Q${game.period}")
        if (game.clock != null) {
            if (isNotEmpty()) append(" · ")
            append(game.clock)
        }
        if (isEmpty()) append("LIVE")
    }
    LiveStatus.SCHEDULED -> if (game.isTimeTBD) "TBD" else game.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))
}

// Base sizes at 100% system font scale. Multiplied by the user's own accessibility text-size
// setting at render time so icons/badges stay proportional to their text on every phone, instead
// of one fixed size looking right on only one tester's device (e.g. 150% vs. 100% text scale).
private val NETWORK_BADGE_HEIGHT_BASE = 32.dp
private val TEAM_LOGO_SIZE_BASE = 28.dp
private val ICON_SCALE_RANGE = 0.85f..2f

@Composable
private fun scaledDp(base: Dp): Dp {
    val fontScale = LocalDensity.current.fontScale.coerceIn(ICON_SCALE_RANGE.start, ICON_SCALE_RANGE.endInclusive)
    return base * fontScale
}

@Composable
private fun NetworkBadge(network: String) {
    val context = LocalContext.current
    val drawableName = networkLogoDrawableName(network)
    val resId = remember(drawableName) {
        drawableName
            ?.let { context.resources.getIdentifier(it, "drawable", context.packageName) }
            ?.takeIf { it != 0 }
    }
    if (resId == null) {
        NetworkColorBadge(network)
        return
    }
    val badgeHeight = scaledDp(NETWORK_BADGE_HEIGHT_BASE)
    Surface(color = networkLogoCardColor(network), shape = RoundedCornerShape(8.dp)) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = network,
            modifier = Modifier
                .height(badgeHeight)
                .widthIn(min = 56.dp, max = 110.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun NetworkColorBadge(network: String) {
    val backgroundColor = networkColor(network)
    val textColor = if (backgroundColor.luminance() > 0.5f) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
    val badgeHeight = scaledDp(NETWORK_BADGE_HEIGHT_BASE)
    Surface(color = backgroundColor, shape = RoundedCornerShape(8.dp)) {
        Box(
            modifier = Modifier
                .height(badgeHeight)
                .widthIn(min = 56.dp)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                network,
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TeamLine(rank: Int?, team: String, logoUrl: String?, score: Int? = null, isWinning: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TeamLogo(logoUrl = logoUrl, team = team, size = scaledDp(TEAM_LOGO_SIZE_BASE))
        Spacer(Modifier.width(8.dp))
        if (rank != null) {
            Text(
                "#$rank ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            team,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        if (score != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                score.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isWinning) FontWeight.Bold else FontWeight.Medium,
                color = if (isWinning) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TeamLogo(logoUrl: String?, team: String, size: Dp) {
    if (logoUrl == null) {
        TeamMonogram(team, size)
        return
    }
    SubcomposeAsyncImage(
        model = logoUrl,
        contentDescription = null,
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit,
        loading = { TeamMonogram(team, size) },
        error = { TeamMonogram(team, size) }
    )
}

@Composable
private fun TeamMonogram(team: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            team.take(1).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
