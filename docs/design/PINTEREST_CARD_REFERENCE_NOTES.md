# Pinterest Card Reference Notes

## Source

- User-provided Pinterest reference: https://www.pinterest.com/pin/419608890300728910/
- Pin title and attribution: **Card section 🪄💙💜**, designed by Metagravity for Dribbble.

## Repository-independent visual observations

The reference presents a single dark feature card on a nearly black page. The card uses a very large radius, a soft low-contrast edge, and generous empty space. Its focal treatment is a violet-to-blue luminous bloom near the lower-right region; the light fades softly into the card rather than becoming a discrete decorative object.

The content is deliberately sparse and anchored near the upper-left: a tiny celestial/mark-like icon, a compact white title, muted supporting copy, and a restrained text call to action. The visual hierarchy comes from contrast, silhouette, glow, and spacing rather than from dense borders, elevations, or multiple competing button styles.

## Safe translation for Telegram Drive Uploader

| Reference trait | Safe uploader translation | Constraint |
|---|---|---|
| Dark card with violet/blue ambient glow | Upload feature card and noninteractive status surfaces can use a low-opacity semantic `secondary`/`tertiary` glow. | Do not introduce a visual effect that hides upload progress, errors, or actions. |
| Sparse upper-left copy | Preserve a short task title, the existing localized description, and one genuine action: select videos. | Do not add fictional AI, sorting, or media features. |
| Very rounded large feature surface | Use the existing Material 3 `extraLarge` shape only for the dominant feature card. | Keep lists and diagnostic panels less rounded for scanability. |
| Text-only low-emphasis CTA | Use existing secondary actions only where they already have a real callback. | Do not create an inert “learn more” action. |
| Soft luminous lower edge | Limit glow to decorative background layers and honor the app’s reduced-motion behavior. | Preserve light theme contrast, Arabic RTL, and screen-reader semantics. |

## Design conclusion

The previous home redesign already adopted the broad dark/violet/lime direction. The most relevant additional refinement is to make the upload feature card more card-section-like: calmer near-black surface, larger breathing room, a restrained lower-right blue/violet bloom, and a hierarchy that favors copy and the authentic file-picker action over high-saturation full-card gradients.


---

# Second Pinterest Reference: Cyberpunk Gradient

## Source evidence

- User-provided Pinterest reference: https://www.pinterest.com/pin/1073756736172561335/
- Public page title/content: **Cyberpunk Gradient**.
- Related visual topics identified on the source: cyberpunk gradient, Figma gradient background, light UI, tech gradient, neon UI, colorful UI, dark UI, and green gradient background.

## Visual direction

This reference supports a cooler, more luminous extension of the existing dark-violet direction: deep blue-black field, a controlled cobalt/violet transition, and an electric lime-green point of energy. The effect should read as an atmospheric gradient or diffused light field—not as a wireframe, a noisy pattern, a strong border, or decoration repeated behind every card.

## Integration decision

| New visual signal | Application decision |
|---|---|
| Cobalt/violet field with a green focal point | Use it only in the home feature-card background or splash/onboarding hero, with the lime focal point reserved for the authentic primary action or selected navigation destination. |
| Cyberpunk/neon energy | Translate it into a soft compositional glow. Avoid neon outlines, grid imagery, or animated pulses around upload progress; such effects would reduce transfer-state readability. |
| Strong color transition | Keep all titles, state text, error feedback, and progress labels on standard Material surfaces with semantic contrast. Color is never the only upload-state signal. |
| Dark/light UI association | Retain the app’s existing system/light/dark preference. The complete cyberpunk treatment applies to the dark theme only; the light theme continues to use its semantic light scheme. |

## Combined direction for the next visual pass

The two references together point to a **Mission Control Aurora** language: sparse rounded dark surfaces; bold space around key content; a single primary feature card with a lower-edge violet/cobalt aurora; and a lime accent only for primary interaction, connected readiness, and selected navigation. This reinforces the current theme without introducing an unrelated music, AI, or gaming interface metaphor.


---

# Third Pinterest Reference: Modern Gradient Construction

## Source evidence

- User-provided Pinterest reference: https://www.pinterest.com/pin/901001469213719268/
- Pin title/description: **Modern Web Design Inspiration** by OrbitX, described as clean layouts, smooth interactions, and a minimal modern aesthetic.
- Visible reference: a dark Figma tutorial composition showing a tall rounded magenta/violet gradient card, a small horizontal rounded aurora card, and a three-step construction sequence: outline shape, apply radial gradients, then apply effects and blur.

## Visual observations and safe translation

| Observed signal | Safe Mission Control Aurora translation | Excluded literal content |
|---|---|---|
| Large soft magenta/violet field inside a tall rounded card | Use a clipped radial-gradient field inside the single high-priority upload feature surface. | Figma tutorial steps, guide labels, and process diagrams. |
| Multiple blurred colored light sources | Compose two or three low-opacity radial gradients rather than a flat multicolor background. | High-contrast rainbow noise or moving light effects. |
| Small horizontal capsule with a blurred violet/orange/blue aurora | Use a quiet status or empty-state accent only when it remains readable without color. | An inert media-control capsule or decorative false button. |
| Near-black backdrop and bold white display text | Preserve the existing dark Material surface and use semantic `onSurface` type for essential content. | Hard-coded white text across light theme or error/status content. |
| Construction based on outline + radial gradients + blur | Keep visual treatment at the rendering boundary; do not persist gradient values in upload state. | Editing TDLib, queues, Room, WorkManager, or authentication to obtain a visual effect. |

## Integration decision

This reference strengthens the **aurora construction method** for the next implementation pass: combine soft radial gradient sources inside one clipped `extraLarge` feature card, make the upper copy area calmer/darker, and reserve the brighter lower/right aurora for visual depth. The existing primary action remains clear and uses the app’s semantic Lime role; the gradient must not replace visible text, progress, error, or selection feedback.


---

# Fourth Pinterest Reference: Liquid Neon

## Source evidence

- User-provided Pinterest reference: https://www.pinterest.com/pin/986640230842128401/
- Pin title/attribution: **Liquid Neon** by Mused; description calls it a mesmerizing attention-catching loop.
- Visible reference: a near-black editorial page with large white display type, restrained header navigation, and one central luminous liquid orb made from intertwined blue, violet, magenta, and white light traces.

## Visual observations and safe translation

| Observed signal | Safe Mission Control Aurora translation | Excluded literal content |
|---|---|---|
| Singular central liquid-neon orb | Use one nonessential, static abstract glow only in a non-status decorative region of a hero/onboarding surface. | Recreating the orb exactly, using a third-party asset, or placing it beside every upload. |
| Violet, magenta, cobalt, and white light traces | Retain the existing violet/cobalt aurora and use a small white highlight in the gradient field. | Adding saturated glow to alerts, destructive actions, or progress bars. |
| Quiet dark editorial frame and large white title | Use a calm dark surface and clear display hierarchy for the primary upload task. | Copying landing-page navigation, generic site sections, or unrelated copy. |
| Attention-catching loop | Keep the app’s visual treatment static or reduced-motion compliant. | Continuous looping animation, especially near upload progress and lists. |

## Integration decision

The reference confirms that a **single focal luminous form** is more effective than repeating neon decoration. If implemented, represent it with two or three static, low-opacity radial-gradient sources clipped inside the home feature card or onboarding hero. Maintain semantic surfaces for connection, upload status, errors, and queue rows; do not turn the app into an animated landing page.
