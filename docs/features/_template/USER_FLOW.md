# User Flow

## Entry Point

Describe how the user reaches the feature and the authentication boundary.

## Primary Flow

1. `<entry action>`
2. `<user action>`
3. `<application state>`
4. `<success result>`

## State Model

| State | User-visible behavior | Persisted? | Recovery |
|---|---|---:|---|
| Loading | `<behavior>` | No / Yes | `<behavior>` |
| Empty | `<behavior>` | No / Yes | `<behavior>` |
| Error | `<safe message>` | No / Yes | Retry / Cancel |
| Success | `<behavior>` | No / Yes | `<next action>` |

## Retry and Cancel

Document retry limits, cancellation semantics, idempotency, and user feedback.

## Back Navigation

Document forward/back behavior, unsaved state, and authentication boundaries.

## Persistence and Lifecycle

Document behavior across rotation, backgrounding, process death, restart, and returning-user flows. Do not claim persistence until it is tested.
