# Contributing

- Work on a branch and open a pull request against `master`. Do not push to `master` directly.
- Use [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `docs:`, `chore:`, `ci:`). Releases and the changelog are generated from them by release-please.
- Run `make ci` before opening a PR. It runs lint, unit tests and the debug build through act, the same way CI does.
- Keep logic that does not need the Android framework in `Logic.kt` so it can be unit tested.
- Every Kotlin file starts with `// SPDX-License-Identifier: Apache-2.0`.
- By contributing you agree that your contribution is licensed under the Apache License 2.0.

Optional: `pre-commit install --hook-type pre-commit --hook-type commit-msg` enables the checks in `.pre-commit-config.yaml`.
