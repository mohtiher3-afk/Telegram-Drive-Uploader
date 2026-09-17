# Material 3 Expressive implementation notes

Source: https://m3.material.io/blog/building-with-m3-expressive
Official article date: May 13, 2025.

Relevant guidance applied to this app:

- Material 3 Expressive is an evolution of Material 3, not a replacement version.
- Expressive design uses hierarchy, component flexibility, richer color, emphasized typography, varied shapes, and natural motion.
- The article identifies updated or new components including app bars, button groups, common buttons, extended FABs, icon buttons, loading indicators, navigation bars/rails, progress indicators, sliders, split buttons, and toolbars.
- The article recommends seven tactics: use a variety of shapes; apply rich and nuanced colors; guide attention with typography; contain content for emphasis; add fluid and natural motion; leverage component flexibility; and combine tactics for limited hero moments.
- The implementation uses compatible Material 3 APIs already available in the project: an emphasized type scale, varied rounded shape hierarchy, richer light/dark color roles, dynamic color support on Android 12+, and preserved existing Compose components.
- The article cautions that expressive treatment should not reduce the prominence of essential actions or overwhelm the interface; the uploader therefore keeps the upload and destination actions explicit and accessible.
