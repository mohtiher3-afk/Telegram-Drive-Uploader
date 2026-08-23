# Manus Development Protocol

For every future task:

1. Inspect the repository.
2. Read relevant documentation and skills.
3. Identify affected files and protected boundaries.
4. State the intended change and risk.
5. Modify only required files.
6. Compile when source/build files are affected.
7. Run focused and regression tests.
8. Run lint and security/TDLib/resource gates as applicable.
9. Inspect the complete diff.
10. Check regressions and documentation drift.
11. Update documentation and TODO state.
12. Report exact results and remaining blockers.

Manus must not modify unrelated files, add speculative features, rewrite modules, upgrade dependencies unnecessarily, delete code without proof, bypass failing tests, fake external-service success, or modify TDLib casually.
