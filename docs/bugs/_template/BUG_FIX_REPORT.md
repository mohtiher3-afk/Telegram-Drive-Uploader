# Bug Fix Report

## Bug

`<bug-id and title>`

## Root Cause

`<confirmed cause and evidence>`

## Fix

`<minimal implementation change>`

## Files Changed

| File | Change | Reason |
|---|---|---|
| `<path>` | `<change>` | `<reason>` |

## Regression Test

Before fix: `FAIL | NOT AVAILABLE | NOT VERIFIED`

After fix: `PASS | FAIL | NOT VERIFIED`

Describe why the test distinguishes the old and corrected behavior.

## Validation

| Gate | Result | Evidence |
|---|---|---|
| Targeted tests | PASS / FAIL / BLOCKED | `<command and result>` |
| Build | PASS / FAIL / BLOCKED | `<command and result>` |
| Lint | PASS / FAIL / BLOCKED | `<command and result>` |
| TDLib/artifacts | PASS / FAIL / NOT APPLICABLE | `<evidence>` |
| Self-check | PASS / FAIL / BLOCKED | `<command and result>` |
| Full regression | PASS / FAIL / BLOCKED | `<paths exercised>` |
| Security | PASS / FAIL / BLOCKED | `<evidence>` |
| Performance | PASS / FAIL / NOT VERIFIED | `<measurement>` |

## Risk

`LOW | MEDIUM | HIGH | CRITICAL`

## Release Impact

`NO RELEASE REQUIRED | PATCH RELEASE REQUIRED | HOTFIX RELEASE REQUIRED | SECURITY RELEASE REQUIRED`

## Remaining Risks

`<known limitations and follow-up evidence>`

## Final Status

`FIX VERIFIED | FIX CONDITIONALLY VERIFIED | FIX NOT VERIFIED`

Use `FIXED` only when the root cause is confirmed, the fix is implemented, the regression test passes where practical, and build validation passes.
