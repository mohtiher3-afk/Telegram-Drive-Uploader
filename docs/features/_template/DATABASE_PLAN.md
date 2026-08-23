# Database Plan

Complete this document only when the feature requires a persistent schema change.

## Current Schema

Describe the existing tables, entities, keys, constraints, and version.

## Proposed Schema

Describe the smallest required schema change and its compatibility implications.

## Migration

Specify the migration path, schema-version increment, transaction behavior, and rollback considerations. Do not downgrade or rewrite production data without migration analysis.

## Data Preservation

Describe how existing queue, history, settings, and session data remain safe.

## Tests

Specify migration tests, upgrade tests, downgrade/rollback analysis where applicable, and data-preservation verification.

## Approval Gate

Database implementation is blocked until this plan and its impact analysis are reviewed and approved.
