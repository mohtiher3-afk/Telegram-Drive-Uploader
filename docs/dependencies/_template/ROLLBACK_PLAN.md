# Dependency Update Rollback Plan

## Previous Commit

`<commit SHA>`

## Previous Versions

| Component | Previous version | Target version |
|---|---|---|
| `<dependency>` | `<version>` | `<version>` |

## Rollback Strategy

Describe the safe revert or forward-fix strategy. Use a focused branch and commit; do not overwrite shared history.

## Database Compatibility

State whether the dependency change affects Room, SQLite, DataStore, serialized data, or persisted upload state. Never downgrade an application across an incompatible schema without migration analysis.

## Artifact and Release Rollback

Identify the previous release tag, artifact hashes, and distribution rollback path where applicable.

## Stop Conditions

Rollback is not approved if data compatibility is unknown, native artifacts do not match, or the previous state cannot be reproduced.
