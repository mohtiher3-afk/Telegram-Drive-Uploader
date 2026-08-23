# High-Risk Bug Plan

Complete before implementation when the bug affects TDLib, JNI, ABI, authentication, Upload Engine, WorkManager, database, security, or native libraries.

## Problem

`<bug-id and verified impact>`

## Root Cause

`<confirmed or explicitly unresolved root cause>`

## Proposed Minimal Fix

`<smallest safe change; no unrelated cleanup>`

## Affected Systems

| System | Impact | Risk | Evidence |
|---|---|---|---|
| TDLib/JNI/ABI | `<impact>` | HIGH / CRITICAL | `<evidence>` |
| Authentication | `<impact>` | HIGH / CRITICAL | `<evidence>` |
| Upload/WorkManager | `<impact>` | HIGH / CRITICAL | `<evidence>` |
| Database/DataStore | `<impact>` | HIGH / CRITICAL | `<evidence>` |
| Security/native libraries | `<impact>` | HIGH / CRITICAL | `<evidence>` |

## Regression Risks

Describe possible regressions in startup, authorization, file state, queue, progress, retry, persistence, background execution, and release packaging.

## Test Plan

Define targeted tests, boundary/integration tests, full regression paths, security checks, TDLib artifact checks, and the master self-check.

## Rollback Plan

Identify the safe commit/artifact rollback path. Never recommend a database downgrade without migration analysis and data-preservation verification.

## Approval Gate

`BLOCKED UNTIL REVIEWED`
