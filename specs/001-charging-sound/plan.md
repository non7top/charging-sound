# Plan: Charging Sound

## Stack

- Kotlin 2.4.21 (overrides the 2.2.10 bundled in AGP's built-in Kotlin), Android Gradle Plugin 9.5.0, Gradle 9.8.2 (wrapper, checksum pinned), JDK 25 (Temurin) for the build, Java 17 language level.
- minSdk 31 (Android 12, which also covers 13 and newer), targetSdk and compileSdk 37, build-tools 37.0.0. Platform `Activity` and framework widgets only, no AndroidX.
- Build, test and release all run as GitHub workflows, locally through `act` (`make ci`, `make release-dry-run`). There is no Dockerfile or compose file; the workflows install the JDK and SDK themselves.
- Package and application id `io.github.non7top.chargingsound`.

## Components

| File | Role |
|---|---|
| `Prefs.kt` | `SharedPreferences` wrapper: `enabled`, `soundUris` (list), notification-permission flag. |
| `Logic.kt` | Pure functions: `isXiaomiFamily`, `Debouncer`, `pickSource` (random pick among usable sounds). Unit tested. |
| `SoundPlayer.kt` | Resolves the source, plays it with `MediaPlayer`, releases on completion. |
| `ChargerService.kt` | Foreground service; registers the `POWER_CONNECTED` receiver; applies debounce; calls `SoundPlayer`. |
| `BootReceiver.kt` | Starts the service after boot or app update if enabled. |
| `Persistence.kt` | Checklist state and settings intents (battery, MIUI autostart, MIUI battery saver, app details). |
| `MainActivity.kt` | Enable switch, sound picker, test button, checklist rows. |

## Manifest

Permissions: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
Service declares `foregroundServiceType="specialUse"` with the `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property.

## Key decisions

- Dynamic receiver in a foreground service, not a manifest receiver: required on Android 8 and newer.
- `START_STICKY` so the system restarts the service after a kill where it is allowed to.
- Sound goes through `USAGE_ASSISTANCE_SONIFICATION` so the user's ringer and DND settings apply.
- Xiaomi autostart is read through reflection on `AppOpsManager.checkOpNoThrow`. A failure maps to "unknown", never to "granted".
- Vendor settings intents are tried in order and fall back to app details on `ActivityNotFoundException` or `SecurityException`.

## CI and release

- `build.yml` (pull requests): lint, unit tests, debug APK.
- `release-please.yml` (push to master): release PR, then on merge calls `release.yml` to build a signed APK, verify it with `apksigner`, attest provenance and upload it with `SHA256SUMS`.
- `release-please-preview.yml`: forecasts the next version on PRs.
- Dependabot covers GitHub Actions and Gradle (including the wrapper).
- act derives its container name from workflow name and job id only, so the two workflows that run under act have project-unique names.

## Risks

- Hidden-API restrictions can block the autostart read; handled as "unknown".
- Vendor activity names can change between MIUI and HyperOS versions; handled by fallback.
- No device here: playback and the Xiaomi screens are untested on hardware until the APK is installed on a phone.
