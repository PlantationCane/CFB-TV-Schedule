# CFB TV Schedule

Native Android app (Kotlin + Jetpack Compose) showing the current week's college
football schedule grouped by day and then by TV network — the same layout as
[cfb.guide](https://cfb.guide).

## Setup

1. Get a free API key at https://collegefootballdata.com/key
2. Open `local.properties` in the project root and replace the placeholder:
   ```
   cfbd.api.key=YOUR_KEY_HERE
   ```
3. Open the project in Android Studio, or build from the command line:
   ```
   ./gradlew.bat :app:assembleDebug
   ```
4. Run on an emulator or device. The debug APK lands at
   `app/build/outputs/apk/debug/app-debug.apk`.

## How it works

- `data/ScheduleRepository.kt` calls the CollegeFootballData API's `/calendar`
  endpoint to find the current week, then `/games/media` for TV network per
  game and `/rankings` for AP Top 25 ranks.
- Games are grouped by day, then by network, with networks ordered by
  broadcast reach (ABC/CBS/FOX/NBC first, then ESPN family, conference
  networks, then streaming-only outlets).
- `ui/ScheduleScreen.kt` renders that structure with Compose.

## Known limitations (v1)

- No favorite-teams star, no week navigation (current week only), no
  notifications — flagged as later additions in planning.
- The AP poll rank lookup matches on team display name; a school's
  CFBD name occasionally differs slightly from the poll's school name,
  in which case the rank badge is silently omitted for that team.
