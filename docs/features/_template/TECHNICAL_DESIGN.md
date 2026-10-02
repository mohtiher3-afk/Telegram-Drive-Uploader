# Technical Design

## Architecture

Describe the existing extension point and why it is the correct one. Reuse existing components, repositories, models, utilities, navigation, and design tokens.

## Files to Modify

| File | Change | Reason | Risk |
|---|---|---|---|
| `<path>` | `<small change>` | `<reason>` | LOW / MEDIUM / HIGH / CRITICAL |

## Files to Create

| File | Purpose | Test |
|---|---|---|
| `<path>` | `<purpose>` | `<verification>` |

## Files Not to Touch

List protected files and systems that are explicitly out of scope, including TDLib/JNI/ABI, authentication, Upload Engine, WorkManager, database schema, and security architecture unless separately approved.

## Data Flow

Describe inputs, transformations, repositories, persistence, external calls, and outputs.

## State Flow

Describe ViewModel and UI state transitions, lifecycle behavior, loading, empty, error, success, retry, and cancel.

## Dependencies

List existing dependencies and any proposed dependency. Dependency changes require a separate compatibility review.

## Error Handling

Map internal failures to safe user-visible messages and define retry, cancellation, logging, and recovery behavior.

## Testing Strategy

Specify unit, integration/boundary, UI, localization/RTL, accessibility, performance, regression, and release checks required for the feature.
