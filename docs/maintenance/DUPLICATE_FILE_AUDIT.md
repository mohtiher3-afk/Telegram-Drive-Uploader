# Duplicate File Audit

**Status: CURRENT — audit record; no automatic deletion performed.**

## Method

The audit reviewed tracked filenames, documentation references, workflow/script references, root configuration, native artifact locations, and ignored output directories. Names containing `final`, `new`, `old`, `copy`, `backup`, `temp`, or `test2` were treated as review signals only, not proof of duplication.

## Findings

| Finding | Classification | Decision |
|---|---|---|
| `docs/*/FINAL_*.md` | Area-specific or historical reports | Retain. These reports document different areas or milestones and are referenced by indexes or audit records. |
| `docs/final-audit/FINAL_REPOSITORY_STRUCTURE.md` and the new `docs/maintenance/FINAL_REPOSITORY_STRUCTURE.md` | Different scope | Retain both. The former is a prior audit artifact; the latter is the current cleanup-phase structure record. |
| `docs/release/PRODUCTION_BASELINE.md` and `docs/performance/PRODUCTION_BASELINE.md` | Different subject areas | Retain both; qualify links by directory. |
| `scripts/check-repository-security.sh` and `scripts/check-secrets.sh` | Intentional wrapper boundary | Retain both. The former is authoritative; the latter provides the stable self-check command required by maintenance documentation. |
| Backup/configuration files | No tracked backup configuration file was found in this Android repository. | No deletion required. |
| `.native-build/`, `build/`, `app/build/`, `.gradle/` | Ignored generated/local outputs | Retain locally and verify ignore rules; do not commit or delete automatically. |
| `debug.keystore` | Local signing artifact | Retain locally only; it is ignored and must not be committed. |

## Conclusion

No tracked duplicate, obsolete script, or dead documentation file had sufficient evidence for safe deletion or consolidation. Historical reports remain historical; current entry points are linked from `docs/README.md` and `docs/operations/MAINTENANCE_INDEX.md`. The cleanup phase therefore makes no file removals or moves.
