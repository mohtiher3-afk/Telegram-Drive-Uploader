# Full Glow UI Theme Maintenance Record

## Intent and evidence

The user requested a complete, coherent Glow UI theme across Telegram Drive rather than an isolated effect. The evidence record in `docs/design/PINTEREST_GLOW_UI_FULL_THEME_REFERENCE.md` is based on the visible Pinterest search: dark fields, isolated lights, fine chromatic edge rims, compact contained icon halos, glass panels, and soft inner glow. The theme is original Mission Control Glow—not a copy of the reference’s Web3, home-button, or generic-dashboard product content.

## Design system: Cobalt Signal Field

| Layer | Rule | Purpose |
|---|---|---|
| Field | Blue-black ambient background with two low-opacity cobalt/teal sources. | Gives every route a common spatial field without turning the entire screen into neon. |
| Primary signal | Existing semantic cobalt `primary` and narrow light rim on the real primary action/selected control. | Makes the next factual action legible. |
| Confirmed state | Existing teal `tertiary` only after confirmed completion. | Separates verified delivery from atmospheric glow. |
| Glass | Translucent dark M3 containers, fine outline, clipped white reflection, small accent bloom. | Gives task groups depth while retaining M3 containment. |
| Quiet instrumentation | Secondary filters, history, settings, diagnostics, and long lists use low-energy surfaces. | Keeps the upload hero and actual controls visually primary. |
| Light fallback | Existing opaque semantic light scheme. | Maintains contrast rather than forcing a dark-only reference. |

## Screen and motion matrix

| Route | Glow UI treatment | Motion source and boundary |
|---|---|---|
| Home | Upload hero is the single high-energy signal field; connection and stats use small factual halos. | Existing Aurora breath and real connection transition; pointer hover remains pointer-only. |
| Upload preparation | Destination, schedule, assistant, and summary become stacked instrument surfaces; queue CTA gets cobalt rim. | Selection/date/suggestion changes use existing M3 press/state feedback only; prepared list does not cascade animate. |
| Queue | Filter chip selection and controls panel get narrow signal rims; `UploadStatusIndicator` remains the real-status focal element. | Real upload fraction drives endpoint glow; pause/retry/failure stop decorative pulse. |
| History | Search/filter/sort are quiet glass instruments; completed factual records use restrained teal evidence. | Filter/sort feedback is finite; no activity pulse or fabricated history. |
| Settings | Existing `SettingsSection` glass surfaces gain consistent icon-node treatment and control-state feedback. | Switch/radio/button state uses M3 interactions only; notification permission and logout stay unaltered. |
| Telegram auth | Auth-state header becomes a contained signal core; inputs and recovery remain high contrast. | Existing `AnimatedContent` follows true auth state; no synthetic waiting/progress. |
| Destination | Search, selected banner, pinned/selected rows, and confirm CTA get radio/selection rims. | The rim follows true stable destination selection and pin state only. |
| Onboarding | Permission/onboarding hero becomes a singular soft signal field with clear page dots and CTA. | Existing pager changes stay finite; launcher/completion paths are untouched. |
| Splash | Brand orbit is a short orientation signal inside shared field. | Existing reduced-motion guard remains authoritative; startup behavior is unchanged. |

## Protected behavior

This visual pass must preserve UI callbacks, NavHost routes/back stack, Hilt/ViewModel state ownership, Room/DataStore, WorkManager, `UploadWorker`, TDLib client/auth state, stable numeric destination IDs, URI selection, progress values and `ProgressBarRangeInfo`, terminal upload notifications, test tags, English/Arabic resources, RTL, light/dark selection, ABI/JNI/signing/CI configuration, and diagnostics privacy semantics. No new fake upload, completion, queue item, destination, history record, notification, AI agent, balance, or nonfunctional CTA is permitted.

## Motion contract

`AppMotion` remains the only timing source. Page entry is finite; button/chip/focus feedback is tactile and interruptible; state color/content transitions are driven by actual changes; upload endpoint glow follows real fraction under `UPLOADING`; nonessential decoration snaps/disappears when Android animator scale is disabled. Do not animate long lists, error text, timing/ETA/speed, notification content, or terminal results merely to look active.

## Official Material 3 alignment

Material’s current motion guidance distinguishes spatial motion from effect motion: movement/shape changes may use controlled spatial springs, while color and opacity should remain non-overshooting effects. This pass therefore reserves finite page/chip/CTA spatial feedback for orientation and confines glow to color/opacity effects. The current color guidance also requires accessible semantic color relationships and supports tone-based surface roles; the existing `ColorScheme` remains the authority for cobalt, teal, error, text, and surface pairings rather than introducing scattered hex colors. [Motion](https://m3.material.io/styles/motion/overview) [Color system](https://m3.material.io/styles/color/system/overview)

## Implementation sequence

First extend shared Glow tokens/helpers and central M3 surfaces. Then apply the system in safe slices: home alignment and shared file/status cards, upload/queue/history, settings, auth/destination, onboarding/splash. Preserve every callback and test tag. Build and test after each meaningful slice with the constrained Gradle command; inspect resource parity and `git diff --check`; stop Gradle after checks.

## Implemented application slice

The shared liquid-glass helper now provides a dark-mode semantic edge rim and exposes `glowSignalRim` for an existing primary/selected control. Home, shared video/status/empty/error cards, and settings already inherit the revised helper. Upload preparation now applies glass to factual destination, schedule, assistant, and summary groups; its add-to-queue CTA has a signal rim only when a real destination is selected. Queue/history filters and factual summary/control cards receive the selected/contained signal treatment. Telegram auth applies it to existing state cards and primary steps; destination applies it to real connection, selected banner, stable radio selection, and confirmation. Navigation selected state and bottom surface follow the same language. Onboarding uses a glass focal surface, finite AnimatedContent, animated factual pager dots, and the existing permission CTA rim. Splash already retains the shared field, short startup pulse, and its animator-scale guard.

The focused debug Kotlin compile completed successfully after these changes. Existing SDK XML, deprecation, and Kotlin annotation-target warnings are unchanged and non-blocking; no broad suppression was added.

## Verification evidence

The complete constrained static check completed successfully with JDK 17, Android SDK `/home/ubuntu/android-sdk`, one worker, and a 1 GiB heap:

```text
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1
```

`git diff --check` completed without whitespace errors and Gradle daemons were stopped afterwards. The interactive preview passed `pnpm check` and `pnpm build`; it now presents Home, queue, history, settings, auth, destination, and onboarding as explicitly local visual demonstrations. Its visual review led to a documented Lime Signal rule, a quieter control laboratory, and one dominant upload hero.

Device/runtime verification is **NOT VERIFIED** for dark/light composition, Arabic RTL, enlarged type, TalkBack, animation-scale-off, pointer/keyboard behavior, notification permission/channel state, Telegram authentication, real destination selection, and real upload delivery. No commit, push, signing, or release action was performed.

## Validation and reversal

Build verification uses `:app:compileDebugKotlin` and `:app:testDebugUnitTest` with JDK 17, Android SDK `/home/ubuntu/android-sdk`, one worker, and constrained heap. The illustrative web preview must state that it is not data-connected. Device verification is required for dark/light contrast, Arabic RTL, large type, TalkBack, animation-scale-off, keyboard/pointer, notification permission/channel state, real Telegram authorization/destination, and genuine upload behavior. Reversal is limited to shared glow helpers/tokens and presentational modifiers; runtime/data paths are not part of this phase.
