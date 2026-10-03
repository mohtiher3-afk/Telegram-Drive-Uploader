# Glow Focus Indicator Refinement

## Scope and protected behavior

This phase adds a visual focus treatment only. It touches the shared UI component layer, the custom Glow editor, focused unit tests, and project records. TDLib ownership, authorization, destinations, Room, WorkManager, upload callbacks, upload progress, notifications, native artifacts, signing, and release workflows are unchanged.

## Implementation

`glowFocusIndicator` is a centralized Compose modifier. A focused Material control reserves `2.dp` outside its normal bounds and receives a solid `3.dp` semantic `outline` border. The ring uses `outline`, not a user-selected primary Glow color, so a very light or dark custom color cannot erase the focus treatment. Its color transition uses the existing short motion token and snaps when Android animation duration is disabled; control content never fades.

The custom Glow editor applies this treatment to the hue, saturation, and brightness sliders and to the save/reset buttons. Their callbacks, test tags, labels, state descriptions, and local-before-save contract remain unchanged. The pointer wheel remains optional and is not made keyboard-focusable merely for decoration.

## Contrast evidence

`GlowFocusIndicatorContrastTest` checks the central outline against the base surface in both static schemes. The dark pair `#AAB5C7` / `#0B101B` and the light pair `#717987` / `#F9FAFF` each satisfy the `3:1` contrast threshold used for focus appearance assessment. The 3dp ring and reserved outer space are designed to exceed a 2px perimeter target on ordinary Android density mappings; exact CSS-pixel equivalence and visual obstruction still require device observation.

## Validation and limitations

`./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --no-daemon --no-configuration-cache --max-workers=1` completed successfully using JDK 17 and the local Android SDK. `git diff --check` also completed successfully, and Gradle was stopped afterward.

No Android device, emulator, `adb`, keyboard hardware, or TalkBack runtime was available. Therefore keyboard traversal, visible focus order, screen-reader announcements, focus obscuration, true display contrast, RTL, large type, and reduced-motion appearance remain **NOT VERIFIED**. This is an engineering-focused refinement, not a claim of completed WCAG AAA certification.
