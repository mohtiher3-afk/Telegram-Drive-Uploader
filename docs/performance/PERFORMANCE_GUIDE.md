# Performance Guide

Correctness comes before speed. Measure before changing production behavior, especially TDLib upload buffers, progress frequency, Room persistence, retry rules, or WorkManager constraints.

Compose should use stable item keys, narrow state reads, and lazy containers for large collections. Coroutines must remain structured and lifecycle-owned; do not introduce `GlobalScope` or arbitrary delays. Flows should avoid duplicate collectors and unnecessary recomputation, but throttling must not make upload progress appear broken.

Large files should remain streamed with bounded resources and explicit closure. Room/DataStore writes require measurement before batching or throttling because queue recovery is a correctness contract. WorkManager must retain background execution, constraints, and retry semantics. Notifications should remain useful without excessive update work, but their cadence must be measured on device.

Every performance change follows: baseline → evidence → smallest change → compile/test → measure again → keep or revert. Runtime claims require device/profiler evidence; CI build success alone does not prove memory, battery, or upload throughput.
