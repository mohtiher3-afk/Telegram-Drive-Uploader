package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.Color

// Legacy Bento primitives retained for compatibility with older previews.
val BentoBg = Color(0xFFF3F4F9)
val BentoTextPrimary = Color(0xFF1B1B1F)
val BentoTextSecondary = Color(0xFF44474E)
val BentoHeroContainer = Color(0xFFD3E4FF)
val BentoHeroText = Color(0xFF001D36)
val BentoPrimaryBlue = Color(0xFF005AC1)
val BentoTileBg = Color(0xFFE1E2EC)
val BentoCardBg = Color(0xFFFFFFFF)
val BentoCardBorder = Color(0xFFE1E2EC)
val BentoIconBg = Color(0xFFF2F0F4)

// Tideglass Relay semantic primitives (Nebula revision):
// Primrose (magenta-pink) is action, Ultraviolet is context, AmberGlow is hero/decorative only.
val TideSeafoam = Color(0xFFE1458C)   // action accent: vivid magenta-pink (kept name for call-site compatibility)
val TideHorizon = Color(0xFF8B5CF6)   // context accent: rich ultraviolet (kept name for call-site compatibility)
val TideCoral = Color(0xFFFFB84D)     // decorative hero glow: warm amber (kept name for call-site compatibility)
val TideHarborInk = Color(0xFF120A1E) // deep violet-near-black foundation
val TideSaltPaper = Color(0xFFFBF6FF) // warm off-white foundation

// Nebula primary brand seeds (modern, bold, pink/violet-forward).
val NebulaRosa = Color(0xFFE1458C)      // action: vivid magenta-pink
val NebulaViolet = Color(0xFF8B5CF6)    // context: rich ultraviolet
val NebulaOrchid = Color(0xFFC44DFF)    // hero: glowing orchid
val NebulaAmber = Color(0xFFFFB84D)     // hero-light: warm amber accent
val NebulaInk = Color(0xFF120A1E)
val NebulaMist = Color(0xFFFBF6FF)

// Compatibility alias retained for existing decorative call sites.
val AuroraCobalt = TideHorizon

// Status & progress colors. Green is reserved for confirmed completion semantics.
val UploadPausedAmber = Color(0xFFB25E00)
val UploadErrorRed = Color(0xFFBA1A1A)
val UploadCompletedGreen = Color(0xFF006B59)

// Legacy dark primitives retained for compatibility with older previews.
val DarkBg = TideHarborInk
val DarkSurface = Color(0xFF1A1C22)
val DarkSurfaceVariant = Color(0xFF2A2D36)
val DarkSurfaceContainer = Color(0xFF21242C)
val DarkCardBg = Color(0xFF1F222A)
val DarkCardBorder = Color(0xFF333842)
val DarkTextPrimary = Color(0xFFE2E2E9)
val DarkTextSecondary = Color(0xFFC4C6D0)
val DarkHeroContainer = Color(0xFF1A3358)
val DarkHeroText = Color(0xFFD3E4FF)
