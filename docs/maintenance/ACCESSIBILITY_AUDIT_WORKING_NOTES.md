# Accessibility Audit Working Notes

## External reference baseline

The audit uses WCAG 2.2 as a technology-neutral benchmark for the Android Compose UI; it is not a claim that an Android application automatically conforms to a web specification. W3C’s current WCAG 2.2 Recommendation identifies focus appearance, dragging movements, target size, contrast, text alternatives, and operability as relevant testable areas. The mobile-specific implementation evidence still requires Android device observation with TalkBack, keyboard or switch input, enlarged fonts, RTL, and animation scale.

| Audit topic | W3C reference | Android translation |
|---|---|---|
| Focus appearance and non-obscuration | https://www.w3.org/TR/WCAG22/#focus-appearance | Inspect Compose focus order and visible focus treatment on actionable controls. |
| Enhanced target size | https://www.w3.org/TR/WCAG22/#target-size-enhanced | Inspect touch dimensions and the actual bounds of icon buttons, radios, and sliders. |
| Dragging movements | https://www.w3.org/TR/WCAG22/#dragging-movements | Retain a non-drag alternative for the color wheel. |
| Enhanced text contrast | https://www.w3.org/TR/WCAG22/#contrast-enhanced | Test semantic foreground/background pairs and account for glass compositing. |

The source extraction was performed on 2026-08-25. W3C’s overview describes WCAG 2.2 as technology-neutral guidance, notes its 2024 Recommendation publication, and advises use of the latest version. The audit must distinguish repository/build evidence from device/runtime evidence.
