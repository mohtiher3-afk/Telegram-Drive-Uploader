# Production Performance Baseline

This baseline records evidence availability rather than inventing measurements.

| Area | Baseline | Evidence status |
|---|---|---|
| Startup | No controlled device trace recorded | NOT MEASURED |
| Memory | No profiler trace for long uploads recorded | NOT MEASURED |
| Upload throughput | No controlled real-Telegram throughput sample recorded | NOT MEASURED |
| Queue responsiveness | Source review found stable item keys; no runtime trace | PARTIAL / NOT MEASURED |
| Database operations | No benchmark or migration timing trace recorded | NOT MEASURED |
| Battery behavior | No controlled idle/background profile recorded | NOT MEASURED |

Future performance changes require before/after measurements on the same device class, Android API, ABI, file class, network condition, and test procedure. If measurement is unavailable, document `PERFORMANCE CHANGE NOT QUANTITATIVELY VERIFIED` and do not claim improvement.
