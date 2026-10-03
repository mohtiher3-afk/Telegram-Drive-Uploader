# Branch Protection Recommendation

Protect `main` through GitHub repository settings rather than changing settings automatically. Require pull-request review, resolved conversations, and successful required checks before merge. Recommended checks are `Repository security gate`, all three Android Multi-ABI jobs, and the device smoke job only if the emulator runner is reliable enough for a required gate.

Disable direct pushes for normal contributors. Keep release publication manual through the release workflow dispatch. Do not grant CI write permissions to pull-request validation jobs.
