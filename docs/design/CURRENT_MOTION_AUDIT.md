# Current Motion Audit

| Location | Animation | Trigger | Duration | Purpose | Problem | Action |
|---|---|---|---|---|---|---|
| `feature/onboarding/OnboardingScreen.kt` | `AnimatedContent` with horizontal slide, fade, and `SizeTransform` | Page index changes | Compose defaults | Preserves context between onboarding pages | Distance is implicit and not documented; no shared token | Tokenize enter/exit timing only; preserve direction and content |
| `feature/telegram/TelegramAuthScreen.kt` | `AnimatedContent` with fade in/out | Telegram connection state changes | Compose defaults | Avoids abrupt auth-state replacement | Timing is implicit | Tokenize short fade timing; preserve state source |
| `core/ui/components/UploadStatusIndicator.kt` | `LinearProgressIndicator` | Real upload state/bytes change | Platform/Material rendering | Communicates current upload progress | Must not imply completion before confirmation | Keep calculations and states unchanged |
| Upload and queue screens | Progress indicators and state-dependent content | Existing ViewModel state changes | No custom transition found | Shows preparation/upload status | No shared motion contract | Document only; do not animate all list items |
| Lazy lists | `LazyColumn`/`items` with stable keys | List data changes | No item animation found | Efficient list rendering | Blanket item animation would add cost and repetition | Keep stable-key behavior and avoid default entrance animations |
| Navigation | Compose navigation graph | Route changes | No explicit custom transition found | Existing navigation semantics | Adding graph-wide transitions could affect back-stack perception | Defer; preserve graph and destinations |
| Dialogs/sheets | Material 3 components | Existing user actions | Native Material behavior | Platform-consistent feedback | No custom motion found | Keep native transitions |

## Accessibility finding

No explicit reduced-motion preference hook was found in the current source inventory. The safe phase change is to keep motion short and non-essential, document the gap, and avoid introducing a custom accessibility abstraction or framework. A future implementation should validate Android animator-scale behavior on device before adding a shared reduced-motion policy.
