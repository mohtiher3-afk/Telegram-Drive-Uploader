# Ongoing Security Policy

Every future change must review its effect on secrets, authentication, local storage, network transport, permissions, diagnostics, dependencies, and native code. Source code must not contain production tokens, API secrets, passwords, private keys, session data, or private media.

Security fixes take priority over normal feature work. A suspected exposure is contained first, then assessed, patched, tested, audited, released, and documented. CI must continue scanning for private keys, obvious token material, insecure network configuration, and accidental private artifacts without printing matched values.

Native and TDLib changes require exact source/version review, artifact validation, ABI/JNI validation, and runtime evidence. Do not weaken fail-closed behavior or bypass signature and artifact checks.
