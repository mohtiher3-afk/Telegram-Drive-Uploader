---
name: tdlib-release-check
on:
  schedule:
    - cron: "0 9 * * *"
  workflow_dispatch:
engine:
  id: copilot
  model: gpt-5.6-luna
permissions:
  contents: read
  metadata: read
  issues: read
  pull-requests: read
tools:
  github:
    mode: local
    toolsets:
      - repos
      - issues
      - pull_requests
    github-token: ${{ secrets.GH_PERSONAL_ACCESS_TOKEN }}
    read-only: true
  bash: ["*"]
network:
  allowed:
    - defaults
    - github
safe-outputs:
  create-issue:
    title-prefix: "[tdlib] "
    labels: [automation, dependencies]
    close-older-issues: true
intent: Notify maintainers when a newer TDLib release than the version pinned in the repository is published.
---

# TDLib Release Check

You are a scheduled maintenance workflow for the Telegram-Drive-Uploader repository. The app depends on TDLib (telegram database library). Your job is to detect when a newer official TDLib release has been published and notify maintainers.

## Steps

1. **Read the pinned version.** Read `app/build.gradle.kts` from this repository and find the pinned TDLib version (search for the `tdlib` dependency and its `version`/`native` version identifier). Record the pinned version exactly.
2. **Check upstream releases.** Using the GitHub toolsets (`repos` toolset, `list_releases` on `tdlib/td`), fetch the newest published releases. Always pass an explicit `perPage` (e.g. `perPage: 10`) — MCP responses have a 25,000 token limit. The workflow's GitHub token is a personal access token (`GH_PERSONAL_ACCESS_TOKEN`) scoped for read access to `tdlib/td`, so you can read upstream releases.
3. **Compare versions.** Compare the latest upstream release tag against the pinned version using semantic-version ordering. Consider a release "newer" only if its version sorts above the pinned version. Ignore pre-release/alpha releases unless the pinned version itself is a pre-release.
4. **Act on the result.**
   - If a newer release exists, call the safe output `create_issue` with a title like `New TDLib release vX.Y.Z available` and a body (at least 20 characters) containing: the newest tag, its release URL, publish date, and the pinned version being updated from.
   - If no newer release exists, call `noop` and state the reason (latest upstream release equals or sorts below the pinned version, giving both versions).

## Constraints

- The agent job is read-only. Never attempt to write files or call GitHub write APIs directly.
- All writes go exclusively through the `create_issue` safe output.
- If a create_issue call fails or the body is rejected, retry once with a corrected body; otherwise call `noop` with the failure reason.
- Do not report the same release twice — `close-older-issues: true` on the safe output keeps the tracking issue list short.