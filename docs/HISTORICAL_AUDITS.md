# Historical Records

Point-in-time material — audits, inventories, dated maintenance records, per-version
release notes, and superseded design references — is **not kept as a live folder in
this repository**. Cleanup commits removed those trees because they had drifted out of
date and duplicated the living documentation, but the documents that indexed them were
never updated, which is why they used to link here.

| Removed area | Removed in | Reason recorded in the commit |
|---|---|---|
| `docs/archive/` | `ac9ed9a`, `acdb37c` | consolidate the audit archive; remove old design audit files |
| `docs/evidence/` | `88d45ef` and later cleanups | remove historical logcat artifacts |
| `docs/{features,bugs,dependencies}/_template/` | `6f1b150` | remove unused documentation templates |

## Where the material is now

The content was deleted, not lost. Git history is the archive:

```
# what was deleted, and when
git log --diff-filter=D --name-only -- docs/archive
git log --all --oneline --diff-filter=D -- '*HISTORICAL_AUDITS.md'

# read a removed document from just before its removal
git show ac9ed9a^:docs/archive/reports/HISTORICAL_AUDITS.md

# search all history for a removed topic
git log --all --oneline -S 'phrase' -- docs/
```

Signed release artifacts and their SHA-256 checksums were never removed; they live on
the GitHub Releases page and are unaffected by these cleanups.

## What is authoritative instead

[`README.md`](README.md) is the maintained index of living documentation. A document not
reachable from that index is not maintained, must not be cited as current behavior, and
must not be reintroduced into the tree without restoring its accuracy first.
