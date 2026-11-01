# Releasing

Releases are automated with release-please and a signed APK built by the release workflow.

## Flow

1. Merge PRs with Conventional Commit titles into `master`.
2. release-please keeps a release PR open with the version bump and changelog. Merge it when ready.
3. Merging creates the tag and GitHub release. The `publish` job in `release-please.yml` calls `release.yml`, which builds the signed APK, verifies it with `apksigner`, attests build provenance, and uploads the APK and `SHA256SUMS` to the release.

To rebuild and re-upload an existing release, run the `charging-sound release` workflow manually with the tag.

## One-time setup: signing key

Create a key once and keep it safe. Losing it means users cannot update in place.

```
keytool -genkeypair -v -keystore charging-sound-release.jks -alias release \
  -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 charging-sound-release.jks > keystore.b64
```

Store these repository secrets (Settings, Secrets and variables, Actions):

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | contents of `keystore.b64` |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | `release` |
| `ANDROID_KEY_PASSWORD` | key password |

Then delete `keystore.b64` and keep the `.jks` file in a password manager or offline backup. Never commit it (`*.jks` is gitignored).

## Dry run

`make release-dry-run` runs the release workflow locally through act without uploading or attesting. It reads secrets from `signing/dryrun.secrets` (gitignored), one `NAME=value` per line, using a throwaway key rather than the real one:

```
ANDROID_KEYSTORE_BASE64=...
ANDROID_KEYSTORE_PASSWORD=...
ANDROID_KEY_ALIAS=...
ANDROID_KEY_PASSWORD=...
```

## Verifying a release

```
sha256sum -c SHA256SUMS
gh attestation verify charging-sound-vX.Y.Z.apk --repo non7top/charging-sound
apksigner verify --print-certs charging-sound-vX.Y.Z.apk
```

Compare the certificate SHA-256 from `apksigner` with the fingerprint published for the release key.
