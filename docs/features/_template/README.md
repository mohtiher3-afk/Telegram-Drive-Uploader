# Controlled Feature Documentation Template

Copy this directory to `docs/features/<feature-name>/` before implementation begins. Replace every placeholder with evidence from the repository and the approved feature request. Do not commit a feature as `READY_FOR_RELEASE` until the implementation, tests, security review, performance review, regression review, and release decision are complete.

| Document | Required purpose |
|---|---|
| `FEATURE_REQUEST.md` | Problem, user need, benefit, scope, dependencies, and risks. |
| `IMPACT_ANALYSIS.md` | Affected boundaries and LOW/MEDIUM/HIGH/CRITICAL risk classification. |
| `REQUIREMENTS.md` | Functional, non-functional, UI, accessibility, RTL, dark-mode, performance, security, and persistence requirements. |
| `USER_FLOW.md` | Entry, states, success, failure, retry, cancel, back navigation, and persistence. |
| `TECHNICAL_DESIGN.md` | Architecture, files, data flow, error handling, and test strategy. |
| `IMPLEMENTATION_PLAN.md` | Small ordered steps with files, risk, and verification for each step. |
| `STATUS.md` | PLANNED, IN_PROGRESS, TESTING, BLOCKED, READY_FOR_RELEASE, or RELEASED. |
| `DATABASE_PLAN.md` | Required only when the feature changes persistent schema or migrations. |

The template is a planning aid, not approval to modify protected systems. Any feature affecting TDLib, JNI, ABI, authentication, Upload Engine, WorkManager, database, or security architecture requires specialized impact analysis before implementation.
