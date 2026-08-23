# Regression Matrix

| Feature | Happy path | Error | Offline | Restart | RTL | Dark | Current status |
|---|---|---|---|---|---|---|---|
| Startup/onboarding | Manual | Manual | N/A | Manual | Manual | Manual | Partial evidence |
| Telegram authentication | Instrumentation/manual | Boundary gap | Boundary gap | Manual | Manual | Manual | High-priority gap |
| Destination selection | Manual | Boundary gap | Boundary gap | Manual | Manual | Manual | High-priority gap |
| File format recognition | JVM unit | JVM unit | N/A | N/A | N/A | N/A | Covered |
| Queue/work policy | JVM policy unit | Partial | Partial | Partial | N/A | N/A | Partial |
| Real upload delivery | TDLib boundary unit mapping | Completion policy unit | Gap | Gap | N/A | N/A | High-priority runtime gap |
| Progress/speed/ETA | JVM formatter unit | JVM formatter unit | N/A | N/A | Locale gap | Visual gap | Partial |
| History | Manual/source review | Gap | N/A | Gap | Manual | Manual | Gap |
| Settings | Manual/source review | Gap | N/A | Manual | Manual | Manual | Gap |
| Resource and locale integrity | Script | Script | N/A | N/A | Static | Static | Covered statically |
