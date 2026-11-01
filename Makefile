# act names its container from workflow name + job id only, so the workflow names here must stay
# unique across projects or concurrent runs kill each other's containers.
# Node's dual-stack connect attempt times out on slow links (the action's own downloads then fail).
ACT ?= act --env NODE_OPTIONS=--no-network-family-autoselection

.PHONY: ci release-dry-run

# Runs the real build workflow (lint, unit tests, debug APK) in act's container.
ci:
	$(ACT) pull_request -W .github/workflows/build.yml

# Builds, signs and verifies a release APK without uploading or attesting.
# Needs signing/dryrun.secrets (gitignored): see docs/RELEASING.md.
release-dry-run:
	$(ACT) workflow_dispatch -W .github/workflows/release.yml \
		--input tag=v0.0.0-dryrun --input publish=false \
		--secret-file signing/dryrun.secrets
