# Navigation Architecture

## Current decision

The navigation shell remains in `core.navigation.AppNavigation`. Route strings are centralized in `core.navigation.AppRoutes`, while the existing `Screen` sealed class continues to own bottom-navigation labels and icons. This keeps one source of truth for route values without changing the graph structure.

## Preserved behavior

The onboarding completion gate remains outside the `NavHost`. The `NavHost` start destination remains `home`. Bottom navigation still uses the existing compact `NavigationBar` and expanded `NavigationRail`, with `popUpTo(findStartDestination())`, `saveState`, `launchSingleTop`, and `restoreState`. Transient flows continue to use callbacks and `popBackStack()` exactly as before.

## Boundaries

Screens emit navigation callbacks; `AppNavigation` owns the `NavController`. No ViewModel or shared component owns a `NavController`, and no business logic was moved into navigation. The Telegram authentication and destination routes remain in the same graph and retain their existing callbacks.

## Deferred work

No nested graphs, deep links, route arguments, route renames, navigation framework changes, or screen redesigns were introduced. Any future change to authentication gating, typed arguments, deep links, or nested feature graphs requires a separate design and runtime test phase.
