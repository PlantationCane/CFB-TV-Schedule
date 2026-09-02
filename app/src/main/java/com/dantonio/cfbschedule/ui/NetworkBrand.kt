package com.dantonio.cfbschedule.ui

import androidx.compose.ui.graphics.Color

/**
 * Approximate brand colors per network, used as the fallback badge when no logo drawable has
 * been supplied for that network yet (see [networkLogoDrawableName]). Falls back to a neutral
 * gray for anything not listed.
 */
private val NETWORK_COLORS = mapOf(
    "ABC" to Color(0xFF000000),
    "CBS" to Color(0xFF0057B8),
    "FOX" to Color(0xFF0A1F44),
    "NBC" to Color(0xFF2A2A72),
    "ESPN" to Color(0xFFD00000),
    "ESPN2" to Color(0xFFE5007E),
    "ESPNU" to Color(0xFFA6192E),
    "ABC/ESPN" to Color(0xFF000000),
    "FS1" to Color(0xFF14213D),
    "FS2" to Color(0xFF4EA6DC),
    "BTN" to Color(0xFF0C2340),
    "SECN" to Color(0xFF041E42),
    "ACCN" to Color(0xFF003DA5),
    "CBSSN" to Color(0xFF0057B8),
    "CBS Sports Network" to Color(0xFF0057B8),
    "Pac-12 Network" to Color(0xFF001E44),
    "USA" to Color(0xFF0A3161),
    "CW" to Color(0xFF00A651),
    "TNT" to Color(0xFFF5851F),
    "truTV" to Color(0xFFFFCC00),
    "Peacock" to Color(0xFF6F42C1),
    "HBO Max" to Color(0xFF1D0F35),
    "Disney+" to Color(0xFF0F1F48),
    "ESPN+" to Color(0xFFD00000),
    "SECN+" to Color(0xFF041E42),
    "ACCN+" to Color(0xFF003DA5),
    "MW+" to Color(0xFF003057),
    "UConn+" to Color(0xFF000E2F)
)

private val DEFAULT_NETWORK_COLOR = Color(0xFF4A4A4A)

fun networkColor(network: String): Color = NETWORK_COLORS[network] ?: DEFAULT_NETWORK_COLOR

/**
 * Drawable resource name (under app/src/main/res/drawable/, no extension) expected for each
 * network's real logo. Drop a PNG with this exact filename in that folder and it's picked up
 * automatically — no code change needed. Networks without a matching file fall back to the
 * colored badge from [networkColor].
 */
private val NETWORK_DRAWABLE_NAMES = mapOf(
    "ABC" to "network_abc",
    "CBS" to "network_cbs",
    "FOX" to "network_fox",
    "NBC" to "network_nbc",
    "ESPN" to "network_espn",
    "ESPN2" to "network_espn2",
    "ESPNU" to "network_espnu",
    "ABC/ESPN" to "network_espn",
    "FS1" to "network_fs1",
    "FS2" to "network_fs2",
    "BTN" to "network_btn",
    "SECN" to "network_secn",
    "ACCN" to "network_accn",
    "CBSSN" to "network_cbssn",
    "CBS Sports Network" to "network_cbssn",
    "Pac-12 Network" to "network_pac12",
    "USA" to "network_usa",
    "CW" to "network_cw",
    "TNT" to "network_tnt",
    "truTV" to "network_trutv",
    "Peacock" to "network_peacock",
    "HBO Max" to "network_hbomax",
    "Disney+" to "network_disneyplus",
    "ESPN+" to "network_espnplus",
    "SECN+" to "network_secnplus",
    "ACCN+" to "network_accnplus",
    "MW+" to "network_mwplus",
    "UConn+" to "network_uconnplus"
)

fun networkLogoDrawableName(network: String): String? = NETWORK_DRAWABLE_NAMES[network]

/**
 * Some supplied logo files are white-on-transparent (designed for a dark backdrop), which would
 * disappear on the default white badge card. List those networks here to force a dark card instead.
 */
private val DARK_BACKING_NETWORKS = setOf("Peacock")

private val LOGO_CARD_LIGHT = Color(0xFFFFFFFF)
private val LOGO_CARD_DARK = Color(0xFF1B1B1F)

fun networkLogoCardColor(network: String): Color =
    if (network in DARK_BACKING_NETWORKS) LOGO_CARD_DARK else LOGO_CARD_LIGHT
