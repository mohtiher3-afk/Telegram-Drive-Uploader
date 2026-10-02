# Accessibility and Inclusive UI Guide

## Semantics

The application uses Material 3 Buttons, TextButtons, FilterChips, TextFields, cards, lists, and progress indicators. These components provide baseline Compose semantics. Visible action labels are localized, while adjacent decorative icons generally use a null content description to avoid duplicate screen-reader output.

The reviewed code does not add custom semantics, `stateDescription`, `role`, `mergeDescendants`, or `clearAndSetSemantics` broadly. This is safer than adding redundant or fabricated descriptions, but it leaves state announcement quality to Material defaults and visible text. TalkBack behavior must be tested rather than inferred.

## Meaningful Controls

Back, search, clear, pin/remove, retry, pause, resume, cancel, skip, continue, and permission-entry actions have visible or localized descriptions in the reviewed paths. Meaningful images have a description where appropriate; decorative icons inside labeled buttons are not described separately.

## Upload State Accessibility

Queue status is rendered as visible text such as `Status: <state>`, with localized action labels for retry, pause, resume, and cancel. Progress percentage, speed, and ETA are visible for active uploads. The progress indicator exposes its numeric progress value through the Material component, but no custom live-region announcement exists. Announcing every small progress update should be avoided; a runtime TalkBack review should check meaningful state changes such as started, paused, failed, and completed.

## Touch Targets and Scaling

Material controls are used for actions, but the repository contains some explicit 40dp and 50dp dimensions in destination UI and compact icon layouts. A static dimension is not automatically a failure because surrounding padding may enlarge the interactive target. Touch-target measurement under display scaling and font scaling requires device inspection. No fixed-height change was made without evidence.

## Color and State

Status colors use Material color roles, but status is also rendered as text. Selected destinations and filters use control state, not color alone. Dark/light contrast, disabled states, and error visibility require visual and accessibility-service testing.

## RTL, IME, and Focus

The manifest enables RTL and English/Arabic resources exist. Compose layout direction should be tested for top bars, navigation, forms, lists, file names, upload information, dialogs, and action order. Authentication and search fields require Arabic and English keyboard tests, focus recovery, IME action, scrolling, and error visibility.

## Reduced Motion

The onboarding uses the existing `AppMotion` animation system. A reduced-motion device test is required to confirm the UI remains usable without relying on animation. No animation was removed or replaced during this review.
