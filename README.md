# FPL Points Widget

A small Android home-screen widget for Fantasy Premier League.

## What it shows

- Current Gameweek live-estimate points
- FPL team name
- Overall rank
- Gameweek rank
- Players finished / remaining
- Last refresh time
- Manual refresh button
- Automatic refresh every 15 minutes while Android permits background work

## Data source

The app reads public JSON endpoints on `https://fantasy.premierleague.com/api/`.
No FPL password or login is stored.

Live points are calculated from your public Gameweek picks plus the FPL live player-points endpoint. Captain/triple-captain multipliers and transfer points hits are included. As with other unofficial live FPL trackers, autosubs/late corrections can temporarily differ from FPL's final official score.

## Build it

1. Open this folder in Android Studio.
2. Let Android Studio install/sync the required Android SDK and Gradle components.
3. This source package does not include the Gradle wrapper binary. In Android Studio, use a local Gradle 8.13 installation or run `gradle wrapper --gradle-version 8.13` once to create the wrapper files.
4. Let Android Studio sync the project dependencies.
5. Connect your Android phone by USB with USB debugging enabled, or use **Build > Build APK(s)**.
6. Install the APK on the phone.
7. Open **FPL Points Widget**, enter your FPL Team ID, tap **SAVE & REFRESH**.
8. Hold the Android home screen > **Widgets** > add **FPL Points Widget**.

## Finding your Team ID

Open your team on the Fantasy Premier League website. The number after `/entry/` in the page address is your Team ID.

## Notes

This is an unofficial personal FPL tracker and is not affiliated with or endorsed by the Premier League.
