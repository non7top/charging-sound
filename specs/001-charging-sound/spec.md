# Feature spec: Charging Sound

Source: `project.md` (play a sound when a charging cable is inserted; check the Xiaomi persistence setting).

## Problem

Phones with a muted or dark screen give no audible cue that a cable is seated. The user wants a sound the moment power is connected. On Xiaomi (MIUI / HyperOS) devices the OS kills background apps and blocks autostart, so the feature silently stops working unless the app is exempted.

## User stories

1. **Sound on plug-in (P1).** When I connect a charger or USB power, the phone plays my chosen sound within about one second.
2. **Survives reboot and app kill (P1).** After a reboot, or after I swipe the app away, the sound still plays without me opening the app.
3. **Choose the sound (P2).** I can pick any audio file on the device, reset to the system default, and test it with a button.
4. **Xiaomi persistence check (P1).** On a Xiaomi, Redmi or POCO device the app shows a checklist of the settings that keep it alive, tells me which ones are not set, and opens the right system screen for each.
5. **Turn it off (P2).** A single switch disables the feature and stops the background service.

## Functional requirements

- FR-1: Detect `ACTION_POWER_CONNECTED` and play the configured sound once per event.
- FR-2: Ignore a second connect event within 2 seconds of the last one (contact bounce).
- FR-3: Play on the sonification audio stream so ringer-silent and Do Not Disturb are respected by the OS. No custom override.
- FR-4: Run as a foreground service (type `specialUse`) with a low-importance, persistent notification, because manifest receivers do not get `ACTION_POWER_CONNECTED` on Android 8 and newer.
- FR-5: Start the service on `BOOT_COMPLETED` and on `MY_PACKAGE_REPLACED` when the feature is enabled.
- FR-6: Sound source is either the system default notification sound or a user-picked document URI with a persisted read grant. If the picked file cannot be opened, fall back to the default sound.
- FR-7: Settings (enabled flag, sound URI) are stored in `SharedPreferences`.
- FR-8: Setup checklist, always shown:
  - Notifications permission (Android 13+).
  - Battery optimization exemption (`isIgnoringBatteryOptimizations`).
- FR-9: Setup checklist, shown only when manufacturer or brand is Xiaomi, Redmi or POCO:
  - MIUI Autostart: state read best-effort through the hidden AppOps op 10008; shown as "unknown" if the read fails.
  - MIUI battery saver set to "No restrictions": cannot be read, so shown as "check manually".
  - Lock the app in the recents screen: cannot be read or opened, so shown as instructions only.
- FR-10: Each actionable checklist row has a button that opens the most specific settings screen available, falling back to the app details screen when the vendor activity does not resolve.
- FR-11: Checklist state is recomputed in `onResume`.

## Edge cases

- No Xiaomi device: Xiaomi rows are hidden, the generic rows stay.
- Vendor activity missing (HyperOS renames screens): fall back to app details settings.
- Notification permission denied: the service still runs, only its notification is hidden; the checklist flags it.
- Picked file deleted later: fallback to default sound, no crash.
- Another sound already playing from this app: the old player is released before the new one starts.
- Charger already connected when the service starts: no sound (only a new connect event counts).

## Out of scope

- Disconnect sound, battery-full alert, per-charger-type sounds, volume slider, bundled sound library, Play Store listing.

## Success criteria

- SC-1: With the service running, a connect event produces playback of the configured sound (verified on a device or emulator via `adb shell dumpsys battery set ac 1`).
- SC-2: `make ci` runs lint and unit tests for the pure logic (Xiaomi detection, debounce, sound resolution).
- SC-3: `make ci` produces an installable debug APK from a clean checkout with only Docker and act installed.

## Assumptions (not verified on hardware)

- `ACTION_POWER_CONNECTED` is delivered to a dynamically registered receiver in a foreground service on current Android versions.
- The MIUI activity names below match the user's firmware; HyperOS may differ, hence the fallback.
  - `com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity`
  - `com.miui.powerkeeper/com.miui.powerkeeper.ui.HiddenAppsConfigActivity`
- AppOps op 10008 reflects MIUI Autostart; it is an undocumented value.
