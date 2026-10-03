# TDLib Update Policy

TDLib is not a routine dependency bump. Any update requires a written reason, exact upstream source/version review, compatibility review, and a rollback reference.

Before acceptance, rebuild native dependencies and TDLib for every supported ABI using the official source and matching bindings. Verify ELF architecture, non-zero size, SHA-256, Java bindings, JNI loading, `Client.create()`, TDLib parameters, authorization state, and packaged APK entries.

The update must include real authentication and real upload tests, queue/background regression tests, JVM tests, lint, release build, APK signature verification, and release notes. Never mix native libraries, Java bindings, generated code, or AAR contents from different TDLib revisions. Never replace a missing artifact with an empty, renamed, mocked, or unknown-source library.

If any artifact or runtime prerequisite fails, keep TDLib activation fail-closed and stop release publication until the evidence is complete.
