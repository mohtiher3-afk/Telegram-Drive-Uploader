# Pull Request Policy

Every pull request must include a concise summary, reason for the change, affected scope, tests actually run, risk assessment, and rollback plan when applicable. UI changes should include screenshots or equivalent device evidence when appropriate. Database changes must include schema and migration notes. TDLib, native, signing, security, and upload changes require the dedicated policies and gates.

The pull request must identify protected areas that were intentionally not changed. Do not include secrets, signing material, generated private artifacts, unrelated formatting noise, or unreviewed dependency upgrades.
