# Motion System

Motion in Dusk is used exclusively to signal state transitions and progress.

## Motion Tokens
| Token | Value | Application |
|---|---|---|
| fast | 120ms | Micro-interactions, toggle switches |
| short | 180ms | Color transitions, status changes |
| medium | 250ms | Standard layout shifts |
| pageEnter | 300ms | Screen transitions |
| auroraBreath | 2800ms | Subtle background ambient pulses |
| signalPulse | 1200ms | Active upload/failure alerts |

## Easing & Behavior
- **Standard**: Material You standard easing for most transitions.
- **Overshoot**: Spring-based easing for "pop" effects on celebratory or error states.
- **Linear Pulse**: Infinite repeatable pulses for active processes (e.g., `PartsMap` active cells).
