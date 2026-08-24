# Known Accessibility Limitations

The repository has not been formally certified for WCAG, Android accessibility, TalkBack, or any platform compliance standard. The following items remain unverified because no device or emulator accessibility run was available.

| Area | Current evidence | Limitation |
|---|---|---|
| TalkBack | Material controls and visible labels are used | Spoken order, state announcements, and duplicate output were not tested. |
| Touch targets | Material buttons plus some compact explicit dimensions | Actual hit rectangles were not measured under display scaling. |
| Font scaling | No font-scaling disablement found | Large/very-large text overflow was not executed. |
| Display scaling | Compose width/weight/lazy-list patterns exist | Small/default/large display layouts were not executed. |
| Dark mode | Material color roles and theme preference exist | Contrast and disabled-state review was not executed. |
| Arabic RTL | Manifest supports RTL and Arabic resources exist | Spoken order, mixed-script names, and IME behavior were not executed. |
| Progress | Material progress component receives numeric progress | Dynamic announcement policy and terminal-state announcement were not tested. |
| Reduced motion | Onboarding uses centralized AppMotion | Reduced-motion runtime behavior was not tested. |
| Keyboard/focus | Android Compose controls are present | Physical keyboard focus order was not tested and desktop support is not claimed. |
| Tablets/landscape | General responsive Compose layout | Dedicated tablet or landscape optimization is not claimed. |

No accessibility issue was confirmed strongly enough to justify a product or layout change in this phase. Any future confirmed defect must be scoped with a focused test and must preserve product behavior.
