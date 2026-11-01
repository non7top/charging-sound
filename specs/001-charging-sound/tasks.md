# Tasks: Charging Sound

- [x] T01 Gradle wrapper (9.8.2, pinned checksum), settings, root and app build files
- [x] T02 Manifest, strings, theme, launcher icon (adaptive, with monochrome layer)
- [x] T03 `Logic.kt` pure functions and unit tests (`LogicTest.kt`)
- [x] T04 `Prefs.kt`, `SoundPlayer.kt`
- [x] T05 `ChargerService.kt`, `BootReceiver.kt`
- [x] T06 `Persistence.kt` (checklist state, settings intents)
- [x] T07 `MainActivity.kt`
- [x] T08 `build.yml` and `make ci`: lint, tests, debug APK pass under act
- [x] T09 Apache-2.0 LICENSE and NOTICE, README, CONTRIBUTING, SECURITY, pre-commit, Dependabot
- [x] T10 `release.yml`, `release-please*.yml`, `docs/RELEASING.md`
- [ ] T11 Release dry run through act with a throwaway key (`make release-dry-run`)
- [ ] T12 On-device check: `adb shell dumpsys battery set ac 1` plays the sound; Xiaomi screens open
- [ ] T13 Publish: create the GitHub repo, set the four signing secrets, protect `master`
