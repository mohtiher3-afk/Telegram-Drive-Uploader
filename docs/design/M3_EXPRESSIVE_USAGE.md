# M3 Expressive Usage

## Applied

Expressive color and shape hierarchy are used in the existing theme to distinguish primary actions, selected destinations, upload states, error containers, and onboarding moments. The app shell uses the M3 active navigation indicator and adapts between NavigationBar and NavigationRail.

## Deliberately Not Applied

No FAB, split button, button group, expanded toolbar, shape-morph animation, or decorative illustration was added. The application has no separate related action that would justify a split button, and upload semantics must remain honest and explicit.

## Motion

Existing onboarding and short screen transitions remain bounded. Functional state changes remain visible as text and controls; no critical information depends on animation. Future motion changes must honor reduced motion and avoid delaying upload or authentication actions.

## Review Rule

Every expressive change must answer which hierarchy, feedback, or orientation problem it solves. If it only adds decoration, it is out of scope for this application.
