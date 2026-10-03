# Development Rules

Before changing code, inspect the relevant files, understand current behavior, identify impact, make the smallest safe change, test it, inspect the diff, and document the decision.

Manus and maintainers must not add features while fixing unrelated bugs, rewrite unrelated code, upgrade dependencies without compatibility evidence, change architecture without justification, disable tests, bypass artifact gates, or claim success without actual evidence.

Every bug fix records its description, root cause, fix, test, affected areas, risk, and rollback path. Every release records version, commit, artifacts, verification, notes, limitations, and rollback reference. Secrets, signing keys, session data, and private media must remain outside source control and diagnostic exports.
