package io.github.movetakeitblue.iitcexpressive.theme

import androidx.compose.ui.graphics.Color

// ==============================================================
// Material 3 Expressive (M3E) - IITC Prime Palette
// Authentic M3 tonal palette, dynamic-ready, no harsh 2012 neon.
// ==============================================================

// M3 Expressive Surfaces (Dark Theme Default)
val SurfaceDark = Color(0xFF111416)
val SurfaceContainerLowestDark = Color(0xFF0C0F11)
val SurfaceContainerLowDark = Color(0xFF191C1E)
val SurfaceContainerDark = Color(0xFF1D2022)
val SurfaceContainerHighDark = Color(0xFF282B2D)
val SurfaceContainerHighestDark = Color(0xFF333538)

// M3 Expressive Primary (Refined Mint / Sage Teal Default Fallback)
val AccentTeal = Color(0xFF78D7C7)
val AccentContainerTeal = Color(0xFF005047)
val OnPrimaryContainerTeal = Color(0xFF95F4E3)
val AccentBorderTeal = Color.Transparent // Cancelled bright glowing border for authentic M3E

// Secondary & Tertiary Accents
val SecondaryTeal = Color(0xFFB1CCC6)
val TertiaryAmber = Color(0xFFE5C07B)
val TertiaryContainerAmber = Color(0xFF3F3517)
val OnTertiaryContainerAmber = Color(0xFFFFE088)

// Outline & Borders (Soft M3 tokens, subtle, no harsh glowing neon)
val OutlineVariantMuted = Color(0x2E899294)
val OutlineSubtle = Color(0x1A899294)

// Text on dark surfaces
val OnSurfaceWhite = Color(0xFFE0E3E3)
val OnSurfaceVariantMuted = Color(0xFFBFC8C9)
val OnSurfaceSubtle = Color(0xFF899294)

// States
val StateRipple = Color(0x1A78D7C7)
val StateSuccess = Color(0xFF81C784)
val StateWarning = Color(0xFFFFB74D)
val StateError = Color(0xFFFF8585)
val StateDisabled = Color(0xFF5A757D)

// ==============================================================
// Modern M3 Expressive Faction Colors
// Refined tones, harmonious with Material You dynamic color schemes.
// ==============================================================

// Enlightened (Modern M3 Spring Mint / Emerald)
val TeamEnlightened = Color(0xFF4EDE92)
val TeamEnlightenedContainer = Color(0xFF00522B)
val OnTeamEnlightenedContainer = Color(0xFF70FBAA)

// Resistance (Modern M3 Cerulean / Sky Blue)
val TeamResistance = Color(0xFF6BB5FF)
val TeamResistanceContainer = Color(0xFF004785)
val OnTeamResistanceContainer = Color(0xFFCCE5FF)

// Machina (Modern M3 Coral Carmine)
val TeamMachina = Color(0xFFFF7171)
val TeamMachinaContainer = Color(0xFF680009)
val OnTeamMachinaContainer = Color(0xFFFFDAD6)

// Neutral / Unclaimed (Modern M3 Grey)
val TeamNeutral = Color(0xFFC0C7C7)
val TeamNeutralContainer = Color(0xFF282B2D)
val OnTeamNeutralContainer = Color(0xFFE0E3E3)

// ==============================================================
// Modern M3 Resonator Level Colors (L0 - L8)
// Smooth tonal spectrum harmonious with M3 dark/light themes.
// ==============================================================
val Level0Color = Color(0xFF8A9294)
val Level1Color = Color(0xFFFFD54F) // Amber
val Level2Color = Color(0xFFFFB066) // Orange
val Level3Color = Color(0xFFFF8566) // Coral / Deep Orange
val Level4Color = Color(0xFFFF6B7D) // Carmine / Red
val Level5Color = Color(0xFFF06595) // Magenta / Pink
val Level6Color = Color(0xFFCC66FF) // Violet
val Level7Color = Color(0xFF9E77ED) // Deep Purple
val Level8Color = Color(0xFF7087FF) // Periwinkle / Indigo

// Component Aliases
val Accent = AccentTeal
val AccentContainer = AccentContainerTeal
val SurfaceColor = SurfaceDark
val SurfaceContainer = SurfaceContainerDark
val SurfaceContainerHigh = SurfaceContainerHighDark
val OnSurface = OnSurfaceWhite
val OnSurfaceVariant = OnSurfaceVariantMuted
