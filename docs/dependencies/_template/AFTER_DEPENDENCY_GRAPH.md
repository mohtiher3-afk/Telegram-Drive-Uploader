# Dependency Graph After Update

## Exact Command

`<dependency insight or dependencies command>`

## Results

| Check | Result | Evidence |
|---|---|---|
| Version conflicts | NONE / FOUND | `<dependency tree or report>` |
| Duplicate versions | NONE / FOUND | `<dependency tree or report>` |
| Forced resolution | NONE / FOUND | `<configuration/reference>` |
| Dependency substitution | NONE / FOUND | `<configuration/reference>` |
| Unexpected transitive changes | NONE / FOUND | `<before/after comparison>` |

## Decision

`ACCEPT | INVESTIGATE | ROLLBACK`

Do not hide conflicts with force resolution or suppress compatibility errors without an approved reason.
