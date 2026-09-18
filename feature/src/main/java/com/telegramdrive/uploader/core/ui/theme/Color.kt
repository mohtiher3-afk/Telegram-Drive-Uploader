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

// Tideglass Relay semantic primitives (Calm Material revision):
// A calm, modern blue is action, a muted indigo is context, and a soft slate is decorative.
// Names retained for call-site compatibility.
val TideSeafoam = Color(0xFF4D6BFE)   // action accent: calm indigo-blue (kept name for call-site compatibility)
val TideHorizon = Color(0xFF7A5AF8)   // context accent: muted violet (kept name for call-site compatibility)
val TideCoral = Color(0xFF5B8DEF)     // decorative hero glow: soft blue (kept name for call-site compatibility)
val TideHarborInk = Color(0xFF0F1115) // calm near-black foundation
val TideSaltPaper = Color(0xFFFDFBFF) // clean off-white foundation

// Calm modern brand seeds (blue/indigo forward, low saturation).
val NebulaRosa = Color(0xFF4D6BFE)      // action: calm indigo-blue
val NebulaViolet = Color(0xFF7A5AF8)    // context: muted violet
val NebulaOrchid = Color(0xFF8E9BFF)    // hero: soft periwinkle
val NebulaAmber = Color(0xFFF5A623)     // hero-light: warm accent
val NebulaInk = Color(0xFF0F1115)
val NebulaMist = Color(0xFFFDFBFF)

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
