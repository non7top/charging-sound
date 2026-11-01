# Charging Sound

Android app that plays a sound the moment a charging cable is connected. It includes a setup checklist that keeps the app alive on Xiaomi, Redmi and POCO phones, where the system otherwise kills background apps.

- Android 12 (API 31) and newer
- No ads, no analytics, no network access, no dependencies beyond the Android framework
- Pick any audio file or use the system notification sound
- License: Apache-2.0

## Install

Download the APK from the [latest release](../../releases/latest), then verify it before installing:

```
sha256sum -c SHA256SUMS
gh attestation verify charging-sound-vX.Y.Z.apk --repo non7top/charging-sound
```

Open the app, turn on the switch, and work through the "Keep it running" checklist.

## Xiaomi, Redmi and POCO

MIUI and HyperOS stop background apps unless you opt out. The app shows a checklist and opens the right screen for each item:

- Battery optimization: allow the app to run without restrictions
- Autostart: turn it on (the app reads this state on a best-effort basis and shows "Unknown" if it cannot)
- Battery saver: set the app to "No restrictions" (cannot be read by the app)
- Lock the app in the recents screen (cannot be read or opened by the app)

## How it works

Android no longer delivers the "power connected" broadcast to apps declared in the manifest, so a small foreground service registers a receiver for it and plays the sound. The service restarts after a reboot or an app update if the switch is on. The sound uses the system's sonification stream, so silent mode and Do Not Disturb are respected.

## Build

Everything runs through [act](https://github.com/nektos/act), which executes the same GitHub workflows that CI runs, so nothing needs to be installed on the host except Docker and act.

```
make ci
```

This lints, runs the unit tests and builds the debug APK. Release builds are done by the release workflow; see [docs/RELEASING.md](docs/RELEASING.md).

The feature spec, plan and task list are in [specs/001-charging-sound](specs/001-charging-sound).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Report security problems as described in [SECURITY.md](SECURITY.md).

## License

Apache License 2.0, see [LICENSE](LICENSE) and [NOTICE](NOTICE).
